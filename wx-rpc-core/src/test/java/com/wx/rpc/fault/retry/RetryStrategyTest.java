package com.wx.rpc.fault.retry;

import com.wx.rpc.fault.retry.impl.FixedIntervalRetryStrategy;
import com.wx.rpc.fault.retry.impl.NoRetryStrategy;
import com.wx.rpc.model.RpcResponse;
import org.junit.Test;

/**
 * 重试策略测试
 */
public class RetryStrategyTest {

    RetryStrategy noRetryStrategy = new NoRetryStrategy();

    RetryStrategy fixedIntervalRetryStrategy = new FixedIntervalRetryStrategy();

    /**
     * 测试 不重试策略
     *
     */
    @Test
    public void doNoRetry() {
        try {
            RpcResponse rpcResponse = noRetryStrategy.doRetry(() -> {
                System.out.println("测试重试");
                throw new RuntimeException("模拟重试失败");
            });

            System.out.println(rpcResponse);
        } catch (Exception e) {
            System.out.println("重试多次失败");
            e.printStackTrace();
        }
    }

    /**
     * 测试 固定重试时间间隔策略
     *
     *
     */
    @Test
    public void doFixedWaitedRetry() {
        try {
            RpcResponse rpcResponse = fixedIntervalRetryStrategy.doRetry(() -> {
                System.out.println("测试重试");
                throw new RuntimeException("模拟重试失败");
            });

            System.out.println(rpcResponse);
        } catch (Exception e) {
            System.out.println("重试多次失败");
            e.printStackTrace();
        }
    }
}
