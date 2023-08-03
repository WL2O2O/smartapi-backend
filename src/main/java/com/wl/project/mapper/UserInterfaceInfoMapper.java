package com.wl.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wl.smartapicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
 * @Entity com.wl.smartapicommon.model.entity.UserInterfaceInfo
 */
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    // 获取前几个调用次数最多的接口
    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);

}




