package com.efeiyi.ec.art.artwork.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtworkManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.service.UploadImageManager;
import com.efeiyi.ec.art.base.util.*;
import com.efeiyi.ec.art.message.dao.MessageDao;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.modelConvert.ArtWorkInvestBean;
import com.efeiyi.ec.art.modelConvert.ArtWorkInvestTopBean;
import com.efeiyi.ec.art.modelConvert.ArtWorkPraiseBean;
import com.efeiyi.ec.art.modelConvert.UserFollowedBean;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.efeiyi.ec.art.organization.util.CommonUtil;
import com.efeiyi.ec.art.organization.util.TimeUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.does.model.PageInfo;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.p.service.AliOssUploadManager;
import com.ming800.core.taglib.PageEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;

import org.junit.*;

/**
 * Created by Administrator on 2016/1/29.
 */
@Controller
public class ArtworkController extends BaseController {
    private static Logger logger = Logger.getLogger(ArtworkController.class);

    @Autowired
    private MessageDao messageDao;

    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    AliOssUploadManager aliOssUploadManager;

    @Autowired
    private ArtworkManager artworkManager;

    @Autowired
    private UploadImageManager uploadImageManager;

    /**
     *艺术家发起项目列表接口
     */
    @RequestMapping(value = "/app/getArtWorkListByAuthor")
    @ResponseBody
    public Map getArtWorkListByAuthor(HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> resultMap = new HashMap<>();
        try {
            JSONObject jsonObject = JsonAcceptUtil.receiveJson(request);
            String type = jsonObject.getString("type");
            if (null == type || type.equals("")){
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "必要参数为空");
                return resultMap;
            }
            PageEntity pageEntity = new PageEntity();
            pageEntity.setIndex(jsonObject.getInteger("pageIndex"));
            pageEntity.setSize(jsonObject.getInteger("pageSize"));
            User user = AuthorizationUtil.getUser();
            if (type != null && !type.equals("")){
                XQuery xQuery = new XQuery("plistArtwork_defaultByAuthor", request);
                xQuery.put("type", type);
                xQuery.put("author_id", user.getId());
                xQuery.setPageEntity(pageEntity);
                List<Artwork> artworkList = baseManager.listPageInfo(xQuery).getList();
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
                resultMap.put("artworkList", artworkList);
            }else {
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数请求错误");
            }
        }catch (Exception e){
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
        }
        return resultMap;
    }

    @RequestMapping(value = "/app/getArtWorkList.do")
    @ResponseBody
    public Map getArtWorkList(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {

            JSONObject jsonObject = JsonAcceptUtil.receiveJson(request);


            Map<String, Object> paramMap = new HashMap<>();

            List<Artwork> artworkList = null;
            List<ArtWorkPraise> artWorkPraiseList = new ArrayList<>();
            List<ArtworkInvest> artworkInvestList = new ArrayList<>();

            User user = (User) baseManager.getObject(User.class.getName(), jsonObject.getString("userId"));

            PageEntity pageEntity = new PageEntity();
            pageEntity.setIndex(jsonObject.getInteger("pageIndex"));
            pageEntity.setSize(jsonObject.getInteger("pageSize"));


            String type = jsonObject.getString("type");
            String step = jsonObject.getString("step");

            String action = jsonObject.getString("action");


            if ("index".equals(action)) {
                XQuery query = new XQuery("plistArtwork_default", request);
                query.setPageEntity(pageEntity);
                query.put("type", type);
                query.put("step", step);
                artworkList = baseManager.listPageInfo(query).getList();

            } else if ("userMain".equals(action)) {
                XQuery query = new XQuery("plistArtwork_default", request);
                query.setPageEntity(pageEntity);
                query.put("author_id", user.getId());
                artworkList = baseManager.listPageInfo(query).getList();

            } else if ("invest".equals(action)) {
                artworkList = new ArrayList<>();
                LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                params.put("userId", user.getId());
                artworkInvestList = baseManager.listPageInfo(AppConfig.SQL_INVEST_ARTWORK, pageEntity, params).getList();

//                XQuery query = new XQuery("plistArtworkInvest_byUser",request);
//                query.setPageEntity(pageEntity);
//                query.put("creator_id",user.getId());
//                artworkInvestList = baseManager.listPageInfo(query).getList();
                if (artworkInvestList != null && artworkInvestList.size() > 0) {
                    for (ArtworkInvest artworkInvest : artworkInvestList) {
                        Artwork artwork = artworkInvest.getArtwork();
                        //是否点赞
                        Boolean isPraise = false;
                        if (!StringUtils.isEmpty(AuthorizationUtil.getUser())) {
                            XQuery xQuery = new XQuery("listArtWorkPraise_default", request);
                            xQuery.put("artwork_id", artwork.getId());
                            xQuery.put("user_id", AuthorizationUtil.getUser().getId());
                            List<ArtWorkPraise> artWorkPraiseList1 = baseManager.listObject(xQuery);
                            if (artWorkPraiseList1 != null) {
                                if (artWorkPraiseList1.size() > 0) {
                                    isPraise = true;
                                }
                            }
                            artwork.setPraise(isPraise);
                        }
                        artworkList.add(artwork);
                    }
                }
//                //当前用户的投资金额
//                LinkedHashMap<String,Object> investParam = new LinkedHashMap<>();
//                investParam.put("userId",AuthorizationUtil.getUserId());
//                BigDecimal investTotal = (BigDecimal) baseManager.listObject(AppConfig.SQL_INVEST_TOTAL,investParam).get(0);
//                //当前用户的回报金额
//                BigDecimal rewardTotal = (BigDecimal) baseManager.listObject(AppConfig.SQL_REWARD_TOTAL,investParam).get(0);
//                paramMap.put("artworkList",artworkList);
//                paramMap.put("investTotal",investTotal==null?0:investTotal);
//                paramMap.put("rewardTotal",rewardTotal==null?0:rewardTotal);
//                paramMap.put("author",AuthorizationUtil.getUser());
            } else if ("praise".equals(action)) {
                artworkList = new ArrayList<>();
                XQuery query = new XQuery("plistArtWorkPraise_byUser", request);
                query.setPageEntity(pageEntity);
                query.put("user_id", user.getId());
                artWorkPraiseList = baseManager.listPageInfo(query).getList();
                if (artWorkPraiseList != null && artWorkPraiseList.size() > 0) {
                    for (ArtWorkPraise artWorkPraise : artWorkPraiseList) {
                        Artwork artwork = artWorkPraise.getArtwork();
                        //是否点赞
                        Boolean isPraise = false;
                        if (!StringUtils.isEmpty(AuthorizationUtil.getUser())) {
                            XQuery xQuery = new XQuery("listArtWorkPraise_default", request);
                            xQuery.put("artwork_id", artWorkPraise.getArtwork().getId());
                            xQuery.put("user_id", AuthorizationUtil.getUser().getId());
                            List<ArtWorkPraise> artWorkPraiseList1 = baseManager.listObject(xQuery);
                            if (artWorkPraiseList1 != null) {
                                if (artWorkPraiseList1.size() > 0) {
                                    isPraise = true;
                                }
                            }
                            artwork.setPraise(isPraise);
                        }

                        artworkList.add(artwork);
                    }
                }

            }


            paramMap.put("artworkList", artworkList);

            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "成功");
            resultMap.put("data", paramMap);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
        }

        return resultMap;
    }

    //
    @RequestMapping(value = "/app/inform.do")
    @ResponseBody
    public Map isFollowed(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try {

            JSONObject jsonObject = JsonAcceptUtil.receiveJson(request);
            Map<String, Object> paramMap = new HashMap<>();

            //是否关注
            Boolean isFollowed = false;
            if (!StringUtils.isEmpty(AuthorizationUtil.getUser())) {
                XQuery xQuery = new XQuery("listArtUserFollowed_isFollowed", request);
                xQuery.put("user_id", AuthorizationUtil.getUserId());
                xQuery.put("follower_id", jsonObject.getString("userId"));
                List<ArtUserFollowed> artUserFollowedList = baseManager.listObject(xQuery);
                if (artUserFollowedList != null) {
                    if (artUserFollowedList.size() > 0) {
                        isFollowed = true;
                    }
                }
            }
            paramMap.put("isFollowed", isFollowed);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "成功");
            resultMap.put("data", paramMap);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
        }

        return resultMap;
    }

    /**
     * 融资首页 接口
     *
     * @param request
     * @return
     */

    @RequestMapping(value = "/app/investorIndex.do")
    @ResponseBody
    public Map investorIndex(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        List<Artwork> artworkList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("investorIndex");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }

            Map paramMap = new HashMap<>();
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("pageSize", jsonObj.getString("pageSize"));
            treeMap.put("pageIndex", jsonObj.getString("pageIndex"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            String userId = "";
            if (AuthorizationUtil.getUser() != null)
                userId = AuthorizationUtil.getUser().getId();
            System.out.println(userId);
            String hql = "from Artwork WHERE 1=1 and status = '1' and type='1' and step='14' order by createDatetime DESC";
            String sql = "SELECT COUNT(1) FROM ArtWorkPraise m where user.id=:userId and artwork.id=:artworkId and status !='0'";
            artworkList = (List<Artwork>) messageDao.getPageList(hql, (jsonObj.getInteger("pageIndex") - 1) * (jsonObj.getInteger("pageSize")), jsonObj.getInteger("pageSize"));
            List<ArtWorkPraiseBean> objectList = new ArrayList<>();
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("userId", userId);
            for (Artwork artwork : artworkList) {
                artwork.setInvestRestTime(TimeUtil.getDistanceTimes(artwork.getInvestEndDatetime(), new Date()));
//                if(artwork.getPicture_url()!=null) {
//                    artwork.setHeight(ImgUtil.getHeight(artwork.getPicture_url()));
//                    artwork.setWidth(ImgUtil.getWidth(artwork.getPicture_url()));
//                }
                map.put("artworkId", artwork.getId());
                List<Long> count = (List<Long>) baseManager.listObject(sql, map);
                if (count.get(0) == 0) {
                    artwork.setPraise(false);
                } else {
                    artwork.setPraise(true);
                }
            }
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            paramMap.put("artworkList", artworkList);
            if (artworkList != null && !artworkList.isEmpty()) {
                resultMap.put("data", paramMap);
            } else {
                resultMap.put("data", null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 融资项目 项目详情页(项目详情tab)
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/investorArtWorkView.do")
    @ResponseBody
    public MappingJacksonValue investorArtWork(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("investorArtWorkView");
            /*if (!CommonUtil.jsonObject(jsonObj)) {
                return new MappingJacksonValue(resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean));
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return new MappingJacksonValue(resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean));
            }*/

            //项目详情
            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), jsonObj.getString("artWorkId"));
            if (artwork == null) {
                return resultMapHandler.handlerResultType(request, resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
            }
            //增加浏览数
            if (artwork.getViewNum() == null) {
                artwork.setViewNum(1);
            } else {
                artwork.setViewNum(artwork.getViewNum() + 1);
            }
            baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
            //投资人
            List<User> investPeople = null;
            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            params.put("artworkId", jsonObj.getString("artWorkId"));
            List<ArtworkInvest> artworkInvestList = (List<ArtworkInvest>) baseManager.listObject(AppConfig.SQL_INVEST_TOP, params);
            //投资人数
            Integer investNum = 0;
            investNum = artwork.getInvestNum();
            if (artworkInvestList != null) {
                investPeople = new ArrayList<>();
                if (artworkInvestList.size() != 0) {
                    for (ArtworkInvest artworkInvest : artworkInvestList) {
                        investPeople.add(artworkInvest.getCreator());
                    }
                }
            }

            List<UserFollowedBean> userFollowedBeanList = new ArrayList<>();
            //点赞列表
            XQuery xQuery1 = new XQuery("listArtWorkPraise_byArtWorkId", request);
            xQuery1.put("artwork_id", jsonObj.getString("artWorkId"));
            List<ArtWorkPraise> artWorkPraiseList1 = baseManager.listObject(xQuery1);
            UserFollowedBean userFollowedBean = null;
            for (ArtWorkPraise artWorkPraise : artWorkPraiseList1) {
                boolean isFollowed = false;
                userFollowedBean = new UserFollowedBean();
                if (!StringUtils.isEmpty(AuthorizationUtil.getUser())) {
                    XQuery xQuery = new XQuery("listArtUserFollowed_isFollowed", request);
                    xQuery.put("user_id", AuthorizationUtil.getUserId());
                    xQuery.put("follower_id", artWorkPraise.getUser().getId());
                    List<ArtUserFollowed> artUserFollowedList = baseManager.listObject(xQuery);
                    if (artUserFollowedList != null) {
                        if (artUserFollowedList.size() > 0) {
                            isFollowed = true;
                        }
                    }
                }
                userFollowedBean.setUser(artWorkPraise.getUser());
                userFollowedBean.setFollowed(isFollowed);
                userFollowedBeanList.add(userFollowedBean);
            }

            //剩余时间
            Date time = null;
            if (artwork.getInvestStartDatetime() != null) {
                time = TimeUtil.getDistanceTimes(artwork.getInvestEndDatetime(), new Date());
                artwork.setInvestRestTime(time);
            }
            //项目文件
            List<ArtworkAttachment> artworkAttachmentList = artwork.getArtworkAttachment();
//            for (ArtworkAttachment artworkAttachment : artworkAttachmentList){
//                artworkAttachment.setWidth(ImgUtil.getWidth(artworkAttachment.getFileName()));
//                artworkAttachment.setHeight(ImgUtil.getHeight(artworkAttachment.getFileName()));
//            }
            //项目制作过程说明、融资解惑
            Artworkdirection artworkdirection = artwork.getArtworkdirection();

            //是否点赞
            Boolean isPraise = false;
            if (!StringUtils.isEmpty(AuthorizationUtil.getUser())) {
                XQuery xQuery = new XQuery("listArtWorkPraise_default", request);
                xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
                xQuery.put("user_id", AuthorizationUtil.getUser().getId());
                List<ArtWorkPraise> artWorkPraiseList = baseManager.listObject(xQuery);
                if (artWorkPraiseList != null) {
                    if (artWorkPraiseList.size() > 0) {
                        isPraise = true;
                    }
                }
            }

            //是否关注
            Boolean isFollowed = false;
            if (!StringUtils.isEmpty(AuthorizationUtil.getUser())) {
                XQuery xQuery = new XQuery("listArtUserFollowed_isFollowed", request);
                xQuery.put("user_id", AuthorizationUtil.getUserId());
                xQuery.put("follower_id", artwork.getAuthor().getId());
                List<ArtUserFollowed> artUserFollowedList = baseManager.listObject(xQuery);
                if (artUserFollowedList != null) {
                    if (artUserFollowedList.size() > 0) {
                        isFollowed = true;
                    }
                }
            }
            data.put("investPeople", investPeople);
            data.put("artWork", artwork);
            data.put("investNum", investNum);
            data.put("time", time);
            data.put("artworkdirection", artworkdirection);
            data.put("artworkAttachmentList", artworkAttachmentList);
            data.put("artWorkPraiseList", userFollowedBeanList);
            data.put("isPraise", isPraise);
            data.put("isFollowed", isFollowed);
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);

            resultMap.put("object", data);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResultType(request, resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
        }

        return resultMapHandler.handlerResultType(request, resultMap);
    }

    /**
     * 融资项目 项目详情页(评论tab)
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/investorArtWorkComment.do")
    @ResponseBody
    public MappingJacksonValue investorArtWorkComment(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("investorArtWorkComment");
            /*if (!CommonUtil.jsonObject(jsonObj)) {
                return new MappingJacksonValue(resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean));
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return new MappingJacksonValue(resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean));
            }*/

//            评论列表
            List<ArtworkComment> artworkCommentList = null;

            XQuery xQuery = new XQuery("plistArtworkComment_default", request);
            xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
            PageEntity pageEntity = new PageEntity();
            pageEntity.setSize(jsonObj.getInteger("pageSize"));
            pageEntity.setIndex(jsonObj.getInteger("pageIndex"));
            xQuery.setPageEntity(pageEntity);
            artworkCommentList = baseManager.listPageInfo(xQuery).getList();

            data.put("artworkCommentList", artworkCommentList);

            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("object", data);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMapHandler.handlerResultType(request, resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
        }

        return resultMapHandler.handlerResultType(request, resultMap);
    }

    /**
     * 融资项目 项目详情页(投资tab)
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/investorArtWorkInvest.do")
    @ResponseBody
    public MappingJacksonValue investorArtWorkInvest(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        List objectList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("investorArtWorkInvest");
            /*if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }*/

            //投资记录列表
            List<ArtworkInvest> artworkInvestList;

            //投资top
            List<ArtWorkInvestTopBean> artworkInvestTopList = null;

            //temp
            List<ArtworkInvest> artworkInvestTopTempList;


            XQuery xQuery = new XQuery("plistArtworkInvest_byArtWork", request);
            xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
            PageEntity pageEntity = new PageEntity();
            pageEntity.setSize(jsonObj.getInteger("pageSize"));
            pageEntity.setIndex(jsonObj.getInteger("pageIndex"));
            xQuery.setPageEntity(pageEntity);
            artworkInvestList = baseManager.listPageInfo(xQuery).getList();


            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            params.put("artworkId", jsonObj.getString("artWorkId"));
            artworkInvestTopTempList = (List<ArtworkInvest>) baseManager.listObject(AppConfig.SQL_INVEST_TOP, params);
            List<BigDecimal> topMoneyList = (List<BigDecimal>) baseManager.listObject(AppConfig.SQL_INVEST_TOP_MONEY, params);
            if (artworkInvestTopTempList != null) {

                artworkInvestTopList = new ArrayList<>();
                ArtWorkInvestTopBean artWorkInvestTopBean;
                int max = 3;
                if (artworkInvestTopTempList.size() < max) {
                    max = artworkInvestTopTempList.size();
                }
                for (int i = 0; i < max; i++) {
                    artWorkInvestTopBean = new ArtWorkInvestTopBean();
                    artWorkInvestTopBean.setCreator(artworkInvestTopTempList.get(i).getCreator());
                    artWorkInvestTopBean.setPrice(topMoneyList.get(i));
                    artWorkInvestTopBean.setCreateDatetime(artworkInvestTopTempList.get(i).getCreateDatetime());
                    artworkInvestTopList.add(artWorkInvestTopBean);
                }
            }
            data.put("artworkInvestTopList", artworkInvestTopList);
            data.put("artworkInvestList", artworkInvestList);

            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("object", data);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMapHandler.handlerResultType(request, resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
        }

        return resultMapHandler.handlerResultType(request, resultMap);
    }

    /**
     * 点赞列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artworkPraiseList.do", method = RequestMethod.POST)
    @ResponseBody
    public Map artworkPraiseList(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<>();
        List objectList = null;
        try {
            JSONObject jsonObject = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObject.toString());
            logBean.setApiName("artworkPraiseList");
            if (!CommonUtil.jsonObject(jsonObject)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            if (!DigitalSignatureUtil.verify2(jsonObject)) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }


            PageEntity pageEntity = new PageEntity();
            pageEntity.setSize(jsonObject.getInteger("size"));
            pageEntity.setIndex(jsonObject.getInteger("index"));

            XQuery xQuery = new XQuery("plistArtWorkPraise_default", request);
            xQuery.put("artwork_id", jsonObject.getString("artworkId"));
            xQuery.setPageEntity(pageEntity);
            List<ArtWorkPraise> artWorkPraiseList = baseManager.listPageInfo(xQuery).getList();
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("object", artWorkPraiseList);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
        return resultMap;
    }

    /**
     * 点赞
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artworkPraise.do", method = RequestMethod.POST)
    @ResponseBody
    public synchronized Map artworkPraise(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<>();
        List objectList = null;
        try {
            JSONObject jsonObject = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObject.toString());
            logBean.setApiName("artworkPraise");

            Map paramMap = new HashMap();
            String action = jsonObject.getString("action");
            paramMap.put("isPraise", action);
            if ("1".equals(action)) {
                if (!artworkManager.isPointedPraise(request, jsonObject)) {
                    if (artworkManager.saveArtWorkPraise(jsonObject.getString("artworkId"), jsonObject.getString("messageId"))) {
                        if (null != jsonObject.getString("messageId") && !jsonObject.getString("messageId").equals("")) {
                            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
                            resultMap.put("artworkMessage", baseManager.getObject(ArtworkMessage.class.getName(), jsonObject.getString("messageId")));
                        } else {
                            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
                        }

                    } else {
                        return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
                    }
                } else {
                    return resultMapHandler.handlerResult("100020", "点赞失败", logBean);
                }

            } else if ("0".equals(action)) {

                if (artworkManager.cancelArtWorkPraise(request, jsonObject.getString("artworkId"), jsonObject.getString("messageId"))) {
                    if (null != jsonObject.getString("messageId") && !jsonObject.getString("messageId").equals("")) {
                        resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
                        resultMap.put("artworkMessage", baseManager.getObject(ArtworkMessage.class.getName(), jsonObject.getString("messageId")));
                    } else {
                        resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
                    }
                } else {
                    return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
                }
            }

            resultMap.put("data", paramMap);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
        return resultMap;
    }

    /**
     * 取消点赞
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/cancelArtworkPraise.do", method = RequestMethod.POST)
    @ResponseBody
    public Map cancelArtworkPraise(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<>();
        List objectList = null;
        try {
            JSONObject jsonObject = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObject.toString());
            logBean.setApiName("cancelArtworkPraise");
            if (!CommonUtil.jsonObject(jsonObject)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            if (!DigitalSignatureUtil.verify2(jsonObject)) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            if (artworkManager.cancelArtWorkPraise(request, jsonObject.getString("artworkId"), jsonObject.getString("messageId"))) {
                resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            } else {
                return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
        return resultMap;
    }


    @Test
    public void testArtworkPraise() throws Exception {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new TreeMap<>();

        /**investorArtWorkView.do测试加密参数**/
        map.put("artworkId", "qydeyugqqiugd2");
        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        map.put("signmsg", signmsg);
        String url = "http://192.168.1.80:8080/app/investorArtWorkView.do";
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

    /**
     * 评论
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artworkComment.do")
    @ResponseBody
    public MappingJacksonValue artworkComment(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        List objectList = null;

        try {
            JSONObject jsonObject = JsonAcceptUtil.receiveJson(request);

            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObject.toString());
            logBean.setApiName("artworkComment");

            map = artworkManager.saveArtWorkComment(jsonObject.getString("artworkId"), jsonObject.getString("content"),
                    jsonObject.getString("fatherCommentId"), jsonObject.getString("messageId"));

            if (map.get("resultCode").equals("0")) {
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
                resultMap.put("artworkComment", map.get("artworkComment"));
            } else {
                return resultMapHandler.handlerResultType(request, resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResultType(request, resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
        }
        return resultMapHandler.handlerResultType(request, resultMap);
    }

    /**
     * 艺术家页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/masterView.do", method = RequestMethod.POST)
    @ResponseBody
    public Map masterView(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        List objectList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("masterView");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("masterId", jsonObj.getString("masterId"));
            treeMap.put("pageSize", jsonObj.getString("pageSize"));
            treeMap.put("pageIndex", jsonObj.getString("pageIndex"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            //艺术家个人介绍
            Master master = (Master) baseManager.getObject(Master.class.getName(), jsonObj.getString("masterId"));
            //关注人数
            XQuery xQuery = new XQuery("listArtUserFollowed_default", request);
            xQuery.put("follower_id", master.getUser().getId());
            Integer followedNum = baseManager.listObject(xQuery).size();

//            xQuery = new XQuery("listArtwork_default",request);
//            xQuery.put("author_id",master.getUser().getId());
//            List<Artwork> artworks = (List<Artwork>)baseManager.listObject(xQuery);

            xQuery = new XQuery("plistArtwork_default1", request);
            xQuery.put("author_id", master.getUser().getId());
            xQuery.getPageEntity().setSize(jsonObj.getInteger("pageSize"));
            xQuery.getPageEntity().setIndex(jsonObj.getInteger("pageIndex"));
            PageInfo pageInfo = baseManager.listPageInfo(xQuery);
            List<Artwork> artworks = (List<Artwork>) pageInfo.getList();
            //投资者
            Integer investsNum = 0;
            //融资金额
            BigDecimal investsMoney = new BigDecimal(0);
            for (Artwork artwork : artworks) {
                investsNum += artwork.getArtworkInvests().size();
                investsMoney = investsMoney.add(artwork.getInvestsMoney());
            }
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("master", master);
            resultMap.put("artWorkList", artworks);
            resultMap.put("investsNum", investsNum);
            resultMap.put("investsMoney", investsMoney);
            resultMap.put("followedNum", followedNum);
        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 游客页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/guestView.do", method = RequestMethod.POST)
    @ResponseBody
    public Map guestView(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        List objectList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("guestView");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("userId", jsonObj.getString("userId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            //游客信息
            User user = (User) baseManager.getObject(User.class.getName(), jsonObj.getString("userId"));
            //
            XQuery xQuery = new XQuery("listArtworkInvest_default", request);
            xQuery.put("creator_id", jsonObj.getString("userId"));
            List<ArtworkInvest> artworkInvests = (List<ArtworkInvest>) baseManager.listObject(xQuery);
            //查询数据参数
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("userId", jsonObj.getString("userId"));
            //投资项目
            List<ArtworkInvest> artworkInvests1 = (List<ArtworkInvest>) baseManager.listObject(AppConfig.SQL_INVEST_ARTWORK_APP, map);
            List<BigDecimal> investMoney = (List<BigDecimal>) baseManager.listObject(AppConfig.SQL_INVEST_MONEY_APP, map);
            List<ArtWorkInvestBean> artworks = new ArrayList<>();
            for (int i = 0; i < artworkInvests1.size(); i++) {
                ArtWorkInvestBean artWorkInvestBean = new ArtWorkInvestBean();
                artWorkInvestBean.setArtwork(artworkInvests1.get(i).getArtwork());
                artWorkInvestBean.setInvestMoney(investMoney.get(i));
                //    artWorkInvestBean.setMaster((Master)baseManager.getObject(Master.class.getName(),artworkInvests1.get(i).getArtwork().getAuthor().getId()));
                artworks.add(artWorkInvestBean);
            }
            //投资金额
            BigDecimal investsMoney = new BigDecimal(0);
            for (ArtworkInvest artworkInvest : artworkInvests) {
                investsMoney = investsMoney.add(artworkInvest.getPrice());
            }

            //投资回报
            BigDecimal reward = new BigDecimal(0);
            xQuery = new XQuery("listROIRecord_default", request);
            xQuery.put("user_id", jsonObj.getString("userId"));
            List<ROIRecord> roiRecordList = (List<ROIRecord>) baseManager.listObject(xQuery);
            for (ROIRecord roiRecord : roiRecordList) {
                reward = reward.add(roiRecord.getCurrentBalance());
            }


            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("user", user);
            resultMap.put("artworks", artworks);
            resultMap.put("investsMoney", investsMoney);
            resultMap.put("reward", reward);
        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 艺术家发起新的项目接口 一
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/initNewArtWork.do", method = RequestMethod.POST)
    @ResponseBody
    public Map initNewArtWork(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(request.getParameter("title") + " " + request.getParameter("brief"));//************记录请求报文
            logBean.setApiName("initNewArtWork");

            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            String artworkId = jsonObj.getString("artworkId");
            String title = jsonObj.getString("title");//标题
            String material = jsonObj.getString("material");//类型
            String brief = jsonObj.getString("brief");//简介
            String duration = jsonObj.getString("duration");//创作时长（天）
            String makeInstru = jsonObj.getString("makeInstru");//制作说明
            String financingAq = jsonObj.getString("financingAq");//资讯解惑
            String description = jsonObj.getString("description");//描述(详细介绍)
            //String artworkDirectionId = jsonObj.getString("artworkDirectionId");//项目创作过程及融资解惑

            String investGoalMoney = jsonObj.getString("investGoalMoney");//融资目标金额
            if(investGoalMoney == null) {
                return resultMapHandler.handlerResult("10005", "融资总额不能为空", logBean);
            }

            BigDecimal money = new BigDecimal(investGoalMoney);

            if(money.compareTo(BigDecimal.valueOf(100)) == -1) {
                return resultMapHandler.handlerResult("10006", "融资总额不能小于100元", logBean);
            }
            if(investGoalMoney.length() > 15) {
                return resultMapHandler.handlerResult("10006", "目前暂不支持如此大的融资额度", logBean);
            }

            String identification = jsonObj.getString("identification");//标识：“000”代表不校验存入，“111”校验存入

            if (artworkId.equals("")){//新录入
                Artwork artwork = new Artwork();
                artwork = artworkManager.saveOrUpdateArtwork(artwork, title, material, brief, investGoalMoney, duration, makeInstru, financingAq, description);
                artwork.setStatus("2");
                artwork.setType("0");
                artwork.setBuffer("yes");
                artwork.setStep("100");
                baseManager.saveOrUpdate(Artwork.class.getName(), artwork);

                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
                resultMap.put("artwork", artwork);
            }else {
                Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), artworkId);
                artwork = artworkManager.saveOrUpdateArtwork(artwork, title, material, brief, investGoalMoney, duration, makeInstru, financingAq, description);
//                if (!financingAq.equals("")){
//                    artwork.setStatus("1");
//                    baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
//                }
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
                resultMap.put("artwork", artwork);
            }

            return resultMap;
            } catch (Exception e) {
                e.printStackTrace();
                return resultMapHandler.handlerResult("10004", "未知错误,请联系管理员", logBean);
            }

    }

    /**
     * 提交项目审核接口
     */
    @RequestMapping(value = "/app/submitNewArtWork.do", method = RequestMethod.POST)
    @ResponseBody
    public Map submitNewArtWork(HttpServletRequest request){
        Map<String, Object> resultMap = new HashMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            String artworkId = jsonObj.getString("artworkId");
            String financingAq = jsonObj.getString("financingAq");
            if (artworkId.equals("") || financingAq.equals("")){
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "请求参数有误");
                return resultMap;
            }
            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), artworkId);
            if (artwork.getTitle().equals("")||artwork.getMaterial().equals("")||artwork.getBrief().equals("")||artwork.getInvestGoalMoney().equals("")||artwork.getDuration().equals("")||artwork.getPicture_url().equals("")){
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "请求参数有误");
                return resultMap;
            }else {
                if (artwork.getArtworkdirection()==null || artwork.getArtworkAttachment().size()<1 || artwork.getArtworkAttachment().size()>8){
                    resultMap.put("resultCode", "10002");
                    resultMap.put("resultMsg", "请求参数有误");
                    return resultMap;
                }else {
                    if (artwork.getArtworkdirection().getMake_instru().equals("")){
                        resultMap.put("resultCode", "10002");
                        resultMap.put("resultMsg", "请求参数有误");
                        return resultMap;
                    }else {
                        //校验完成的操作
                        Artworkdirection artworkdirection = artwork.getArtworkdirection();
                        artworkdirection.setFinancing_aq(financingAq);
                        baseManager.saveOrUpdate(Artworkdirection.class.getName(), artworkdirection);
                        artwork.setStatus("1");
                        artwork.setStep("10");
                        baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
                        resultMap.put("resultCode", "0");
                        resultMap.put("resultMsg", "成功");
                    }
                }
            }
        }catch (Exception e){
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误,请联系管理员");
        }
        return resultMap;
    }


    /**
     * 获取当前用户缓存项目接口
     */
    @RequestMapping(value = "/app/getBufferedArtwork.do", method = RequestMethod.POST)
    @ResponseBody
    public Map getBufferedArtwork(HttpServletRequest request){
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            logBean.setCreateDate(new Date());
            logBean.setApiName("getBufferedArtwork");

            String userId = AuthorizationUtil.getUser().getId();

            /*String hql = "select s from com.efeiyi.ec.art.model.Artwork s where s.author.id = :userId and s.step = :step and s.status = :status";
            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            params.put("userId", userId);
            params.put("step", "100");
            params.put("status", "2");*/
            XQuery xQuery = new XQuery("listArtwork_byBuffered", request);
            xQuery.put("author_id", userId);
            List artworkList = baseManager.listObject(xQuery);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "成功");
            if (artworkList.size()>0){
                resultMap.put("artwork", artworkList.get(0));
            }else {
                resultMap.put("artwork",null);
            }
        }catch (Exception e){
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误,请联系管理员");

        }
        return resultMap;
    }

    /**
     * 上传创建项目图片接口
     */
    @RequestMapping(value = "/app/uploadArtworkPicture.do", method = RequestMethod.POST)
    @ResponseBody
    public Map uploadArtworkPicture(HttpServletRequest request){
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            logBean.setCreateDate(new Date());//操作时间
            logBean.setApiName("uploadArtworkPicture");

            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            String artworkId = jsonObj.getString("artworkId");
            String type = jsonObj.getString("type");//111表示是封面上传，000表示概念图（附件）上传
            if (type.equals("")){
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "传输参数有误");
                return resultMap;
            }
            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), artworkId);
            List<Map<String, Object>> list = uploadImageManager.uplaodImage(request);
            if (type.equals("000")){
                List<ArtworkAttachment> artworkAttachments = new ArrayList<>();
                for (Map<String, Object> map : list){
                    ArtworkAttachment artworkAttachment = new ArtworkAttachment();
                    artworkAttachment.setArtwork(artwork);
                    artworkAttachment.setFileType(map.get("pictureUrl").toString().substring(map.get("pictureUrl").toString()
                            .lastIndexOf("."), map.get("pictureUrl").toString().length()));
                    artworkAttachment.setFileName(map.get("pictureUrl").toString());
                    artworkAttachment.setWidth(Integer.parseInt(map.get("width").toString()));
                    artworkAttachment.setHeight(Integer.parseInt(map.get("height").toString()));
                    artworkAttachment.setStatus("1");
                    baseManager.saveOrUpdate(ArtworkAttachment.class.getName(), artworkAttachment);
                    artworkAttachments.add(artworkAttachment);
                }
                resultMap.put("artworkAttachmentList", artworkAttachments);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
            }else if (type.equals("111")){
                artwork.setPicture_url(list.get(0).get("pictureUrl").toString());
                artwork.setWidth(Integer.parseInt(list.get(0).get("width").toString()));
                artwork.setHeight(Integer.parseInt(list.get(0).get("height").toString()));
                baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
                resultMap.put("artwork", artwork);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
            }else {
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "传输参数有误");
                return resultMap;
            }
        }catch (Exception e){
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误,请联系管理员");
        }
        return resultMap;
    }

    /**
     * 单张删除创建项目图片接口
     */
    @RequestMapping(value = "/app/deleteArtworkPicture.do", method = RequestMethod.POST)
    @ResponseBody
    public Map deleteArtworkPicture(HttpServletRequest request){
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            logBean.setCreateDate(new Date());//操作时间
            logBean.setApiName("deleteArtworkPicture");

            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            String artworkAttachmentId = jsonObj.getString("artworkAttachmentId");
            String artworkId = jsonObj.getString("artworkId");
            String type = jsonObj.getString("type");//111表示是封面删除，000表示概念图（附件）删除
            if (type.equals("")){
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "请求参数有误");
                return resultMap;
            }
            if (type.equals("000")){
                if (artworkAttachmentId.equals("")){
                    resultMap.put("resultCode", "10002");
                    resultMap.put("resultMsg", "请求参数有误");
                    return resultMap;
                }
                ArtworkAttachment artworkAttachment = (ArtworkAttachment) baseManager.getObject(ArtworkAttachment.class.getName(), artworkAttachmentId);
                artworkAttachment.setStatus("0");
                baseManager.saveOrUpdate(ArtworkAttachment.class.getName(), artworkAttachment);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
            }else if (type.equals("111")){
                if (artworkId.equals("")){
                    resultMap.put("resultCode", "10002");
                    resultMap.put("resultMsg", "请求参数有误");
                    return resultMap;
                }
                Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), artworkId);
                artwork.setPicture_url("");
                baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
            }else {
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "请求参数有误");
                return resultMap;
            }
        }catch (Exception e){
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误,请联系管理员");
        }
        return resultMap;
    }

    /**
     * 艺术家发起新的项目接口 二
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/initNewArtWork2.do", method = RequestMethod.POST)
    @ResponseBody
    public Map initNewArtWork2(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {

            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(request.getParameter("artworkId") + " " + request.getParameter("description")
                    + " " + request.getParameter("timestamp") + " " + request.getParameter("signmsg"));//************记录请求报文
            logBean.setApiName("initNewArtWork2");
            if ("".equals(request.getParameter("signmsg")) || "".equals(request.getParameter("timestamp"))
                    || "".equals(request.getParameter("description"))
                    || "".equals(request.getParameter("artworkId"))
                    ) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
//            String signmsg = request.getParameter("signmsg");
//            treeMap.put("artworkId", request.getParameter("artworkId"));
//            treeMap.put("timestamp", request.getParameter("timestamp"));
//            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
//            if (verify != true) {
//                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
//            }

//            String [] actions = request.getParameterValues("actions");
//            String [] attachmentIds = request.getParameterValues("attachmentIds");
            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), request.getParameter("artworkId"));
            System.out.println(request.getParameter("artworkId"));
            try {
                Artworkdirection artworkdirection = null;
                if (artwork != null && artwork.getId() != null) {
                    if (artwork.getArtworkdirection() != null)
                        artworkdirection = artwork.getArtworkdirection();
                    else
                        artworkdirection = new Artworkdirection();

                    artworkdirection.setFinancing_aq(request.getParameter("financing_aq"));
                    artworkdirection.setMake_instru(request.getParameter("make_instru"));
                    artworkdirection.setArtwork(artwork);

                    //List<ArtworkAttachment> artworkAttachments = new ArrayList<ArtworkAttachment>();

                    baseManager.saveOrUpdate(Artworkdirection.class.getName(), artworkdirection);
                    artwork.setDescription(request.getParameter("description"));
                    artwork.setArtworkdirection(artworkdirection);
                    List<ArtworkAttachment> artworkAttachmentList = artwork.getArtworkAttachment();
                    List<String> urlList = new ArrayList<>();
                    //创建一个通用的多部分解析器
                    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
                    //判断 request 是否有文件上传,即多部分请求
                    if (multipartResolver.isMultipart(request)) {
                        //转换成多部分request
                        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                        //取得request中的所有文件名
                        Iterator<MultipartFile> iter = multiRequest.getFiles("file").iterator();
                        int i = 0;
                        while (iter.hasNext()) {
                            //取得上传文件
                            //MultipartFile file = multiRequest.getFile(iter.next());

                            MultipartFile file = iter.next();
                            if (file != null) {
                                //取得当前上传文件的文件名称
                                String myFileName = file.getOriginalFilename();
                                //如果名称不为“”,说明该文件存在，否则说明该文件不存在
                                if (myFileName.trim() != "") {
                                    //重命名上传后的文件名
                                    String url = "artwork/" + System.currentTimeMillis() + myFileName;
                                    String pictureUrl = "http://rongyitou2.efeiyi.com/" + url;
                                    //将图片上传至阿里云
                                    aliOssUploadManager.uploadFile(file, "ec-efeiyi2", url);
                                    urlList.add(pictureUrl);
                                    //artworkAttachments.add(artworkAttachment);
//                                        artwork.getArtworkAttachment().add(artworkAttachment);
                                }

                            }
                        }
                    }
                    if (artworkAttachmentList != null) {
                        for (ArtworkAttachment artworkAttachment : artworkAttachmentList) {
                            baseManager.delete(ArtworkAttachment.class.getName(), artworkAttachment.getId());
                        }
                    }
                    if (urlList.size() != 0) {
                        for (String url : urlList) {
                            ArtworkAttachment artworkAttachment = null;
                            artworkAttachment = new ArtworkAttachment();//项目附件
                            artworkAttachment.setArtwork(artwork);
                            artworkAttachment.setFileType(url.substring(url.lastIndexOf("."), url.length()));
                            artworkAttachment.setFileName(url);
                            baseManager.saveOrUpdate(ArtworkAttachment.class.getName(), artworkAttachment);
                        }
                    }
                    //artwork.setArtworkAttachment(artworkAttachments);
                    baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
                    resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
                    resultMap.put("artworkId", artwork.getId());
                    return resultMap;

                } else {
                    return resultMapHandler.handlerResult("10008", "查无数据，稍后再试", logBean);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return resultMapHandler.handlerResult("10005", "查询数据出现异常", logBean);
            }


        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

    }


    /**
     * 艺术家变更项目状态
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/updateArtWork.do", method = RequestMethod.POST)
    @ResponseBody
    public Map updateArtWork(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        TreeMap treeMap = new TreeMap();
        try {
            logBean.setCreateDate(new Date());//操作时间
            logBean.setApiName("updateArtWork");
            JSONObject jsonObject = JsonAcceptUtil.receiveJson(request);
            if ("".equals(jsonObject.getString("signmsg"))
                    || "".equals(jsonObject.getString("timestamp"))
                    || "".equals(jsonObject.getString("userId"))
                    || "".equals(jsonObject.getString("artworkId"))
                    || "".equals(jsonObject.getString("step"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObject.getString("signmsg");
            treeMap.put("userId", jsonObject.getString("userId"));
            treeMap.put("timestamp", jsonObject.getString("timestamp"));
            treeMap.put("artworkId", jsonObject.getString("artworkId"));
            treeMap.put("step", jsonObject.getString("step"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), jsonObject.getString("artworkId"));
            if (!jsonObject.getString("userId").equals(artwork.getAuthor().getId())) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            artwork.setStep(jsonObject.getString("step"));
            artwork.setStatus("1");
            baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
            return resultMapHandler.handlerResult("0", "成功", logBean);
        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
    }

    /**
     * 艺术家发布项目动态接口
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/releaseArtworkDynamic.do", method = RequestMethod.POST)
    @ResponseBody
    public Map releaseArtworkDynamic(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        JSONObject jsonObj = null;
        String tmpFileUrl = "";

        try{
            jsonObj = JsonAcceptUtil.receiveJson(request);//入参
        } catch (Exception e) {
            e.printStackTrace();
            return  resultMapHandler.handlerResult("100010","获取传入参数失败",logBean);
        }

        try {

            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), jsonObj.getString("artworkId"));
            try {
                if (artwork != null && artwork.getId() != null) {

                    ArtworkMessage artworkMessage = new ArtworkMessage();
                    artworkMessage.setContent(!"".equals(jsonObj.getString("content")) ? jsonObj.getString("content") : "");
                    artworkMessage.setCreator(artwork.getAuthor());
                    artworkMessage.setArtwork(artwork);
                    artworkMessage.setStatus("1");
                    artworkMessage.setCreateDatetime(new Date());
                    baseManager.saveOrUpdate(ArtworkMessage.class.getName(), artworkMessage);
                    //List<ArtworkMessageAttachment> artworkMessageAttachments =  new ArrayList<ArtworkMessageAttachment>();//动态附件有可能是多个文件
                    //创建一个通用的多部分解析器
                    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
                    //判断 request 是否有文件上传,即多部分请求
                    if (multipartResolver.isMultipart(request)) {
                        //转换成多部分request
                        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                        //取得request中的所有文件名
                        Iterator<String> iter = multiRequest.getFileNames();
                        while (iter.hasNext()) {
                            //取得上传文件
                            MultipartFile file = multiRequest.getFile(iter.next());
                            if (file != null) {
                                ArtworkMessageAttachment artworkMessageAttachment = new ArtworkMessageAttachment();//动态附件
                                //取得当前上传文件的文件名称
                                String myFileName = file.getOriginalFilename();
                                //如果名称不为“”,说明该文件存在，否则说明该文件不存在
                                if (myFileName.trim() != "") {
                                    //重命名上传后的文件名
                                    StringBuilder url = new StringBuilder("artwork/");
                                    String fileType = jsonObj.getString("type");

                                    if ("0".equals(fileType)) {
                                        url.append("picture/" + new Date().getTime() + myFileName);
                                    } else if ("1".equals(fileType)) {
                                        url.append("video/" + new Date().getTime() + myFileName);
                                    } else {
                                        url.append(new Date().getTime() + myFileName);
                                    }

                                    String pictureUrl = "http://rongyitou2.efeiyi.com/" + url.toString();
                                    tmpFileUrl = pictureUrl;
                                    //将图片上传至阿里云
                                    aliOssUploadManager.uploadFile(file, "ec-efeiyi2", url.toString());

                                    //上传视频文件的第一帧
                                    String videoPicture = jsonObj.getString("pictureUrl[][pictureUrl]");
                                    if("1".equals(fileType) && videoPicture != null) {
                                        artworkMessageAttachment.setVideoPicture(videoPicture);
                                    } else if("1".equals(fileType)) {
                                        artworkMessageAttachment.setVideoPicture("http://rongyitou2.efeiyi.com/artwork/picture/1473666549651dynamicImage1.jpg");
                                    }

                                    artworkMessageAttachment.setFileUri(pictureUrl);
                                    artworkMessageAttachment.setArtworkMessage(artworkMessage);
                                    artworkMessageAttachment.setFileType(fileType);
                                    baseManager.saveOrUpdate(ArtworkMessageAttachment.class.getName(), artworkMessageAttachment);
                                }
                            }
                        }
                    }
                    resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
                    resultMap.put("artworkId", artwork.getId());
                    resultMap.put("artworkMessageId", artworkMessage.getId());
                    resultMap.put("nowDate", new Date().getTime());
                    resultMap.put("fileUrl", tmpFileUrl);
                    return resultMap;

                } else {
                    return resultMapHandler.handlerResult("10008", "查无数据，稍后再试", logBean);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return resultMapHandler.handlerResult("10005", "查询数据出现异常", logBean);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

    }

    /**
     * 项目完成接口
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artworkComplete.do", method = RequestMethod.POST)
    @ResponseBody
    public Map artworkComplete(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(request.getParameter("artworkId"));//************记录请求报文
            logBean.setApiName("artworkComplete");
            if ("".equals(request.getParameter("signmsg")) || "".equals(request.getParameter("timestamp"))

                    || "".equals(request.getParameter("artworkId"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = request.getParameter("signmsg");
            treeMap.put("artworkId", request.getParameter("artworkId"));
            treeMap.put("timestamp", request.getParameter("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }


            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), request.getParameter("artworkId"));
            try {
                if (artwork != null && artwork.getId() != null) {

                    //创建一个通用的多部分解析器
                    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
                    //判断 request 是否有文件上传,即多部分请求
                    if (multipartResolver.isMultipart(request)) {
                        //转换成多部分request
                        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                        //取得request中的所有文件名
                        Iterator<String> iter = multiRequest.getFileNames();
                        int i = 0;
                        while (iter.hasNext()) {
                            //取得上传文件
                            MultipartFile file = multiRequest.getFile(iter.next());
                            if (file != null) {
                                //取得当前上传文件的文件名称
                                String myFileName = file.getOriginalFilename();
                                //如果名称不为“”,说明该文件存在，否则说明该文件不存在
                                if (myFileName.trim() != "") {
                                    //重命名上传后的文件名
                                    String url = "artwork/" + System.currentTimeMillis() + myFileName;
                                    String pictureUrl = "http://rongyitou2.efeiyi.com/" + url;
                                    //将图片上传至阿里云
                                    aliOssUploadManager.uploadFile(file, "ec-efeiyi2", url);
                                    if (i == 0) {
                                        artwork.setPicture_url(pictureUrl);
                                        artwork.setHeight(ImageIO.read(file.getInputStream()).getHeight());
                                        artwork.setWidth(ImageIO.read(file.getInputStream()).getWidth());
                                    } else if (i == 1) {
                                        artwork.setPictureBottom(pictureUrl);
                                    } else if (i == 2) {
                                        artwork.setPictureSide(pictureUrl);
                                    }
                                    baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
                                }
                                i++;
                            }
                        }
                    }
                    artwork.setStep("23");
                    baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
                    resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
                    resultMap.put("artwork", artwork);
                    return resultMap;

                } else {
                    return resultMapHandler.handlerResult("10008", "查无数据，稍后再试", logBean);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return resultMapHandler.handlerResult("10005", "查询数据出现异常", logBean);
            }


        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

    }

    /**
     * 项目进展动态查询接口
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artworkProgress.do", method = RequestMethod.POST)
    @ResponseBody
    public Map artworkProgress(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap;
        TreeMap treeMap = new TreeMap();
        try {
            String artworkId = request.getParameter("artworkId");
            String timestamp = request.getParameter("timestamp");
            String signmsg = request.getParameter("signmsg");

            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(artworkId + " ...");//************记录请求报文
            logBean.setApiName("artworkProgress");
            if ("".equals(signmsg)
                    || "".equals(timestamp)
                    || "".equals(signmsg)
                    ) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            treeMap.put("artworkId", artworkId);
            treeMap.put("timestamp", request.getParameter("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), artworkId);
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("artwork", artwork);
            resultMap.put("investTimes", artwork.getArtworkInvests().size());
            resultMap.put("artworkMessages", artwork.getArtworkMessages());
            resultMap.put("artworkBidding", artwork.getArtworkBiddings());
        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
        return resultMap;
    }

    public static void main(String[] arg) throws Exception {


        String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<String, Object>();

        /**investorIndex.do测试加密参数**/
        map.put("pageSize", "10");
        map.put("pageNum", "1");
        map.put("timestamp", timestamp);
        /**investorArtWorkView.do测试加密参数**/
//        map.put("artWorkId", "qydeyugqqiugd2");
////        map.put("messageId","2");
//        map.put("pageSize", "4");
//        map.put("pageIndex", "1");
//        map.put("timestamp", timestamp);

        /**masterView.do测试加密参数**/
//        map.put("pageSize","3");
//        map.put("pageNum","1");
//        map.put("masterId","icjxkedl0000b6i0");
//        map.put("timestamp", timestamp);
        /**guestView.do测试加密参数**/
//        map.put("userId","icjxkedl0000b6i0");
//        map.put("timestamp", timestamp);

        /**artworkPraise.do测试加密参数**/
//        map.put("artWorkId","qydeyugqqiugd2");
//        map.put("currentUserId","iih8wrlm31r449bh");
//        map.put("messageId","2");
//        map.put("timestamp", timestamp);

        /**artworkComment.do测试加密参数**/
//        map.put("artWorkId","qydeyugqqiugd2");
//        map.put("currentUserId","iih8wrlm31r449bh");
//        map.put("fatherCommentId","3");
//        map.put("content","同意+1");
//        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.75:8080/app/investorIndex.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  investorArtWork.do测试 **/
        String json = "{\"pageNum\":\"1\",\"pageSize\":\"10\",\"userId\":\"imhfp1yr4636pj49\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
//        String json = "{\"currentUserId\":\"iickhknq3h7yrku2\",\"artWorkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  investorIndex.do测试 **/
//        String json = "{\"pageSize\":\"3\",\"pageNum\":\"1\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  guestView.do测试 **/
//        String json = "{\"userId\":\"icjxkedl0000b6i0\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  masterView.do测试 **/
//        String json = "{\"masterId\":\"icjxkedl0000b6i0\",\"pageSize\":\"3\",\"pageNum\":\"1\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  artworkPraise.do测试 **/
//        String json = "{\"currentUserId\":\"iih8wrlm31r449bh\",\"artWorkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";

        /**json参数  artworkPraise.do测试 **/
//        String json = "{\"content\":\"同意+1\",\"fatherCommentId\":\"3\",\"currentUserId\":\"iih8wrlm31r449bh\",\"artWorkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        JSONObject jsonObj = (JSONObject) JSONObject.parse(json);
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
            System.out.println(stringBuilder);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 项目进展动态查询接口测试
     *
     * @throws Exception
     */
    @Test
    public void testArtworkProgress() throws Exception {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new TreeMap<>();

        /**investorArtWorkView.do测试加密参数**/
        map.put("artworkId", "qydeyugqqiugd2");
        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("artworkId", "qydeyugqqiugd2"));
        params.add(new BasicNameValuePair("timestamp", Long.toString(timestamp)));
        params.add(new BasicNameValuePair("signmsg", signmsg));
        String url = "http://192.168.1.41:8085/app/artworkProgress.do?" + URLEncodedUtils.format(params, HTTP.UTF_8);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json;charset=utf-8");
        System.out.println("url:  " + url);
        try {
            HttpResponse response = httpClient.execute(httpGet);
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

    @RequestMapping(value = "/app/removeMasterWork.do", method = RequestMethod.POST)
    @ResponseBody
    public Map removeMasterWork(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("removeMasterWork");
            if ("".equals(jsonObj.getString("signmsg"))
                    || "".equals(jsonObj.getString("timestamp"))
                    || "".equals(jsonObj.getString("userId"))
                    || "".equals(jsonObj.getString("masterWorkId"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("userId", jsonObj.getString("userId"));
            treeMap.put("masterWorkId", jsonObj.getString("masterWorkId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            baseManager.remove(MasterWork.class.getName(), jsonObj.getString("masterWorkId"));
            return resultMapHandler.handlerResult("0", "成功", logBean);
        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
    }


    @RequestMapping(value = "/app/updateCreationStatus.do", method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public Map updateCreationStatus(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("updateCreationStatus");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }

            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), jsonObj.getString("id"));
            String type = jsonObj.getString("type");
            if ("invest".equals(type)) {
                if (artwork.getInvestsMoney().compareTo(artwork.getInvestGoalMoney()) >= 0 && "1".equals(artwork.getType())) {
                    artwork.setType("2");
                    artwork.setStep("21");
                    baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
                }
            } else if ("auction".equals(type)) {
                artwork.setType("3");
                XQuery xQuery = new XQuery("listArtworkBidding_default", request);
                xQuery.put("artwork_id", artwork.getId());
                List<ArtworkBidding> artworkBiddingList = baseManager.listObject(xQuery);
                if (artworkBiddingList != null && artworkBiddingList.size() > 0) {
                    artwork.setStep("32");
                    artwork.setWinner(artworkBiddingList.get(0).getCreator());
                    artworkManager.saveAuctionOrder(artwork);
                    artworkManager.returnMargin(artwork.getId(), artwork.getWinner().getId());
                } else {
                    artwork.setStep("33");
                    artworkManager.returnMargin(artwork.getId(), null);
                    artworkManager.returnInvestmentFunds(artwork);
                }
                baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
            }
            return resultMapHandler.handlerResult("0", "成功", logBean);
        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
    }

    @RequestMapping({"/app/artWorkNum.do"})
    @ResponseBody
    public MappingJacksonValue artWorkNum(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resultMap;
        LogBean logBean = new LogBean();
        MappingJacksonValue mappingJacksonValue;
        try {
            LinkedHashMap<String, Object> queryParam = new LinkedHashMap<>();
            queryParam.put("masterId", request.getParameter("masterId"));
            String hql = "select obj from " + Artwork.class.getName() + " obj where obj.author.id=:masterId";
            List<Artwork> artworkList = baseManager.listObject(hql, queryParam);
            resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
            resultMap.put("artworkList", artworkList);
        } catch (Exception e) {
            resultMap = resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
        mappingJacksonValue = new MappingJacksonValue(resultMap);
        if (request.getParameter("callback") != null) {
            mappingJacksonValue.setJsonpFunction(request.getParameter("callback"));
        }
        return mappingJacksonValue;
    }

    @RequestMapping({"/app/fansNum.do"})
    @ResponseBody
    public MappingJacksonValue fansNum(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> resultMap;
        LogBean logBean = new LogBean();
        MappingJacksonValue mappingJacksonValue;
        try {
            LinkedHashMap<String, Object> queryParam = new LinkedHashMap<>();
            queryParam.put("masterId", request.getParameter("masterId"));
            String hql = "select obj from " + ArtUserFollowed.class.getName() + " obj where obj.follower.id=:masterId";
            List<ArtUserFollowed> artUserFollowedList = baseManager.listObject(hql, queryParam);
            resultMap = resultMapHandler.handlerResult("0", "请求成功", logBean);
            resultMap.put("artUserFollowedList", artUserFollowedList);
        } catch (Exception e) {
            resultMap = resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
        mappingJacksonValue = new MappingJacksonValue(resultMap);
        if (request.getParameter("callback") != null) {
            mappingJacksonValue.setJsonpFunction(request.getParameter("callback"));
        }
        return mappingJacksonValue;
    }


    @Test
    public void testArtworkView() throws Exception {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new TreeMap<>();

        /**investorIndex.do测试加密参数**/
        map.put("pageNum", "1");
        map.put("pageSize", "10");
        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.75:8080/app/investorIndex.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  artWorkCreationList.do测试 **/
//        String json = "{\"pageNum\":\"1\",\"pageSize\":\"5\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  artWorkCreationView.do测试 **/
        String json = "{\"pageNum\":\"1\",\"pageSize\":\"10\",\"userId\":\"ipcgwgyj28ppzwiw\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
        JSONObject jsonObj = (JSONObject) JSONObject.parse(json);
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

        } catch (Exception e) {

        }
    }
}
