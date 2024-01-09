package com.wl.smartapicommon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wl.smartapicommon.model.entity.UserInterfaceInfo;

/**
 * 内部用户接口信息服务
 *
 * @author <a href="https://github.com/wl2o2o">程序员CSGUIDER</a>
 * @from <a href="https://wl2o2o.github.io/">CSGUIDER博客</a>
 */
public interface InnerUserInterfaceInfoService {

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

}
