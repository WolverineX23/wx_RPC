package com.wx.rpc.config;

import lombok.Data;

/**
 * RPC 框架配置
 * 给属性指定默认值
 */
@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "wx-rpc";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;
}
