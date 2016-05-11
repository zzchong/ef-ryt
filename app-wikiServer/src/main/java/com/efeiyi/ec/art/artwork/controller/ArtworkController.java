package com.efeiyi.ec.art.artwork.controller;


import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtworkManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.message.dao.MessageDao;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.modelConvert.ArtWorkInvestBean;
import com.efeiyi.ec.art.organization.model.User;
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
import org.apache.http.client.methods.HttpDelete;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URLEncoder;
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


    @RequestMapping(value = "/app/getArtWorkList.do", method = RequestMethod.POST)
    @ResponseBody
    public Map getArtWorkList(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            XQuery query = new XQuery("plistArtwork_default", request);
            PageInfo pageInfo = baseManager.listPageInfo(query);
            List<Artwork> list = pageInfo.getList();
            if (list != null && !list.isEmpty()) {
                resultMap.put("responseInfo", list);
            } else {
                resultMap.put("responseInfo", null);
            }
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "成功");
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

    @RequestMapping(value = "/app/investorIndex.do", method = RequestMethod.POST)
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
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("pageSize", jsonObj.getString("pageSize"));
            treeMap.put("pageNum", jsonObj.getString("pageNum"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            String hql = "from Artwork WHERE 1=1 and status = '1'  order by investStartDatetime asc";
            artworkList = (List<Artwork>) messageDao.getPageList(hql, (jsonObj.getInteger("pageNum") - 1) * (jsonObj.getInteger("pageSize")), jsonObj.getInteger("pageSize"));
//            List<ArtWorkBean> objectList = new ArrayList<>();
//            for (Artwork artwork : artworkList){
//                       ArtWorkBean artWorkBean = new ArtWorkBean();
//                       artWorkBean.setArtwork(artwork);
//                       artWorkBean.setMaster((Master)baseManager.getObject(Master.class.getName(),artwork.getAuthor().getId()));
//                       objectList.add(artWorkBean);
//            }
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            if (artworkList != null && !artworkList.isEmpty()) {
                resultMap.put("objectList", artworkList);
            } else {
                resultMap.put("objectList", null);
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
    @RequestMapping(value = "/app/investorArtWorkView.do", method = RequestMethod.POST)
    @ResponseBody
    public Map investorArtWork(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        List objectList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("investorArtWorkView");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            //项目详情
            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), jsonObj.getString("artWorkId"));
            if (artwork == null) {
                return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
            }
            //投资人数
            Integer investNum = 0;
            if (artwork.getArtworkInvests() != null) {
                investNum = artwork.getArtworkInvests().size();
            }
            //剩余时间
            String time = TimeUtil.getDistanceTimes2(new Date(), artwork.getInvestStartDatetime(), "", TimeUtil.SECOND).get("time").toString();
            //项目文件
            List<ArtworkAttachment> artworkAttachmentList = artwork.getArtworkAttachment();
            //项目制作过程说明、融资解惑
            Artworkdirection artworkdirection = artwork.getArtworkdirection();

            //是否点赞
            Boolean isPraise = false;
            XQuery xQuery = new XQuery("listArtWorkPraise_default", request);
            xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
            xQuery.put("user_id", jsonObj.getString("currentUserId"));
            List<ArtWorkPraise> artWorkPraiseList = baseManager.listObject(xQuery);
            if (artWorkPraiseList != null) {
                if (artWorkPraiseList.size() > 0) {
                    isPraise = true;
                }
            }
            data.put("artWork", artwork);
            data.put("investNum", investNum);
            data.put("time", time);
            data.put("artworkdirection", artworkdirection);
            data.put("artworkAttachmentList", artworkAttachmentList);
            data.put("isPraise", isPraise);

            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);

            resultMap.put("object", data);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**
     * 融资项目 项目详情页(评论tab)
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/investorArtWorkComment.do", method = RequestMethod.POST)
    @ResponseBody
    public Map investorArtWorkComment(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        List objectList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("investorArtWorkComment");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

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
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**
     * 融资项目 项目详情页(投资tab)
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/investorArtWorkInvest.do", method = RequestMethod.POST)
    @ResponseBody
    public Map investorArtWorkInvest(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        List objectList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("investorArtWorkInvest");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            //投资记录列表
            List<ArtworkInvest> artworkInvestList = null;
            //投资top
            List<ArtworkInvest> artworkInvestTopList = null;
            //temp
            List<ArtworkInvest> artworkInvestTopTempList = null;


            XQuery xQuery = new XQuery("plistArtworkInvest_default", request);
            xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
            PageEntity pageEntity = new PageEntity();
            pageEntity.setSize(jsonObj.getInteger("pageSize"));
            pageEntity.setIndex(jsonObj.getInteger("pageIndex"));
            xQuery.setPageEntity(pageEntity);
            artworkInvestList = baseManager.listPageInfo(xQuery).getList();

            data.put("artworkInvestList", artworkInvestList);

            xQuery = new XQuery("listArtworkInvest1_default", request);
            xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
            artworkInvestTopTempList = baseManager.listObject(xQuery);
            if (artworkInvestTopTempList != null) {
                if (artworkInvestTopTempList.size() > 3) {
                    artworkInvestTopList = new ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        artworkInvestTopList.add(artworkInvestTopTempList.get(i));
                    }
                } else {
                    artworkInvestTopList = artworkInvestTopTempList;
                }
            }
            data.put("artworkInvestTopList", artworkInvestTopList);

            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("object", data);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
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
    public Map artworkPraise(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<>();
        List objectList = null;
        try {
            JSONObject jsonObject = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObject.toString());
            logBean.setApiName("artworkPraise");
            if (!CommonUtil.jsonObject(jsonObject)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            if (!DigitalSignatureUtil.verify2(jsonObject)) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            if (artworkManager.saveArtWorkPraise(jsonObject.getString("artworkId"), jsonObject.getString("currentUserId"), jsonObject.getString("messageId"))) {
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
        map.put("currentUserId", "ieatht97wfw30hfd");
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        map.put("signmsg", signmsg);
        String url = "http://192.168.1.41:8085/app/artworkPraise.do";
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
    @RequestMapping(value = "/app/artworkComment.do", method = RequestMethod.POST)
    @ResponseBody
    public Map artworkComment(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<>();
        List objectList = null;
        try {
            JSONObject jsonObject = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObject.toString());
            logBean.setApiName("artworkComment");
            if (!CommonUtil.jsonObject(jsonObject)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            if (!DigitalSignatureUtil.verify2(jsonObject)) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            if (artworkManager.saveArtWorkComment(jsonObject.getString("artWorkId"), jsonObject.getString("content"), jsonObject.getString("fatherCommentId"), jsonObject.getString("currentUserId"), jsonObject.getString("messageId"))) {
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
            treeMap.put("pageNum", jsonObj.getString("pageNum"));
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
            xQuery.getPageEntity().setIndex(jsonObj.getInteger("pageNum"));
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
            if ("".equals(request.getParameter("signmsg")) || "".equals(request.getParameter("timestamp"))
                    || "".equals(request.getParameter("title")) || "".equals(request.getParameter("brief")) || "".equals(request.getParameter("duration"))
                    || "".equals(request.getParameter("userId")) || "".equals(request.getParameter("investGoalMoney"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = request.getParameter("signmsg");
            treeMap.put("userId", request.getParameter("userId"));
            treeMap.put("timestamp", request.getParameter("timestamp"));
            treeMap.put("title", request.getParameter("title"));
            treeMap.put("investGoalMoney", request.getParameter("investGoalMoney"));
            treeMap.put("duration", request.getParameter("duration"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }


            User user = (User) baseManager.getObject(User.class.getName(), request.getParameter("userId"));
            try {
                if (user != null && user.getId() != null) {

                    Artwork artwork = new Artwork();
                    artwork.setStatus("0");//不可用状态，不能进入融资阶段
                    artwork.setType("0");
                    artwork.setStep("100");//编辑阶段，尚未提交 提交后置为 10
                    artwork.setTitle(request.getParameter("title"));
                    artwork.setBrief(request.getParameter("brief"));
                    artwork.setDuration(Integer.parseInt(request.getParameter("duration")));
                    artwork.setCreateDatetime(new Date());
                    artwork.setInvestGoalMoney(new BigDecimal(request.getParameter("investGoalMoney")));
                    MultipartFile artwork_img = ((MultipartHttpServletRequest) request).getFile("picture_url");
                    String fileType = "";
                    if (artwork_img.getContentType().contains("jpg")) {
                        fileType = ".jpg";
                    } else if (artwork_img.getContentType().contains("jpeg")) {
                        fileType = ".jpeg";
                    } else if (artwork_img.getContentType().contains("png") || artwork_img.getContentType().contains("PNG")) {
                        fileType = ".png";
                    } else if (artwork_img.getContentType().contains("gif")) {
                        fileType = ".gif";
                    }
                    String url = "artwork/" + new Date().getTime() + fileType;
                    String pictureUrl = "http://rongyitou2.efeiyi.com/" + url;
                    //将图片上传至阿里云
                    aliOssUploadManager.uploadFile(artwork_img, "ec-efeiyi2", url);
                    artwork.setPicture_url(pictureUrl);
                    artwork.setAuthor(user);
                    baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
                    resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
                    resultMap.put("artworkId", artwork.getId());
                    return resultMap;
                } else {
                    return resultMapHandler.handlerResult("10007", "用户名不存在", logBean);
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

                    Artworkdirection artworkdirection = new Artworkdirection();
                    artworkdirection.setFinancing_aq(request.getParameter("financing_aq"));
                    artworkdirection.setMake_instru(request.getParameter("make_instru"));
                    artwork.setDescription(request.getParameter("description"));
                    artwork.setArtworkdirection(artworkdirection);
                    //List<ArtworkAttachment> artworkAttachments = new ArrayList<ArtworkAttachment>();

                    //创建一个通用的多部分解析器
                    CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
                    //判断 request 是否有文件上传,即多部分请求
                    if (multipartResolver.isMultipart(request)) {
                        //转换成多部分request
                        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
                        //取得request中的所有文件名
                        Iterator<MultipartFile> iter = multiRequest.getFiles("file").iterator();
                        while (iter.hasNext()) {
                            //取得上传文件
                            //MultipartFile file = multiRequest.getFile(iter.next());
                            MultipartFile file = iter.next();
                            if (file != null) {
                                ArtworkAttachment artworkAttachment = new ArtworkAttachment();//项目附件
                                //取得当前上传文件的文件名称
                                String myFileName = file.getOriginalFilename();
                                //如果名称不为“”,说明该文件存在，否则说明该文件不存在
                                if (myFileName.trim() != "") {

                                    //重命名上传后的文件名
                                    String url = "artwork/" + new Date().getTime() + myFileName;
                                    String pictureUrl = "http://rongyitou2.efeiyi.com/" + url;
                                    //将图片上传至阿里云
                                    aliOssUploadManager.uploadFile(file, "ec-efeiyi2", url);
                                    artworkAttachment.setArtwork(artwork);
                                    artworkAttachment.setFileType(myFileName.substring(myFileName.lastIndexOf("."), myFileName.length()));
                                    artworkAttachment.setFileName(pictureUrl);
                                    //artworkAttachments.add(artworkAttachment);
                                    baseManager.saveOrUpdate(ArtworkAttachment.class.getName(), artworkAttachment);
                                    artwork.getArtworkAttachment().add(artworkAttachment);
                                }
                            }
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
        try {
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(request.getParameter("artworkId") + " ...");//************记录请求报文
            logBean.setApiName("releaseArtworkDynamic");
            if ("".equals(request.getParameter("signmsg")) || "".equals(request.getParameter("timestamp"))

                    || "".equals(request.getParameter("artworkId")) || "".equals(request.getParameter("type"))
                    ) {
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

                    ArtworkMessage artworkMessage = new ArtworkMessage();
                    artworkMessage.setContent(!"".equals(request.getParameter("content")) ? request.getParameter("content") : "");
                    artworkMessage.setCreator(artwork.getAuthor());
                    artworkMessage.setArtwork(artwork);
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

                                    if ("0".equals(request.getParameter("type"))) {
                                        url.append("picture/" + new Date().getTime() + myFileName);
                                    } else if ("1".equals(request.getParameter("type"))) {
                                        url.append("video/" + new Date().getTime() + myFileName);
                                    } else {
                                        url.append(new Date().getTime() + myFileName);
                                    }

                                    String pictureUrl = "http://rongyitou2.efeiyi.com/" + url.toString();
                                    //将图片上传至阿里云
                                    aliOssUploadManager.uploadFile(file, "ec-efeiyi2", url.toString());
                                    artworkMessageAttachment.setFileUri(pictureUrl);
                                    artworkMessageAttachment.setArtworkMessage(artworkMessage);
                                    artworkMessageAttachment.setFileType(request.getParameter("type"));
                                    //artworkMessageAttachments.add(artworkMessageAttachment);
                                    baseManager.saveOrUpdate(ArtworkMessageAttachment.class.getName(), artworkMessageAttachment);
                                    //artworkMessage.getArtworkMessageAttachments().add(artworkMessageAttachment);
                                }
                            }
                        }
                    }
                    //artworkMessage.setCreateDatetime(new Date());
                    //artworkMessage.setArtworkMessageAttachments(artworkMessageAttachments);
                    //baseManager.saveOrUpdate(ArtworkMessage.class.getName(),artworkMessage);
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
     * 项目进展动态查询接口
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artworkProgress.do", method = RequestMethod.GET)
    @ResponseBody
    public Map artworkProgress(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap ;
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

            Artwork artwork = (Artwork)baseManager.getObject(Artwork.class.getName(),artworkId);
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("artwork",artwork);
            resultMap.put("investTimes",artwork.getArtworkInvests().size());
            resultMap.put("artworkMessages",artwork.getArtworkMessages());
            resultMap.put("artworkBidding",artwork.getArtworkBiddings());
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
//        map.put("pageSize","3");
//        map.put("pageNum","1");
//        map.put("timestamp", timestamp);
        /**investorArtWorkView.do测试加密参数**/
        map.put("artWorkId", "qydeyugqqiugd2");
//        map.put("messageId","2");
        map.put("pageSize", "4");
        map.put("pageIndex", "1");
        map.put("timestamp", timestamp);

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
        String url = "http://192.168.1.75:8001/app/investorArtWorkComment.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  investorArtWork.do测试 **/
        String json = "{\"pageIndex\":\"1\",\"pageSize\":\"4\",\"artWorkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
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
        params.add(new BasicNameValuePair("artworkId","qydeyugqqiugd2"));
        params.add(new BasicNameValuePair("timestamp",Long.toString(timestamp)));
        params.add(new BasicNameValuePair("signmsg",signmsg));
        String url = "http://192.168.1.41:8085/app/artworkProgress.do?" + URLEncodedUtils.format(params,HTTP.UTF_8);
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

    @RequestMapping(name = "/app/artworkRemove.do", method = RequestMethod.DELETE)
    @ResponseBody
    public Map removeArtwork(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson2(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artworkRemove");
            if ("".equals(jsonObj.getString("signmsg"))
                    || "".equals(jsonObj.getString("timestamp"))
                    || "".equals(jsonObj.getString("userId"))
                    || "".equals(jsonObj.getString("artworkId"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("userId", jsonObj.getString("userId"));
            treeMap.put("artworkId", jsonObj.getString("artworkId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            baseManager.remove(Artwork.class.getName(), jsonObj.getString("artworkId"));
            return resultMapHandler.handlerResult("0", "成功", logBean);
        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
    }

    @Test
    public void testArtworkRemove() throws Exception {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new TreeMap<>();

        /**investorArtWorkView.do测试加密参数**/
        map.put("artworkId", "aaaaaaaaaaaaaa");
        map.put("userId", "ieatht97wfw30hfd");
        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("artworkId", "aaaaaaaaaaaaaa"));
        params.add(new BasicNameValuePair("userId", "ieatht97wfw30hfd"));
        params.add(new BasicNameValuePair("timestamp", Long.toString(timestamp)));
        params.add(new BasicNameValuePair("signmsg", signmsg));
        String url = "http://192.168.1.41:8080/app/artworkRemove.do?" + URLEncodedUtils.format(params, HTTP.UTF_8);
        HttpDelete httpDelete = new HttpDelete(url);
        httpDelete.setHeader("Content-Type", "application/json;charset=utf-8");
        System.out.println("url:  " + url);
        try {
            HttpResponse response = httpClient.execute(httpDelete);
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
