package com.wl.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wl.project.model.entity.Post;

/**
 * @author <a href="https://github.com/wl2o2o">程序员CSGUIDER</a>li
 * @description 针对表【post(帖子)】的数据库操作Service
 */
public interface PostService extends IService<Post> {

    /**
     * 校验
     *
     * @param post
     * @param add 是否为创建校验
     */
    void validPost(Post post, boolean add);
}
