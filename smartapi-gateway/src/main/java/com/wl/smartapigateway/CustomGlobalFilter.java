package com.wl.smartapigateway;

import com.wl.smartapiclientsdk.utils.SignUtils;
import com.wl.smartapicommon.model.entity.InterfaceInfo;
import com.wl.smartapicommon.model.entity.User;
import com.wl.smartapicommon.service.InnerInterfaceInfoService;
import com.wl.smartapicommon.service.InnerUserInterfaceInfoService;
import com.wl.smartapicommon.service.InnerUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全局过滤
 */
@Component
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    private static final String INTERFACE_HOST = "http://localhost:8123";
    @DubboReference
    InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @DubboReference
    InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    InnerUserService innerUserService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // 1. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        String path = INTERFACE_HOST + request.getPath().value();
        String method = request.getMethod().toString();
        log.info("请求唯一标识：" + request.getId());
        log.info("请求路径：" + path);
        log.info("请求方法：" + method);
        log.info("请求参数：" + request.getQueryParams());
        String sourceAdress = request.getLocalAddress().getHostString();
        log.info("请求来源地址：" + sourceAdress);
        log.info("请求来源地址：" + request.getRemoteAddress());

        // 2. 访问控制 -- 设置黑白名单（可以用设置响应状态码来实现）
        ServerHttpResponse response = exchange.getResponse();
        if(!IP_WHITE_LIST.contains(sourceAdress)) {
            // handleNoAuth(response);
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }

        // 3. 用户鉴权（如何？判断ak、sk）
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timeStamp = headers.getFirst("timeStamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");
        // 要去数据库中查询
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        } catch (Exception e) {
            log.error("getInvokeUser error", e);
        }
        if (invokeUser == null) {
            return handleNoAuth(response);
        }
        // 为了方便进行校验，直接进行判断数据，正规来说应该从数据库中进行校验数据
        // if (!"wl".equals(accessKey)){
        //     // throw new RuntimeException("无权限！");
        //     // 封装了一个方法，专门用于处理异常请求
        //     return handleNoAuth(response);
        // }

        if (Long.parseLong(nonce) > 10000L){
            return handleNoAuth(response);
        }
        //  时间戳校验自己实现，时间和当前时间不能超过5min
        Long currentTime = System.currentTimeMillis() / 1000;
        final Long FIVE_MINUTES = 60 * 5L;
        if ((currentTime-Long.parseLong(timeStamp)) >= FIVE_MINUTES) {
            return handleNoAuth(response);
        }

        // 要去数据库中查询aksk
        String secretKey = invokeUser.getSecretKey();
        String serverSign = SignUtils.getSign(body, secretKey);
        if (sign == null || !sign.equals(serverSign)) {
            return handleNoAuth(response);
        }

        // 4. 请求的模拟接口是否存在？
            // TODO 从数据库中进行查询接口是否存在，以及请求方法是否匹配（严格的话可以再校验一下请求参数，但是业务层面的请求参数不建议放到全局请求网关里面）
            // 因为数据库的访问方法已经再backend中已经写过，操作较为复杂的话不建议重复写，所以我们可以采用远程调用的方式（也就是可以说是微服务，调用接口，避免重复写业务逻辑）
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        } catch (Exception e) {
            log.error("getInterfaceInfo error", e);
        }
        if (interfaceInfo == null) {
            return handleNoAuth(response);
        }

        // TODO 发送请求前，先判断是否还有调用次数，否则剩余次数可以为负数
        // 5. 请求转发，调用模拟接口 + 响应日志
        // Mono<Void> filter = chain.filter(exchange); 已经在下面的装饰器方法中实现，可以注释掉
        // log.info("响应：" + response.getStatusCode());
        // return filter();
        return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());
    }

    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId ){

        try {
            //从交换寄拿响应对象
            ServerHttpResponse originalResponse = exchange.getResponse();
            //缓冲区工厂，拿到缓存数据
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            //拿到响应码
            HttpStatus statusCode = originalResponse.getStatusCode();
            if(statusCode == HttpStatus.OK){
                //装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    //等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        //对象是响应式的
                        if (body instanceof Flux) {
                            //我们拿到真正的body
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            //往返回值里面写数据
                            //拼接字符串
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                // 7. 调用成功，接口调用次数 + 1 invokeCount
                                try {
                                    innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                } catch (Exception e) {
                                    // 真实的企业项目中一般此处会接入一个告警系统
                                    log.info("invokeCount error", e);
                                }
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                sb2.append("<--- {} {} \n");
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                //rspArgs.add(requestUrl);
                                String data = new String(content, StandardCharsets.UTF_8);//data
                                sb2.append(data);
                                log.info("响应结果：" + data);//log.info("<-- {} {}\n", originalResponse.getStatusCode(), data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            // 8. 调用失败，返回一个规范的错误码
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                //设置 response 对象为装饰过的
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            return chain.filter(exchange);//降级处理返回数据
        }catch (Exception e){
            log.error("gateway log exception.\n" + e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
    // 用户鉴权异常
    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        return response.setComplete();
    }
    // 自定义错误异常
    public Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}