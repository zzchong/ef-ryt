package com.efeiyi.ec.art.artwork.service;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/14.
 *
 */
public interface ArtworkManager {

    /**
     * 点赞
     * @param id
     * @param currentUserId
     * @return
     */
    boolean  saveArtWorkPraise(String id,String currentUserId);

    /**
     * 评论
     * @param id
     * @param content
     * @param fatherCommentId
     * @param currentUserId
     * @return
     */
    boolean  saveArtWorkComment(String id,String content,String fatherCommentId,String currentUserId);


}
