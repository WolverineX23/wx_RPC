package com.wx.rpc.fault.tolerant.impl;

import com.wx.rpc.fault.tolerant.TolerantStrategy;
import com.wx.rpc.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 故障转移策略：转移到其他服务节点
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // todo 扩展，获取其他服务节点并调用
        return null;
    }
}
