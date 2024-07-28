package com.wx.example.consumer.proxy;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.wx.example.common.model.User;
import com.wx.example.common.service.UserService;
import com.wx.rpc.model.RpcRequest;
import com.wx.rpc.model.RpcResponse;
import com.wx.rpc.serializer.Serializer;
import com.wx.rpc.serializer.impl.JdkSerializer;

import java.io.IOException;

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
        Serializer serializer = new JdkSerializer();

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
