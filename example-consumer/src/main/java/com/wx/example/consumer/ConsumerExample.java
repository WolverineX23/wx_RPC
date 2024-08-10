package com.wx.example.consumer;

import com.wx.example.common.model.User;
import com.wx.example.common.service.UserService;
import com.wx.rpc.config.RpcConfig;
import com.wx.rpc.proxy.ServiceProxyFactory;
import com.wx.rpc.utils.ConfigUtils;

/**
 * 服务消费者示例
 *
 */
public class ConsumerExample {

    public static void main(String[] args) {
        /* 加载 RPC
        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);

        // 服务调用
         */

        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("wx");

        // 调用1
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println("调用了真实服务1: " + newUser.getName());
        } else {
            System.out.println("调用了模拟服务1：user == null");
        }

        /* test
        // 调用2
        User newUser2 = userService.getUser(user);
        if (newUser2 != null) {
            System.out.println("调用了真实服务2: " + newUser2.getName());
        } else {
            System.out.println("调用了模拟服务2：user == null");
        }

        // 调用3
        User newUser3 = userService.getUser(user);
        if (newUser3 != null) {
            System.out.println("调用了真实服务3: " + newUser3.getName());
        } else {
            System.out.println("调用了模拟服务3：user == null");
        }
         */

        short num = userService.getNumber();
        System.out.println(num);
    }
}
