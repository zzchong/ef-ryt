package com.efeiyi.ec.art.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/14.
 */
public class RestfulAuthenticationEntryPointInterceptor implements AuthenticationEntryPoint {

    private static final Log logger = LogFactory.getLog(RestfulAuthenticationEntryPointInterceptor.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;

        String url = request.getRequestURI();
        if(logger.isDebugEnabled()){
            logger.debug("url:"+url);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        JsonGenerator jsonGenerator = objectMapper.getJsonFactory().createJsonGenerator(response.getOutputStream(),
                JsonEncoding.UTF8);
        try {

            Map map = new HashMap<>();
            map.put("resultCode","2");
            map.put("resultMsg","null");
            JSONObject jsonData = new JSONObject(map);
            objectMapper.writeValue(jsonGenerator,jsonData);
            System.out.println(jsonData);
        } catch (JsonProcessingException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
        }
//        PrintWriter writer;
//        String returnStr = "{exception:{name:'" + authException.getClass()
//                + "',message:'" + authException.getMessage() + "'}}";
//        writer = response.getWriter();
//        writer.write(returnStr);
//        writer.flush();
//        writer.close();
    }
}
