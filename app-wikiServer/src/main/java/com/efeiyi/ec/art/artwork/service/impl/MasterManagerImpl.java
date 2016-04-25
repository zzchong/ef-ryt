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

import javax.servlet.http.HttpServletRequest;
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
    public boolean saveMasterWork(HttpServletRequest request, MultipartFile multipartFile) {

        try {
            MasterWork masterWork = new MasterWork();

            masterWork.setStatus("1");

            masterWork.setCreator((User)baseManager.getObject(User.class.getName(),request.getParameter("currentUserId")));

            masterWork.setCreateDatetime(new Date());

            masterWork.setMaterial(request.getParameter("material"));

            masterWork.setName(request.getParameter("name"));

            String url = "masterWork/"+System.currentTimeMillis()+multipartFile.getOriginalFilename();

            aliOssUploadManager.uploadFile(multipartFile,"ec-efeiyi2",url);

            masterWork.setPictureUrl("http://rongyitou2.efeiyi.com/"+url);

            masterWork.setType(request.getParameter("type"));

            baseManager.saveOrUpdate(MasterWork.class.getName(),masterWork);


        }catch (Exception e){

            e.printStackTrace();

            return false;
        }

        return true;
    }
}


