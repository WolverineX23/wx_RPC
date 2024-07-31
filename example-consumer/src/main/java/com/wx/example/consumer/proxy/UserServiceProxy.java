package com.wx.example.consumer.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.wx.example.common.model.User;
import com.wx.example.common.service.UserService;
import com.wx.rpc.RpcApplication;
import com.wx.rpc.model.RpcRequest;
import com.wx.rpc.model.RpcResponse;
import com.wx.rpc.serializer.Serializer;
import com.wx.rpc.serializer.SerializerFactory;
import com.wx.rpc.serializer.impl.JdkSerializer;

import java.io.IOException;
import java.util.ServiceLoader;

/**
 * UserService服务 静态代理
 *
 * 需为每一个特定类型的接口或对象，编写一个代理类，
 * 如这里的 consumer 模块，要调用 provider 模块的 user 服务，
 * 需创建这个静态代理 UserServiceProxy,，实现 UserService 接口 和 getUser 方法
 * 实现方式：通过构造 HTTP 请求去调用服务提供者
 */
public class UserServiceProxy implements UserService {

    public User getUser(User user) {
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

        // 配置请求信息
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();

        // 序列化 + 发送请求 / 反序列化 + 处理响应
        try {
            byte[] bodyBytes = serializer.serializer(rpcRequest);
            byte[] result;

            try (HttpResponse httpResponse = HttpRequest.post("http://localhost:8080").body(bodyBytes).execute()) {
                result = httpResponse.bodyBytes();
            }
            RpcResponse rpcResponse = serializer.deserializer(result, RpcResponse.class);
            return (User) rpcResponse.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
