package com.efeiyi.ec.art.version.controller;

import com.alibaba.fastjson.JSONObject;

import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.version.model.AppVersionUpGrade;
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
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Administrator on 2016/1/23.
 *
 */
@Controller
public class AppVersionController extends BaseController{
    private static Logger logger = Logger.getLogger(AppVersionController.class);
    @Autowired
    BaseManager baseManager;
    @RequestMapping(value = "/app/upgrade.do", method = RequestMethod.POST)
    @ResponseBody
    public Map upgrade(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("platform"))
                    || "".equals(jsonObj.getString("version_id")) || "".equals(jsonObj.getString("timestamp"))
                    || "".equals(jsonObj.getString("version_mini")) || "".equals(jsonObj.getString("version_code"))) {
                logBean.setResultCode("10001");
                logBean.setMsg("必选参数为空，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                return resultMap;
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("platform", jsonObj.getString("platform"));
            String version_id = jsonObj.getString("version_id");
            String version_mini = jsonObj.getString("version_mini");
            treeMap.put("version_id", version_id);
            treeMap.put("version_mini", version_mini);
            treeMap.put("version_code", jsonObj.getString("version_code"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                logBean.setResultCode("10002");
                logBean.setMsg("参数校验不合格，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                return resultMap;
            }

           //判断逻辑
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("platform", jsonObj.getString("platform"));
            AppVersionUpGrade appVersionUpGrade = (AppVersionUpGrade)baseManager.getUniqueObjectByConditions(AppConfig.SQL_APP_VERSION_INFO, map);
            if(appVersionUpGrade!= null){
                if( jsonObj.getString("version_code").equals(appVersionUpGrade.getVersion_code())){//0不升级
                    logBean.setResultCode("100010");
                    logBean.setMsg("已是最新版本，无需升级！");
                    baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                    resultMap.put("resultCode", "100010");
                    resultMap.put("resultMsg", "已是最新版本，无需升级！");
                    return resultMap;
                }else{
                    logBean.setResultCode("100012");
                    logBean.setMsg("检测到有新版本了，可以升级");
                    baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                    resultMap.put("resultCode", "100013");
                    resultMap.put("version_info", appVersionUpGrade);
                    resultMap.put("resultMsg", "检测到有新版本了，可以升级");
                    resultMap.put("apkUrl",appVersionUpGrade.getApk_url());
                }

//                  else{
//                        if(version_mini!=appVersionUpGrade.getSub_version_id()){//小版本不同，可以提示升级
//                            logBean.setResultCode("100013");
//                            logBean.setMsg("检测到有新版本了，可以升级");
//                            baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
//                            resultMap.put("resultCode", "100013");
//                            resultMap.put("version_info", appVersionUpGrade);
//                            resultMap.put("resultMsg", "检测到有新版本了，可以升级");
//                            return resultMap;
//                        }else {//最新版本
//                            logBean.setResultCode("100010");
//                            logBean.setMsg("已是最新版本，无需升级！");
//                            baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
//                            resultMap.put("resultCode", "100010");
//                            resultMap.put("resultMsg", "已是最新版本，无需升级！");
//                        }
//                    }

//                }else if("2".equals(appVersionUpGrade.getUpdateType())){//强制升级
//                    if(version_id==appVersionUpGrade.getVersion_id()){
//                        if(version_mini==appVersionUpGrade.getSub_version_id()){//已是最新版本
//                            logBean.setResultCode("100010");
//                            logBean.setMsg("已是最新版本，无需升级！");
//                            baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
//                            resultMap.put("resultCode", "100010");
//                            resultMap.put("resultMsg", "已是最新版本，无需升级！");
//                            return resultMap;
//                        }
//                    }else{//强制升级
//                        logBean.setResultCode("100014");
//                        logBean.setMsg("请升级到最新版本，以免影响您的使用");
//                        baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
//                        resultMap.put("resultCode", "100014");
//                        resultMap.put("version_info", appVersionUpGrade);
//                        resultMap.put("resultMsg", "请升级到最新版本，以免影响您的使用");
//                    }
//                }
            }else {
                logBean.setResultCode("100011");
                logBean.setMsg("版本校验出错了");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "100011");
                resultMap.put("resultMsg", "版本校验出错了");
                return resultMap;
            }



        } catch(Exception e){
            logBean.setResultCode("10004");
            logBean.setMsg("未知错误，请联系管理员");
            baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMap;
        }

        return resultMap;
    }

    public  static  void  main(String [] arg) throws Exception {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new TreeMap<>();

        /**investorArtWorkView.do测试加密参数**/
        map.put("platform", "android");
        map.put("version_id","V1");
        map.put("version_mini","1.1");
        map.put("version_code","V1_1.1");
        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        map.put("signmsg",signmsg);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.75:8080/app/upgrade.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  artWorkCreationList.do测试 **/
//        String json = "{\"pageNum\":\"1\",\"pageSize\":\"5\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  artWorkCreationView.do测试 **/
        String json = "{\"version_code\":\"V1_1.1\",\"platform\":\"android\",\"version_id\":\"V1\",\"version_mini\":\"1.1\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
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
