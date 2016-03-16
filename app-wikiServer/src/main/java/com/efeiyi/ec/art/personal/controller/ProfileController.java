package com.efeiyi.ec.art.personal.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.*;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.modelConvert.ConvertArtWork;
import com.efeiyi.ec.art.organization.model.AddressProvince;
import com.efeiyi.ec.art.organization.model.MyUser;
import com.efeiyi.ec.art.organization.model.User;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.p.service.AliOssUploadManager;
import com.ming800.core.taglib.PageEntity;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
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
                resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
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
                resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
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
                resultMapHandler.handlerResult("0", "请求成功", logBean);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
                resultMap.put("userInfo", user);
            } else {
                resultMapHandler.handlerResult("10008", "查无数据,稍后再试", logBean);
                resultMap.put("resultCode", "10008");
                resultMap.put("resultMsg", "查无数据,稍后再试");
            }
        } catch (Exception e) {
            resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMap;
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
        MyUser user;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId"))
                    || "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("type"))
                    || "".equals(jsonObj.getString("content"))) {
                resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
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
                resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                return resultMap;
            }
            user = (MyUser) baseManager.getObject(MyUser.class.getName(), userId);
            if (user != null && user.getId() != null) {
                if ("11".equals(type)) {
                    user.setName2(content);
                    baseManager.saveOrUpdate(MyUser.class.getName(), user);
                    resultMapHandler.handlerResult("0", "请求成功", logBean);
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
                        resultMapHandler.handlerResult("0", "请求成功", logBean);
                        resultMap.put("resultCode", "0");
                        resultMap.put("resultMsg", "请求成功");
                        resultMap.put("userInfo", user);
                    } else {
                        resultMapHandler.handlerResult("10006", "手机号码校验不合格", logBean);
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
                resultMapHandler.handlerResult("10008", "查无数据,稍后再试", logBean);
                resultMap.put("resultCode", "10008");
                resultMap.put("resultMsg", "查无数据,稍后再试");
            }
        } catch (Exception e) {
            resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMap;
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
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            String signmsg = jsonObj.getString("signmsg");
            String timestamp = jsonObj.getString("timestamp");
            String userId = jsonObj.getString("userId");
            String name = jsonObj.getString("name");
            String username = jsonObj.getString("username");
            String province = jsonObj.getString("province");
            String provinceName = jsonObj.getString("provinceName");
            String artCategory = jsonObj.getString("artCategory");
            String titleCertificate = jsonObj.getString("titleCertificate");
            if ("".equals(signmsg) || "".equals(name) || "".equals(timestamp) || "".equals(username) || "".equals(userId)
                    || "".equals(province) || "".equals(provinceName) || "".equals(artCategory) || "".equals(titleCertificate)) {
                resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                return resultMap;
            }
            treeMap.put("name", name);
            treeMap.put("userId", userId);
            treeMap.put("username", username);
            treeMap.put("province", province);
            treeMap.put("provinceName", provinceName);
            treeMap.put("artCategory", artCategory);
            treeMap.put("titleCertificate", titleCertificate);
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                return resultMap;
            }
            MyUser myUser = (MyUser) baseManager.getObject(MyUser.class.getName(), userId);
            myUser.setName(name);
            myUser.setUsername(username);
            Master master = new Master();
            master.setStatus("1");
            AddressProvince addressProvince = (AddressProvince) baseManager.getObject(AddressProvince.class.getName(), province);
            master.setOriginProvince(addressProvince);
            master.setProvinceName(provinceName);
            master.setArtCategory(artCategory);
            master.setTitleCertificate(titleCertificate);
            baseManager.saveOrUpdate(Master.class.getName(), master);
            baseManager.saveOrUpdate(MyUser.class.getName(), myUser);

        } catch (Exception e) {
            resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMap;
        }
        return resultMap;
    }


    /**
     * 图片上传
     *
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/uploadFile.do", method = RequestMethod.POST)
    public String uploadFile(HttpServletRequest request , String userId , String dataType) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String identify = sdf.format(new Date());
        String url = "";
        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            //上传文件
            MultipartFile mf = entry.getValue();
            String fileName = mf.getOriginalFilename();//获取原文件名
            String prefix = fileName.substring(fileName.indexOf("."), fileName.length());
            if ("jpg".equals(prefix)) {
                url = "app/" + fileName.substring(0, fileName.indexOf(".jpg")) + identify + ".jpg";
            } else if ("png".equals(prefix)) {
                url = "app/" + fileName.substring(0, fileName.indexOf(".png")) + identify + ".png";
            }
            if ("".equals(dataType)) {

            }
            try {
                aliOssUploadManager.uploadFile(mf, "artWork", url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
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
            String userId = jsonObj.getString("userId");
            String index = jsonObj.getString("pageIndex");
            String size = jsonObj.getString("pageSize");
            String timestamp = jsonObj.getString("timestamp");
            if ("".equals(signmsg) || "".equals(userId) || "".equals(timestamp)
                    || "".equals(index) || "".equals(size)) {
                resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                return resultMap;
            }
            treeMap.put("userId", userId);
            treeMap.put("pageIndex", index);
            treeMap.put("pageSize", size);
            treeMap.put("timestamp", timestamp);
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                return resultMap;
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
            String querySql = "FROM ArtworkInvest where creator.id = :userId GROUP BY artwork.id ORDER BY createDatetime DESC";
            XQuery xQuery = new XQuery();
            xQuery.setHql(querySql);
            LinkedHashMap<String, Object> queryMap = new LinkedHashMap<>();
            queryMap.put("userId", userId);
            xQuery.setQueryParamMap(queryMap);
            xQuery.setPageEntity(entity);
            List<ArtworkInvest> invests = baseManager.listObject(xQuery);

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
            xQuery = new XQuery("listInvestReward_default", request);
            xQuery.put("investUser_id", jsonObj.getString("userId"));
            List<InvestReward> investRewards = (List<InvestReward>) baseManager.listObject(xQuery);
            for (InvestReward investReward : investRewards) {
                reward = reward.add(investReward.getReward());
            }
            ConvertArtWork convert = ConvertArtWorkUtil.convert(invests, followedList.size(), toFollowedList.size(), investMoney, sumInvestsMoney, reward, user);
            logBean.setResultCode("0");
            logBean.setMsg("请求成功");
            resultMapHandler.handlerResult("0", "请求成功", logBean);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "成功");
            resultMap.put("pageInfo", convert);
            System.out.print(convert);
        } catch (Exception e) {
            resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMap;
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
                resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                return resultMap;
            }
            String signmsg = jsonObj.getString("signmsg");
            String userId = jsonObj.getString("userId");
            treeMap.put("userId", userId);
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                return resultMap;
            }
            Master master = (Master) baseManager.getObject(Master.class.getName(), userId);
            System.out.println(master);
        } catch (Exception e) {
            resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMap;
        }

        return resultMap;
    }

}
