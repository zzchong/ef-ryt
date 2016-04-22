package com.efeiyi.ec.art.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Administrator on 2016/4/14.
 */
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    ResultMapHandler resultMapHandler;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            String signmsg = jsonObj.getString("signmsg");
            String userId = jsonObj.getString("userId");
//            String index = jsonObj.getString("pageIndex");
//            String size = jsonObj.getString("pageSize");
            String timestamp = jsonObj.getString("timestamp");
            if ("".equals(signmsg) || "".equals(userId) || "".equals(timestamp)
//                    || "".equals(index) || "".equals(size)
                    ) {
                request.setAttribute("resultMap", resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean));
            }
            treeMap.put("userId", userId);
            treeMap.put("timestamp", timestamp);
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                request.setAttribute("resultMap", resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean));
                throw new Exception();
            }
        } catch (Exception e) {
            Map resultMap = (Map)request.getAttribute("resultMap");
            if(resultMap == null) {
                request.setAttribute("resultMap", resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
            }
            throw e;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
       response.getWriter().write("111111111111111111111111111111111111");
        super.afterCompletion(request, response, handler, ex);
        if(ex != null && request.getAttribute("resultMap") == null){
            LogBean logBean = new LogBean();
            request.setAttribute("resultMap", resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
        }
    }

}
