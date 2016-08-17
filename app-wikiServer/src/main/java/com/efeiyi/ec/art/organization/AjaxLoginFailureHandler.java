package com.efeiyi.ec.art.organization;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming800.core.base.service.BaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 13-1-9
 * Time: 下午3:00
 * 处理spring security 登录失败
 */
@Component
public class AjaxLoginFailureHandler implements AuthenticationFailureHandler {
    @Autowired
    BaseManager baseManager;

    @Autowired
    ResultMapHandler resultMapHandler;
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {

        ObjectMapper objectMapper = new ObjectMapper();
        response.setHeader("Content-Type", "application/javascript;application/json;charset=UTF-8");
        try {
            //日志
            LogBean logBean = new LogBean();
            logBean.setApiName("login");
            logBean.setCreateDate(new Date());


            Map<String, Object> resultMap = new HashMap<String, Object>();

            resultMap = resultMapHandler.handlerResult("10003","用户名或密码错误",logBean);
            JSONObject jsonData = new JSONObject(resultMap);
            //失败为-1
            if (null != request.getParameter("callback") &&!request.getParameter("callback").equals("")){
                String callback = request.getParameter("callback");
                response.getWriter().write(callback+"("+ jsonData +")");
            }else {
                JsonGenerator jsonGenerator = objectMapper.getJsonFactory().createJsonGenerator(response.getOutputStream(),
                        JsonEncoding.UTF8);
                objectMapper.writeValue(jsonGenerator, jsonData);
            }
        } catch (JsonProcessingException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
        }
    }
}
