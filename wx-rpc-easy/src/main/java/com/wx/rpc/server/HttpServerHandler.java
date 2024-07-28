package com.wx.rpc.server;

import com.wx.rpc.model.RpcRequest;
import com.wx.rpc.model.RpcResponse;
import com.wx.rpc.registry.LocalRegistry;
import com.wx.rpc.serializer.Serializer;
import com.wx.rpc.serializer.impl.JdkSerializer;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.lang.reflect.Method;

/**
 * Vert.x HTTP 请求处理器
 */
public class HttpServerHandler implements Handler<HttpServerRequest> {

    @Override
    public void handle(HttpServerRequest event) {
        // 指定序列化器
        final Serializer serializer = new JdkSerializer();

        // 记录日志
        System.out.println("Received request: " + event.method() + " " + event.uri());

        // 异步处理 HTTP 请求
        event.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserializer(bytes, RpcRequest.class);
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
                Object result = method.invoke(impClass.newInstance(), rpcRequest.getArgs());

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
            byte[] serialized = serializer.serializer(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serialized));
        } catch (Exception e) {
            e.printStackTrace();
            httpServerResponse.end(Buffer.buffer());
        }
    }
}
