package com.wx.rpc.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Etcd 官方 Demo
 *
 * 2379: 提供 HTTP API 服务，和 etcdctl 交互
 * 2380: 集群中节点间通讯
 *
 * 常用客户端：
 * KVClient: 键值对操作
 * LeaseClient: 租约机制
 * watchClient: 监视 etcd 中键的变化，并在键的值发送变化时接收通知
 * clusterClient: etcd 集群
 * authClient: 管理 etcd 的身份验证和授权
 * lockClient: 分布式锁功能 - 并发控制
 * maintenanceClient: etcd 的维护操作，如健康检查、数据库备份、成员维护、数据库快照、数据库压缩等
 * electionClient: 分布式选举功能
 */
public class EtcdRegistryDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // create client using endpoints
        Client client = Client.builder().endpoints("http://localhost:2379")
                .build();

        KV kvClient = client.getKVClient();
        ByteSequence key = ByteSequence.from("test_key".getBytes());
        ByteSequence value = ByteSequence.from("test_value".getBytes());

        // put the key-value
        kvClient.put(key, value).get();

        // get the CompletableFuture
        CompletableFuture<GetResponse> getFuture = kvClient.get(key);

        // get the value from CompletableFuture
        GetResponse response = getFuture.get();

        // delete the key - 可在此设断点，debug 运行查看
        kvClient.delete(key).get();

    }
}
