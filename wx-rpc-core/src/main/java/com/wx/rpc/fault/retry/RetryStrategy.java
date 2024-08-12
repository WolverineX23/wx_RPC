package com.wx.rpc.fault.retry;

import com.wx.rpc.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * 重试策略
 */
public interface RetryStrategy {

    /**
     * 重试
     *
     * @param callable      代表一个任务
     * @return
     * @throws Exception
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
