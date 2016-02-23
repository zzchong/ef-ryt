package com.efeiyi.ec.art.personal.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.model.Account;
import com.efeiyi.ec.art.organization.model.MyUser;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.does.model.XQuery;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/2/17.
 */
@Controller
public class ProfileController extends BaseController {
    private static Logger logger = Logger.getLogger(ProfileController.class);

    /**
     * 获取用户资料
     * @param request 接口调用路径 /app/userDatum.do
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/userDatum.do", method = RequestMethod.POST)
    public Map getUserProfile(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("username")) || "".equals(jsonObj.getString("timestamp"))) {
                logBean.setResultCode("10001");
                logBean.setMsg("必选参数为空，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                return resultMap;
            }
            String signmsg = jsonObj.getString("signmsg");
            String username = jsonObj.getString("username");
            treeMap.put("username", username);
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
            XQuery xQuery = new XQuery("listUser_default", request);
            xQuery.put("username", username);
            xQuery.put("status", 0);
            List<MyUser> users = baseManager.listObject(xQuery);
            if (users != null && users.size() > 0) {
                MyUser user = users.get(0);
                logBean.setResultCode("0");
                logBean.setMsg("请求成功");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
                resultMap.put("userInfo", user);
            } else {
                logBean.setResultCode("10008");
                logBean.setMsg("查无数据,稍后再试");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "10008");
                resultMap.put("resultMsg", "查无数据,稍后再试");
            }
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

    /**
     * 编辑用户资料接口
     * 接口调用路径 /app/editProfile.do
     * @param request 参数type决定编辑哪项资料   type 11/昵称  type 12/手机号码  type 13/签名
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/editProfile.do", method = RequestMethod.POST)
    public Map editProfile(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        MyUser user;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId"))
                    || "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("type"))
                    || "".equals(jsonObj.getString("content"))) {
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
            String content = jsonObj.getString("content");
            treeMap.put("userId", userId);
            treeMap.put("type", type);
            treeMap.put("content", content);
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
            user = (MyUser) baseManager.getObject(MyUser.class.getName(), userId);
            if (user != null && user.getId() != null) {
                if ("11".equals(type)) {
                    user.setName2(content);
                    baseManager.saveOrUpdate(MyUser.class.getName(), user);
                    logBean.setResultCode("0");
                    logBean.setMsg("成功");
                    baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                    resultMap.put("resultCode", "0");
                    resultMap.put("resultMsg", "请求成功");
                    resultMap.put("userInfo", user);
                } else if ("12".equals(type)) {
                    /**
                     * 这里的手机号码验证仅包含{13/15/18}开头的号段
                     * 有需要的可以再增加其他的
                     */
                    String regExp = "^((13[0-9])|(15[0-9])|(18[0-9]))\\d{8}$";
                    Pattern pattern = Pattern.compile(regExp);
                    Matcher matcher = pattern.matcher(content);
                    boolean flag = matcher.find();
                    if (flag) {
                        user.setUsername(content);
                        baseManager.saveOrUpdate(MyUser.class.getName(), user);
                        logBean.setResultCode("0");
                        logBean.setMsg("成功");
                        baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                        resultMap.put("resultCode", "0");
                        resultMap.put("resultMsg", "请求成功");
                        resultMap.put("userInfo", user);
                    } else {
                        logBean.setResultCode("10006");
                        logBean.setMsg("手机号码校验不合格");
                        baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                        resultMap.put("resultCode", "10006");
                        resultMap.put("resultMsg", "手机号码校验不合格");
                    }
                } else if ("13".equals(type)) {
                    /**
                     * 此处为编辑签名的操作
                     * 修改model后添加业务逻辑
                     */
                }
            } else {
                logBean.setResultCode("10008");
                logBean.setMsg("查无数据,稍后再试");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "10008");
                resultMap.put("resultMsg", "查无数据,稍后再试");
            }
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


    /**
     * 保存用户提现密码
     * 接口调用路径 /app/savePassword.do
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/savePassword.do", method = RequestMethod.POST)
    public Map savePassword(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId"))
                    || "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("level_two_pwd"))) {
                logBean.setResultCode("10001");
                logBean.setMsg("必选参数为空，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                return resultMap;
            }
            String signmsg = jsonObj.getString("signmsg");
            String userId = jsonObj.getString("userId");
            String level_two_pwd = jsonObj.getString("level_two_pwd");
            treeMap.put("userId", userId);
            treeMap.put("level_two_pwd", level_two_pwd);
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
            LinkedHashMap<String, Object> queryMap = new LinkedHashMap<>();
            queryMap.put("userId", userId);
            Account account = (Account) baseManager.getUniqueObjectByConditions(AppConfig.SQL_ACCOUNT_BY_USER_ID, queryMap);
            if (account != null && account.getId() != null) {
                account.setPassword(level_two_pwd);
                baseManager.saveOrUpdate(Account.class.getName(), account);
                logBean.setResultCode("0");
                logBean.setMsg("成功");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "请求成功");
                resultMap.put("account", account);
            } else {
                logBean.setResultCode("10008");
                logBean.setMsg("查无数据,稍后再试");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                resultMap.put("resultCode", "10008");
                resultMap.put("resultMsg", "查无数据,稍后再试");
            }
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

}
