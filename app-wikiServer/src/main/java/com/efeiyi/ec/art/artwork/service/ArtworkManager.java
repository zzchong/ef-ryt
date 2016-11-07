package com.efeiyi.ec.art.artwork.service;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.model.AuctionOrder;
import com.efeiyi.ec.art.model.ConsumerAddress;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
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
     *
     * 取消点赞
     * @param request
     * @param artworkId
     * @return
     */
     boolean cancelArtWorkPraise(HttpServletRequest request,String artworkId, String messageId);

    /**
     * 评论
     * @param id
     * @param content
     * @param fatherCommentId
     * @return
     */
    Map  saveArtWorkComment(String id,String content,String fatherCommentId,String messageId);

    ConsumerAddress saveConsumerAddress(JSONObject jsonObject,HttpServletRequest request) throws Exception;

    boolean isPointedPraise(HttpServletRequest request,JSONObject jsonObject);


    /**
     * 项目创建或修改
     */
    Artwork saveOrUpdateArtwork(Artwork artwork, String title, String material, String brief, String investGoalMoney, String duration, String makeInstru, String financingAq, String description) throws Exception;

    void saveAuctionOrder(Artwork artwork) throws Exception;

    void returnMargin(String artworkId, String winnerId) throws Exception;

    void returnInvestmentFunds(Artwork artwork) throws Exception;
}
