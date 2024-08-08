package com.wx.rpc.config;

import lombok.Data;

/**
 * RPC 框架注册中心配置
 */
@Data
public class RegistryConfig {

    /**
     * 注册中心类别
     *
     * zookeeper
     */
    private String registry = "etcd";

    /**
     * 注册中心地址
     *
     * zookeeper: http://localhost:2181
     *
     */
    private String address = "http://localhost:2380";

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 超时时间（单位：毫秒）
     */
    private Long timeout = 10000L;
}
