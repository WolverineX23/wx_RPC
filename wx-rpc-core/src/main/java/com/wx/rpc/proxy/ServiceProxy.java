package com.wx.rpc.proxy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.wx.rpc.RpcApplication;
import com.wx.rpc.config.RpcConfig;
import com.wx.rpc.constant.RpcConstant;
import com.wx.rpc.fault.retry.RetryStrategy;
import com.wx.rpc.fault.retry.RetryStrategyFactory;
import com.wx.rpc.fault.tolerant.TolerantStrategy;
import com.wx.rpc.fault.tolerant.TolerantStrategyFactory;
import com.wx.rpc.loadbalancer.LoadBalancer;
import com.wx.rpc.loadbalancer.LoadBalancerFactory;
import com.wx.rpc.model.RpcRequest;
import com.wx.rpc.model.RpcResponse;
import com.wx.rpc.model.ServiceMetaInfo;
import com.wx.rpc.protocol.ProtocolConstant;
import com.wx.rpc.protocol.ProtocolMessage;
import com.wx.rpc.protocol.code.ProtocolMessageDecoder;
import com.wx.rpc.protocol.code.ProtocolMessageEncoder;
import com.wx.rpc.protocol.enums.ProtocolMessageSerializerEnum;
import com.wx.rpc.protocol.enums.ProtocolMessageTypeEnum;
import com.wx.rpc.registry.Registry;
import com.wx.rpc.registry.RegistryFactory;
import com.wx.rpc.serializer.Serializer;
import com.wx.rpc.serializer.SerializerFactory;
import com.wx.rpc.serializer.impl.JdkSerializer;
import com.wx.rpc.server.tcp.VertxTcpClient;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;

/**
 * 服务代理（JDK 动态代理） - InvocationHandler
 *
 * 改 HTTP 请求，升级为 TCP 请求
 *
 * JDK 动态代理 vs CGLIB（基于字节码）：前者简单易用、无需引入额外的库，但只能对接口进行代理；后者可对任何类进行代理，灵活但性能略低。
 */
public class ServiceProxy implements InvocationHandler {

    /**
     * 调用代理 - TCP 方式
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器
//        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        // 构造请求
        // fixme 注册中心服务的本地缓存不支持多个服务
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        // 从注册中心获取服务提供者请求地址
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        // todo 版本号需获取动态配置的数据，这里写死了
        serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);     // 这里怎么就写死了版本？ 将导致获取不到非默认版本号的服务的问题
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        if (CollUtil.isEmpty(serviceMetaInfoList)) {
            throw new RuntimeException("暂无服务地址");
        }

        // 负载均衡
        LoadBalancer loadBalancer = LoadBalancerFactory.getInstance(rpcConfig.getLoadBalancer());

        // 将调用方法名（请求路径）作为负载均衡参数 - 若使用 一致性 Hash 负载均衡器，则调用相同方法的请求总会打到同一个服务器节点上
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        ServiceMetaInfo selectedServiceMetaInfo = loadBalancer.select(requestParams, serviceMetaInfoList);

//            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);

        // rpc 请求
        RpcResponse rpcResponse;
        try {
            // 发送 TCP 请求， 获取响应结果
//            RpcResponse rpcResponse = VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo);
            // 使用 重试机制
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(rpcConfig.getRetryStrategy());
            rpcResponse = retryStrategy.doRetry(() ->
                VertxTcpClient.doRequest(rpcRequest, selectedServiceMetaInfo)
            );
        } catch (Exception e) {
//            throw new RuntimeException("调用失败");

            // 容错机制
            TolerantStrategy tolerantStrategy = TolerantStrategyFactory.getInstance(rpcConfig.getTolerantStrategy());
            rpcResponse = tolerantStrategy.doTolerant(null, e);
        }

        return rpcResponse.getData();
    }

    /**
     * 调用代理 - HTTP 方式
     *
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
//    @Override
    public Object invokeHttp(Object proxy, Method method, Object[] args) throws Throwable {
        // 指定序列化器
        /*
         * 方式一：静态
        Serializer serializer = new JdkSerializer();
         */

        /*
         * 方式二：动态，java 自带 ServiceLoader 方式， 识别并加载 resources/META-INF/.. 文件中的序列化器实现类
        Serializer serializer = null;
        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class);
        for (Serializer service : serviceLoader) {
            serializer = service;
        }
         */

        // 方式三： 使用工厂 + 读取配置 动态获取序列化器实现类对象
        Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        // 构造请求
        String serviceName = method.getDeclaringClass().getName();
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();

        try {
            // 序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);

            /* 发送请求 old
             * todo 注意，这里地址被硬编码了（需要使用注册中心和服务发现机制解决）
            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080").body(bodyBytes).execute()) {
                byte[] result = httpResponse.bodyBytes();

                // 反序列化
                RpcResponse rpcResponse = serializer.deserializer(result, RpcResponse.class);
                return rpcResponse.getData();
            }
             */

            // 从注册中心获取服务提供者请求地址
            RpcConfig rpcConfig = RpcApplication.getRpcConfig();
            Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
            List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceMetaInfo.getServiceKey());
            if (CollUtil.isEmpty(serviceMetaInfoList)) {
                throw new RuntimeException("暂无服务地址");
            }
            ServiceMetaInfo selectedServiceMetaInfo = serviceMetaInfoList.get(0);   // 为何默认获取第一个？

            // 发送 HTTP 请求
            try (HttpResponse httpResponse = HttpRequest.post(selectedServiceMetaInfo.getServiceAddress())  // RPC 的调用只需要 host:port 路径就可以了？~
                    .body(bodyBytes)    // 请求的方法，已经放在 body 里了，服务提供者只需要监听 port 端口，收到请求，获取到 body 中的接口方法，处理逻辑并响应即可
                    .execute()) {
                byte[] result = httpResponse.bodyBytes();

                // 反序列化
                RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                return rpcResponse.getData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
