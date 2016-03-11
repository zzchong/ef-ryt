package com.efeiyi.ec.art.organization.controller;

import cn.jmessage.api.JMessageClient;
import cn.jmessage.api.common.model.RegisterInfo;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.PushUserBinding;
import com.efeiyi.ec.art.organization.model.BigUser;
import com.efeiyi.ec.art.organization.model.Consumer;
import com.efeiyi.ec.art.organization.model.MyUser;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.service.SmsCheckManager;
import com.efeiyi.ec.art.organization.service.imp.SmsCheckManagerImpl;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.PConst;
import com.ming800.core.p.service.AliOssUploadManager;
import com.ming800.core.util.VerificationCodeGenerator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2015/12/22.
 *
 */
@Controller
public class SigninController extends BaseController {
    private static Logger logger = Logger.getLogger(SigninController.class);
    private static final String appKey ="d1573e16403c2482826bbd35";
    private static final String masterSecret = "0b6ca44da0dfe0b7ea6331f1";
    private SmsCheckManager smsCheckManager = new SmsCheckManagerImpl();
    @Autowired
    BaseManager baseManager;
    @Autowired
    ResultMapHandler resultMapHandler;
    @Autowired
    AliOssUploadManager aliOssUploadManager;
    @RequestMapping(value = "/app/login.do", method = RequestMethod.POST)
    @ResponseBody
    public Map login(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("login");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("username")) || "".equals(jsonObj.getString("password")) || "".equals(jsonObj.getString("timestamp"))) {
                logBean.setResultCode("10001");
                logBean.setMsg("必选参数为空，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                return resultMap;
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("username", jsonObj.getString("username"));
            treeMap.put("password", jsonObj.getString("password"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                logBean.setResultCode("10002");
                logBean.setMsg("参数校验不合格，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                return resultMap;
            }

            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username", jsonObj.getString("username"));
            MyUser user;
            try {
                user = (MyUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_MYUSER_GET, map);
                if (user.getPassword().equals(jsonObj.getString("password"))) {
                    logBean.setResultCode("0");
                    logBean.setMsg("成功");
                    baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                    resultMap.put("resultCode", "0");
                    resultMap.put("resultMsg", "成功");
                    resultMap.put("userInfo",user);
                }else{
                    logBean.setResultCode("10003");
                    logBean.setMsg("用户名或密码错误");
                    baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                    resultMap.put("resultCode", "10003");
                    resultMap.put("resultMsg", "用户名或密码错误");
                }
            } catch (Exception e) {
                e.printStackTrace();
                logBean.setResultCode("10003");
                logBean.setMsg("用户名或密码错误");
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                resultMap.put("resultCode", "10003");
                resultMap.put("resultMsg", "用户名或密码错误");
            }
        } catch (Exception e) {
            logBean.setResultCode("10004");
            logBean.setMsg("未知错误，请联系管理员");
            baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMap;
        }
        return resultMap;
    }


    @RequestMapping(value = "/app/register.do", method = RequestMethod.POST)
    @ResponseBody
    public Map register(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("register");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("username")) ||
                "".equals(jsonObj.getString("password")) || "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("truename2"))) {
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                logBean.setResultCode("10001");
                logBean.setMsg("必选参数为空，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                return resultMap;
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("username", jsonObj.getString("username"));
            treeMap.put("password", jsonObj.getString("password"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            treeMap.put("truename2", jsonObj.getString("truename2"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                logBean.setResultCode("10002");
                logBean.setMsg("参数校验不合格，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                return resultMap;
            }
            boolean flagg= isMobile(jsonObj.getString("username"));
            if(flagg==false){
                resultMap.put("resultCode", "10006");
                resultMap.put("resultMsg", "手机号码校验不合格");
                logBean.setResultCode("10006");
                logBean.setMsg("手机号码校验不合格");
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                return resultMap;
            }
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username", jsonObj.getString("username"));
            MyUser user;
            try {
                user = (MyUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_MYUSER_GET, map);
                if (user!=null && user.getId()!=null) {
                    resultMap.put("resultCode", "-1");
                    resultMap.put("resultMsg", "用户名已经存在");
                    logBean.setResultCode("-1");
                    logBean.setMsg("用户名已经存在");
                    baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                    return resultMap;
                }
            }catch(Exception e){
                resultMap.put("resultCode", "10005");
                resultMap.put("resultMsg", "查询数据出现异常");
                logBean.setResultCode("10005");
                logBean.setMsg("查询数据出现异常");
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                return resultMap;
            }
           //***************************************保存用户信息
            request.getSession().removeAttribute(jsonObj.getString("username"));//session移除验证码
            Consumer myUser = new Consumer();
            myUser.setUsername(jsonObj.getString("username"));
            myUser.setPassword(jsonObj.getString("password"));
            myUser.setName2(jsonObj.getString("truename2"));
            myUser.setAccountExpired(false);
            myUser.setAccountLocked(false);
            myUser.setCredentialsExpired(false);
            myUser.setEnabled(true);
            myUser.setStatus("1");
            myUser.setCreateDatetime(new Date());
            baseManager.saveOrUpdate(Consumer.class.getName(),myUser);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "注册成功！");
            logBean.setResultCode("0");
            logBean.setMsg( "注册成功！");
            resultMap.put("userInfo",myUser);//响应的用户信息
            baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
        } catch (Exception e) {
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            logBean.setResultCode("10004");
            logBean.setMsg("未知错误，请联系管理员");
            baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
            return resultMap;
        }
        return resultMap;
    }


    @RequestMapping(value = "/app/checkUserName.do", method = RequestMethod.POST)
    @ResponseBody
    public Map checkUserName(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("checkUserName");
        Map<String, String> resultMap = new HashMap<String, String>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("username")) ||
                "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("truename2"))) {
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                logBean.setResultCode("10001");
                logBean.setMsg("必选参数为空，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                return resultMap;
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("username", jsonObj.getString("username"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                logBean.setResultCode("10002");
                logBean.setMsg("参数校验不合格，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                return resultMap;
            }

            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username", jsonObj.getString("username"));
            MyUser user;
            try {
                user = (MyUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_MYUSER_GET, map);
                if (user!=null && user.getId()!=null) {
                    resultMap.put("resultCode", "-1");
                    resultMap.put("resultMsg", "用户名已经存在");
                    logBean.setResultCode("-1");
                    logBean.setMsg("用户名已经存在");
                }else {
                    resultMap.put("resultCode", "0");
                    resultMap.put("resultMsg", "成功");
                    logBean.setResultCode("0");
                    logBean.setMsg("成功");

                }
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
            } catch (Exception e) {
                resultMap.put("resultCode", "10005");
                resultMap.put("resultMsg", "查询数据出现异常");
                logBean.setResultCode("10005");
                logBean.setMsg("查询数据出现异常");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                return resultMap;
                //e.printStackTrace();
            }


        } catch(Exception e){
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            logBean.setResultCode("10004");
            logBean.setMsg("未知错误，请联系管理员");
            baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
            return resultMap;
        }
        return resultMap;
    }
    /**
     * 手机号验证
     *
     * @param  str
     * @return 验证通过返回true
     */
    public static boolean isMobile(String str) {
        Pattern p ;
        Matcher m ;
        boolean b;
        p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }



    @RequestMapping(value = "/app/userBinding.do", method = RequestMethod.POST)
    @ResponseBody
    public Map JpushBinding(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("userBinding");
        Map<String, String> resultMap = new HashMap<String, String>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("username")) ||
                    "".equals(jsonObj.getString("cid")) || "".equals(jsonObj.getString("timestamp"))
                    || "".equals(jsonObj.getString("password")) ) {
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                logBean.setResultCode("10001");
                logBean.setMsg("必选参数为空，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                return resultMap;
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("username", jsonObj.getString("username"));
            //treeMap.put("nickname", jsonObj.getString("nickname"));
            treeMap.put("password", jsonObj.getString("password"));
            treeMap.put("cid", jsonObj.getString("cid"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                logBean.setResultCode("10002");
                logBean.setMsg("参数校验不合格，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                return resultMap;
            }

            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username", jsonObj.getString("username"));
            User user;
            try {
                user = (User) baseManager.getUniqueObjectByConditions(AppConfig.SQL_USER_GET, map);
                if (user==null || user.getId()==null) {
                    resultMap.put("resultCode", "10007");
                    resultMap.put("resultMsg", "用户名不存在");
                    logBean.setResultCode("10007");
                    logBean.setMsg("用户名不存在");
                    baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                    return resultMap;
                }
                PushUserBinding pushUserBinding = new PushUserBinding();
                pushUserBinding.setCid(jsonObj.getString("cid"));
                pushUserBinding.setUser(user);
                String res = RegisterUsers(jsonObj.getString("username"),jsonObj.getString("password"));//若果绑定失败，人工处理
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
                logBean.setResultCode("0");
                logBean.setMsg("成功");
                logBean.setExtend1(res);
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                baseManager.saveOrUpdate(PushUserBinding.class.getName(),pushUserBinding);
            } catch (Exception e) {
                resultMap.put("resultCode", "10005");
                resultMap.put("resultMsg", "查询数据出现异常");
                logBean.setResultCode("10005");
                logBean.setMsg("查询数据出现异常");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                return resultMap;
                //e.printStackTrace();
            }


        } catch(Exception e){
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            logBean.setResultCode("10004");
            logBean.setMsg("未知错误，请联系管理员");
            baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
            return resultMap;
        }
        return resultMap;
    }


    //注册验证码
    @RequestMapping(value = "/app/sendCode.do", method = RequestMethod.POST)
    @ResponseBody
    public Map registerSendCode(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("sendCode");
        Map<String, String> resultMap = new HashMap<String, String>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("username")) ||
                 "".equals(jsonObj.getString("timestamp"))) {
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                logBean.setResultCode("10001");
                logBean.setMsg("必选参数为空，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                return resultMap;
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("username", jsonObj.getString("username"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                logBean.setResultCode("10002");
                logBean.setMsg("参数校验不合格，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
                return resultMap;
            }
            String verificationCode = VerificationCodeGenerator.createVerificationCode();

            String message = this.smsCheckManager.send(jsonObj.getString("username"), verificationCode, "1104699", PConst.TIANYI);
            request.getSession().setAttribute(jsonObj.getString("username"), verificationCode);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "成功");
            resultMap.put("message",message);//响应的用户信息
            logBean.setResultCode("0");
            logBean.setMsg("成功");
            baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
        } catch(Exception e){
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            logBean.setResultCode("10004");
            logBean.setMsg("未知错误，请联系管理员");
            baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
            return resultMap;
        }
        return resultMap;
    }

   //校验验证码
   @RequestMapping(value = "/app/verifyCode.do", method = RequestMethod.POST)
   @ResponseBody
   public Map verifyCode(HttpServletRequest request) {
       LogBean logBean = new LogBean();
       logBean.setApiName("verifyCode");
       Map<String, String> resultMap = new HashMap<String, String>();
       TreeMap treeMap = new TreeMap();
       try {
           JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
           logBean.setCreateDate(new Date());
           logBean.setRequestMessage(jsonObj.toString());
           if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("username")) ||
                   "".equals(jsonObj.getString("timestamp"))|| "".equals(jsonObj.getString("code"))) {
               resultMap.put("resultCode", "10001");
               resultMap.put("resultMsg", "必选参数为空，请仔细检查");
               logBean.setResultCode("10001");
               logBean.setMsg("必选参数为空，请仔细检查");
               baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
               return resultMap;
           }

           String signmsg = jsonObj.getString("signmsg");
           treeMap.put("username", jsonObj.getString("username"));
           treeMap.put("code", jsonObj.getString("code"));
           treeMap.put("timestamp", jsonObj.getString("timestamp"));
           boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
           if (verify != true) {
               resultMap.put("resultCode", "10002");
               resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
               logBean.setResultCode("10002");
               logBean.setMsg("参数校验不合格，请仔细检查");
               baseManager.saveOrUpdate(LogBean.class.getName(), logBean);
               return resultMap;
           }

           if(request.getSession().getAttribute(jsonObj.getString("username")).toString()==null){
               resultMap.put("resultCode", "100011");
               resultMap.put("resultMsg", "验证码失效，请重新发送");
               logBean.setResultCode("100011");
               logBean.setMsg("验证码失效，请重新发送");
               baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
               return resultMap;
           }

           String code= request.getSession().getAttribute(jsonObj.getString("username")).toString();
          if (code!=null && code.equals(jsonObj.getString("code"))){
              resultMap.put("resultCode", "0");
              resultMap.put("resultMsg", "成功");
              logBean.setResultCode("0");
              logBean.setMsg("成功");
              baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
          }else{
              resultMap.put("resultCode", "100010");
              resultMap.put("resultMsg", "验证码验证失败");
              logBean.setResultCode("100010");
              logBean.setMsg("验证码验证失败");
          }


       } catch(Exception e){
           resultMap.put("resultCode", "10004");
           resultMap.put("resultMsg", "未知错误，请联系管理员");
           logBean.setResultCode("10004");
           logBean.setMsg("未知错误，请联系管理员");
           baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
           return resultMap;
       }
       return resultMap;
   }


    @RequestMapping(value = "/app/completeUserInfo.do", method = RequestMethod.POST)
    @ResponseBody
    public  Map paramBind(HttpServletRequest request){//,@RequestParam MultiValueMap<String, Object> params, @RequestParam("headPortrait") MultipartFile headPortrait

       /* Map<String, List<Object>> paramsMap = new HashMap<>();
        paramsMap = params;//参数列表*/

        LogBean logBean = new LogBean();
        logBean.setApiName("completeUserInfo");
        Map<String, String> resultMap = new HashMap<String, String>();
        TreeMap treeMap = new TreeMap();
        try {

            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(request.getParameter("username").toString()+" "+request.getParameter("nickname").toString()
            +" "+request.getParameter("sex").toString()+" "+request.getParameter("timestamp").toString());
            if ("".equals(request.getParameter("signmsg")) || "".equals(request.getParameter("username")) ||
                    "".equals(request.getParameter("timestamp")) || "".equals(request.getParameter("nickname "))
                    || "".equals(request.getParameter("sex "))) {

                resultMap = resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
                return resultMap;
            }

            String signmsg = request.getParameter("signmsg").toString();
            treeMap.put("username", request.getParameter("username"));
            treeMap.put("nickname", request.getParameter("nickname"));
            treeMap.put("sex", request.getParameter("sex"));
            treeMap.put("timestamp", request.getParameter("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                resultMap = resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
                return resultMap;
            }

            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username",request.getParameter("username").toString());
            BigUser user;
            try {
                user = (BigUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_BIGUSER_GET, map);
                if (user!=null && user.getId()!=null) {
                    String url = "app/" + request.getParameter("username").toString() + ".jpg";
                    String pictureUrl = "http://http://pro.efeiyi.com//"+url+"@!pc-classify-right";
                    MultipartFile headPortrait = ((MultipartHttpServletRequest) request).getFile("headPortrait");
                    //将用户头像上传至阿里云
                    aliOssUploadManager.uploadFile(headPortrait,"ec-efeiyi",pictureUrl);
                    user.setName2(request.getParameter("username").toString());
                    user.setSex(Integer.parseInt(request.getParameter("sex ").toString()));
                    user.setPictureUrl(pictureUrl);
                    baseManager.saveOrUpdate(BigUser.class.getName(),user);
                }else {
                    return  resultMapHandler.handlerResult("10007","未知错误，请联系管理员",logBean);
                }

            } catch (Exception e) {
                resultMap = resultMapHandler.handlerResult("10005","户名不存在",logBean);
                return resultMap;
            }

        } catch(Exception e){
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
        return resultMap;


    }




    @RequestMapping(value = "/app/test.do", method = RequestMethod.POST)
    @ResponseBody
    public  Map test(HttpServletRequest request){//,@RequestParam MultiValueMap<String, Object> params, @RequestParam("headPortrait") MultipartFile photo) {
        Map paramsMap = new HashMap<>();
        request.getParameter("sex");
        MultipartFile formFile1 = ((MultipartHttpServletRequest) request).getFile("headPortrait");
       /*
        paramsMap = params;*/
        //System.out.println(paramsMap.get("name").get(0));
        return paramsMap;
    }
    @RequestMapping(value = "/app/test2.do", method = RequestMethod.POST)
    @ResponseBody
    public Map completeUserInfo(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("completeUserInfo");
        Map<String, String> resultMap = new HashMap<String, String>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("username")) ||
                    "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("nickname "))
                    || "".equals(jsonObj.getString("sex "))) {

                resultMap = resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
                return resultMap;
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("username", jsonObj.getString("username"));
            treeMap.put("nickname", jsonObj.getString("nickname"));
            treeMap.put("sex", jsonObj.getString("sex"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                resultMap = resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
                return resultMap;
            }

            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username", jsonObj.getString("username"));
            BigUser user;
            try {
                user = (BigUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_MYUSER_GET, map);
                if (user!=null && user.getId()!=null) {
                    //将用户头像上传至阿里云
                    //aliOssUploadManager.uploadFile();
                    user.setName2(jsonObj.getString("username"));
                    user.setSex(Integer.parseInt(jsonObj.getString("sex ")));
                    //user.setPictureUrl();
                    baseManager.saveOrUpdate(BigUser.class.getName(),user);
                }else {
                    return  resultMapHandler.handlerResult("10007","未知错误，请联系管理员",logBean);
                }

            } catch (Exception e) {
                resultMap = resultMapHandler.handlerResult("10005","户名不存在",logBean);
                return resultMap;
            }

        } catch(Exception e){
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
        return resultMap;
    }















    public static String RegisterUsers(String username,String password) {
        JMessageClient client = new JMessageClient(appKey, masterSecret);

        try {

            List<RegisterInfo> users = new ArrayList<RegisterInfo>();

            RegisterInfo user = RegisterInfo.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .build();


            users.add(user);

            RegisterInfo[] regUsers = new RegisterInfo[users.size()];

            String res = client.registerUsers(users.toArray(regUsers));
            logger.info(res);
            return  res;
        } catch (APIConnectionException e) {
            logger.error("Connection error. Should retry later. ", e);
            return null;
        } catch (APIRequestException e) {
            logger.error("Error response from JPush server. Should review and fix it. ", e);
            logger.info("HTTP Status: " + e.getStatus());
            logger.info("Error Message: " + e.getMessage());
            return null;
        }catch(Exception e){
            logger.info("Error Message: " + e.getMessage());
            return null;
        }
    }


}