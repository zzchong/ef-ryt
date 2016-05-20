package com.efeiyi.ec.art.artwork.service.impl;

import com.efeiyi.ec.art.artwork.service.ArtworkManager;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.JPushConfig;
import com.efeiyi.ec.art.jpush.EfeiyiPush;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.model.User;
import com.ming800.core.base.service.BaseManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public  boolean  saveArtWorkPraise(String id,String currentUserId,String messageId){

        Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(),id);

        User currentUser = (User) baseManager.getObject(User.class.getName(),currentUserId);
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
    public boolean saveArtWorkComment(String id, String content,String fatherCommentId,String currentUserId,String messageId) {

        if(StringUtils.isEmpty(currentUserId) || StringUtils.isEmpty(id)) {

            return  false;
        }

        Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), id);

        User currentUser = (User)baseManager.getObject(User.class.getName(),currentUserId);

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

        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
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
}


