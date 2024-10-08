package com.wx.example.provider;

import com.wx.example.common.service.UserService;
import com.wx.rpc.RpcApplication;
import com.wx.rpc.bootstrap.ProviderBootstrap;
import com.wx.rpc.config.RegistryConfig;
import com.wx.rpc.config.RpcConfig;
import com.wx.rpc.model.ServiceMetaInfo;
import com.wx.rpc.model.ServiceRegisterInfo;
import com.wx.rpc.registry.LocalRegistry;
import com.wx.rpc.registry.Registry;
import com.wx.rpc.registry.RegistryFactory;
import com.wx.rpc.server.tcp.VertxTcpServer;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务提供者示例
 *
 */
public class ProviderExample {

    public static void main(String[] args) {

        // 启动机制 rpc 框架启动代码
        // 要注册的服务
        List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList<>();
        ServiceRegisterInfo<UserService> serviceRegisterInfo = new ServiceRegisterInfo<>(UserService.class.getName(), UserServiceImpl.class);
        serviceRegisterInfoList.add(serviceRegisterInfo);

        // 服务提供者初始化
        ProviderBootstrap.init(serviceRegisterInfoList);

        /* old usage method
        // RPC 框架初始化
        RpcApplication.init();

        String serviceName = UserService.class.getName();

        // 注册服务到本地注册中心
        LocalRegistry.register(serviceName, UserServiceImpl.class);

        // 注册服务到 Etcd 注册中心
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

        // 启动 TCP 服务
        VertxTcpServer vertxTcpServer = new VertxTcpServer();
        vertxTcpServer.doStart(rpcConfig.getServerPort());
         */
    }
}
