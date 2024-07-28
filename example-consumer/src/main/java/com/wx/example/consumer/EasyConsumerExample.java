package com.wx.example.consumer;

import com.wx.example.common.model.User;
import com.wx.example.common.service.UserService;
import com.wx.example.consumer.proxy.UserServiceProxy;
import com.wx.rpc.proxy.ServiceProxyFactory;

/**
 * 简易服务消费者示例
 */
public class EasyConsumerExample {

    public static void main(String[] args) {
        // 静态代理
//        UserService userService = new UserServiceProxy();

        // 动态代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("wx");

        // 调用
        User newUser = userService.getUser(user);   // 调用 getUser 方法时，实际调用被分派到 ServiceProxy 的 invoke 方法
        if (newUser != null) {
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
