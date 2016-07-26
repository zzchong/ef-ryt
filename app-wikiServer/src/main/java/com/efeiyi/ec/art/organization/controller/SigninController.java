package com.efeiyi.ec.art.organization.controller;

import cn.jmessage.api.JMessageClient;
import cn.jmessage.api.common.model.RegisterInfo;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.util.*;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.model.BigUser;
import com.efeiyi.ec.art.organization.model.MyUser;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.service.SmsCheckManager;
import com.efeiyi.ec.art.organization.service.imp.SmsCheckManagerImpl;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.dao.hibernate.XdoDaoSupport;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.p.PConst;
import com.ming800.core.p.service.AliOssUploadManager;
import com.ming800.core.util.CookieTool;
import com.ming800.core.util.MD5Encode;
import com.ming800.core.util.StringUtil;
import com.ming800.core.util.VerificationCodeGenerator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
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
    private static final String appKey ="539b73fd73c82f1134120a57";
    private static final String masterSecret = "fea912e000db6a462048f9ef";
    private SmsCheckManager smsCheckManager = new SmsCheckManagerImpl();
    @Autowired
    BaseManager baseManager;
    @Autowired
    private XdoDaoSupport xdoDao;
    @Autowired
    ResultMapHandler resultMapHandler;
    @Autowired
    AliOssUploadManager aliOssUploadManager;
//    @RequestMapping(value = "/app/login.do", method = RequestMethod.POST)
//    @ResponseBody
//    public Map login(HttpServletRequest request) {
//        LogBean logBean = new LogBean();
//        logBean.setApiName("login");
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        TreeMap treeMap = new TreeMap();
//        try {
//            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
//            logBean.setCreateDate(new Date());
//            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
//            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("username")) || "".equals(jsonObj.getString("password")) || "".equals(jsonObj.getString("timestamp"))) {
//
//                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
//            }
//
//            String signmsg = jsonObj.getString("signmsg");
//            treeMap.put("username", jsonObj.getString("username"));
//            treeMap.put("password", jsonObj.getString("password"));
//            treeMap.put("timestamp", jsonObj.getString("timestamp"));
//            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
//            if (verify != true) {
//
//                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
//            }
//
//            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
//            map.put("username", jsonObj.getString("username"));
//            MyUser user;
//            try {
//                user = (MyUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_MYUSER_GET, map);
//                if(user==null){
//                    resultMap = resultMapHandler.handlerResult("10003","用户名或密码错误",logBean);
//                    return resultMap;
//                }
//                if (user.getPassword().equals(StringUtil.encodePassword(jsonObj.getString("password"), "SHA1"))) {
//                    resultMap = resultMapHandler.handlerResult("0","成功",logBean);
//                    resultMap.put("userInfo",(User)baseManager.getObject(User.class.getName(),user.getId()));
//                    //获取用户的关注数量  粉丝
//                    LinkedHashMap<String, Object> paramMap = new LinkedHashMap<String, Object>();
//                    paramMap.put("userId", user.getId());
//                    List<Long> counts = (List<Long>)baseManager.listObject(AppConfig.SQL_GET_USER_FOLLOWED, paramMap);
//                    Long count =0l;
//                    if(counts != null && !counts.isEmpty()){
//                        count= counts.get(0);
//                    }
//                    //Long count = (Long)baseManager.listObject(AppConfig.SQL_GET_USER_FOLLOWED, paramMap).get(0);
//                    List<Long> count1s = (List<Long>)baseManager.listObject(AppConfig.SQL_GET_USER_FOLLOW, paramMap);
//                    //Long count1 = (Long)baseManager.listObject(AppConfig.SQL_GET_USER_FOLLOW, paramMap).get(0);
//                    Long count1 =0l;
//                    if(count1s != null && !count1s.isEmpty()){
//                        count1= count1s.get(0);
//                    }
//                    //获取签名 SQL_GET_USER_SIGNER
//                    List<UserBrief> userBriefs = (List<UserBrief>)baseManager.listObject(AppConfig.SQL_GET_USER_SIGNER, paramMap);
//                    UserBrief userBrief = new UserBrief();
//                    if (userBriefs!=null && !userBriefs.isEmpty()){
//                        userBrief = userBriefs.get(0);
//                    }
//                    //UserBrief userBrief = (UserBrief)baseManager.listObject(AppConfig.SQL_GET_USER_SIGNER, paramMap).get(0);
//                    resultMap.put("count",count);
//                    resultMap.put("count1",count1);
//                    resultMap.put("userBrief",userBrief.getSigner());
//                    User user1 = (User)baseManager.getObject(User.class.getName(),user.getId());
//                    BigDecimal investsMoney = new BigDecimal("0.00");
//                    BigDecimal roiMoney = new BigDecimal("0.00");
//                    BigDecimal rate = new BigDecimal("0.00");
//
//                    BigDecimal investsMoney2 = new BigDecimal("0.00");
//                    BigDecimal roiMoney2 = new BigDecimal("0.00");
//                    BigDecimal rate2 = new BigDecimal("0.00");
//                    if(user1.getMaster()!=null && user1.getMaster().getId()!=null){
//                       // 2 艺术家
//                       //项目总金额
//                        List<Artwork> artworks = (List<Artwork>) baseManager.listObject(AppConfig.SQL_GET_USER_ARTWORK, paramMap);
//                        for (Artwork artwork:artworks){
//                            investsMoney2 = investsMoney2.add(artwork.getInvestGoalMoney());
//                        }
//
//
//                       //项目总拍卖金额
//                        List<Artwork> artworks2 = (List<Artwork>) baseManager.listObject(AppConfig.SQL_GET_USER_ARTWORK_OVER, paramMap);
//                        for (Artwork artwork:artworks2){
//                            ArtworkBidding artworkBidding = (ArtworkBidding)xdoDao.getSession().createSQLQuery(AppConfig.GET_ART_WORK_WINNER).addEntity(ArtworkBidding.class).setString("artworkId", artwork.getId()).uniqueResult();
//                            roiMoney2 = roiMoney2.add(artworkBidding.getPrice());
//                        }
//                        //项目拍卖溢价率
//                       if(investsMoney2.doubleValue()!=0.00 && roiMoney2.doubleValue()!=0.00){
//                           rate2 = roiMoney2.divide(investsMoney2,2);
//                       }
//
//                        resultMap.put("investsMoney",investsMoney2);
//                        resultMap.put("roiMoney",roiMoney2);
//                        resultMap.put("rate",rate2);
//                        resultMap.put("flag","2");
//
//
//                   }else {
//                       // 1 普通用户
//                       //获取投资金额
//                       List<ArtworkInvest> artworkInvests = (List<ArtworkInvest>) baseManager.listObject(AppConfig.SQL_INVEST_ARTWORK_APP, paramMap);
//                       for (ArtworkInvest artworkInvest:artworkInvests){
//                           investsMoney =  investsMoney.add(artworkInvest.getPrice());
//                       }
//
//                       //获取投资收益金额 SQL_GET_USER_ROI
//                     List<ROIRecord>  roiRecords = (List<ROIRecord>) baseManager.listObject(AppConfig.SQL_GET_USER_ROI, paramMap);
//                       for (ROIRecord roiRecord : roiRecords){
//                           roiMoney = roiMoney.add(roiRecord.getCurrentBalance().subtract(roiRecord.getArtworkInvest().getPrice()));
//                       }
//
//                       //投资回报率
//                        if(investsMoney.doubleValue()!=0.00&&roiMoney.doubleValue()!=0.00){
//                            rate = roiMoney.divide(investsMoney,2);
//
//                        }
//                        resultMap.put("investsMoney",investsMoney);
//                        resultMap.put("roiMoney",roiMoney);
//                        resultMap.put("rate",rate);
//                        resultMap.put("flag","1");
//
//                   }
//
//
//
//                }else{
//
//                    resultMap = resultMapHandler.handlerResult("10003","用户名或密码错误",logBean);
//                }
//            } catch (Exception e) {
//
//                resultMap = resultMapHandler.handlerResult("10005","查询数据出现异常",logBean);
//            }
//        } catch (Exception e) {
//
//            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
//        }
//        return resultMap;
//    }


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
                "".equals(jsonObj.getString("password")) || "".equals(jsonObj.getString("timestamp")) ) {//|| "".equals(jsonObj.getString("truename2"))

                return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("username", jsonObj.getString("username"));
            treeMap.put("password", jsonObj.getString("password"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {

                return  resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            boolean flagg= isMobile(jsonObj.getString("username"));
            if(flagg==false){

                return  resultMapHandler.handlerResult("10006","手机号码校验不合格",logBean);
            }
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username", jsonObj.getString("username"));
            MyUser user;
            try {
                user = (MyUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_MYUSER_GET, map);
                if (user!=null && user.getId()!=null) {

                    return  resultMapHandler.handlerResult("-1","用户名已经存在",logBean);
                }
            }catch(Exception e){

                return  resultMapHandler.handlerResult("10005","查询数据出现异常",logBean);
            }
           //***************************************保存用户信息
            MyUser myUser = new MyUser();
            myUser.setUsername(jsonObj.getString("username"));
            myUser.setPassword(StringUtil.encodePassword(jsonObj.getString("password"),"SHA1"));
            //myUser.setName2(jsonObj.getString("truename2"));
            myUser.setAccountExpired(false);
            myUser.setAccountLocked(false);
            myUser.setCredentialsExpired(false);
            myUser.setEnabled(true);
            myUser.setStatus(1);
            myUser.setUtype(2);
            myUser.setCreateDatetime(new Date());
            baseManager.saveOrUpdate(MyUser.class.getName(),myUser);
            //给用户绑定一个账户
            Account account = new Account();
            account.setCurrentUsableBalance(new BigDecimal("0.00"));
            account.setCurrentBalance(new BigDecimal("0.00"));
            account.setCreateDatetime(new Date());
            account.setStatus("1");
            account.setUser((User)baseManager.getObject(User.class.getName(),myUser.getId()));
            baseManager.saveOrUpdate(Account.class.getName(),account);
            resultMap = resultMapHandler.handlerResult("0","注册成功！",logBean);
            resultMap.put("userInfo",myUser);//响应的用户信息
            return  resultMap;
        } catch (Exception e) {
             e.printStackTrace();
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
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
                return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
        }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("username", jsonObj.getString("username"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return  resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }

            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username", jsonObj.getString("username"));
            MyUser user;
            try {
                user = (MyUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_MYUSER_GET, map);
                if (user!=null && user.getId()!=null) {

                    return  resultMapHandler.handlerResult("-1","用户名已经存在",logBean);
                }else {

                    return  resultMapHandler.handlerResult("0","成功",logBean);
                }
            } catch (Exception e) {
                return  resultMapHandler.handlerResult("10005","查询数据出现异常",logBean);
            }
        } catch(Exception e){
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
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
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("username", jsonObj.getString("username"));
            //treeMap.put("nickname", jsonObj.getString("nickname"));
            treeMap.put("password", jsonObj.getString("password"));
            treeMap.put("cid", jsonObj.getString("cid"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username", jsonObj.getString("username"));
            User user;
            try {
                user = (User) baseManager.getUniqueObjectByConditions(AppConfig.SQL_USER_GET, map);
                if (user==null || user.getId()==null) {
                    return resultMapHandler.handlerResult("10007","用户名不存在",logBean);
                }
                LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
                param.put("userId", user.getId());
                PushUserBinding pushUserBinding = (PushUserBinding)baseManager.getUniqueObjectByConditions(AppConfig.SQL_USER_BINDING_GET, param);
                if(pushUserBinding == null || pushUserBinding.getId() == null){
                    pushUserBinding = new PushUserBinding();
                    pushUserBinding.setUser(user);
                }
                pushUserBinding.setCid(jsonObj.getString("cid"));
                String res = RegisterUsers(jsonObj.getString("username"),jsonObj.getString("password"));//若果绑定失败，人工处理
                logBean.setExtend1(res);
                baseManager.saveOrUpdate(PushUserBinding.class.getName(),pushUserBinding);
                resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            } catch (Exception e) {
                return resultMapHandler.handlerResult("10005","查询数据出现异常",logBean);
                //e.printStackTrace();
            }
        } catch(Exception e){
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
        return resultMap;
    }


    /**
     * 微信登录绑定消息
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/wxBinding.do", method = RequestMethod.POST)
    @ResponseBody
    public Map wxBinding(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("wxBinding");
        Map<String, String> resultMap = new HashMap<String, String>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg")) ||
                    "".equals(jsonObj.getString("cid")) || "".equals(jsonObj.getString("timestamp"))
                    ) {
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("cid", jsonObj.getString("cid"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            User user =AuthorizationUtil.getUser();
            String userId = AuthorizationUtil.getUserId();
            PushUserBinding pushUserBinding;
            try {
                XQuery xQuery = new XQuery("listPushUserBinding_default",request);
                xQuery.put("user_id",userId);
                List<PushUserBinding> pushUserBindingList = (List<PushUserBinding>)baseManager.listObject(xQuery);
                if(pushUserBindingList!=null && pushUserBindingList.size()!=0){
                    if(!pushUserBindingList.get(0).getCid().equals(jsonObj.getString("cid"))){
                       pushUserBinding = pushUserBindingList.get(0);
                        pushUserBinding.setCid(jsonObj.getString("cid"));
                        baseManager.saveOrUpdate(PushUserBinding.class.getName(),pushUserBinding);
                    }

                   return   resultMapHandler.handlerResult("0","成功",logBean);
                }
//                user = (User) baseManager.getObject(User.class.getName(), jsonObj.getString("id"));
                if (user==null || user.getId()==null) {
                    return resultMapHandler.handlerResult("10007","用户名不存在",logBean);
                }
                pushUserBinding = new PushUserBinding();
                pushUserBinding.setCid(jsonObj.getString("cid"));
                pushUserBinding.setUser(user);
                baseManager.saveOrUpdate(PushUserBinding.class.getName(),pushUserBinding);
                return   resultMapHandler.handlerResult("0","成功",logBean);

            } catch (Exception e) {
                return resultMapHandler.handlerResult("10005","查询数据出现异常",logBean);
                //e.printStackTrace();
            }
        } catch(Exception e){
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
    }



    //注册验证码
    @RequestMapping(value = "/app/sendCode.do", method = RequestMethod.POST)
    @ResponseBody
    public Map registerSendCode(HttpServletRequest request, HttpServletResponse response) {//添加同步
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
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("username", jsonObj.getString("username"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            String verificationCode = VerificationCodeGenerator.createVerificationCode();
            request.getSession().setAttribute(jsonObj.getString("username"), verificationCode);
            String message = this.smsCheckManager.send(jsonObj.getString("username"), verificationCode, "1104699", PConst.TIANYI);
            CookieTool.addCookie(resultMapHandler.getResponse(), jsonObj.getString("username").toString(),verificationCode, 120);
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("message",message);//响应的用户信息
        } catch(Exception e){
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
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
               return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
           }
           String signmsg = jsonObj.getString("signmsg");
           treeMap.put("username", jsonObj.getString("username"));
           treeMap.put("code", jsonObj.getString("code"));
           treeMap.put("timestamp", jsonObj.getString("timestamp"));
           boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
           if (verify != true) {
               return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
           }
//           Cookie cookie = CookieTool.getCookieByName(request, jsonObj.getString("username").toString());
//           String code =  cookie.getValue();
           String code = (String) request.getSession().getAttribute(jsonObj.getString("username"));

           if(code==null){
               return  resultMapHandler.handlerResult("100011","验证码失效，请重新发送",logBean);
           }


          if (code!=null && code.equals(jsonObj.getString("code"))){
              resultMap = resultMapHandler.handlerResult("0","成功",logBean);
          }else{
              resultMap = resultMapHandler.handlerResult("100010","验证码验证失败",logBean);
          }

       } catch(Exception e){
           return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
       }
       return resultMap;
   }



    @RequestMapping(value = "/app/completeUserInfo.do", method = RequestMethod.POST)
    @ResponseBody
    public  Map completeUserInfo(HttpServletRequest request){//,@RequestParam MultiValueMap<String, Object> params, @RequestParam("headPortrait") MultipartFile headPortrait

       /* Map<String, List<Object>> paramsMap = new HashMap<>();
        paramsMap = params;//参数列表*/

        LogBean logBean = new LogBean();
        logBean.setApiName("completeUserInfo");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {

            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(request.getParameter("username").toString()+" "+request.getParameter("nickname").toString()
            +" "+request.getParameter("sex").toString()+" "+request.getParameter("timestamp").toString());
            if ("".equals(request.getParameter("signmsg")) || "".equals(request.getParameter("username")) ||
                    "".equals(request.getParameter("timestamp")) || "".equals(request.getParameter("nickname "))
                    || "".equals(request.getParameter("sex"))) {

                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            String signmsg = request.getParameter("signmsg").toString();
            treeMap.put("username", request.getParameter("username"));
            treeMap.put("nickname", request.getParameter("nickname"));
            treeMap.put("sex", request.getParameter("sex"));
            treeMap.put("timestamp", request.getParameter("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }

            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username",request.getParameter("username").toString());
            BigUser user;
            try {
                user = (BigUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_BIGUSER_GET, map);
                if (user!=null && user.getId()!=null) {
                    MultipartFile headPortrait = ((MultipartHttpServletRequest) request).getFile("headPortrait");
                    String fileType = "";
                    if(headPortrait.getContentType().contains("jpg")){
                        fileType = ".jpg";
                    }else if(headPortrait.getContentType().contains("jpeg")){
                        fileType = ".jpeg";
                    }else if(headPortrait.getContentType().contains("png")||headPortrait.getContentType().contains("PNG")){
                        fileType = ".png";
                    }else if(headPortrait.getContentType().contains("gif")){
                        fileType = ".gif";
                    }
                    String url = "headPortrait/" + request.getParameter("username").toString() +System.currentTimeMillis()+ fileType;
                    String pictureUrl = "http://rongyitou2.efeiyi.com/"+url;
                    //将用户头像上传至阿里云
                    aliOssUploadManager.uploadFile(headPortrait,"ec-efeiyi2",url);
                    user.setName(request.getParameter("nickname").toString());
                    user.setSex(Integer.parseInt(request.getParameter("sex").toString()));
                    user.setPictureUrl(pictureUrl);
                    baseManager.saveOrUpdate(BigUser.class.getName(),user);
                    resultMap = resultMapHandler.handlerResult("0","成功",logBean);
                    resultMap.put("headPortraitURI",pictureUrl);
                    resultMap.put("userInfo",(User)baseManager.getObject(User.class.getName(),user.getId()));
                    return resultMap;
                }else {
                    return  resultMapHandler.handlerResult("10007","用户名不存在",logBean);
                }

            } catch (Exception e) {
                e.printStackTrace();
                return  resultMapHandler.handlerResult("10005","查询数据出现异常",logBean);
            }

        } catch(Exception e){
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
    }


    //找回密码
    @RequestMapping(value = "/app/retrievePassword.do", method = RequestMethod.POST)
    @ResponseBody
    public Map retrievePassword(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("retrievePassword");
        Map<String, String> resultMap = new HashMap<String, String>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("username")) ||
                    "".equals(jsonObj.getString("timestamp"))|| "".equals(jsonObj.getString("password"))) {
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("username", jsonObj.getString("username"));
            treeMap.put("password", jsonObj.getString("password"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("username", jsonObj.getString("username"));
            MyUser user;
            try {
                user = (MyUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_MYUSER_GET, map);
                if (user!=null && user.getId()!=null) {
                    user.setPassword(jsonObj.getString("password"));
                    baseManager.saveOrUpdate(MyUser.class.getName(),user);
                    return  resultMapHandler.handlerResult("0","成功",logBean);
                }else {

                    return  resultMapHandler.handlerResult("10007","用户名不存在",logBean);
                }
            } catch (Exception e) {
                return  resultMapHandler.handlerResult("10005","查询数据出现异常",logBean);
            }


        } catch(Exception e){
            resultMap = resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
            return resultMap;
        }
    }




    //找回密码
    @RequestMapping(value = "/app/feedBack.do", method = RequestMethod.POST)
    @ResponseBody
    public Map feedBack(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("feedBack");
        Map<String, String> resultMap = new HashMap<String, String>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg"))  ||
                    "".equals(jsonObj.getString("timestamp"))|| "".equals(jsonObj.getString("content"))) {
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
//            treeMap.put("userId", jsonObj.getString("userId"));
            treeMap.put("content", jsonObj.getString("content"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }

            User user = null;
            try {
//                user = (User)baseManager.getObject(User.class.getName(),jsonObj.getString("userId"));
                user = AuthorizationUtil.getUser();
                if (user!=null && user.getId()!=null) {
                    UserFeedBack userFeedBack = new UserFeedBack();
                    userFeedBack.setCreateDatetime(new Date());
                    userFeedBack.setStatus("1");
                    userFeedBack.setContent(jsonObj.getString("content"));
                    userFeedBack.setUser(user);
                    userFeedBack.setEmail(jsonObj.getString("email"));
                    baseManager.saveOrUpdate(UserFeedBack.class.getName(),userFeedBack);
                    return  resultMapHandler.handlerResult("0","成功",logBean);
                }else {

                    return  resultMapHandler.handlerResult("10007","用户名不存在",logBean);
                }
            } catch (Exception e) {
                return  resultMapHandler.handlerResult("10005","查询数据出现异常",logBean);
            }


        } catch(Exception e){
            resultMap = resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
            return resultMap;
        }
    }


    @RequestMapping(value = "/app/test.do", method = RequestMethod.POST)//测试方法
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




    public static String RegisterUsers(String username,String password) {
        JMessageClient client = new JMessageClient(JPushConfig.appKey, JPushConfig.masterSecret);

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

    //微信登录
    @RequestMapping(value = "/app/WxLogin.do", method = RequestMethod.POST)
    @ResponseBody
    public Map WxLogin(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("WxLogin");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("unionid")) ||
                    "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("unionid", jsonObj.getString("unionid"));
            treeMap.put("nickname", jsonObj.getString("nickname"));
            treeMap.put("headimgurl", jsonObj.getString("headimgurl"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("unionid", jsonObj.getString("unionid"));
            MyUser user = null;
            try {
                user = (MyUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_WX_LOGIN, map);

                if (user!=null && user.getId()!=null) {
//                    user.setPassword(jsonObj.getString("password"));
//                    baseManager.saveOrUpdate(MyUser.class.getName(),user);
                    if(StringUtils.isEmpty(user.getUsername())){
                        user.setUsername(System.currentTimeMillis()+"");
                        user.setPassword(StringUtil.encodePassword("rongyitou","SHA1"));
                        baseManager.saveOrUpdate(User.class.getName(),user);
                    }
                    resultMap =  resultMapHandler.handlerResult("0","成功",logBean);
//                    resultMap.put("userInfo",(User) baseManager.getObject(User.class.getName(), user.getId()));
                    resultMap.put("username",user.getUsername());
//                    resultMap.put("password","rongyitou");
                    return  resultMap;
                }else {
                    user = new MyUser();
                    user.setUsername(System.currentTimeMillis()+"");
                    user.setPassword(StringUtil.encodePassword("rongyitou","SHA1"));
                    user.setName(jsonObj.getString("nickname"));
                    user.setUnionid(jsonObj.getString("unionid"));
                    user.setPictureUrl(jsonObj.getString("headimgurl"));
                    user.setAccountExpired(false);
                    user.setAccountLocked(false);
                    user.setCredentialsExpired(false);
                    user.setEnabled(true);
                    user.setStatus(1);
                    user.setUtype(2);
                    user.setCreateDatetime(new Date());
                    baseManager.saveOrUpdate(MyUser.class.getName(),user);
                    //给用户绑定一个账户
                    Account account = new Account();
                    account.setCurrentUsableBalance(new BigDecimal("0.00"));
                    account.setCurrentBalance(new BigDecimal("0.00"));
                    account.setCreateDatetime(new Date());
                    account.setStatus("1");
                    account.setUser((User)baseManager.getObject(User.class.getName(),user.getId()));
                    baseManager.saveOrUpdate(Account.class.getName(),account);
                    resultMap =  resultMapHandler.handlerResult("0","成功",logBean);
//                    resultMap.put("userInfo",(User) baseManager.getObject(User.class.getName(), user.getId()));
                    resultMap.put("username",user.getUsername());
//                    resultMap.put("password","rongyitou");
                    return  resultMap;
                }
            } catch (Exception e) {
                return  resultMapHandler.handlerResult("10005","查询数据出现异常",logBean);
            }


        } catch(Exception e){
            resultMap = resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
            return resultMap;
        }
    }


    @Test
    public void testArtworkPraise() throws Exception {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new TreeMap<>();

        /**登录.do测试加密参数**/
        map.put("username", "18612997469");
        map.put("password", "5555");
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        map.put("signmsg", signmsg);
        String url = "http://192.168.1.75:8080/app/j_spring_security_check";
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
        System.out.println("url:  " + url);

        String jsonString = JSONObject.toJSONString(map);
        StringEntity stringEntity = new StringEntity(jsonString, "utf-8");
        stringEntity.setContentType("text/json");
        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(stringEntity);
        System.out.println("url:  " + url);
        try {
            byte[] b = new byte[(int) stringEntity.getContentLength()];
            System.out.println(stringEntity);
            stringEntity.getContent().read(b);
            System.out.println("报文:" + new String(b, "utf-8"));
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    entity.getContent(), "UTF-8"));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            System.out.println(stringBuilder);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}