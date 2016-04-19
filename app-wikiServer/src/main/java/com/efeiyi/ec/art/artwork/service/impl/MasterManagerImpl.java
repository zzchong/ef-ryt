package com.efeiyi.ec.art.artwork.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtworkManager;
import com.efeiyi.ec.art.artwork.service.MasterManager;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.JPushConfig;
import com.efeiyi.ec.art.jpush.EfeiyiPush;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.model.User;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.service.AliOssUploadManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * Created by Administrator on 2016/4/14.
 *
 */
@Service
public class MasterManagerImpl implements MasterManager {
    private static Logger logger = Logger.getLogger(MasterManagerImpl.class);

    @Autowired
    private AliOssUploadManager aliOssUploadManager;

    @Autowired
    BaseManager baseManager;


    @Override
    public boolean saveMasterWork(JSONObject jsonObject, MultipartFile multipartFile,String hz) {

        try {
            MasterWork masterWork = new MasterWork();

            masterWork.setStatus("1");

            masterWork.setCreator((User)baseManager.getObject(User.class.getName(),jsonObject.getString("currentUserId")));

            masterWork.setCreateDatetime(new Date());

            masterWork.setMaterial(jsonObject.getString("material"));

            masterWork.setName(jsonObject.getString("name"));

            masterWork.setPictureUrl(jsonObject.getString("pictureUrl"));

            String url = "masterWork/"+System.currentTimeMillis()+jsonObject.getString("name")+hz;

            aliOssUploadManager.uploadFile(multipartFile,"ec-efeiyi2",url);

            masterWork.setPictureUrl("http://rongyitou2.efeiyi.com/"+url);

            masterWork.setType("0");

            baseManager.saveOrUpdate(MasterWork.class.getName(),masterWork);


        }catch (Exception e){

            e.printStackTrace();

            return false;
        }

        return true;
    }
}


