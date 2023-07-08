package com.wl.smartapiinterface.controller;



import com.wl.smartapiclientsdk.model.User;
import com.wl.smartapiclientsdk.utils.SignUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;

/**
 * @author WLei224
 * @create 2023/7/8 0:22
 */
@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/")
    public String getNameByGet(String name){
        return "GET 你的名字是" + name;
    }

    @PostMapping("/")
    public String getNameByPost(@RequestParam String name){
        return "POST 你的名字是" + name;

    }
    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request){
        String assessKey = request.getHeader("accessKey");
        // String secretKey = request.getHeader("secretKey");
        String nonce = request.getHeader("nonce");
        String timeStamp = request.getHeader("timeStamp");
        String sign = request.getHeader("sign");
        String body = request.getHeader("body");
        // TODO 要去数据库中查询
        // 为了方便进行校验，直接进行判断数据，正规来说应该从数据库中进行校验数据
        if (!assessKey.equals("wl")){
            throw new RuntimeException("无权限！");
        }
        if (Long.parseLong(nonce) > 10000){
            throw new RuntimeException("无权限！");
        }
        // TODO 时间戳校验自己实现，时间和当前时间不能超过5min
        // if (timeStamp) {
        //
        // }

        // TODO 要去数据库中查询
        String serverSign = SignUtils.getSign(body, "abcdefgh");
        if (!serverSign.equals(sign)) {
            throw new RuntimeException("无权限！");
        }
        return "POST 你的名字是" + user.getUsername();

    }


}
