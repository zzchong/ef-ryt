package com.efeiyi.ec.art.artwork.service;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.model.ConsumerAddress;

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
     * @return
     */
    boolean  saveArtWorkPraise(String id,String messageId);

    /**
     * 评论
     * @param id
     * @param content
     * @param fatherCommentId
     * @return
     */
    boolean  saveArtWorkComment(String id,String content,String fatherCommentId,String messageId);

    ConsumerAddress saveConsumerAddress(JSONObject jsonObject,HttpServletRequest request) throws Exception;


}
