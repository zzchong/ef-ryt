package com.efeiyi.ec.art.base.util;

import com.alibaba.fastjson.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/1/13.
 */
public class JsonAcceptUtil {

    public static JSONObject receiveJson(HttpServletRequest request) throws  Exception{
            request.setCharacterEncoding("utf-8");
            InputStream inputStream = request.getInputStream();
            byte[] bytes = new byte[request.getContentLength()];
            inputStream.read(bytes);
            String param = new String(bytes,"UTF-8");
            JSONObject jsonObj = (JSONObject) JSONObject.parse(param);
            return jsonObj;
    }

    public static JSONObject receiveJson3(HttpServletRequest request) throws  Exception{
        Enumeration paramNames = request.getParameterNames();
        JSONObject jsonObject = new JSONObject();
        while (paramNames.hasMoreElements()){
           String paramName = paramNames.nextElement().toString();
            String [] paramValues = request.getParameterValues(paramName);
            if(paramValues.length==1){
                String paramValue = paramValues[0];
                jsonObject.put(paramName,paramValue);
            }

        }
        return jsonObject;
    }

    public static JSONObject receiveJson2(HttpServletRequest request) throws  Exception{
        Map map = new HashMap();
        request.setCharacterEncoding("utf-8");
        for(Map.Entry<String,String[]> entry : request.getParameterMap().entrySet()){
            map.put(entry.getKey(),entry.getValue()[0]);
        }
        String jsonString = JSONObject.toJSONString(map);
        return JSONObject.parseObject(jsonString);
    }
}
