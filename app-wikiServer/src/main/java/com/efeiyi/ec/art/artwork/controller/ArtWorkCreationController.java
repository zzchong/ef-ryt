package com.efeiyi.ec.art.artwork.controller;


import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtworkPraiseManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.*;
import com.efeiyi.ec.art.message.dao.MessageDao;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.efeiyi.ec.art.organization.util.CommonUtil;
import com.efeiyi.ec.art.organization.util.TimeUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.taglib.PageEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2016/1/29.
 *
 */
@Controller
public class ArtWorkCreationController extends BaseController {
    private static Logger logger = Logger.getLogger(ArtWorkCreationController.class);

    @Autowired
    private MessageDao messageDao;

    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    ArtworkPraiseManager artworkPraiseManager;


    /**
     * 创作首页
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artWorkCreationList.do", method = RequestMethod.POST)
    @ResponseBody
    public Map ArtWorkCreation(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        List<Artwork> artworkList = null;
        try{
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkCreationList");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            if (!DigitalSignatureUtil.verify2(jsonObj)) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            XQuery xQuery = new XQuery("plistArtwork_default2", request);
            xQuery.put("type", "2");
            PageEntity pageEntity = new PageEntity();
            pageEntity.setSize(jsonObj.getInteger("pageSize"));
            pageEntity.setIndex(jsonObj.getInteger("pageIndex"));
            xQuery.setPageEntity(pageEntity);
            artworkList = baseManager.listPageInfo(xQuery).getList();
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
//            String str1 = sdf.format(new Date());
            if(artworkList!=null){
            for (Artwork artwork : artworkList) {
//                if(artwork.getPicture_url()!=null) {
//                    artwork.setWidth(ImgUtil.getWidth(artwork.getPicture_url()));
//                    artwork.setHeight(ImgUtil.getHeight(artwork.getPicture_url()));
//                }
                //是否点赞
                Boolean isPraise = false;
                if (!StringUtils.isEmpty(AuthorizationUtil.getUser())) {
                    XQuery xQuery1 = new XQuery("listArtWorkPraise_default", request);
                    xQuery1.put("artwork_id", artwork.getId());
                    xQuery1.put("user_id", AuthorizationUtil.getUser().getId());
                    List<ArtWorkPraise> artWorkPraiseList = baseManager.listObject(xQuery1);
                    if (artWorkPraiseList != null) {
                        if (artWorkPraiseList.size() > 0) {
                            isPraise = true;
                        }
                    }
                }
                artwork.setPraise(isPraise);
                //项目动态
                if (artwork.getArtworkMessages() != null && artwork.getArtworkMessages().size() > 0) {
                    artwork.setNewCreationDate(artwork.getArtworkMessages().get(0).getCreateDatetime());
                } else {
                    artwork.setNewCreationDate(null);
                }
            }
            }
            data.put("artworkList",artworkList);
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("object",data);
        } catch(Exception e){
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }

        return resultMap;
    }


    /**
     * 创作详情页
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artWorkCreationView.do")
    @ResponseBody
    public MappingJacksonValue ArtWorkCreationView(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        List<Artwork> artworkList = null;
        try{
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkCreationView");
            /*if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            if (!DigitalSignatureUtil.verify2(jsonObj)) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }*/
            Artwork artwork = (Artwork)baseManager.getObject(Artwork.class.getName(),jsonObj.getString("artWorkId"));
            //已创作时长
            String createdTime = "";
            //剩余时长
            String restTime = "";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            if(artwork.getInvestEndDatetime()!=null && artwork.getCreationEndDatetime()!=null){
            String str1 = sdf.format(new Date());
            String str2 = sdf.format(artwork.getInvestEndDatetime());
            String str3 = sdf.format(artwork.getCreationEndDatetime());
            createdTime = TimeUtil.getDistanceTimes(str1,str2);
            restTime = TimeUtil.getDistanceTimes(str3,str1);
            }
            //是否点赞
            Boolean isPraise = false;
            if(!StringUtils.isEmpty(AuthorizationUtil.getUser())) {
                XQuery xQuery = new XQuery("listArtWorkPraise_default", request);
                xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
                xQuery.put("user_id", AuthorizationUtil.getUser().getId());
                List<ArtWorkPraise> artWorkPraiseList = baseManager.listObject(xQuery);
                if (artWorkPraiseList != null) {
                    if (artWorkPraiseList.size() > 0) {
                        isPraise = true;
                    }
                }
            }

            PageEntity pageEntity = new PageEntity();
            if (jsonObj.getInteger("pageSize") != null && jsonObj.getInteger("pageIndex") != null && jsonObj.getInteger("pageSize")>0 && jsonObj.getInteger("pageIndex")>0){
                pageEntity.setSize(jsonObj.getInteger("pageSize"));
                pageEntity.setIndex(jsonObj.getInteger("pageIndex"));
            }
            XQuery xQuery = new XQuery("plistArtworkMessage_byArtwork", request);
            xQuery.put("artwork_id", artwork.getId());
            xQuery.setPageEntity(pageEntity);
            List<ArtworkMessage> artworkMessageList = baseManager.listPageInfo(xQuery).getList();
            if (AuthorizationUtil.getUser() != null && artworkMessageList != null && artworkMessageList.size()>0){
                //1.该用户已对该动态点赞 2.该用户未对该动态点赞
                for (ArtworkMessage artworkMessage:artworkMessageList){
                    if (artworkPraiseManager.isToArtworkMessagePraise(request, artworkMessage, AuthorizationUtil.getUser())){
                        artworkMessage.setPraiseIsOrNot("1");
                    }else {
                        artworkMessage.setPraiseIsOrNot("0");
                    }
                }

            }

            data.put("artwork",artwork);
            data.put("artworkMessageList",artworkMessageList);
            data.put("createdTime",createdTime);
            data.put("restTime",restTime);
            data.put("isPraise",isPraise);
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("object",data);
        } catch(Exception e){
            e.printStackTrace();
            return resultMapHandler.handlerResultType(request, resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean));
        }

        return resultMapHandler.handlerResultType(request, resultMap);
    }

    /**
     * 创作动态接口
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/plistArtWorkMessage.do")
    @ResponseBody
    public Map PlistArtWorkMessage(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        try{
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("plistArtWorkMessage");

            PageEntity pageEntity = new PageEntity();
            pageEntity.setSize(jsonObj.getInteger("pageSize"));
            pageEntity.setIndex(jsonObj.getInteger("pageIndex"));
            XQuery xQuery = new XQuery("plistArtworkMessage_byArtwork", request);
            xQuery.put("artwork_id", jsonObj.getString("artworkId"));
            xQuery.setPageEntity(pageEntity);
            List<ArtworkMessage> artworkMessageList = baseManager.listPageInfo(xQuery).getList();
            if (AuthorizationUtil.getUser() != null && artworkMessageList != null && artworkMessageList.size()>0){
                //1.该用户已对该动态点赞 2.该用户未对该动态点赞
                for (ArtworkMessage artworkMessage:artworkMessageList){
                    if (artworkPraiseManager.isToArtworkMessagePraise(request, artworkMessage, AuthorizationUtil.getUser())){
                        artworkMessage.setPraiseIsOrNot("1");
                    }else {
                        artworkMessage.setPraiseIsOrNot("0");
                    }
                }

            }
            data.put("artworkMessageList", artworkMessageList);
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("data",data);
        } catch(Exception e){
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }

        return resultMap;
    }


    public  static  void  main(String [] arg) throws Exception {


        String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
        long timestamp = System.currentTimeMillis();
        Map<String, Object> map = new HashMap<String, Object>();

        /**artWorkCreationList.do测试加密参数**/
//        map.put("pageNum","1");
//        map.put("pageSize","5");
        /**artWorkCreationView.do测试加密参数**/
        map.put("artWorkId","qydeyugqqiugd2");
        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.75:8001/app/artWorkCreationView.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  artWorkCreationList.do测试 **/
//        String json = "{\"pageNum\":\"1\",\"pageSize\":\"5\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  artWorkCreationView.do测试 **/
        String json = "{\"artWorkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        JSONObject jsonObj = (JSONObject)JSONObject.parse(json);
        String jsonString = jsonObj.toJSONString();

        StringEntity stringEntity = new StringEntity(jsonString,"utf-8");
        stringEntity.setContentType("text/json");
        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httppost.setEntity(stringEntity);
        System.out.println("url:  " + url);
        try {
            byte[] b = new byte[(int) stringEntity.getContentLength()];
            System.out.println(stringEntity);
            stringEntity.getContent().read(b);
            System.out.println("报文:" + new String(b, "utf-8"));
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
