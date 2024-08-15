package com.wx.wxrpc.springboot.starter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用 RPC 注解：用于全局标识项目需要引入 RPC 框架、执行初始化方法
 *
 * 由于服务消费者和服务提供者初始化的模块不同，我们需要在 EnableRpc 注解中，指定是否需要启动服务器等属性
 * 当然也可以拆分成 EnableRpcProvider 和 EnableRpcConsumer 分别标识服务提供者和消费者，但可能存在模块重复初始化的可能性。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableRpc {

    /**
     * 需要启动 server：用于服务提供者初始化
     *
     * @return
     */
    boolean needServer() default true;
}
