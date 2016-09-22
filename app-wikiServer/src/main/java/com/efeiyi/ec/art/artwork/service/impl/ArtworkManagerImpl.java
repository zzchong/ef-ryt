package com.efeiyi.ec.art.artwork.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtworkManager;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.JPushConfig;
import com.efeiyi.ec.art.jpush.EfeiyiPush;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
import org.apache.commons.lang.StringUtils;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2016/4/14.
 *
 */
@Service
public class ArtworkManagerImpl implements ArtworkManager {
    private static Logger logger = Logger.getLogger(ArtworkManagerImpl.class);
    @Autowired
    BaseManager baseManager;


    @Override
    public  boolean  saveArtWorkPraise(String id,String messageId){

        Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(),id);

        User currentUser = AuthorizationUtil.getUser();
        try {

            ArtWorkPraise artWorkPraise = new ArtWorkPraise();

            artWorkPraise.setCreateDateTime(new Date());

            artWorkPraise.setStatus("1");

            artWorkPraise.setUser(currentUser);

            artWorkPraise.setWatch("0");

            artWorkPraise.setArtwork(artwork);

            if(!"".equals(messageId) && !StringUtils.isEmpty(messageId)){

                ArtworkMessage artworkMessage = (ArtworkMessage) baseManager.getObject(ArtworkMessage.class.getName(),messageId);

                artWorkPraise.setArtworkMessage(artworkMessage);

//                saveNotification(artwork,"有人回复了!",currentUser,fatherComment.getCreator());

//                cidList.add(fatherComment.getCreator().getId());

            }

            Notification notification = saveNotification(artwork,"有人点赞了!",currentUser,artwork.getAuthor());

            LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();

            param.put("userId", artwork.getAuthor().getId());

            PushUserBinding pushUserBinding = (PushUserBinding)baseManager.getUniqueObjectByConditions(AppConfig.SQL_USER_BINDING_GET,param);

            baseManager.saveOrUpdate(ArtWorkPraise.class.getName(),artWorkPraise);//

            EfeiyiPush.SendPushNotification(JPushConfig.appKey,JPushConfig.masterSecret,notification,pushUserBinding.getCid());

        }catch (Exception e){

            e.printStackTrace();

            return false;
        }
        return  true;

    }

    @Override
    public boolean cancelArtWorkPraise(HttpServletRequest request,String artworkId){

        try {
            List<ArtWorkPraise> artWorkPraiseList = new ArrayList<>();
            if(null != request.getParameter("messageId") && !request.getParameter("messageId").equals("")){
                XQuery xQuery = new XQuery("listArtWorkPraise_byArtWorkMessageId",request);
                xQuery.put("artworkMessage_id", request.getParameter("messageId"));
                xQuery.put("artwork_id",artworkId);
                xQuery.put("user_id",AuthorizationUtil.getUser().getId());
                artWorkPraiseList = baseManager.listObject(xQuery);
            }else {
                XQuery xQuery = new XQuery("listArtWorkPraise_default", request);
                xQuery.put("artwork_id",artworkId);
                xQuery.put("user_id",AuthorizationUtil.getUser().getId());
                artWorkPraiseList = baseManager.listObject(xQuery);
            }

            if(artWorkPraiseList!=null && artWorkPraiseList.size()>0){
                ArtWorkPraise artWorkPraise = artWorkPraiseList.get(0);
                artWorkPraise.setStatus("0");
                baseManager.saveOrUpdate(ArtWorkPraise.class.getName(),artWorkPraise);
                return true;
            }
            return  false;
        }catch (Exception e){
            e.printStackTrace();
            return  false;
        }
    }

    @Override
    public Map saveArtWorkComment(String id, String content,String fatherCommentId,String messageId) {

        Map<String, Object> resultMap = new HashMap<>();
        String currentUserId =AuthorizationUtil.getUserId();

        Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), id);

        User currentUser = AuthorizationUtil.getUser();

        Map<String,Object> map = new HashMap();

        List<String> cidList = new ArrayList<>();

        try {

            ArtworkComment artworkComment = new ArtworkComment();

            artworkComment.setIsWatch("0");

            artworkComment.setCreateDatetime(new Date());

            artworkComment.setContent(content);

            artworkComment.setArtwork(artwork);

            artworkComment.setCreator(currentUser);

            artworkComment.setStatus("1");

            if(!"".equals(fatherCommentId) && !StringUtils.isEmpty(fatherCommentId)){

                ArtworkComment fatherComment = (ArtworkComment)baseManager.getObject(ArtworkComment.class.getName(),fatherCommentId);

                artworkComment.setFatherComment(fatherComment);

                saveNotification(artwork,"有人回复了!",currentUser,fatherComment.getCreator());

                cidList.add(fatherComment.getCreator().getId());

            }


            if(!"".equals(messageId) && !StringUtils.isEmpty(messageId)){

                ArtworkMessage artworkMessage = (ArtworkMessage) baseManager.getObject(ArtworkMessage.class.getName(),messageId);

                artworkComment.setArtworkMessage(artworkMessage);

//                saveNotification(artwork,"有人回复了!",currentUser,fatherComment.getCreator());

//                cidList.add(fatherComment.getCreator().getId());

            }

            baseManager.saveOrUpdate(ArtworkComment.class.getName(),artworkComment);

            saveNotification(artwork,"项目有新的回复了!",currentUser,artwork.getAuthor());

            cidList.add(artwork.getAuthor().getId());

            map.put("cid",cidList);
            map.put("title","");
            map.put("content","有新消息了!");
            EfeiyiPush.sendPushToSingle(JPushConfig.appKey,JPushConfig.masterSecret,map);
            resultMap.put("artworkComment", artworkComment);
            resultMap.put("resultCode", "0");
        }catch (Exception e){
            e.printStackTrace();
            resultMap.put("resultCode", "1");
        }

        return resultMap;
    }


    /**
     * 保存通知
     * @param artwork
     * @param content
     * @param fromUser
     * @param TargetUser
     * @return
     */
    public  Notification saveNotification(Artwork artwork ,String content,User fromUser,User TargetUser){

        Notification notification = null;

        try {

            notification = new Notification();

            notification.setArtwork(artwork);

            notification.setStatus("1");

            notification.setContent(content);

            notification.setCreateDatetime(new Date());

            notification.setFromUser(fromUser);

            notification.setIsWatch("0");

            notification.setTargetUser(TargetUser);

            baseManager.saveOrUpdate(Notification.class.getName(),notification);


        }catch (Exception e){
            e.printStackTrace();
        }


        return notification;

    }

    @Override
    public ConsumerAddress saveConsumerAddress(JSONObject jsonObj, HttpServletRequest request) throws Exception{
        ConsumerAddress consumerAddress = null;
        if (org.springframework.util.StringUtils.isEmpty(jsonObj.getString("addressId")))
            consumerAddress = new ConsumerAddress();
        else
            consumerAddress = (ConsumerAddress) baseManager.getObject(ConsumerAddress.class.getName(), jsonObj.getString("addressId"));

        consumerAddress.setEmail(jsonObj.getString("email"));
        if("2".equals(jsonObj.getString("status"))){
            XQuery xQuery = new XQuery("listAddress_default1",request);
            //xQuery.put("consumer_id",jsonObj.getString("userId"));
            xQuery.put("consumer_id", AuthorizationUtil.getUser().getId());
            List<ConsumerAddress> consumerAddressList = baseManager.listObject(xQuery);
            if(consumerAddressList!=null && consumerAddressList.size()>0){
                for (ConsumerAddress consumerAddress1:consumerAddressList){
                    consumerAddress1.setStatus("1");
                    baseManager.saveOrUpdate(ConsumerAddress.class.getName(), consumerAddress1);
                }
            }
        }
        consumerAddress.setStatus(jsonObj.getString("status"));
//            consumerAddress.setCity((AddressCity)baseManager.getObject(AddressCity.class.getName(),jsonObj.getString("cityId")));
        consumerAddress.setConsignee(jsonObj.getString("consignee"));
        consumerAddress.setConsumer(AuthorizationUtil.getUser());
        consumerAddress.setDetails(jsonObj.getString("details"));
//            consumerAddress.setDistrict((AddressDistrict)baseManager.getObject(AddressCity.class.getName(),jsonObj.getString("districtId")));
        consumerAddress.setPhone(jsonObj.getString("phone"));
        consumerAddress.setProvinceStr(jsonObj.getString("provinceStr"));
//            consumerAddress.setPost(jsonObj.getString("post"));
//            consumerAddress.setProvince((AddressProvince) baseManager.getObject(AddressCity.class.getName(),jsonObj.getString("provinceId")));
        baseManager.saveOrUpdate(ConsumerAddress.class.getName(), consumerAddress);
        return consumerAddress;
    }

    /**
     *校验是否已经点赞
     *
     */
    @Override
    public boolean isPointedPraise(HttpServletRequest request, JSONObject jsonObject){
        try {
            List<ArtWorkPraise> artWorkPraiseList = new ArrayList<>();
            if (null != jsonObject.getString("artworkId") && !jsonObject.getString("artworkId").equals("")){
                if(null != jsonObject.getString("messageId") && !jsonObject.getString("messageId").equals("")){
                    XQuery xQuery = new XQuery("listArtWorkPraise_byArtWorkMessageId",request);
                    xQuery.put("artworkMessage_id", request.getParameter("messageId"));
                    xQuery.put("artwork_id",jsonObject.getString("artworkId"));
                    xQuery.put("user_id",AuthorizationUtil.getUser().getId());
                    artWorkPraiseList = baseManager.listObject(xQuery);
                }else {
                    XQuery xQuery = new XQuery("listArtWorkPraise_default", request);
                    xQuery.put("artwork_id",jsonObject.getString("artworkId"));
                    xQuery.put("user_id",AuthorizationUtil.getUser().getId());
                    artWorkPraiseList = baseManager.listObject(xQuery);
                }
                if(artWorkPraiseList!=null && artWorkPraiseList.size()>0){
                    return true;
                }else {
                    return false;
                }
            }else {
                return true;
            }

        }catch (Exception e){
            return  true;
        }
    }

    @Override
    public Artwork saveOrUpdateArtwork(Artwork artwork, String title, String material, String brief, String investGoalMoney, String duration, String makeInstru, String financingAq, String description) throws Exception {
        if (!title.equals("")){
            artwork.setTitle(title);
        }
        if (!material.equals("")){
            artwork.setMaterial(material);
        }
        if (!brief.equals("")){
            artwork.setBrief(brief);
        }
        if (!investGoalMoney.equals("")){
            artwork.setInvestGoalMoney(new BigDecimal(investGoalMoney));
            artwork.setStartingPrice(new BigDecimal(investGoalMoney));
        }
        if (!duration.equals("")){
            artwork.setDuration(Integer.valueOf(duration));
        }
        if (!description.equals("")){
            artwork.setDescription(description);
        }
        artwork.setCreateDatetime(new Date());
        artwork.setAuthor(AuthorizationUtil.getUser());
        Artworkdirection artworkdirection = null;
        if (!makeInstru.equals("")){
            if (artwork.getArtworkdirection()==null){
                artworkdirection = new Artworkdirection();
            }else {
                artworkdirection = artwork.getArtworkdirection();
            }
            artworkdirection.setMake_instru(makeInstru);
            artworkdirection.setArtwork(artwork);
            baseManager.saveOrUpdate(Artworkdirection.class.getName(), artworkdirection);
        }
        if (!financingAq.equals("")){
            artworkdirection = artwork.getArtworkdirection();
            artworkdirection.setFinancing_aq(financingAq);
            baseManager.saveOrUpdate(Artworkdirection.class.getName(), artworkdirection);
        }
        baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
        artwork.setArtworkdirection(artworkdirection);
        return artwork;
    }


}


