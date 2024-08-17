package com.wx.wxrpc.springboot.starter.bootstrap;

import com.wx.rpc.RpcApplication;
import com.wx.rpc.config.RpcConfig;
import com.wx.rpc.server.tcp.VertxTcpServer;
import com.wx.wxrpc.springboot.starter.annotation.EnableRpc;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Objects;

/**
 * RPC 框架启动
 *
 * 从 Spring 元信息中获取到了 EnableRpc 注解的 needServer 属性，并通过它来判断 除了初始化 rpc 外，是否还需要启动服务器
 *
 * ImportBeanDefinitionRegistrar 接口：通常用于根据某些条件动态添加 Bean 到 Spring 容器中。
 * RpcInitBootstrap 类实现了 ImportBeanDefinitionRegistrar 接口，这意味着这个类可以在 Spring 启动时动态注册 Bean 定义。
 */
@Slf4j
public class RpcInitBootstrap implements ImportBeanDefinitionRegistrar {

    /**
     * Spring 初始化时执行，初始化 RPC 框架
     *
     * Spring 启动时会调用 registerBeanDefinitions 方法通过编程方式来动态注册 Bean 定义
     *
     * @param importingClassMetadata    AnnotationMetadata 是 Spring 框架中的一个接口，表示一个类的注解元数据。它提供了获取该类上所有注解及其属性的方法。
     * @param registry BeanDefinitionRegistry 是 Spring 框架中的一个接口，表示一个可以注册 BeanDefinition 的注册表。
     *                 registry 允许在 Spring 应用启动时通过编程的方式向 Spring 容器中注册 Bean 定义。
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 获取 EnableRpc 注解的属性值
        boolean needServer = (boolean) Objects.requireNonNull(importingClassMetadata.getAnnotationAttributes(EnableRpc.class.getName()))
                .get("needServer");

        // RPC 框架初始化（加载 rpc 配置和注册中心初始化）
        RpcApplication.init();

        // 全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 启动服务器
        if (needServer) {
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(rpcConfig.getServerPort());
        } else {
            log.info("不启动 server");
        }
    }
}
