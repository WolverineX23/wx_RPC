package com.wx.rpc.bootstrap;

import com.wx.rpc.RpcApplication;
import com.wx.rpc.config.RegistryConfig;
import com.wx.rpc.config.RpcConfig;
import com.wx.rpc.model.ServiceMetaInfo;
import com.wx.rpc.model.ServiceRegisterInfo;
import com.wx.rpc.registry.LocalRegistry;
import com.wx.rpc.registry.Registry;
import com.wx.rpc.registry.RegistryFactory;
import com.wx.rpc.server.tcp.VertxTcpServer;

import java.util.List;

/**
 * 服务提供者初始化 启动类
 */
public class ProviderBootstrap {

    /**
     * 初始化
     *
     * @param serviceRegisterInfoList   注册服务信息列表
     */
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList) {

        // RPC 框架初始化（加载 rpc 配置和注册中心初始化）
        RpcApplication.init();

        // 全局配置
        final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        // 注册服务
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();

            // 本地注册
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());

            // 注册服务到注册中心
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try {
                registry.register(serviceMetaInfo);
            } catch (Exception e) {
                throw new RuntimeException(serviceName + " 服务注册失败", e);
            }

            // 启动服务器
            VertxTcpServer vertxTcpServer = new VertxTcpServer();
            vertxTcpServer.doStart(rpcConfig.getServerPort());
        }
    }
}
