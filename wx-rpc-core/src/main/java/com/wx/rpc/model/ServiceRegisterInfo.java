package com.wx.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 服务注册信息类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRegisterInfo<T> {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 实现类
     *
     * ？ 为通配符，表示未知的类型，
     * T 为一个类型参数，表示任意类型
     * 意味着 implClass 必须是 T 或 T 的子类
     */
    private Class<? extends T> implClass;
}
