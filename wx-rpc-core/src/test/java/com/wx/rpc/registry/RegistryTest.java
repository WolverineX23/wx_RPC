package com.wx.rpc.registry;

import com.wx.rpc.config.RegistryConfig;
import com.wx.rpc.model.ServiceMetaInfo;
import com.wx.rpc.registry.Registry;
import com.wx.rpc.registry.impl.EtcdRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * 注册中心测试
 * 验证注册中心能否正常完成服务注册、注销、服务发现
 *
 */
public class RegistryTest {

    final Registry registry = new EtcdRegistry();

    /**
     * 注册中心 初始化
     */
    @Before
    public void init() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("http://localhost:2379");
        registry.init(registryConfig);
    }

    /**
     * 服务注册
     *
     * @throws Exception
     */
    @Test
    public void register() throws Exception {
        // 服务1
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);
        registry.register(serviceMetaInfo);

        // 服务2
        serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1235);
        registry.register(serviceMetaInfo);

        // 服务3
        serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("2.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);
        registry.register(serviceMetaInfo);
    }

    /**
     * 服务注销
     *
     */
    @Test
    public void unRegister() throws ExecutionException, InterruptedException{
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);
        registry.unRegister(serviceMetaInfo);
    }

    /**
     * 服务发现
     *
     */
    @Test
    public void serviceDiscovery() {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");

        String serviceKey = serviceMetaInfo.getServiceKey();
        List<ServiceMetaInfo> serviceMetaInfoList = registry.serviceDiscovery(serviceKey);
        Assert.assertNotNull(serviceMetaInfoList);
        System.out.println("Service list: " + serviceMetaInfoList);
    }

    /**
     * 心跳检测与续期机制 测试
     *
     * 用 etcdkeeper 观察节点底部的过期时间，当 TTL 到 20 的时候，又会重置为 30
     *
     * @throws Exception
     */
    @Test
    public void heartBeat() throws Exception {
        // init 方法中已经执行了 心跳检测
        register();

        // 阻塞 1 分钟
        Thread.sleep(60 * 1000L);
    }
}
