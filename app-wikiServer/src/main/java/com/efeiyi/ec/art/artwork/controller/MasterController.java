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
     * 艺术家 发布作品
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/saveMasterWork.do", method = RequestMethod.POST)
    @ResponseBody
    public Map saveMasterWork(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        TreeMap map = new TreeMap();
        List objectList = null;
        try{
//            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
//            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("saveMasterWork");
            if(StringUtils.isEmpty(request.getParameter("name"))
                    ||StringUtils.isEmpty(request.getParameter("material"))
//                    || StringUtils.isEmpty(request.getParameter("currentUserId"))
                    || StringUtils.isEmpty(request.getParameter("type"))
                    || StringUtils.isEmpty(request.getParameter("timestamp"))
                    || StringUtils.isEmpty(request.getParameter("createYear"))){
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            map.put("name",request.getParameter("name"));
            map.put("material",request.getParameter("material"));
//            map.put("currentUserId",request.getParameter("currentUserId"));
            map.put("type",request.getParameter("type"));
            map.put("createYear",request.getParameter("createYear"));
            map.put("timestamp",request.getParameter("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(map,request.getParameter("signmsg"));
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }

            MultipartFile picture = ((MultipartHttpServletRequest)request).getFile("pictureUrl");


            if(masterManager.saveMasterWork(request,picture)){

                return resultMapHandler.handlerResult("0","成功",logBean);
            }else {
                return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
            }


        } catch(Exception e){

            e.printStackTrace();
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }

    }










    public  static  void  main(String [] arg) throws Exception {


        String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<String, Object>();

        File file = new File("C:\\Users\\Administrator\\Desktop\\石榴瓶.JPG");

        /**saveMasterWork.do测试加密参数**/
        map.put("name", "清明上河图");
        map.put("material", "纸质");
        map.put("currentUserId", "iih8wrlm31r449bh");
        map.put("type", "0");
//        map.put("pictureUrl",file);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.80:8001/app/saveMasterWork.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/octet-stream;charset=utf-8");

        /**json参数  investorArtWork.do测试 **/
//        String json = "{\"pictureUrl\":file,\"type\":\"0\",\"name\":\"清明上河图\",\"material\":\"纸质\",\"currentUserId\":\"iih8wrlm31r449bh\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
//        String json = "{\"currentUserId\":\"iickhknq3h7yrku2\",\"artWorkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  investorIndex.do测试 **/
//        String json = "{\"pageSize\":\"3\",\"pageNum\":\"1\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  guestView.do测试 **/
//        String json = "{\"userId\":\"icjxkedl0000b6i0\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  masterView.do测试 **/
//        String json = "{\"masterId\":\"icjxkedl0000b6i0\",\"pageSize\":\"3\",\"pageNum\":\"1\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  artworkPraise.do测试 **/
//        String json = "{\"currentUserId\":\"iih8wrlm31r449bh\",\"artWorkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";

        /**json参数  artworkPraise.do测试 **/
//        String json = "{\"content\":\"同意+1\",\"fatherCommentId\":\"3\",\"currentUserId\":\"iih8wrlm31r449bh\",\"artWorkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
//        JSONObject jsonObj = (JSONObject)JSONObject.parse(json);
//        String jsonString = jsonObj.toJSONString();


        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setLaxMode();

        FileBody fb  = new FileBody(file,ContentType.MULTIPART_FORM_DATA);
        multipartEntityBuilder.addBinaryBody("pictureUrl",file);
//        multipartEntityBuilder.seContentType(ContentType.MULTIPART_FORM_DATA);
//        multipartEntityBuilder.setCharset(Consts.UTF_8);
        multipartEntityBuilder.addTextBody("name","清明上河图",ContentType.MULTIPART_FORM_DATA);
        multipartEntityBuilder.addTextBody("currentUserId","iih8wrlm31r449bh",ContentType.MULTIPART_FORM_DATA);
        multipartEntityBuilder.addTextBody("type","0",ContentType.MULTIPART_FORM_DATA);
        multipartEntityBuilder.addTextBody("material","纸质",ContentType.MULTIPART_FORM_DATA);
//        StringEntity stringEntity = new StringEntity(jsonString,"utf-8");
//        stringEntity.setContentType("text/json");
//        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httppost.setEntity(multipartEntityBuilder.build());
        System.out.println("url:  " + url);
        try {
//            byte[] b = new byte[(int) stringEntity.getContentLength()];
//            System.out.println(stringEntity);
//            stringEntity.getContent().read(b);
//            System.out.println("报文:" + new String(b, "utf-8"));
            HttpResponse response = httpClient.execute(httppost);
            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    entity.getContent(), "UTF-8"));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

        }catch (Exception e){

        }
    }



}
