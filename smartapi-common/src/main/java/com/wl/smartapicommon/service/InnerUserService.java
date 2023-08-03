package com.wl.smartapicommon.service;


import com.wl.smartapicommon.model.entity.User;

/**
 * 用户服务
 * @author wl
 */
public interface InnerUserService {
    /**
     * 数据库中查是否已分配给用户秘钥（accessKey）
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);

}
