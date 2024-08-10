package com.wx.rpc.server;

import com.wx.rpc.RpcApplication;
import com.wx.rpc.model.RpcRequest;
import com.wx.rpc.model.RpcResponse;
import com.wx.rpc.registry.LocalRegistry;
import com.wx.rpc.serializer.Serializer;
import com.wx.rpc.serializer.SerializerFactory;
import com.wx.rpc.serializer.impl.JdkSerializer;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.lang.reflect.Method;
import java.util.ServiceLoader;

/**
 * Vert.x HTTP 请求处理器
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest event) {
        // 指定序列化器
        /*
         * 方式一：静态
        final Serializer serializer = new JdkSerializer();
         */

        /*
         * 方式二：动态，java 自带 ServiceLoader 方式， 识别并加载 resources/META-INF/.. 文件中的序列化器实现类
        Serializer initSerializer = null;
        ServiceLoader<Serializer> serviceLoader = ServiceLoader.load(Serializer.class); // 动态加载指定接口的实现类
        for (Serializer service : serviceLoader) {
            initSerializer = service;
        }
        final Serializer serializer = initSerializer;
         */

        // 方式三： 使用工厂 + 读取配置 动态获取序列化器实现类对象
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());

        // 记录日志
        System.out.println("Received request: " + event.method() + " " + event.uri());

        // 异步处理 HTTP 请求
        event.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();

            // 如果请求为 null，直接返回
            if (rpcRequest == null) {
                rpcResponse.setMessage("rpcRequest is null");
                doResponse(event, rpcResponse, serializer);
                return;
            }

            try {
                // 获取要调用的服务实现类，通过 反射 调用
                Class<?> impClass = LocalRegistry.get(rpcRequest.getServiceName());
                Method method = impClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());

                // 创建目标类的实例：impClass.newInstance() 在 java9 被弃用
//                Object result = method.invoke(impClass.newInstance(), rpcRequest.getArgs());

                // impClass.getDeclaredConstructor().newInstance(): 新方式 更安全且支持选择特定的构造函数
                Object result = method.invoke(impClass.getDeclaredConstructor().newInstance(), rpcRequest.getArgs());

                // 封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            } catch (Exception e) {
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }

            // 响应
            doResponse(event, rpcResponse, serializer);
        });
    }

    void doResponse(HttpServerRequest event, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse httpServerResponse = event.response()
                .putHeader("content-type", "application/json");

        try {
            // 序列化
            byte[] serialized = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serialized));
        } catch (Exception e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
