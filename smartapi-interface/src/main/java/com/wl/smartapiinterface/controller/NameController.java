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

    @GetMapping("/get")
    public String getNameByGet(String name, HttpServletRequest request){
        // 调用成功后，次数 + 1     (因为写通用方法代码侵入性强，还需要开发者自己调用，所以我们使用AOP的方法)
        // System.out.println(request.getHeader("smart"));
        return "GET 你的名字是" + name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam String name){
        // 调用成功后，次数 + 1
        return "POST 你的名字是" + name;
    }

    @PostMapping("/user")
    public String getUserNameByPost(@RequestBody User user, HttpServletRequest request){
        // String accessKey = request.getHeader("accessKey");
        // // String secretKey = request.getHeader("secretKey");
        // String nonce = request.getHeader("nonce");
        // String timeStamp = request.getHeader("timeStamp");
        // String sign = request.getHeader("sign");
        // String body = request.getHeader("body");
        // //  要去数据库中查询
        // // 为了方便进行校验，直接进行判断数据，正规来说应该从数据库中进行校验数据
        // if (!accessKey.equals("wl")){
        //     throw new RuntimeException("无权限！");
        // }
        // if (Long.parseLong(nonce) > 10000){
        //     throw new RuntimeException("无权限！");
        // }
        // //  时间戳校验自己实现，时间和当前时间不能超过5min
        // // if (timeStamp) {
        // //
        // // }
        //
        // //  要去数据库中查询
        // String serverSign = SignUtils.getSign(body, "abcdefgh");
        // if (!serverSign.equals(sign)) {
        //     throw new RuntimeException("无权限！");
        // }
        String result = "POST 你的名字是" + user.getUsername();
        // 调用成功后，次数加1
        return result;


    }


}
