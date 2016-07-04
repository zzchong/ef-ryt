package com.efeiyi.ec.art.organization.service;


import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: 12-10-15
 * Time: 下午5:01
 * To change this template use File | Settings | File Templates.
 */
public interface UserManager {

     Map loginSuccess(String userId);

    Map<String,String> usernameAuthentication(JSONObject jsonObj);
}
