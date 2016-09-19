package com.efeiyi.ec.art.artwork.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtMasterAttachmentManager;
import com.efeiyi.ec.art.artwork.service.MasterManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.ArtMasterAttachment;
import com.efeiyi.ec.art.model.ArtworkMessageAttachment;
import com.efeiyi.ec.art.model.Master;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.service.AliOssUploadManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/7.
 */

@Controller
public class ArtMasterAttachmentController extends BaseController {

    @Autowired
    ArtMasterAttachmentManager attachmentManager;

    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    BaseManager baseManager;

    @Autowired
    private MasterManager masterManager;

    @Autowired
    private AliOssUploadManager aliOssUploadManager;

    @RequestMapping("/app/saveMasterAttachment.do")
    @ResponseBody
    public Map saveMasterAttachment(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<>();
        Master master = null;

        try{
            String userId = AuthorizationUtil.getUserId();

            master = masterManager.getMasterByUserId(userId);

            if(master == null) {
                return  resultMapHandler.handlerResult("10004","艺术家为空",logBean);
            }

            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);

            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("saveMasterAttachment");

            ArtMasterAttachment masterAttachment = new ArtMasterAttachment();
            masterAttachment.setMaster(master);
            masterAttachment.setPaperNo(jsonObj.getString("paperNo"));
            masterAttachment.setPaperType(jsonObj.getString("paperType"));
            masterAttachment.setRemark(jsonObj.getString("remark"));

            String imageStr = jsonObj.getString("image");
            String[] imageArr = null;
            if(imageStr != null) {
                imageArr = imageStr.split(",");
            }

            if(imageStr != null) {
                master.setIdentityFront(imageArr[0]);
                master.setIdentityBack(imageArr[1]);
            }

            attachmentManager.saveMasterAttachment(masterAttachment, imageArr);

            master.setTheStatus("2");//把状态改为审核中
            baseManager.saveOrUpdate(Master.class.getName(), master);

            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "审核提交成功");
        } catch (Exception e) {
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }

        return resultMap;
    }

}
