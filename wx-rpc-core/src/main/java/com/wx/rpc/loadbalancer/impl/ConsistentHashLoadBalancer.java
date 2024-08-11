package com.wx.rpc.loadbalancer.impl;

import com.wx.rpc.loadbalancer.LoadBalancer;
import com.wx.rpc.model.ServiceMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 一致性哈希负载均衡器
 *
 * 使用 TreeMap 实现一致性 Hash 环，该数据结构提供了 ceilingEntry 和 firstEntry 两个方法，便于获取符合算法要求的节点。
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * 一致性 Hash 环，存放虚拟节点
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();

    /**
     * 虚拟节点数
     */
    private static final int VIRTUAL_NODE_NUM = 100;

    /**
     * 选择节点
     *
     * 根据 requestParams 对象计算 hash 值：这里用 hashCode 方法简单实现了，可优化
     * 每次调用负载均衡时，都会重新构造 Hash 环，这是为了能够即时处理节点的变化。
     *
     * @param requestParams             请求参数
     * @param serviceMetaInfoList       可用服务列表
     * @return
     */
    @Override
    public ServiceMetaInfo select(Map<String, Object> requestParams, List<ServiceMetaInfo> serviceMetaInfoList) {
        if (serviceMetaInfoList.isEmpty()) {
            return null;
        }

        // 构建虚拟节点环
        for (ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList) {
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                int hash = getHash(serviceMetaInfo.getServiceAddress() + "#" + i);
                virtualNodes.put(hash, serviceMetaInfo);
            }
        }

        // 获取调用请求的 hash 值
        int hash = getHash(requestParams);

        // 获取最接近且大于等于调用请求 hash 值的虚拟节点
        Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);

        // 如果没有，则返回环首部的节点
        if (entry == null) {
            entry = virtualNodes.firstEntry();
        }

        return entry.getValue();
    }

    /**
     * Hash 算法，可自定义实现
     *
     * @param key
     * @return
     */
    private int getHash(Object key) {
        return key.hashCode();
    }
}
