package com.wx.rpc.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地服务注册器
 * 区别于注册中心： 注册中心-侧重于管理注册的服务，提供服务信息给消费者；本地服务注册器：根据服务名获取到对应的实现类，是完成调用必不可少的模块。
 */
public class LocalRegistry {

    // 注册信息存储
    private static final Map<String, Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     *
     * @param serviceName
     * @param impClass
     */
    public static void register(String serviceName, Class<?> impClass) {
        map.put(serviceName, impClass);
    }

    /**
     * 获取服务
     *
     * @param serviceName
     * @return
     */
    public static Class<?> get(String serviceName) {
        return map.get(serviceName);
    }

    /**
     * 删除服务
     *
     * @param serviceName
     */
    public static void remove(String serviceName) {
        map.remove(serviceName);
    }
}
