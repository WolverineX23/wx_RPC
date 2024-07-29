package com.wx.example.consumer;

import com.wx.rpc.config.RpcConfig;
import com.wx.rpc.utils.ConfigUtils;

/**
 * 服务消费者示例
 *
 */
public class ConsumerExample {

    public static void main(String[] args) {
        // 加载 RPC
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);

        // 服务调用
    }
}
