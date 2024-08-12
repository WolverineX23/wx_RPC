package com.wx.rpc.fault.retry.impl;

import com.github.rholder.retry.*;
import com.wx.rpc.fault.retry.RetryStrategy;
import com.wx.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔 - 重试策略
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {

    /**
     * 重试
     *
     * @param callable      代表一个任务
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws ExecutionException, RetryException {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfExceptionOfType(Exception.class)                                        // 重试条件
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))       // 重试策略
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))               // 停止策略
                .withRetryListener(new RetryListener() {                                        // 重试工作
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试次数 {}", attempt.getAttemptNumber());
                    }
                })
                .build();

        return retryer.call(callable);  // 重试工作
    }
}
