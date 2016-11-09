package com.efeiyi.ec.system.service.impl;

import com.efeiyi.ec.art.organization.model.SendCode;
import com.efeiyi.ec.art.organization.model.SmsProvider;
import com.efeiyi.ec.art.organization.model.YunPianSmsProvider;
import com.efeiyi.ec.system.service.SmsCheckManager;

import java.util.HashMap;
import java.util.Map;

public class SmsCheckManagerImpl implements SmsCheckManager {

    @Override
    public String createCheckCode() {
        return String.valueOf(Math.random()).substring(2, 8);
    }

    @Override
    public String send(String phone, String content, String tpl_id, Integer company) {
        SmsProvider smsProvider = new YunPianSmsProvider();
        SendCode sendCode = smsProvider.post(phone, content, tpl_id);
        return sendCode.getMsg();
    }

    @Override
    public String send(String mobile, String tpl_id, String tpl_value) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("apikey", YunPianSmsProvider.apikey);
        paramsMap.put("mobile", mobile);
        paramsMap.put("tpl_id", tpl_id);
        paramsMap.put("tpl_value", tpl_value);

        SmsProvider smsProvider = new YunPianSmsProvider();
        return smsProvider.post(YunPianSmsProvider.URI_TPL_SEND_SMS, paramsMap);
    }

}