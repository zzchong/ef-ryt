package com.efeiyi.ec.art.base.util;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.aop.ThrowsAdvice;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Administrator on 2016/4/14.
 */
@org.aspectj.lang.annotation.Aspect
@Component
public class Aspect {

    @Pointcut("execution(* com.efeiyi.*.controller..*Controller(..))")
    public void aspect(){}
    @Around(value = "aspect()")
    public Map around(ProceedingJoinPoint point) {
        System.out.println("before!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Map<String, Object> resultMap = new HashMap<String, Object>();
//        LogBean logBean = new LogBean();
//        TreeMap treeMap = new TreeMap();
//        JSONObject jsonObj;
//        jsonObj = JsonAcceptUtil.receiveJson(request);
//        logBean.setCreateDate(new Date());
//        logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
//        if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId"))
//                || "".equals(jsonObj.getString("timestamp"))) {
//            return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
//        }
//        String signmsg = jsonObj.getString("signmsg");
//        String userId = jsonObj.getString("userId");
//        treeMap.put("userId", userId);
//        treeMap.put("timestamp", jsonObj.getString("timestamp"));
//        boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
//        if (!verify) {
//            return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
//        }
//        return resultMap;
        try {
            Object result = point.proceed(point.getArgs());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        System.out.println("after!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        return resultMap;
    }

}
