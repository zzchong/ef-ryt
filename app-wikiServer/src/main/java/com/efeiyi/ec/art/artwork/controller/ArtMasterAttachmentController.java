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

            MultipartFile attachmentFile = null;

            //创建一个通用的多部分解析器
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            //判断 request 是否有文件上传,即多部分请求
            if (multipartResolver.isMultipart(request)) {
                //转换成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                //取得request中的所有文件名
                Iterator<String> iter = multiRequest.getFileNames();

                while (iter.hasNext()) {
                    //取得上传文件
                    MultipartFile file = multiRequest.getFile(iter.next());
                    if (file != null) {
                        //取得当前上传文件的文件名称
                        String myFileName = file.getOriginalFilename();
                        //如果名称不为“”,说明该文件存在，否则说明该文件不存在
                        if (myFileName.trim() != "") {
                            //重命名上传后的文件名
                            StringBuilder url = new StringBuilder("master/");

                            url.append("picture/" + new Date().getTime() + myFileName);

                            String pictureUrl = "http://rongyitou2.efeiyi.com/" + url.toString();
                            //将图片上传至阿里云
                            aliOssUploadManager.uploadFile(file, "ec-efeiyi2", url.toString());
                            if("file0".equals(file.getName())) {
                                master.setIdentityFront(pictureUrl);
                                attachmentFile = file;
                            } else if("file1".equals(file.getName())) {
                                master.setIdentityBack(pictureUrl);
                            }
                        }
                    }
                }
            }

            attachmentManager.saveMasterAttachment(masterAttachment, attachmentFile);

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
