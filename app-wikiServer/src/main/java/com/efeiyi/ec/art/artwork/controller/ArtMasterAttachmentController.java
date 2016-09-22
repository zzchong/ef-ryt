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

    @RequestMapping("/app/saveArtMasterAttachment.do")
    @ResponseBody
    public Map saveArtMasterAttachment(HttpServletRequest request) {
        Map<String, Object> resultMap = null;
        LogBean logBean = new LogBean();

        try {
            resultMap = attachmentManager.saveArtMasterAttachment(request, logBean);
        } catch(Exception e) {
            return  resultMapHandler.handlerResult("100011","保存附件失败",logBean);
        }

        return resultMap;
    }

    @RequestMapping("/app/deleteArtMasterAttachment.do")
    @ResponseBody
    public Map deleteArtMasterAttachment(HttpServletRequest request) {
        Map<String, Object> resultMap = null;
        LogBean logBean = new LogBean();

        try {
            resultMap = attachmentManager.deleteArtMasterAttachment(request, logBean);
        } catch(Exception e) {
            return  resultMapHandler.handlerResult("100011","删除附件失败",logBean);
        }

        return resultMap;
    }


}
