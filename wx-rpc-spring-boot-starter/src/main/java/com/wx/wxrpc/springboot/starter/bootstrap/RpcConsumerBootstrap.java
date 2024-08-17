package com.wx.wxrpc.springboot.starter.bootstrap;

import com.wx.rpc.proxy.ServiceProxyFactory;
import com.wx.wxrpc.springboot.starter.annotation.RpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * Rpc 服务消费者启动
 *
 * 这里只针对 serviceVersion 进行了关注，@RpcReference 注解中的其他字段均忽略了，并且 ServiceProxy 中将 serviceVersion 写死为 DEFAULT_SERVICE_VERSION
 * 将导致问题：服务提供者注册服务时，@RpcService 中配置了其他版本号，服务消费者将获取不到该类服务！！！
 */
@Slf4j
public class RpcConsumerBootstrap implements BeanPostProcessor {

    /**
     * Bean 初始化后执行，注入服务
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        // 遍历对象的所有属性
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field field : declaredFields) {
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            // 若该属性上配置了 @RpcReference，则为属性生成代理对象
            if (rpcReference != null) {
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if (interfaceClass == void.class) {
                    interfaceClass = field.getType();
                }
                field.setAccessible(true);          // 取消 Java 访问控制检查，使得对应的字段、方法或构造器可以被访问和修改。
                Object proxyObject = ServiceProxyFactory.getProxy(interfaceClass);
                try {
                    field.set(bean, proxyObject);   // 将代理对象赋值给 bean 对象实例
                    field.setAccessible(false);     // 操作完成后，再恢复原来的访问控制
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("为字段注入代理对象失败", e);
                }
            }
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
