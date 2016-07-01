package com.efeiyi.ec.art.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Administrator on 2016/6/30.
 */
public class RestfulUsernamePasswordAuthenticationInterceptor extends AbstractAuthenticationProcessingFilter {

    private String username = "";

    private String password = "rongyitou";

    private boolean postOnly = true;


    protected RestfulUsernamePasswordAuthenticationInterceptor(){
        super(new AntPathRequestMatcher("/app/login.do","POST"));
    }


    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        if(postOnly && !request.getMethod().equals("POST")){
            throw new AuthenticationServiceException("Authentication method not supported : "+request.getMethod());
        }

        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            String username = jsonObj.getString("username");
            String password = jsonObj.getString("password");

            if(StringUtils.isEmpty(username))
                username = "";
            if(StringUtils.isEmpty(password))
                password="rongyitou";

            username = username.trim();
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username,password);
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

         return  null;


    }
}
