package com.wx.rpc.serializer;

import com.wx.rpc.serializer.impl.HessianSerializer;
import com.wx.rpc.serializer.impl.JdkSerializer;
import com.wx.rpc.serializer.impl.JsonSerializer;
import com.wx.rpc.serializer.impl.KryoSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列化器工厂（用于获取序列化器对象）
 *
 * 由于序列化器对象的可复用性，不需每次执行序列化操作前都创建一个新的对象
 * 可使用 工厂模式 + 单例模式 来简化创建和获取序列化器对象的操作
 */
public class SerializerFactory {

    /**
     * 序列化映射（用于实现 单例）
     */
    private static final Map<String, Serializer> KEY_SERIALIZER_MAP = new HashMap<>() {
        {
            put(SerializerKeys.JDK, new JdkSerializer());
            put(SerializerKeys.JSON, new JsonSerializer());
            put(SerializerKeys.KRYO, new KryoSerializer());
            put(SerializerKeys.HESSIAN, new HessianSerializer());
        }
    };

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = KEY_SERIALIZER_MAP.get("jdk");

    /**
     * 获取实例
     *
     * @param key
     * @return
     */
    public static Serializer getInstance(String key) {
        return KEY_SERIALIZER_MAP.getOrDefault(key, DEFAULT_SERIALIZER);
    }
}
