package com.wx.example.provider;

import com.wx.example.common.service.UserService;
import com.wx.rpc.RpcApplication;
import com.wx.rpc.config.RegistryConfig;
import com.wx.rpc.config.RpcConfig;
import com.wx.rpc.model.ServiceMetaInfo;
import com.wx.rpc.registry.LocalRegistry;
import com.wx.rpc.registry.Registry;
import com.wx.rpc.registry.RegistryFactory;
import com.wx.rpc.server.HttpServer;
import com.wx.rpc.server.VertxHttpServer;

/**
 * 简易服务提供者示例
 */
public class EasyProviderExample {

    public static void main(String[] args) {
        // RPC 框架初始化
        RpcApplication.init();

        String serviceName = UserService.class.getName();

        // 注册服务到本地
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到注册中心
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(serviceName);
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        try {
            registry.register(serviceMetaInfo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 启动 web 服务
        HttpServer httpServer = new VertxHttpServer();
//        httpServer.doStart(8080);
        httpServer.doStart(RpcApplication.getRpcConfig().getServerPort());
    }
}
