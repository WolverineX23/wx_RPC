package com.wx.example.common.service;

import com.wx.example.common.model.User;

/**
 * 用户服务 接口
 */
public interface UserService {

    /**
     * 获取用户
     *
     * @param user
     * @return
     */
    User getUser(User user);
}
