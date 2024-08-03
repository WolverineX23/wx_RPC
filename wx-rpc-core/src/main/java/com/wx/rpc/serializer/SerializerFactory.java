package com.wx.rpc.serializer;

import com.wx.rpc.serializer.impl.HessianSerializer;
import com.wx.rpc.serializer.impl.JdkSerializer;
import com.wx.rpc.serializer.impl.JsonSerializer;
import com.wx.rpc.serializer.impl.KryoSerializer;
import com.wx.rpc.spi.SpiLoader;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 序列化器工厂（用于获取序列化器对象）
 *
 * 由于序列化器对象的可复用性，不需每次执行序列化操作前都创建一个新的对象
 * 可使用 工厂模式 + 单例模式 来简化创建和获取序列化器对象的操作
 */
@Slf4j
public class SerializerFactory {

    /**
     * 静态代码块：在工厂首次加载时，就会调用 SpiLoader 的 load 方法加载序列化器接口的所有实现类；
     *              之后就可以通过调用 getInstance 方法获取指定的实现类对象。
     */
    static {
        SpiLoader.load(Serializer.class);
    }

    /**
     * 序列化映射（用于实现 单例）
     * 硬编码方式
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

        /* 硬编码方式
        Serializer serializer = KEY_SERIALIZER_MAP.getOrDefault(key, DEFAULT_SERIALIZER);
        log.info("Get Serializer: {}", serializer.getClass().getName());

        return serializer;
         */

        // SPI 动态加载方式
        return SpiLoader.getInstance(Serializer.class, key);
    }
}
