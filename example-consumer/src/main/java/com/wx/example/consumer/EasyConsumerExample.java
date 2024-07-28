package com.wx.example.consumer;

import com.wx.example.common.model.User;
import com.wx.example.common.service.UserService;
import com.wx.example.consumer.proxy.UserServiceProxy;

/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {

    public static void main(String[] args) {
        // 静态代理
        UserService userService = new UserServiceProxy();
        User user = new User();
        user.setName("wx");

        // 调用
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
