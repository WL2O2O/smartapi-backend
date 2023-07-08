package com.wl.smartapiinterface;

import com.wl.smartapiclientsdk.client.MyClient;
import com.wl.smartapiclientsdk.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class SmartapiinterfaceApplicationTests {

    @Resource
    private MyClient myClient;

    @Test
    void contextLoads() {
        String result = myClient.getNameByGet("wl");
        User user = new User();
        user.setUsername("wl");
        String userNameByPost = myClient.getUserNameByPost(user);
        System.out.println(result);
        System.out.println(userNameByPost);
    }

}
