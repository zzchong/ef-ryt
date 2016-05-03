package com.efeiyi.ec.art.artwork.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtworkInvestManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.Account;
import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.model.ArtworkInvest;
import com.efeiyi.ec.art.organization.model.MyUser;
import com.efeiyi.ec.art.organization.model.User;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2016/4/14.
 *
 */
@Controller
public class ArtworkInvestController extends BaseController {
    private static Logger logger = Logger.getLogger(ArtworkInvestController.class);

    @Autowired
    BaseManager baseManager;
    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    ArtworkInvestManager artworkInvestManager;



    /**
     * 用户投资接口，此接口不涉及微信、支付宝
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artworkInvest.do", method = RequestMethod.POST)
    @ResponseBody
    public Map artworkInvest(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("artworkInvest");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId"))
                    || "".equals(jsonObj.getString("artworkId"))  || "".equals(jsonObj.getString("price"))
                    || "".equals(jsonObj.getString("timestamp"))) {

                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("userId", jsonObj.getString("userId"));
            treeMap.put("artworkId", jsonObj.getString("artworkId"));
            treeMap.put("price", jsonObj.getString("price"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {

                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            resultMap = artworkInvestManager.artworkInvest(request,jsonObj,logBean);

        } catch (Exception e) {

            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
        return resultMap;
    }

    public static void main(String[]a) throws Exception {
        String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<String, Object>();

        /**investorIndex.do测试加密参数**/
//        map.put("pageSize","3");
//        map.put("pageNum","1");
//        map.put("timestamp", timestamp);
        /**investorArtWorkView.do测试加密参数**/
        map.put("artWorkId", "qydeyugqqiugd2");
//        map.put("currentUserId","iickhknq3h7yrku2");
        map.put("pageSize", "4");
        map.put("pageIndex", "1");
        map.put("timestamp", timestamp);

        /**masterView.do测试加密参数**/
//        map.put("pageSize","3");
//        map.put("pageNum","1");
//        map.put("masterId","icjxkedl0000b6i0");
//        map.put("timestamp", timestamp);
        /**guestView.do测试加密参数**/
//        map.put("userId","icjxkedl0000b6i0");
//        map.put("timestamp", timestamp);

        /**artworkPraise.do测试加密参数**/
//        map.put("artWorkId","qydeyugqqiugd2");
//        map.put("currentUserId","iih8wrlm31r449bh");
//        map.put("timestamp", timestamp);

        /**artworkComment.do测试加密参数**/
//        map.put("artWorkId","qydeyugqqiugd2");
//        map.put("currentUserId","iih8wrlm31r449bh");
//        map.put("fatherCommentId","3");
//        map.put("content","同意+1");
//        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.41:8001/app/investorArtWorkComment.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  investorArtWork.do测试 **/
        String json = "{\"pageIndex\":\"1\",\"pageSize\":\"4\",\"artWorkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
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
        JSONObject jsonObj = (JSONObject) JSONObject.parse(json);
        String jsonString = jsonObj.toJSONString();


        StringEntity stringEntity = new StringEntity(jsonString, "utf-8");
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
            System.out.println(stringBuilder);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
