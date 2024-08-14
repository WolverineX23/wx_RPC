package com.wx.rpc.fault.tolerant.impl;

import com.wx.rpc.fault.tolerant.TolerantStrategy;
import com.wx.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 故障恢复策略：降级到其他服务
 */
@Slf4j
public class FailBackTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // todo 扩展，获取降级的服务并调用
        return null;
    }
}
