package com.wl.smartapiclientsdk;

import com.wl.smartapiclientsdk.client.MyClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author WLei224
 * @create 2023/7/8 16:49
 */
@Configuration
@ConfigurationProperties("smartapi.client")
@Data
@ComponentScan
public class SmartApiClientConfig {

    private String accessKey;

    private String secretKey;

    /**
     * 生成客户端的方法
     */
    @Bean
    public MyClient myClient() {
        return new MyClient(accessKey,secretKey);
    }
}
