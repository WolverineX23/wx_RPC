package com.wx.wxrpc.springboot.starter.bootstrap;

import com.wx.rpc.RpcApplication;
import com.wx.rpc.config.RegistryConfig;
import com.wx.rpc.config.RpcConfig;
import com.wx.rpc.model.ServiceMetaInfo;
import com.wx.rpc.registry.LocalRegistry;
import com.wx.rpc.registry.Registry;
import com.wx.rpc.registry.RegistryFactory;
import com.wx.wxrpc.springboot.starter.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Rpc 服务提供者启动
 *
 * 作用：获取到所有包含 @RpcService 注解的类，并且通过注解的属性和反射机制，获取到要注解的服务信息，完成服务注册。
 *
 * 如何获取所有包含 @RpcService 注解的类？ - 1. 主动扫描包； 2. 利用 Spring 的特性监听 Bean 的加载。（可见 RpcService 注解类上都加了个 @Component
 * 采用后者实现
 */
@Slf4j
public class RpcProviderBootstrap implements BeanPostProcessor {

    /**
     * Bean 初始化后执行，注册服务
     *
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Class<?> beanClass = bean.getClass();
        RpcService rpcService = beanClass.getAnnotation(RpcService.class);

        // 若类上打了 @RpcService，则需要注册服务
        if (rpcService != null) {
            // 1. 获取服务基本信息
            //  获取 serviceName - 默认值处理
            Class<?> interfaceClass = rpcService.interfaceClass();
            if (interfaceClass == void.class) {
                interfaceClass = beanClass.getInterfaces()[0];      // 获取当前类实现的第一个 接口类
            }
            String serviceName = interfaceClass.getName();

            // 获取 serviceVersion
            String serviceVersion = rpcService.serviceVersion();

            // 2. 注册服务
            // 本地注册
            LocalRegistry.register(serviceName, beanClass);

            // 全局配置
            final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceVersion(serviceVersion);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());

            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }
        }

        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
