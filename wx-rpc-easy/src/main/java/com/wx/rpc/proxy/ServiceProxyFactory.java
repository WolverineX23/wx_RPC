package com.wx.rpc.proxy;

import java.lang.reflect.Proxy;

/**
 * 服务代理工厂（用于创建代理对象）
 *
 * 工厂设计模式
 */
public class ServiceProxyFactory {

    /**
     * 根据服务类创建代理对象
     *
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getProxy(Class<T> serviceClass) {
        // 创建代理对象 - Proxy.newProxyInstance
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},      // 只能是接口
                new ServiceProxy()              // InvocationHandler 实现
        );
    }
}
