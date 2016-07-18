package com.efeiyi.ec.art.personal.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.Account;
import com.efeiyi.ec.art.model.UserBrief;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Administrator on 2016/4/21.
 *
 */
@RestController
public class UserBriefController  extends BaseController {
    private static Logger logger = Logger.getLogger(UserBriefController.class);

    @Autowired
    BaseManager baseManager;
    @Autowired
    ResultMapHandler resultMapHandler;

    @RequestMapping(value = "/app/saveUserBrief.do", method = RequestMethod.POST)//获取投资者排行榜
    @ResponseBody
    public Map getInvestorTopList(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("saveUserBrief");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg"))
//                    || "".equals(jsonObj.getString("userId"))
                    ||"".equals(jsonObj.getString("timestamp"))
                  //  || "".equals(jsonObj.getString("content"))
                    || "".equals(jsonObj.getString("type"))
                    ) {
                return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }

            String signmsg = jsonObj.getString("signmsg");
//            treeMap.put("userId",jsonObj.getString("userId"));
            treeMap.put("type",jsonObj.getString("type"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return  resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
//            User user = (User)baseManager.getObject(User.class.getName(),jsonObj.getString("userId"));
            User user = AuthorizationUtil.getUser();
            //

            LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
            param.put("userId", user.getId());
            UserBrief userBrief =  (UserBrief)baseManager.getUniqueObjectByConditions(AppConfig.SQL_GET_USER_BRIEF, param);//如果已存在
            if(userBrief==null || userBrief.getId()==null){
                userBrief = new UserBrief();
            }
            if(user.getMaster()!= null && user.getMaster().getId()!=null){
                userBrief.setType("1");
            }else{
                userBrief.setType("2");
            }
            userBrief.setUser(user);
            userBrief.setStatus("1");
            if ("1".equals(jsonObj.getString("type"))){
                if("".equals(jsonObj.getString("signer"))){
                    return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
                }
                userBrief.setContent(jsonObj.getString("signer"));//编辑签名
            } else{
                if("".equals(jsonObj.getString("content"))){
                    return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
                }
                userBrief.setContent(jsonObj.getString("content"));
            }

            userBrief.setCreateDatetime(new Date());
            baseManager.saveOrUpdate(UserBrief.class.getName(),userBrief);
            return resultMapHandler.handlerResult("0","成功",logBean);
        } catch(Exception e){
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
    }

    public static void main(String[] arg) throws Exception {


        String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new TreeMap<>();

        /**investorIndex.do测试加密参数**/
//        map.put("pageSize","3");
//        map.put("pageNum","1");
//        map.put("timestamp", timestamp);
        /**investorArtWorkView.do测试加密参数**/
//        map.put("artWorkId", "qydeyugqqiugd2");
//        map.put("messageId","2");
//        map.put("pageSize", "4");
//        map.put("pageIndex", "1");
        map.put("timestamp", timestamp);
        map.put("type", 2);
        map.put("userId", "ieatht97wfw30hfd");


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
//        map.put("messageId","2");
//        map.put("timestamp", timestamp);

        /**artworkComment.do测试加密参数**/
//        map.put("artWorkId","qydeyugqqiugd2");
//        map.put("currentUserId","iih8wrlm31r449bh");
//        map.put("fatherCommentId","3");
//        map.put("content","同意+1");
//        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        map.put("content","geiwwwwwwwww");
        map.put("signmsg",signmsg);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.41:8080/app/saveUserBrief.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  investorArtWork.do测试 **/
//        String json = "{\"pageIndex\":\"1\",\"pageSize\":\"4\",\"artWorkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
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
//        JSONObject jsonObj = (JSONObject) JSONObject.parse(json);
//        String jsonString = jsonObj.toJSONString();
        String jsonString = JSONObject.toJSONString(map);

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
