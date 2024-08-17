package com.wx.examplespringbootprovider.service;

import com.wx.example.common.model.User;
import com.wx.example.common.service.UserService;
import com.wx.wxrpc.springboot.starter.annotation.RpcService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Service
@RpcService
public class UserServiceImpl implements UserService {


    @Override
    public User getUser(User user) {

        System.out.println("用户名：" + user.getName());
        return user;
    }
}
