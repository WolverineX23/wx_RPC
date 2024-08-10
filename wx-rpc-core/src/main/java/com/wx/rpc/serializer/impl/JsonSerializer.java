package com.wx.rpc.serializer.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wx.rpc.model.RpcRequest;
import com.wx.rpc.model.RpcResponse;
import com.wx.rpc.serializer.Serializer;

import java.io.IOException;

/**
 * Json 序列化器
 *
 * JSON 序列化器的实现相对复杂，要考虑一些对象转换的兼容性问题，比如 Object 数组在序列化后会丢弃类型。
 */
public class JsonSerializer implements Serializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public <T> byte[] serialize(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        T object = OBJECT_MAPPER.readValue(bytes, type);
        if (object instanceof RpcRequest) {
            return handleRequest((RpcRequest) object, type);
        }
        if (object instanceof RpcResponse) {
            return handleResponse((RpcResponse) object, type);
        }

        return object;
    }

    /**
     * Rpc 请求对象的反序列化特殊处理：
     * 由于 Object 的原始对象会被擦除，导致反序列化是会被作为 LinkedHashMap，而无法转换成原始对象，因此这里需做特殊处理
     *
     * @param rpcRequest
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    private <T> T handleRequest(RpcRequest rpcRequest, Class<T> type) throws IOException {
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getArgs();

        // 循环处理每个参数的类型
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];

            // 如果类型不同，则重新处理一下类型
            if (!clazz.isAssignableFrom(args[i].getClass())) {
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(argBytes, clazz);
            }
        }

        return type.cast(rpcRequest);
    }

    /**
     * Rpc 响应对象的反序列化特殊处理
     *
     * @param rpcResponse
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    private <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        // 处理响应数据
        byte[] dataBytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse.getData());
        rpcResponse.setData(OBJECT_MAPPER.readValue(dataBytes, rpcResponse.getDataType()));

        return type.cast(rpcResponse);
    }
}
