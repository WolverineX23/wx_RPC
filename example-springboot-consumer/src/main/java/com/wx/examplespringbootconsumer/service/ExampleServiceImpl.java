package com.wx.examplespringbootconsumer.service;

import com.wx.example.common.model.User;
import com.wx.example.common.service.UserService;
import com.wx.wxrpc.springboot.starter.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl {

    @RpcReference
    private UserService userService;

    public void test() {
        User user = new User();
        user.setName("Wolverine23");

        User resultUser = userService.getUser(user);
        System.out.println(resultUser.getName());
    }
}
