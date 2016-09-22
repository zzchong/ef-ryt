package com.efeiyi.ec.art.artwork.controller;


import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtworkManager;
import com.efeiyi.ec.art.artwork.service.MasterManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.message.dao.MessageDao;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.modelConvert.ArtWorkInvestBean;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.efeiyi.ec.art.organization.util.CommonUtil;
import com.efeiyi.ec.art.organization.util.TimeUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.does.model.PageInfo;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.p.service.AliOssUploadManager;
import com.ming800.core.taglib.PageEntity;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2016/1/29.
 *
 */
@Controller
public class MasterController extends BaseController {
    private static Logger logger = Logger.getLogger(MasterController.class);

    @Autowired
    private MessageDao messageDao;

    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    AliOssUploadManager aliOssUploadManager;

    @Autowired
    private MasterManager masterManager;


    /**
     * 艺术家认证-基本信息页
     * @param request
     */
    @RequestMapping("/app/saveMaster.do")
    @ResponseBody
    public Map saveMaster(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap;

        try{
            resultMap = masterManager.saveMasterBasic(request, logBean);
        } catch (Exception e) {
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
        return resultMap;
    }

    @RequestMapping("/app/saveMasterIdentity.do")
    @ResponseBody
    public Map saveMasterIdentity(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = null;

        try{
            resultMap = masterManager.saveMasterIdentity(request, logBean);
        } catch (Exception e) {
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }

        return resultMap;
    }

    @RequestMapping("/app/getMaster.do")
    @ResponseBody
    public Map getMaster(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        User user = AuthorizationUtil.getUser();
        Map<String, Object> resultMap = new HashMap<>();
        Master master = null;

        try{
            master = masterManager.getMasterByUserId(user.getId());
            if(master == null) {
                return  resultMapHandler.handlerResult("100011","第一次验证没有缓冲数据",logBean);
            }

            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "艺术家获取成功");
            resultMap.put("data", master);
        } catch (Exception e) {
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }

        return resultMap;
    }

    /**
     * 艺术家 发布作品
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/saveMasterWork.do", method = RequestMethod.POST)
    @ResponseBody
    public Map saveMasterWork(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try{
            logBean.setCreateDate(new Date());//操作时间
            logBean.setApiName("saveMasterWork");

            MasterWork masterWork = masterManager.saveMasterWork(request);

            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "作品上传成功");
            resultMap.put("data", masterWork);
        } catch(Exception e){
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }

        return resultMap;
    }

    @RequestMapping("/app/getMasterWorks.do")
    @ResponseBody
    public Map getMasterWorks(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = null;

        try{
            resultMap = masterManager.getMasterWorks(request, logBean);
        } catch (Exception e) {
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
        return resultMap;
    }


}
