package com.efeiyi.ec.art.personal.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.ArtUserFollowed;
import com.efeiyi.ec.art.model.FollowUserUtil;
import com.efeiyi.ec.art.model.Master;
import com.efeiyi.ec.art.model.UserBrief;
import com.efeiyi.ec.art.organization.model.MyUser;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.does.model.PageInfo;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.taglib.PageEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Administrator on 2016/2/18.
 */
@Controller
public class ArtUserFollowedController extends BaseController {

    private static Logger logger = Logger.getLogger(ArtUserFollowedController.class);

    @Autowired
    private ResultMapHandler resultMapHandler;

    @ResponseBody
    @RequestMapping(value = "/app/userFollowed.do", method = RequestMethod.POST)
    public Map getUserProfile(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId")) ||
                    "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("flag"))
                    || "".equals(jsonObj.getString("type")) ||
                    "".equals(jsonObj.getString("pageIndex")) || "".equals(jsonObj.getString("pageSize"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }

            String flag = jsonObj.getString("flag");
            String signmsg = jsonObj.getString("signmsg");
            String userId = jsonObj.getString("userId");
            String type = jsonObj.getString("type");
            String index = jsonObj.getString("pageIndex");
            String size = jsonObj.getString("pageSize");
            String otherUserId = jsonObj.getString("otherUserId");
            treeMap.put("userId", userId);
            treeMap.put("type", type);
            treeMap.put("pageIndex", index);
            treeMap.put("pageSize", size);
            treeMap.put("flag", flag);
            if(!StringUtils.isEmpty(otherUserId))
                treeMap.put("otherUserId",otherUserId);
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            if ("1".equals(flag)) {//自己查看自己



                XQuery query = new XQuery("listArtUserFollowed_num", request);
                query.put("user_id", userId);
                query.put("follower_type", type);
                List<ArtUserFollowed> userFollowedList = baseManager.listObject(query);

                XQuery xQuery = new XQuery("plistArtUserFollowed_default", request);
                xQuery.put("user_id", userId);
                xQuery.put("follower_type", type);
                PageEntity entity = new PageEntity();
                entity.setSize(Integer.parseInt(size));
                entity.setIndex(Integer.parseInt(index));
                xQuery.setPageEntity(entity);
                PageInfo pageInfo = baseManager.listPageInfo(xQuery);
                List<ArtUserFollowed> followedList = pageInfo.getList();
                LinkedHashMap<String, Object> paramMap = new LinkedHashMap<String, Object>();
                List<FollowUserUtil> followUserUtils = new ArrayList<FollowUserUtil>();
                if (followedList != null) {
                    for (ArtUserFollowed artUserFollowed : followedList) {//去取签名或头衔  BeanUtils.copyProperties
                        FollowUserUtil followUserUtil = new FollowUserUtil();
                        paramMap.put("userId", artUserFollowed.getFollower().getId());
                        if (type.equals("1")) {
                            List<Master> master = (List<Master>) baseManager.listObject(AppConfig.SQL_GET_MASTER_INFO, paramMap);
                            followUserUtil.setArtUserFollowed(artUserFollowed);
                            if (master != null && !master.isEmpty()) {
                                followUserUtil.setMaster(master.get(0));
                            } else {
                                followUserUtil.setMaster(null);
                            }
                            followUserUtil.setFlag("1");
                            followUserUtils.add(followUserUtil);
                        } else if (type.equals("2")) {
                            List<UserBrief> userBrief = (List<UserBrief>) baseManager.listObject(AppConfig.SQL_GET_USER_SIGNER, paramMap);
                            followUserUtil.setArtUserFollowed(artUserFollowed);
                            if (userBrief != null && !userBrief.isEmpty()) {
                                followUserUtil.setUserBrief(userBrief.get(0));
                            } else {
                                followUserUtil.setUserBrief(new UserBrief());
                            }
                            followUserUtil.setFlag("1");
                            followUserUtils.add(followUserUtil);
                        }

                    }
                }
                resultMapHandler.handlerResult("0", "请求成功", logBean);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "请求成功");
                resultMap.put("followsNum", userFollowedList.size());
                resultMap.put("pageInfoList", followUserUtils);
            } else if ("2".equals(flag)) {//自己查看别人

                XQuery query = new XQuery("listArtUserFollowed_num", request);
                query.put("user_id", otherUserId);
                query.put("follower_type", type);
                List<ArtUserFollowed> userFollowedList = baseManager.listObject(query);

                XQuery xQuery = new XQuery("plistArtUserFollowed_default", request);
                xQuery.put("user_id", otherUserId);
                xQuery.put("follower_type", type);
                PageEntity entity = new PageEntity();
                entity.setSize(Integer.parseInt(size));
                entity.setIndex(Integer.parseInt(index));
                xQuery.setPageEntity(entity);
                PageInfo pageInfo = baseManager.listPageInfo(xQuery);
                List<ArtUserFollowed> followedList = pageInfo.getList();
                LinkedHashMap<String, Object> paramMap = new LinkedHashMap<String, Object>();
                List<FollowUserUtil> followUserUtils = new ArrayList<FollowUserUtil>();
                if(followedList!=null) {
                    for (ArtUserFollowed artUserFollowed : followedList) {//去取签名或头衔  BeanUtils.copyProperties
                        FollowUserUtil followUserUtil = new FollowUserUtil();
                        paramMap.put("userId", userId);
                        paramMap.put("followerId", artUserFollowed.getFollower().getId());
                        String is_followed = "2";
                        ArtUserFollowed followed = null;
                        List<ArtUserFollowed> artUserFollowedList = (List<ArtUserFollowed>) baseManager.listObject(AppConfig.SQL_GET_IS_FOLLOWED, paramMap);
                        if (artUserFollowedList!=null && artUserFollowedList.size()!=0)
                            followed = artUserFollowedList.get(0);
                        if (followed != null && followed.getId() != null) {
                            is_followed = "1";
                        }
                        User user = (User) baseManager.getObject(User.class.getName(), artUserFollowed.getFollower().getId());
                        if (type.equals("1")) {//大师

                            Master master = null;
                            if (user != null) {
                                master = user.getMaster();
                            }

                            followUserUtil.setArtUserFollowed(artUserFollowed);
                            if (master != null) {
                                followUserUtil.setMaster(master);
                            } else {
                                followUserUtil.setMaster(new Master());
                            }
                            followUserUtil.setFlag(is_followed);
                            followUserUtils.add(followUserUtil);

                        } else if (type.equals("2")) {//用户
                            if (user != null)
                                followUserUtil.setUserBrief(user.getUserBrief());
                            else
                                followUserUtil.setUserBrief(new UserBrief());
                            followUserUtil.setArtUserFollowed(artUserFollowed);
                            followUserUtil.setFlag(is_followed);
                            followUserUtils.add(followUserUtil);
                        }

                    }
                }
                resultMapHandler.handlerResult("0", "请求成功", logBean);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "请求成功");
                resultMap.put("followsNum", userFollowedList.size());
                resultMap.put("pageInfoList", followUserUtils);
            }


        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    @ResponseBody
    @RequestMapping(value = "/app/userFans.do", method = RequestMethod.POST)
    public Map userFans(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId")) ||
                    "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("flag"))
                    || "".equals(jsonObj.getString("type")) ||
                    "".equals(jsonObj.getString("pageIndex")) || "".equals(jsonObj.getString("pageSize"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }

            String flag = jsonObj.getString("flag");
            String signmsg = jsonObj.getString("signmsg");
            String userId = jsonObj.getString("userId");
            String type = jsonObj.getString("type");
            String index = jsonObj.getString("pageIndex");
            String size = jsonObj.getString("pageSize");
            String otherUserId = jsonObj.getString("otherUserId");
            treeMap.put("userId", userId);
            treeMap.put("type", type);
            treeMap.put("pageIndex", index);
            treeMap.put("pageSize", size);
            treeMap.put("flag", flag);
            if(!StringUtils.isEmpty(otherUserId))
                treeMap.put("otherUserId",otherUserId);
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            if ("1".equals(flag)) {//自己查看自己

                XQuery query = new XQuery("listArtUserFollowed_fan", request);
                query.put("follower_id", userId);
                query.put("user_type", type);
                List<ArtUserFollowed> userFollowedList = baseManager.listObject(query);

                XQuery xQuery = new XQuery("plistArtUserFollowed_default1", request);
                xQuery.put("follower_id", userId);
                xQuery.put("user_type", type);
                PageEntity entity = new PageEntity();
                entity.setSize(Integer.parseInt(size));
                entity.setIndex(Integer.parseInt(index));
                xQuery.setPageEntity(entity);
                PageInfo pageInfo = baseManager.listPageInfo(xQuery);
                List<ArtUserFollowed> followedList = pageInfo.getList();
                LinkedHashMap<String, Object> paramMap = new LinkedHashMap<String, Object>();
                List<FollowUserUtil> followUserUtils = new ArrayList<FollowUserUtil>();
                if (followedList != null) {
                    for (ArtUserFollowed artUserFollowed : followedList) {//去取签名或头衔  BeanUtils.copyProperties
                        FollowUserUtil followUserUtil = new FollowUserUtil();
                        paramMap.put("userId", artUserFollowed.getUser().getId());
                        if (type.equals("1")) {
                            List<Master> master = (List<Master>) baseManager.listObject(AppConfig.SQL_GET_MASTER_INFO, paramMap);
                            followUserUtil.setArtUserFollowed(artUserFollowed);
                            if (master != null && !master.isEmpty()) {
                                followUserUtil.setMaster(master.get(0));
                            } else {
                                followUserUtil.setMaster(null);
                            }
                            followUserUtil.setFlag("1");
                            followUserUtils.add(followUserUtil);
                        } else if (type.equals("2")) {
                            List<UserBrief> userBrief = (List<UserBrief>) baseManager.listObject(AppConfig.SQL_GET_USER_SIGNER, paramMap);
                            followUserUtil.setArtUserFollowed(artUserFollowed);
                            if (userBrief != null && !userBrief.isEmpty()) {
                                followUserUtil.setUserBrief(userBrief.get(0));
                            } else {
                                followUserUtil.setUserBrief(new UserBrief());
                            }
                            followUserUtil.setFlag("1");
                            followUserUtils.add(followUserUtil);
                        }

                    }
                }
                resultMapHandler.handlerResult("0", "请求成功", logBean);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "请求成功");
                resultMap.put("followsNum", userFollowedList.size());
                resultMap.put("pageInfoList", followUserUtils);
            } else if ("2".equals(flag)) {//自己查看别人

                XQuery query = new XQuery("listArtUserFollowed_fan", request);
                query.put("follower_id", otherUserId);
                query.put("user_type", type);
                List<ArtUserFollowed> userFollowedList = baseManager.listObject(query);

                XQuery xQuery = new XQuery("plistArtUserFollowed_default1", request);
                xQuery.put("follower_id", otherUserId);
                xQuery.put("user_type", type);
                PageEntity entity = new PageEntity();
                entity.setSize(Integer.parseInt(size));
                entity.setIndex(Integer.parseInt(index));
                xQuery.setPageEntity(entity);
                PageInfo pageInfo = baseManager.listPageInfo(xQuery);
                List<ArtUserFollowed> followedList = pageInfo.getList();
                LinkedHashMap<String, Object> paramMap = new LinkedHashMap<String, Object>();
                List<FollowUserUtil> followUserUtils = new ArrayList<FollowUserUtil>();
                if(followedList!=null) {
                    for (ArtUserFollowed artUserFollowed : followedList) {//去取签名或头衔  BeanUtils.copyProperties
                        FollowUserUtil followUserUtil = new FollowUserUtil();
                        paramMap.put("userId", userId);
                        paramMap.put("followerId", artUserFollowed.getUser().getId());
                        String is_followed = "2";
                        ArtUserFollowed followed = null;
                        List<ArtUserFollowed> artUserFollowedList = (List<ArtUserFollowed>) baseManager.listObject(AppConfig.SQL_GET_IS_FOLLOWED, paramMap);
                        if (artUserFollowedList!=null && artUserFollowedList.size()!=0)
                            followed = artUserFollowedList.get(0);
                        if (followed != null && followed.getId() != null) {
                            is_followed = "1";
                        }
                        User user = (User) baseManager.getObject(User.class.getName(), artUserFollowed.getUser().getId());
                        if (type.equals("1")) {//大师

                            Master master = null;
                            if (user != null) {
                                master = user.getMaster();
                            }
                            followUserUtil.setArtUserFollowed(artUserFollowed);
                            if (master != null) {
                                followUserUtil.setMaster(master);
                            } else {
                                followUserUtil.setMaster(null);
                            }
                            followUserUtil.setFlag(is_followed);
                            followUserUtils.add(followUserUtil);

                        } else if (type.equals("2")) {//用户
                            if (user != null)
                                followUserUtil.setUserBrief(user.getUserBrief());
                            else
                                followUserUtil.setUserBrief(new UserBrief());
                            followUserUtil.setArtUserFollowed(artUserFollowed);
                            followUserUtil.setFlag(is_followed);
                            followUserUtils.add(followUserUtil);
                        }

                    }
                }
                resultMapHandler.handlerResult("0", "请求成功", logBean);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "请求成功");
                resultMap.put("followsNum", userFollowedList.size());
                resultMap.put("pageInfoList", followUserUtils);
            }


        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 关注/取消关注
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/app/changeFollowStatus.do", method = RequestMethod.POST)
    public Map changeFollowStatus(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LogBean logBean = new LogBean();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj;
        try {
            jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if (!"".equals(jsonObj.getString("identifier"))) {
                if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("followId"))
                      || "".equals(jsonObj.getString("followType"))) {
                    return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
                }
                String signmsg = jsonObj.getString("signmsg");
                String userId = AuthorizationUtil.getUserId();
                String followId = jsonObj.getString("followId");
                String identifier = jsonObj.getString("identifier");
                String followType = jsonObj.getString("followType");
//                treeMap.put("userId", userId);
                treeMap.put("followId", followId);
                treeMap.put("identifier", identifier);
                treeMap.put("followType", followType);
                treeMap.put("timestamp", jsonObj.getString("timestamp"));
                boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
                if (!verify) {
                    return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
                }
                ArtUserFollowed userFollowed=null;
                XQuery xQuery = new XQuery("listArtUserFollowed_EFollowed",request);
                xQuery.put("user_id",userId);
                xQuery.put("follower_id",followId);
                List<ArtUserFollowed> artUserFollowedList = baseManager.listObject(xQuery);
                if(artUserFollowedList!=null && artUserFollowedList.size()!=0)
                    userFollowed = artUserFollowedList.get(0);
                else
                    userFollowed = new ArtUserFollowed();

                User myUser = (User) baseManager.getObject(User.class.getName(), userId);
                User user = (User) baseManager.getObject(User.class.getName(), followId);
                userFollowed.setUser(myUser);
                userFollowed.setFollower(user);
                userFollowed.setCreateDatetime(new Date());
                userFollowed.setType(followType);
                if ("0".equals(jsonObj.getString("identifier"))) {
                    userFollowed.setStatus("1");
                    baseManager.saveOrUpdate(ArtUserFollowed.class.getName(), userFollowed);
                    resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
                    resultMap.put("flag","1");
                    resultMap.put("artUserFollowed", userFollowed);
                } else if ("1".equals(jsonObj.getString("identifier"))) {
                    userFollowed.setStatus("0");
                    baseManager.saveOrUpdate(ArtUserFollowed.class.getName(), userFollowed);
                    resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
                    resultMap.put("artUserFollowed", userFollowed);
                    resultMap.put("flag","0");
                }
            } else {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    public static void main(String[] args) throws Exception {
        long timestamp = System.currentTimeMillis();
        String signmsg;
//        String pageIndex = "1";
//        String pageSize = "3";
        TreeMap map = new TreeMap();
        map.put("userId", "ieatht97wfw30hfd");
//        map.put("pageIndex", pageIndex);
//        map.put("pageSize", pageSize);
        map.put("timestamp", timestamp);
        map.put("type", "1");
        map.put("pageSize", "11");
        map.put("pageIndex", "1");
        signmsg = DigitalSignatureUtil.encrypt(map);
        map.put("flag", "1");
        map.put("signmsg", signmsg);
        System.out.println(signmsg);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.41:8080/app/userFollowed.do";
        HttpPost httppost = new HttpPost(url);

//        String changeFollowStatus = "{\"userId\":\"1\",\"pageIndex\":\"1\",\"pageSize\":\"3\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
//        String changeFollowStatus = "{\"userId\":\"igxhnwhnmhlwkvnw\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";

//        String json = changeFollowStatus;
        String json = JSONObject.toJSONString(map);
        JSONObject jsonObj = (JSONObject) JSONObject.parse(json);
        String jsonString = jsonObj.toJSONString();

        StringEntity stringEntity = new StringEntity(jsonString, "utf-8");

        httppost.setEntity(stringEntity);
        System.out.println("url:  " + url);
        byte[] b = new byte[(int) stringEntity.getContentLength()];
        stringEntity.getContent().read(b);
        System.out.println("报文:" + new String(b));
        HttpResponse response = httpClient.execute(httppost);
        HttpEntity entity = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                entity.getContent(), "UTF-8"));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        System.out.println(stringBuilder.toString());

    }
}
