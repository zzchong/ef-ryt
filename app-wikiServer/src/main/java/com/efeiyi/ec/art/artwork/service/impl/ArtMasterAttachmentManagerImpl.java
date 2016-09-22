package com.efeiyi.ec.art.artwork.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtMasterAttachmentManager;
import com.efeiyi.ec.art.artwork.service.ArtworkAuctionManager;
import com.efeiyi.ec.art.artwork.service.MasterManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.service.UploadImageManager;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.ArtMasterAttachment;
import com.efeiyi.ec.art.model.Master;
import com.efeiyi.ec.art.model.MasterWork;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.service.AliOssUploadManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by Administrator on 2016/9/7.
 */

@Service
public class ArtMasterAttachmentManagerImpl implements ArtMasterAttachmentManager {

    @Autowired
    private UploadImageManager uploadImageManager;

    @Autowired
    private MasterManager masterManager;

    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    BaseManager baseManager;


    @Override
    public Map saveArtMasterAttachment(HttpServletRequest request, LogBean logBean) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<ArtMasterAttachment> attachments = new ArrayList<>();

        String userId = AuthorizationUtil.getUserId();

        Master master = masterManager.getMasterByUserId(userId);

        if(master == null) {
            return  resultMapHandler.handlerResult("100011","获取艺术家出错",logBean);
        }

        List<Map<String, Object>> list = uploadImageManager.uplaodImage(request);

        for(Map<String, Object> map : list) {
            ArtMasterAttachment attachment = new ArtMasterAttachment();
            attachment.setType("1");
            attachment.setMaster(master);
            attachment.setUrl(map.get("pictureUrl").toString());
            attachment.setWidth(map.get("width").toString());
            attachment.setHeight(map.get("height").toString());

            baseManager.saveOrUpdate(ArtMasterAttachment.class.getName(), attachment);

            attachments.add(attachment);
        }

        resultMap.put("resultCode", "0");
        resultMap.put("resultMsg", "附件保存成功");
        resultMap.put("dataList", attachments);
        return resultMap;
    }

    @Override
    public Map deleteArtMasterAttachment(HttpServletRequest request, LogBean logBean) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参

        String id = jsonObj.getString("id");

        if(id == null) {
            return  resultMapHandler.handlerResult("100011","获取删除附件Id失败",logBean);
        }

        baseManager.delete(ArtMasterAttachment.class.getName(), id);

        resultMap.put("resultCode", "0");
        resultMap.put("resultMsg", "附件删除成功");
        resultMap.put("data", id);
        return resultMap;
    }

}
