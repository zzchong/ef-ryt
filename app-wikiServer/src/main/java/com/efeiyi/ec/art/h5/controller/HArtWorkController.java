package com.efeiyi.ec.art.h5.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.p.PConst;
import com.ming800.core.util.CookieTool;
import com.ming800.core.util.VerificationCodeGenerator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Administrator on 2016/5/17.
 */
@Controller
public class HArtWorkController extends BaseController {

    @Autowired
    ResultMapHandler resultMapHandler;

    private final static String URL = "/app/shareView.do";

    private final static String VIEW = "/shareView";

    @RequestMapping("/app/toShareView.do")
    @ResponseBody
    public Map createView(HttpServletRequest request) throws Exception{
        LogBean logBean = new LogBean();
        logBean.setApiName("toShareView");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());

//            User user = (User)baseManager.getObject(User.class.getName(),jsonObj.getString("userId"));
            User user = AuthorizationUtil.getUser();
            String userId = user==null?"":user.getId();
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("user",user);//响应的用户信息
            resultMap.put("url","http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+URL+"?userId="+userId);
            System.out.println(request.getServerName()+"-----"+request.getServerPort()+"-----"+request.getContextPath());
        } catch(Exception e){
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
        return resultMap;
    }

    @RequestMapping("/app/shareView.do")
    public String shareView(HttpServletRequest request){

        return VIEW;
    }


    @RequestMapping("/app/shareViewWap.do")
    public String shareViewWap(HttpServletRequest request){

        return  "/shareView";
    }

    public static void main(String[] arg) throws Exception {


        String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
        long timestamp = System.currentTimeMillis();
        Map<String, Object> map = new HashMap<String, Object>();


        /**toShareView.do测试加密参数**/
        map.put("userId", "ina6pqm2d036fya5");
        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        map.put("signmsg",signmsg);
        HttpClient httpClient = new DefaultHttpClient();
//        String url = "http://192.168.1.41:8080/app/myArtwork.do";
        String url = "http://192.168.1.75:8080/app/toShareView.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  artWorkCreationList.do测试 **/
//        String json = "{\"pageNum\":\"1\",\"pageSize\":\"5\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  artWorkCreationView.do测试 **/
//        String json = "{\"userId\":\"ina6pqm2d036fya5\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
        JSONObject jsonObj = (JSONObject) JSONObject.toJSON(map);
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
            System.out.println(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
