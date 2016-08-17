package com.efeiyi.ec.art.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.efeiyi.ec.art.organization.util.CookieUtil;
import com.ming800.core.util.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Administrator on 2015/7/20.
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    ResultMapHandler resultMapHandler;

    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response, Object o, ModelAndView mav)
            throws Exception {

    }


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{

        LogBean logBean = new LogBean();
        logBean.setCreateDate(new Date());
        PrintWriter out = null;
        HashMap<String,Object> resultMap = new HashMap<>();
        Cookie cookie = CookieUtil.getCookieByName(request,"JSESSIONID");
        System.out.println(cookie);
        if(cookie!=null && (request.getSession(false)==null || AuthorizationUtil.getUser()==null)){
            resultMap = (HashMap<String, Object>) resultMapHandler.handlerResult("000000", "用户已过期", logBean);
//            resultMap.put("resultMap",resultMapHandler.handlerResult("000000", "用户未登陆", logBean));
            request.setAttribute("resultMap", resultMap);
            out = response.getWriter();
            if (null != request.getParameter("callback") && !request.getParameter("callback").equals("")){
                response.setHeader("Content-Type", "application/javascript;charset=UTF-8");
                out.write(request.getParameter("callback") + "(" + new JSONObject(resultMap) + ")");
            }else {
                out.print(JSONObject.toJSONString(resultMap));
            }
            return false;
        }

        return true;
    }


}
