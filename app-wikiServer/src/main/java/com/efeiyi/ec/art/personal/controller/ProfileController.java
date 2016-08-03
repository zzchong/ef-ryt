package com.efeiyi.ec.art.personal.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.*;
import com.efeiyi.ec.art.message.dao.MessageDao;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.modelConvert.ConvertArtWork;
import com.efeiyi.ec.art.organization.model.*;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.dao.XdoDao;
import com.ming800.core.base.dao.hibernate.XdoDaoSupport;
import com.ming800.core.does.model.PageInfo;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.p.service.AliOssUploadManager;
import com.ming800.core.taglib.PageEntity;
import org.apache.commons.lang.SystemUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/2/17.
 */
@Controller
public class ProfileController extends BaseController {
    private static Logger logger = Logger.getLogger(ProfileController.class);

    @Autowired
    private AliOssUploadManager aliOssUploadManager;
    @Autowired
    ResultMapHandler resultMapHandler;
    @Autowired
    private XdoDaoSupport xdoDao;

    @Autowired
    private MessageDao messageDao;

    /**
     * 获取用户资料
     *
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
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            String username = jsonObj.getString("username");
            treeMap.put("username", username);
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            XQuery xQuery = new XQuery("listUser_default", request);
            xQuery.put("username", username);
            xQuery.put("status", 0);
            List<MyUser> users = baseManager.listObject(xQuery);
            if (users != null && users.size() > 0) {
                MyUser user = users.get(0);
                resultMapHandler.handlerResult("0", "请求成功", logBean);
                resultMap.put("userInfo", user);
            } else {
                return resultMapHandler.handlerResult("10008", "查无数据,稍后再试", logBean);
            }
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**
     * 编辑用户资料接口
     * 接口调用路径 /app/editProfile.do
     *
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
        User user;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId"))
                    || "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("type"))
                   ) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
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
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            user = (User) baseManager.getObject(User.class.getName(), userId);
            if (user != null && user.getId() != null) {
                if ("11".equals(type)) {
                    user.setName(content);
                    baseManager.saveOrUpdate(User.class.getName(), user);
                   resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
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
                        baseManager.saveOrUpdate(User.class.getName(), user);
                       resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
                        resultMap.put("userInfo", user);
                    } else {
                        return resultMapHandler.handlerResult("10006", "手机号码校验不合格", logBean);
                    }
                } else if ("13".equals(type)) {
                    /**
                     * 此处为编辑签名的操作
                     * 修改model后添加业务逻辑
                     */
                     UserBrief userBrief = null;
//                   XQuery xQuery = new XQuery("listUserBrief_default",request);
//                    xQuery.put("user_id",user.getId());
//                    List<UserBrief> userBriefList = baseManager.listObject(xQuery);
                    if(user.getUserBrief()!=null)
                        userBrief = (UserBrief)baseManager.getObject(UserBrief.class.getName(),user.getUserBrief().getId());
                    else
                       userBrief = new UserBrief();
                    userBrief.setUser(user);
                    userBrief.setSigner(content);
                    userBrief.setStatus("1");
                    userBrief.setCreateDatetime(new Date());
                    if(user.getMaster()!=null)
                        userBrief.setType("1");
                    else
                        userBrief.setType("2");
                    baseManager.saveOrUpdate(UserBrief.class.getName(), userBrief);
                    user.setUserBrief(userBrief);
                    resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
                    resultMap.put("userInfo", user);
                }else if("10".equals(type)){
                    MultipartFile headPortrait = ((MultipartHttpServletRequest) request).getFile("headPortrait");
                    String url = "headPortrait/" + user.getUsername() + headPortrait.getOriginalFilename();
                    String pictureUrl = "http://rongyitou2.efeiyi.com/"+url;
                    //将用户头像上传至阿里云
                    aliOssUploadManager.uploadFile(headPortrait,"ec-efeiyi2",url);
                    user.setPictureUrl(pictureUrl);
                    baseManager.saveOrUpdate(User.class.getName(),user);
                    resultMap = resultMapHandler.handlerResult("0","请求成功",logBean);
                    resultMap.put("userInfo", user);
                }else if("14".equals(type)){
                    user.setSex(Integer.parseInt(content));
                    baseManager.saveOrUpdate(User.class.getName(), user);
                    resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
                    resultMap.put("userInfo", user);
                }
            } else {
                return resultMapHandler.handlerResult("10008", "查无数据,稍后再试", logBean);
            }
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 编辑用户资料接口
     * 接口调用路径 /app/editPicUrl.do
     *
     * @param request 参数type决定编辑哪项资料   type 11/昵称  type 12/手机号码  type 13/签名
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/editPicUrl.do", method = RequestMethod.POST)
    public Map editPicUrl(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        User user;
        try {
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(request.getParameter("userId"));//************记录请求报文
            if ("".equals(request.getParameter("userId"))
                    || "".equals(request.getParameter("timestamp")) )
                    {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            String signmsg = request.getParameter("signmsg");
            String userId = request.getParameter("userId");
            treeMap.put("userId", userId);
//            treeMap.put("content", content);
            treeMap.put("timestamp", request.getParameter("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            user = (User) baseManager.getObject(User.class.getName(), userId);
            if (user != null && user.getId() != null) {
                    MultipartFile headPortrait = ((MultipartHttpServletRequest) request).getFile("headPortrait");
                    String url = "headPortrait/" + System.currentTimeMillis()+ user.getUsername() + headPortrait.getOriginalFilename();
                    String pictureUrl = "http://rongyitou2.efeiyi.com/"+url;
                    //将用户头像上传至阿里云
                    aliOssUploadManager.uploadFile(headPortrait,"ec-efeiyi2",url);
                    user.setPictureUrl(pictureUrl);
                    baseManager.saveOrUpdate(User.class.getName(),user);
                    resultMap = resultMapHandler.handlerResult("0","请求成功",logBean);
                    resultMap.put("userInfo", user);

            } else {
                return resultMapHandler.handlerResult("10008", "查无数据,稍后再试", logBean);
            }
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 保存用户提现密码
     * 接口调用路径 /app/savePassword.do
     *
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
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            String userId = jsonObj.getString("userId");
            String level_two_pwd = jsonObj.getString("level_two_pwd");
            treeMap.put("userId", userId);
            treeMap.put("level_two_pwd", level_two_pwd);
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            LinkedHashMap<String, Object> queryMap = new LinkedHashMap<>();
            queryMap.put("userId", userId);
            Account account = (Account) baseManager.getUniqueObjectByConditions(AppConfig.SQL_ACCOUNT_BY_USER_ID, queryMap);
            if (account != null && account.getId() != null) {
                account.setPassword(level_two_pwd);
                baseManager.saveOrUpdate(Account.class.getName(), account);
                resultMapHandler.handlerResult("0", "请求成功", logBean);
                resultMap.put("account", account);
            } else {
                return resultMapHandler.handlerResult("10008", "查无数据,稍后再试", logBean);
            }
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**
     * 普通用户申请成为艺术家接口
     *
     * @param request 接口调用路径 /app/applyArtMaster.do
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/applyArtMaster.do", method = RequestMethod.POST)
    public Map applyArtMaster(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
//        JSONObject jsonObj;
        try {
//            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
//            logBean.setMsg(jsonObj.toString());//************记录请求报文
            String signmsg = request.getParameter("signmsg");
            String timestamp = request.getParameter("timestamp");
//            String userId = request.getParameter("userId");
            String paramType = request.getParameter("paramType");
            String province = request.getParameter("province");
            String provinceName = request.getParameter("provinceName");
            String artCategory = request.getParameter("artCategory");
            String titleCertificate = request.getParameter("titleCertificate");
            String name = request.getParameter("name");
            String phone = request.getParameter("phone");
            if ("".equals(signmsg)
                    || "".equals(timestamp)
//                    || "".equals(userId)
                    || "".equals(paramType)
//                    || "".equals(artCategory)
//                    || "".equals(titleCertificate)
//                    || "".equals(provinceName)
//                    || "".equals(province)
                    || "".equals(name)
                    || "".equals(phone)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
//            treeMap.put("userId", userId);
//            treeMap.put("name", name);
//            treeMap.put("phone", phone);
//            treeMap.put("paramType", paramType);
//            treeMap.put("timestamp", timestamp);
//            treeMap.put("province", province);
//            treeMap.put("provinceName", provinceName);
//            treeMap.put("artCategory", artCategory);
//            treeMap.put("titleCertificate", titleCertificate);
//
//            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
//            if (!verify) {
//                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
//            }



            User user = (User)baseManager.getObject(User.class.getName(),AuthorizationUtil.getUserId());
            Master master = new Master();
            master.setTheStatus("2");
            master.setPresentAddress(provinceName);
            master.setProvinceName(province);
            master.setArtCategory(artCategory);
            master.setTitle(titleCertificate);
            master.setTitleCertificate(titleCertificate);
            master.setPhone(phone);
            master.setName(name);
            master.setUser(user);
            System.out.print(master.getId());
            baseManager.saveOrUpdate(Master.class.getName(), master);
//            user.setMaster(master);
            user.setType("0");
            baseManager.saveOrUpdate(User.class.getName(), user);

            resultMap = uploadFile(request);

            resultMapHandler.handlerResult("0", "成功", logBean);
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
        return resultMap;
    }


    /**
     * 图片上传
     *
     * @param request
     * @return
     */
    private Map uploadFile(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        try {
            String querySql = "from Master m where m.user.id=:userId and m.theStatus != '0'";
            LinkedHashMap<String, Object> queryMap = new LinkedHashMap<>();
            String userId = request.getParameter("userId");
            queryMap.put("userId", userId);
            Master master = (Master) baseManager.getUniqueObjectByConditions(querySql, queryMap);
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            List<MultipartFile> oneList = multipartRequest.getFiles("one");//工作照片
            List<MultipartFile> twoList = multipartRequest.getFiles("two");//工作室照片
            List<MultipartFile> threeList = multipartRequest.getFiles("three");//资格认证照片
            MultipartFile identityFront = multipartRequest.getFile("identityFront");//身份证前照
            MultipartFile identityBack = multipartRequest.getFile("identityBack");//身份证后照
            String filePath;
            String paramType = request.getParameter("paramType");
//            if ("yes".equals(paramType)){
                if (!oneList.isEmpty()) {
                    uploadFilePath(oneList, master, "1");
                }
                if (!twoList.isEmpty()) {
                    uploadFilePath(twoList, master, "2");
                }
                if (!threeList.isEmpty()){
                    uploadFilePath(threeList,master,"3");
                }
//            }else{
                if (identityFront!=null){
                    filePath = uploadFile(identityFront);
                    master.setIdentityFront(filePath);
                    baseManager.saveOrUpdate(Master.class.getName(),master);
                }
                if (identityBack!=null){
                    filePath = uploadFile(identityBack);
                    master.setIdentityBack(filePath);
                    baseManager.saveOrUpdate(Master.class.getName(),master);
                }
//            }
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);

        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
        return resultMap;
    }

//    /**
//     * 普通用户申请成为艺术家接口
//     *
//     * @param request 接口调用路径 /app/applyArtMaster.do
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value = "/app/applyArtMaster.do", method = RequestMethod.POST)
//    public Map applyArtMaster(HttpServletRequest request) {
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        LogBean logBean = new LogBean();
//        TreeMap treeMap = new TreeMap();
//        JSONObject jsonObj;
//        try {
//            jsonObj = JsonAcceptUtil.receiveJson(request);
//            logBean.setCreateDate(new Date());
//            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
//            String signmsg = jsonObj.getString("signmsg");
//            String timestamp = jsonObj.getString("timestamp");
//            String userId = jsonObj.getString("userId");
//            String name = jsonObj.getString("name");
//            String username = jsonObj.getString("username");
//            String province = jsonObj.getString("province");
//            String provinceName = jsonObj.getString("provinceName");
//            String artCategory = jsonObj.getString("artCategory");
//            String titleCertificate = jsonObj.getString("titleCertificate");
//            if ("".equals(signmsg) || "".equals(name) || "".equals(timestamp) || "".equals(username) || "".equals(userId)
//                    || "".equals(province) || "".equals(provinceName) || "".equals(artCategory) || "".equals(titleCertificate)) {
//                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
//            }
//            treeMap.put("name", name);
//            treeMap.put("userId", userId);
//            treeMap.put("username", username);
//            treeMap.put("province", province);
//            treeMap.put("provinceName", provinceName);
//            treeMap.put("artCategory", artCategory);
//            treeMap.put("titleCertificate", titleCertificate);
//            treeMap.put("timestamp", jsonObj.getString("timestamp"));
//            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
//            if (!verify) {
//                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
//            }
//            Master master = new Master();
//            master.setTheStatus("1");
//            AddressProvince addressProvince = (AddressProvince) baseManager.getObject(AddressProvince.class.getName(), province);
//            master.setOriginProvince(addressProvince);
//            master.setProvinceName(provinceName);
//            master.setArtCategory(artCategory);
//            master.setTitleCertificate(titleCertificate);
//            User user = (User) baseManager.getObject(User.class.getName(), userId);
//            master.setUser(user);
//            System.out.print(master.getId());
//            baseManager.saveOrUpdate(Master.class.getName(), master);
//            user.setMaster(master);
//            baseManager.saveOrUpdate(User.class.getName(), user);
//            resultMapHandler.handlerResult("0", "成功", logBean);
//        } catch (Exception e) {
//            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
//        }
//        return resultMap;
//    }
//
//
//    /**
//     * 图片上传
//     *
//     * @param request
//     * @return
//     */
//    @ResponseBody
//    @RequestMapping(value = "/app/uploadFile.do", method = RequestMethod.POST)
//    public Map uploadFile(HttpServletRequest request) {
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        LogBean logBean = new LogBean();
//        TreeMap treeMap = new TreeMap();
//        JSONObject jsonObj;
//        try {
//            jsonObj = JsonAcceptUtil.receiveJson(request);
//            logBean.setCreateDate(new Date());
//            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
//            String signmsg = jsonObj.getString("signmsg");
//            String timestamp = jsonObj.getString("timestamp");
//            String userId = jsonObj.getString("userId");
//            String paramType = jsonObj.getString("paramType");
//            if ("".equals(signmsg) || "".equals(timestamp) || "".equals(userId) || "".equals(paramType)) {
//                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
//            }
//            treeMap.put("userId", userId);
//            treeMap.put("paramType",paramType);
//            treeMap.put("timestamp", jsonObj.getString("timestamp"));
//            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
//            if (!verify) {
//                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
//            }
//            String querySql = "from Master m where m.user.id=:userId and m.theStatus = '1'";
//            LinkedHashMap<String, Object> queryMap = new LinkedHashMap<>();
//            queryMap.put("userId", userId);
//            Master master = (Master) baseManager.getUniqueObjectByConditions(querySql, queryMap);
//            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
//            List<MultipartFile> oneList = multipartRequest.getFiles("one");
//            List<MultipartFile> twoList = multipartRequest.getFiles("two");
//            List<MultipartFile> threeList = multipartRequest.getFiles("three");
//            MultipartFile identityFront = multipartRequest.getFile("identityFront");
//            MultipartFile identityBack = multipartRequest.getFile("identityBack");
//            String filePath;
//            if ("yes".equals(paramType)){
//                if (!oneList.isEmpty()) {
//                    uploadFilePath(oneList, master, "1");
//                }
//                if (!twoList.isEmpty()) {
//                    uploadFilePath(twoList, master, "2");
//                }
//                if (!threeList.isEmpty()){
//                    uploadFilePath(threeList,master,"3");
//                }
//            }else{
//                if (identityFront!=null){
//                    filePath = uploadFile(identityFront);
//                    master.setIdentityFront(filePath);
//                    baseManager.saveOrUpdate(Master.class.getName(),master);
//
//                }
//                if (identityBack!=null){
//                    filePath = uploadFile(identityBack);
//                    master.setIdentityBack(filePath);
//                    baseManager.saveOrUpdate(Master.class.getName(),master);
//                }
//            }
//            resultMapHandler.handlerResult("0", "成功", logBean);
//
//        } catch (Exception e) {
//            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
//        }
//        return resultMap;
//    }

    /**
     * 上传单个附件调用的方法
     * @param mf
     * @return
     * @throws IOException
     */
    public String uploadFile(MultipartFile mf){
        String pictureUrl = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String identify = sdf.format(new Date());
            String url ;
            String fileName = mf.getOriginalFilename();//获取原文件名
            String file = fileName.substring(0, fileName.indexOf("."));
            String prefix = "";
            switch (mf.getContentType()) {
                case "image/jpg":
                    prefix = ".jpg";
                    break;
                case "image/jpeg":
                    prefix = ".jpeg";
                    break;
                case "image/png":
                    prefix = ".png";
                    break;
                case "image/gif":
                    prefix = ".gif";
                    break;
            }
            url = "master/" + file + identify + prefix;
            aliOssUploadManager.uploadFile(mf, "ec-efeiyi2", url);
            pictureUrl = "http://rongyitou2.efeiyi.com/" + url ;
        }catch (Exception e){
            e.printStackTrace();
        }
        return pictureUrl;
    }

    /**
     * 上传列表附件内部调用单附件上传方法
     * @param list
     * @param master
     * @param msg
     * @return
     */
    public void uploadFilePath(List<MultipartFile> list, Master master, String msg){
        List<ArtMasterAttachment> attachmentList = new ArrayList<>(3);
        String pictureUrl;
        if (!list.isEmpty()) {
            for (MultipartFile mf : list) {
                pictureUrl = uploadFile(mf);
                ArtMasterAttachment attachment = new ArtMasterAttachment();
                attachment.setMaster(master);
                attachment.setUrl(pictureUrl);
                baseManager.saveOrUpdate(ArtMasterAttachment.class.getName(), attachment);
                attachmentList.add(attachment);
            }
            putAttachment(msg,attachmentList,master);
        }
    }

    /**
     * master实体put附件列表
     * @param msg
     * @param attachments
     * @param master
     * @return
     */
    public void putAttachment(String msg , List<ArtMasterAttachment> attachments,Master master){
        if ("1".equals(msg)) {
            master.setWorksPhotos(attachments);
        } else if("2".equals(msg)) {
            master.setWorkShopPhotos(attachments);
        } else if("3".equals(msg)){
            master.setCertificatePhotos(attachments);
        }
        baseManager.saveOrUpdate(Master.class.getName(), master);
    }

    /**
     * 获取用户首页
     *
     * @param request 接口调用路径 /app/my.do
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/user.do", method = RequestMethod.POST)
    public Map User(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            String signmsg = jsonObj.getString("signmsg");
            String userId = jsonObj.getString("userId");
            String timestamp = jsonObj.getString("timestamp");
            if ("".equals(signmsg)  || "".equals(timestamp) || "".equals(userId)
                    ) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            treeMap.put("userId", userId);
            treeMap.put("timestamp", timestamp);
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            Map<String,Object> data = new HashMap<>();

            User user = (User) baseManager.getObject(User.class.getName(), userId);
            data.put("user",user);

            //关注列表
            XQuery beQuery = new XQuery("listArtUserFollowed_followed", request);
            beQuery.put("user_id", userId);
            List<ArtUserFollowed> followedList = baseManager.listObject(beQuery);
            if(followedList!=null)
                data.put("num",followedList.size());
            else
                data.put("num",0);
            //被关注列表
            XQuery toQuery = new XQuery("listArtUserFollowed_default", request);
            toQuery.put("follower_id", userId);
            List<ArtUserFollowed> toFollowedList = baseManager.listObject(toQuery);
            if(toFollowedList!=null)
                data.put("followNum",toFollowedList.size());
            else
                data.put("followNum",0);


            //是否关注
            Boolean isFollowed = false;
            if (!StringUtils.isEmpty(AuthorizationUtil.getUser())) {
                XQuery xQuery = new XQuery("listArtUserFollowed_isFollowed", request);
                xQuery.put("user_id", AuthorizationUtil.getUserId());
                xQuery.put("follower_id", jsonObj.getString("userId"));
                List<ArtUserFollowed> artUserFollowedList = baseManager.listObject(xQuery);
                if (artUserFollowedList != null) {
                    if (artUserFollowedList.size() > 0) {
                        isFollowed = true;
                    }
                }
            }


            //根据用户id获取投资记录
            XQuery xquery = new XQuery("listArtworkInvest_default", request);
            xquery.put("creator_id", userId);
            List<ArtworkInvest> artworkInvests = (List<ArtworkInvest>) baseManager.listObject(xquery);

            //同一用户投资所有项目的总投资金额
            BigDecimal sumInvestsMoney = new BigDecimal("0.00");
            for (ArtworkInvest artworkInvest : artworkInvests) {
                sumInvestsMoney = sumInvestsMoney.add(artworkInvest.getPrice());
            }

            data.put("sumInvestment",sumInvestsMoney);
            //投资回报
            BigDecimal reward = new BigDecimal("0.00");
            XQuery xQuery = new XQuery("listROIRecord_default",request);
            xQuery.put("user_id",jsonObj.getString("userId"));
            List<ROIRecord> roiRecordList = (List<ROIRecord>)baseManager.listObject(xQuery);
            for (ROIRecord roiRecord : roiRecordList){
                reward = reward.add(roiRecord.getCurrentBalance());
            }

            data.put("yield",reward);
            data.put("isFollowed",isFollowed);
            resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
            resultMap.put("data", data);
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }



    /**
     * 获取用户首页
     *
     * @param request 接口调用路径 /app/my.do
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/my.do", method = RequestMethod.POST)
    public Map getUser(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            String signmsg = jsonObj.getString("signmsg");
//            String currentId = jsonObj.getString("currentId");
            String userId = jsonObj.getString("userId");
            String index = jsonObj.getString("pageIndex");
            String size = jsonObj.getString("pageSize");
            String timestamp = jsonObj.getString("timestamp");
            if ("".equals(signmsg) || "".equals(userId) || "".equals(timestamp)
                    || "".equals(index) || "".equals(size)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            treeMap.put("userId", userId);
//            treeMap.put("currentId", currentId);
            treeMap.put("pageIndex", index);
            treeMap.put("pageSize", size);
            treeMap.put("timestamp", timestamp);
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            User user = (User) baseManager.getObject(User.class.getName(), userId);
            String currentId = AuthorizationUtil.getUserId();
            //关注列表
            XQuery beQuery = new XQuery("listArtUserFollowed_followed", request);
            beQuery.put("user_id", userId);
            List<ArtUserFollowed> followedList = baseManager.listObject(beQuery);
            //被关注列表
            XQuery toQuery = new XQuery("listArtUserFollowed_default", request);
            toQuery.put("follower_id", userId);
            List<ArtUserFollowed> toFollowedList = baseManager.listObject(toQuery);
            //是否关注
            boolean isFollowed = false;
            //是否点赞
            boolean isPraise = false;
            //关注Id
            String artUserFollowId = "";
            if(currentId!=null && !"".equals(currentId) && !currentId.equals(userId)){
                XQuery xQuery1  = new XQuery("listArtUserFollowed_isFollowed",request);
                xQuery1.put("user_id",currentId);
                xQuery1.put("follower_id",userId);
                List<ArtUserFollowed> artUserFollowedList = (List<ArtUserFollowed>) baseManager.listObject(xQuery1);
                if(artUserFollowedList!=null && artUserFollowedList.size()!=0){
                    isFollowed = true;
                    artUserFollowId = artUserFollowedList.get(0).getId();
                }
            }


            //初始化一个通用的PageEntity
            PageEntity entity = new PageEntity();
            entity.setIndex(Integer.parseInt(index));
            entity.setSize(Integer.parseInt(size));

            //按项目分组获取同一用户多次投资同一项目所投资金额总和
            String queryHql = "select SUM(price) FROM ArtworkInvest where creator.id = :userId GROUP BY artwork.id ORDER BY createDatetime DESC";
            XQuery queryTo = new XQuery();
            queryTo.setHql(queryHql);
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("userId", userId);
            queryTo.setQueryParamMap(map);
            queryTo.setPageEntity(entity);
            List<BigDecimal> investMoney = baseManager.listObject(queryTo);

            //按项目分组获取投资记录
            String querySql = "FROM ArtworkInvest where creator.id = :userId and status='1' GROUP BY artwork.id ORDER BY createDatetime DESC";
            XQuery xQuery = new XQuery();
            xQuery.setHql(querySql);
            LinkedHashMap<String, Object> queryMap = new LinkedHashMap<>();
            queryMap.put("userId", userId);
            xQuery.setQueryParamMap(queryMap);
            xQuery.setPageEntity(entity);
//            List<ArtworkInvest> artworkInvestList = baseManager.listObject(queryHql,queryMap);
            PageInfo investPage = baseManager.listPageInfo(xQuery);
            List<ArtworkInvest> invests = new ArrayList<>();
            if(investPage!=null){
                invests = investPage.getList();
            }

            //根据用户id获取投资记录
            XQuery xquery = new XQuery("listArtworkInvest_default", request);
            xquery.put("creator_id", userId);
            List<ArtworkInvest> artworkInvests = (List<ArtworkInvest>) baseManager.listObject(xquery);

            //同一用户投资所有项目的总投资金额
            BigDecimal sumInvestsMoney = new BigDecimal("0.00");
            for (ArtworkInvest artworkInvest : artworkInvests) {
                sumInvestsMoney = sumInvestsMoney.add(artworkInvest.getPrice());
            }
            //投资回报
            BigDecimal reward = new BigDecimal("0.00");
            xQuery = new XQuery("listROIRecord_default",request);
            xQuery.put("user_id",jsonObj.getString("userId"));
            List<ROIRecord> roiRecordList = (List<ROIRecord>)baseManager.listObject(xQuery);
            for (ROIRecord roiRecord : roiRecordList){
                reward = reward.add(roiRecord.getCurrentBalance());
            }
            ConvertArtWork convert = ConvertArtWorkUtil.convert(invests, followedList.size(), toFollowedList.size(), investMoney, sumInvestsMoney, reward, user);
            convert.setFollowed(isFollowed);
            convert.setArtUserFollowId(artUserFollowId);


            resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
            resultMap.put("pageInfo", convert);
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 获取艺术家详细资料
     *
     * @param request 接口调用路径 /app/masterDetails.do
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/masterDetails.do", method = RequestMethod.POST)
    public Map MasterDetils(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId"))
                    || "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            String userId = jsonObj.getString("userId");
            treeMap.put("userId", userId);
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            String querySql = "from Master m where m.user.id=:userId and m.theStatus = '1'";
            LinkedHashMap<String, Object> queryMap = new LinkedHashMap<>();
            queryMap.put("userId", userId);
            Master master = (Master) baseManager.getUniqueObjectByConditions(querySql, queryMap);
            if (master != null && master.getId() != null) {
                resultMap.put("master", master);
            } else {
                resultMapHandler.handlerResult("10008", "查无数据,稍后再试", logBean);
            }
            System.out.println(master);
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    @RequestMapping("/app/myArtwork.do")
    @ResponseBody
    public Map myArtwork(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
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
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            treeMap.put("userId", userId);
            treeMap.put("timestamp", timestamp);
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            User user = (User) baseManager.getObject(User.class.getName(), userId);

            //关注列表
            XQuery beQuery = new XQuery("listArtUserFollowed_followed", request);
            beQuery.put("user_id", userId);
            List<ArtUserFollowed> followedList = baseManager.listObject(beQuery);
            //被关注列表
            XQuery toQuery = new XQuery("listArtUserFollowed_default", request);
            toQuery.put("follower_id", userId);
            List<ArtUserFollowed> toFollowedList = baseManager.listObject(toQuery);

            //获取同一用户所有项目
            LinkedHashMap queryMap = new LinkedHashMap();
            queryMap.put("author", user);
            List<Artwork> artworkList = baseManager.listObject(AppConfig.GET_ART_WORK_BY_ARTIST, queryMap);

            //同一用户所有项目的总金额
            BigDecimal sumInvestsMoney = new BigDecimal("0.00");
            for (Artwork artwork : artworkList) {
                sumInvestsMoney = sumInvestsMoney.add(artwork.getInvestsMoney());
            }
            //同一用户所有项目拍卖总金额
            BigDecimal reward = new BigDecimal("0.00");
            for (Artwork artwork : artworkList) {
                reward = reward.add(artwork.getNewBidingPrice());
            }

            ConvertArtWork convert = ConvertArtWorkUtil.convert2(artworkList,followedList.size(), toFollowedList.size(), sumInvestsMoney, reward, user);
            resultMapHandler.handlerResult("0", "请求成功", logBean);
            resultMap.put("pageInfo", convert);
            System.out.print(resultMap);

        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 获取赞过的项目列表
     * @param request 接口调用路径 /app/followed.do
     * @return
     */
    @ResponseBody
    @RequestMapping("/app/followed.do")
    public Map followed(HttpServletRequest request){
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId")) ||
                    "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("type")) ||
                    "".equals(jsonObj.getString("pageIndex")) || "".equals(jsonObj.getString("pageSize"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            String userId = jsonObj.getString("userId");
            String type = jsonObj.getString("type");
            String index = jsonObj.getString("pageIndex");
            String size = jsonObj.getString("pageSize");
//            String currentId = jsonObj.getString("currentId");
            treeMap.put("userId", userId);
            treeMap.put("type", type);
            treeMap.put("pageIndex", index);
            treeMap.put("pageSize", size);
//            treeMap.put("currentId", currentId);
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }


            Map<String,Object> data = new HashMap<>();
            User user = (User) baseManager.getObject(User.class.getName(),AuthorizationUtil.getUserId());
            List<String> list = new ArrayList<>();
            if(user!=null) {
                if (user.getArtWorkPraiseList() != null && user.getArtWorkPraiseList().size() > 0) {
                    for (ArtWorkPraise artWorkPraise : user.getArtWorkPraiseList()) {
                        list.add(artWorkPraise.getArtwork().getId());
                    }

                }
            }

            List<Artwork> artworkList = new ArrayList<Artwork>();
            PageEntity entity = new PageEntity();
            entity.setIndex(Integer.parseInt(index));
            entity.setSize(Integer.parseInt(size));
            XQuery xQuery = new XQuery("listArtWorkPraise_byUserId",request);
            xQuery.put("user_id",userId);
            xQuery.setPageEntity(entity);
            PageInfo info = baseManager.listPageInfo(xQuery);
            List<ArtWorkPraise> workPraises = info.getList();



            if (workPraises != null && !workPraises.isEmpty() && user!=null){
                Artwork artwork = null;
                for (ArtWorkPraise praise : workPraises){
//                    XQuery query = new XQuery("listArtWorkPraise_byArtWorkId",request);
//                    query.put("artwork_id",praise.getArtwork().getId());
//                    List<ArtWorkPraise> praises = baseManager.listObject(query);
//                    praise.getArtwork().setPraiseNUm(praises.size());
//                    artworkList.add(praise.getArtwork());
                    artwork = praise.getArtwork();
                    if(user.getArtWorkPraiseList()!=null && user.getArtWorkPraiseList().size()>0 && list.contains(praise.getArtwork().getId()))
                              artwork.setPraise(true);
                    else
                              artwork.setPraise(false);

                    artworkList.add(artwork);

                }

//                resultMap.put("pageInfoList", workPraises);
            }else{
                workPraises = new ArrayList<ArtWorkPraise>();
            }
            resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
            resultMap.put("pageInfoList", artworkList);
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
        return resultMap;
    }

    /**
     * 点赞查询测试
     * @throws Exception
     */
    @Test
    public void testFollowed() throws Exception {

        long timestamp = System.currentTimeMillis();
        Map<String, Object> map = new TreeMap<>();

        /**artWorkCreationView.do测试加密参数**/
        map.put("userId", "ieatht97wfw30hfd");
        map.put("currentId", "imhfp1yr4636pj49");
        map.put("timestamp", timestamp);
        map.put("type", "1");
        map.put("pageIndex", 1);
        map.put("pageSize", 10);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        map.put("signmsg", signmsg);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.75:8080/app/followed.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  artWorkCreationList.do测试 **/
//        String json = "{\"pageNum\":\"1\",\"pageSize\":\"5\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  artWorkCreationView.do测试 **/
//        String json = "{\"userId\":\"ina6pqm2d036fya5\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
//        JSONObject jsonObj = (JSONObject) JSONObject.parse(json);
//        String jsonString = jsonObj.toJSONString();
        String jsonString= JSONObject.toJSONString(map);
        StringEntity stringEntity = new StringEntity(jsonString, "utf-8");
        stringEntity.setContentType("text/json");
        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httppost.setEntity(stringEntity);
        System.out.println("url:  " + url);
        try {
            byte[] b = new byte[(int) stringEntity.getContentLength()];
            System.out.println(stringEntity);
            stringEntity.getContent().read(b);
            System.out.println("报文:" + new String(b, "utf-8"));
            HttpResponse response = httpClient.execute(httppost);
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

    /**
     * 获取简介
     * @param request 接口调用路径 /app/intro.do
     * @return
     */
    @ResponseBody
    @RequestMapping("/app/intro.do")
    public Map intro(HttpServletRequest request){
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId")) ||
                    "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            String userId = jsonObj.getString("userId");
            treeMap.put("userId", userId);
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            LinkedHashMap<String , Object> map = new LinkedHashMap<>();
            map.put("userId",userId);
            UserBrief userBrief = (UserBrief) baseManager.getUniqueObjectByConditions(AppConfig.SQL_GET_USER_BRIEF, map);
            resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
            resultMap.put("userBrief",userBrief);
            resultMap.put("user",userBrief==null?null:userBrief.getUser());
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
        return resultMap;
    }

    //@RequestMapping("/app/test.do")
    public void test(HttpServletRequest request)throws  Exception{
        // 创建根节点 并设置它的属性 ;
        Element root = new Element("root");
        // 将根节点添加到文档中；
        Document Doc = new Document(root);
        //获取全部省
        XQuery xQuery = new XQuery("listAddressProvince_default", request);
        List<AddressProvince> addressProvinces =(List<AddressProvince>) baseManager.listObject(xQuery);
        for (AddressProvince addressProvince :addressProvinces){
            // 创建节点 book;
            Element element = new Element("province");
            element.setAttribute("name",addressProvince.getName());
            element.setAttribute("id",addressProvince.getId());
            root.addContent(element);
            //获取省下所有市
          /*  XQuery xQuery2 = new XQuery("listAddressCity_province", request);
            xQuery2.put("addressProvince_id",addressProvince.getId());
            List<AddressCity> list = (List<AddressCity>)baseManager.listObject(xQuery2);*/
            List<AddressCity> list = (List<AddressCity>)xdoDao.getSession().createQuery("from AddressCity where addressProvince.id =:addressProvinceId").setString("addressProvinceId",addressProvince.getId()).list();
            for (AddressCity addressCity:list){
                Element element2 = new Element("city");
                element2.setAttribute("name",addressCity.getName());
                element2.setAttribute("id",addressCity.getId());

                //获取所有地区
                /*XQuery xQuery3 = new XQuery("listAddressDistrict_city", request);
                xQuery3.put("addressCity_id",addressCity.getId());
                List<AddressDistrict> list2 = (List<AddressDistrict>)baseManager.listObject(xQuery3);*/
                List<AddressDistrict> list2 = (List<AddressDistrict>)xdoDao.getSession().createQuery("from AddressDistrict where addressCity.id=:addressCityId").setString("addressCityId",addressCity.getId()).list();

                for(AddressDistrict addressDistrict:list2){
                    Element element3 = new Element("district");
                    element3.setAttribute("name",addressDistrict.getName());
                    element3.setAttribute("id",addressDistrict.getId());
                    element2.addContent(element3);
                }
                element.addContent(element2);
            }
        }
        Format format = Format.getPrettyFormat();
        XMLOutputter XMLOut = new XMLOutputter(format);
        XMLOut.output(Doc, new FileOutputStream("d:/address.xml"));
    }
    public static void main(String[] arg) throws Exception {


        String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
        long timestamp = System.currentTimeMillis();
        Map<String, Object> map = new HashMap<String, Object>();

        /**artWorkCreationList.do测试加密参数**/
//        map.put("pageNum","1");
//        map.put("pageSize","5");
        /**editProfile.do测试加密参数**/
        map.put("userId", "iickhknq3h7yrku2");
//        map.put("currentId", "iickhknq3h7yrku2");
        map.put("pageSize", "20");
        map.put("pageIndex", "7");
        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        map.put("signmsg",signmsg);
        HttpClient httpClient = new DefaultHttpClient();
//        String url = "http://192.168.1.41:8080/app/myArtwork.do";
        String url = "http://192.168.1.75:8080/app/my.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  artWorkCreationList.do测试 **/
//        String json = "{\"pageNum\":\"1\",\"pageSize\":\"5\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  artWorkCreationView.do测试 **/
        String json = "{\"userId\":\"ina6pqm2d036fya5\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
        JSONObject jsonObj = (JSONObject) JSONObject.toJSON(map);
        String jsonString = jsonObj.toJSONString();

        StringEntity stringEntity = new StringEntity(jsonString, "utf-8");
        stringEntity.setContentType("text/json");
        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httppost.setEntity(stringEntity);
        System.out.println("url:  " + url);
        try {
            byte[] b = new byte[(int) stringEntity.getContentLength()];
            System.out.println(stringEntity);
            stringEntity.getContent().read(b);
            System.out.println("报文:" + new String(b, "utf-8"));
            HttpResponse response = httpClient.execute(httppost);
            HttpEntity entity = response.getEntity();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    entity.getContent(), "UTF-8"));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            System.out.println(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
