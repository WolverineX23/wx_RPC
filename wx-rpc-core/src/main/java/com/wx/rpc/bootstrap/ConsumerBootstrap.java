package com.wx.rpc.bootstrap;

import com.wx.rpc.RpcApplication;

/**
 * 服务消费者启动类(初始化)
 */
public class ConsumerBootstrap {

    /**
     * 初始化
     */
    public static void init() {
        // RPC 框架初始化（加载 rpc 配置和注册中心初始化）
        RpcApplication.init();
    }
}
