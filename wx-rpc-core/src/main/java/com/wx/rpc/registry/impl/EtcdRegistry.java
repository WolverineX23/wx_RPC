package com.wx.rpc.registry.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSONUtil;
import com.wx.rpc.config.RegistryConfig;
import com.wx.rpc.model.ServiceMetaInfo;
import com.wx.rpc.registry.Registry;
import com.wx.rpc.registry.RegistryServiceCache;
import com.wx.rpc.registry.RegistryServiceMultiCache;
import io.etcd.jetcd.*;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Etcd 注册中心
 *
 */
@Slf4j
public class EtcdRegistry implements Registry {

    private Client client;

    private KV kvClient;

    /**
     * 根节点: 定义 Etcd 键存储的根路径为 /rpc/，为了区分不同的项目
     */
    private static final String ETCD_ROOT_PATH = "/rpc/";

    /**
     * 本机注册的节点 key 集合（用于维护续期）
     */
    private final Set<String> localRegistryNodeKeySet = new HashSet<>();

    /**
     * 注册中心服务缓存（只支持单个服务缓存，已废弃，请使用下方的 RegistryServiceMultiCache）
     */
    @Deprecated
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    private final RegistryServiceMultiCache registryServiceMultiCache = new RegistryServiceMultiCache();

    /**
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * 初始化
     * 读取注册中心配置并初始化客户端对象
     *
     * @param registryConfig
     */
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();

        heartBeat();    // 启动心跳检测
    }

    /**
     * 服务注册
     * 创建 key 并设置过期时间，value 为服务注册信息的 JSON 序列化
     *
     * @param serviceMetaInfo
     * @throws Exception
     */
    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 创建 Lease 和 KV 客户端
        Lease leaseClient = client.getLeaseClient();

        // 创建一个 30 秒的租约
        long leaseId = leaseClient.grant(30).get().getID();

        // 设置要存储的键值对
        String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registerKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        // 将键值对与租约关联起来，并设置过期时间
        PutOption putOption = PutOption.builder()
                .withLeaseId(leaseId)
                .build();
        kvClient.put(key, value, putOption).get();

        // 添加节点信息到本地缓存
        localRegistryNodeKeySet.add(registerKey);
    }

    /**
     * 注销服务
     * 删除 key
     *
     * 仅 delete(key) 方法：异步操作，立即返回一个 CompletableFuture<DeleteResponse> 对象，而不会等待删除操作完成。
     * delete(key) 方法后，加一个 get() 方法：作用是(同步操作)阻塞主线程，直到 delete 操作完成，并返回 DeleteResponse 对象，获取删除信息
     *
     * @param serviceMetaInfo
     */
    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        try {
            String registerKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
            DeleteResponse response = kvClient.delete(ByteSequence.from(registerKey, StandardCharsets.UTF_8))
                    .get();
            log.info("UnRegistry succeed: {}", response);

            // 从本地缓存移除
            localRegistryNodeKeySet.remove(registerKey);
        } catch (ExecutionException | InterruptedException e) {
            log.info("UnRegistry failed cause {}", e.toString());
        }
    }

    /**
     * 服务发现
     * 根据服务名称作为前缀
     *
     * @param serviceKey
     * @return
     */
    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 优先从缓存获取服务
        // 原教程代码，不支持多个服务同时缓存
//        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();

        // 优化后的代码，支持多个服务同时缓存
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceMultiCache.readCache(serviceKey);

        // fixed：cachedServiceMetaInfoList != null but size == 0
        if (cachedServiceMetaInfoList != null && cachedServiceMetaInfoList.size() != 0) {
            return cachedServiceMetaInfoList;
        }

        // 前缀搜索，结尾一定要加 "/"
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + "/";

        try {
            // 前缀查询
            GetOption getOption = GetOption.builder()
                    .isPrefix(true)
                    .build();
            List<KeyValue> keyValues = kvClient.get(ByteSequence.from(searchPrefix, StandardCharsets.UTF_8), getOption)
                    .get()
                    .getKvs();

            // 解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = keyValues.stream()
                    .map(keyValue -> {
                        String key = keyValue.getKey().toString(StandardCharsets.UTF_8);

                        // 监听 key 的变化
                        watch(key);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    })
                    .collect(Collectors.toList());

            // 写入服务缓存
            // 原教程代码，不支持多个服务同时缓存
//            registryServiceCache.writeCache(serviceMetaInfoList);

            // 优化后的代码，支持多个服务同时缓存
            registryServiceMultiCache.writeCache(serviceKey, serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    /**
     * 注册中心销毁
     * 项目关闭后释放资源
     *
     */
    @Override
    public void destroy() {
        // 下线节点
        // 遍历本节点所有的 key
        for (String key : localRegistryNodeKeySet) {
            try {
                DeleteResponse response = kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8)).get();
                log.info("Node:{} Destroy succeed: {}", key, response);
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }

        // 释放资源
        if (kvClient != null) {
            kvClient.close();
        }
        if (client != null) {
            client.close();
        }
    }

    /**
     * 心跳检测
     * 使用 hutool 的 CronUtil 实现定时任务，对所有集合中的节点执行 重新注册 操作 - 续签
     *
     */
    @Override
    public void heartBeat() {
        // 10s 续签一次
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                // 遍历本节点所有的 key
                for (String key : localRegistryNodeKeySet) {
                    try {
                        List<KeyValue> keyValues = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();

                        // 该节点已过期（需要重启节点才能重新注册）
                        if (CollUtil.isEmpty(keyValues)) {
                            continue;
                        }

                        // 节点未过期，重新注册 - 续签
                        KeyValue keyValue = keyValues.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo serviceMetaInfo = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(serviceMetaInfo);
                    } catch (Exception e) {
                        throw new RuntimeException(key + "续签失败", e);
                    }
                }
            }
        });

        // 支持秒级别定时任务
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }

    /**
     * 监听（消费端）
     *
     * @param serviceKey
     */
    @Override
    public void watch(String serviceKey) {
        Watch watchClient = client.getWatchClient();

        // 之前未被监听，开启监听
        boolean newWatch = watchingKeySet.add(serviceKey);

        if (newWatch) {
            watchClient.watch(ByteSequence.from(serviceKey, StandardCharsets.UTF_8), watchResponse -> {
                for (WatchEvent event : watchResponse.getEvents()) {
                    switch (event.getEventType()) {
                        // key 删除时触发
                        case DELETE:
                            // 清理注册服务缓存
                            // fixed：这里直接清空所有服务了
                            // 原教程代码，不支持多个服务同时缓存
//                            registryServiceCache.clearCache();
                            // 优化后的代码，支持多个服务同时缓存
                            registryServiceMultiCache.clearCache(serviceKey);
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }
}
