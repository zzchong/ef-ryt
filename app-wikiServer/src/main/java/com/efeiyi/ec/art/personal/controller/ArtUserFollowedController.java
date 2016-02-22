package com.efeiyi.ec.art.personal.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.model.ArtUserFollowed;
import com.efeiyi.ec.art.organization.model.MyUser;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.does.model.PageInfo;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.taglib.PageEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Administrator on 2016/2/18.
 */
@Controller
public class ArtUserFollowedController extends BaseController {

    private static Logger logger = Logger.getLogger(ArtUserFollowedController.class);

    @ResponseBody
    @RequestMapping(value = "/app/userFollowed.do", method = RequestMethod.POST)
    public Map getUserProfile(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId")) ||
                    "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("type")) ||
                    "".equals(jsonObj.getString("index")) || "".equals(jsonObj.getString("size"))) {
                logBean.setResultCode("10001");
                logBean.setMsg("必选参数为空，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                return resultMap;
            }
            String signmsg = jsonObj.getString("signmsg");
            String userId = jsonObj.getString("userId");
            String type = jsonObj.getString("type");
            String index = jsonObj.getString("pageIndex");
            String size = jsonObj.getString("pageSize");
            treeMap.put("userId", userId);
            treeMap.put("type", type);
            treeMap.put("pageIndex", index);
            treeMap.put("pageSize", size);
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                logBean.setResultCode("10002");
                logBean.setMsg("参数校验不合格，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                return resultMap;
            }
            XQuery xQuery = new XQuery("plistArtUserFollowed_default", request);
            xQuery.put("follower_id", userId);
            xQuery.put("type", type);
            PageEntity entity = new PageEntity();
            entity.setSize(Integer.parseInt(size));
            entity.setIndex(Integer.parseInt(index));
            xQuery.setPageEntity(entity);
            PageInfo pageInfo = baseManager.listPageInfo(xQuery);
            List<ArtUserFollowed> followedList = pageInfo.getList();
            logBean.setResultCode("0");
            logBean.setMsg("请求成功");
            baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "成功");
            resultMap.put("pageInfoList", followedList);
        } catch (Exception e) {
            logBean.setResultCode("10004");
            logBean.setMsg("未知错误，请联系管理员");
            baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMap;
        }

        return resultMap;
    }

    public static void main(String[] args) throws Exception {
        boolean cd = false;
        long timestamp = System.currentTimeMillis();
        String num = "15538398530";
        String userId = "iiv2np9v2x7eg6yh";
//        String type = "12";
        String type = "1";
        String content = "13565585985";
        String size = "11";
        String index = "1";
        String signmsg;
        TreeMap map = new TreeMap();
//        if (cd) {
//            map.put("username", num);
//        } else {
//            map.put("userId", userId);
//            map.put("type", type);
//            map.put("content", content);
//        }
        map.put("userId",userId);
        map.put("type",type);
        map.put("pageIndex",index);
        map.put("pageSize",size);
        map.put("timestamp", timestamp);
        signmsg = DigitalSignatureUtil.encrypt(map);
        System.out.println(signmsg);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.68:8080/app/userFollowed.do";
        HttpPost httppost = new HttpPost(url);

        String userDatum = "{\"username\":\"15538398530\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
        String editProfile = "{\"userId\":\"iiv2np9v2x7eg6yh\",\"type\":\"12\",\"content\":\"13565585985\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
        String userFollowed = "{\"userId\":\"iiv2np9v2x7eg6yh\",\"type\":\"1\",\"pageSize\":\""+size+"\",\"pageIndex\":\""+index+"\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";

        String json = userFollowed;
        JSONObject jsonObj = (JSONObject) JSONObject.parse(json);
        String jsonString = jsonObj.toJSONString();

        StringEntity stringEntity = new StringEntity(jsonString, "utf-8");

        httppost.setEntity(stringEntity);
        System.out.println("url:  " + url);
        byte[] b = new byte[(int) stringEntity.getContentLength()];
        stringEntity.getContent().read(b);
        System.out.println("报文:" + new String(b));
        HttpResponse response = httpClient.execute(httppost);
        HttpEntity entity = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                entity.getContent(), "UTF-8"));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        System.out.println(stringBuilder.toString());

    }

}
