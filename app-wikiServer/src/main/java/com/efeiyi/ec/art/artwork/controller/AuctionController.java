package com.efeiyi.ec.art.artwork.controller;


import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtworkAuctionManager;
import com.efeiyi.ec.art.artwork.service.ArtworkInvestManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.message.dao.MessageDao;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.modelConvert.ArtWorkBean;
import com.efeiyi.ec.art.modelConvert.ArtWorkInvestBean;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.util.CommonUtil;
import com.efeiyi.ec.art.organization.util.TimeUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.dao.hibernate.XdoDaoSupport;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.PageInfo;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.taglib.PageEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2016/1/29.
 */
@Controller
public class AuctionController extends BaseController {
    private static Logger logger = Logger.getLogger(AuctionController.class);

    @Autowired
    private MessageDao messageDao;

    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    BaseManager baseManager;

    @Autowired
    private XdoDaoSupport xdoDao;

    @Autowired
    private ArtworkAuctionManager artworkAuctionManager;

    /**
     * 拍卖首页
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artWorkAuctionList.do", method = RequestMethod.POST)
    @ResponseBody
    public Map ArtWorkCreation(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        List<Artwork> artworkList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkAuctionList");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("pageNum", jsonObj.getString("pageNum"));
            treeMap.put("pageSize", jsonObj.getString("pageSize"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            //项目信息
            String hql = "from Artwork WHERE 1=1 and status = '1' and type = '3' order by investStartDatetime asc";
            artworkList = (List<Artwork>) messageDao.getPageList(hql, (jsonObj.getInteger("pageNum") - 1) * (jsonObj.getInteger("pageSize")), jsonObj.getInteger("pageSize"));
//            List<ArtWorkBean> objectList = new ArrayList<>();
            for (Artwork artwork : artworkList) {

                XQuery xQuery = new XQuery("listArtworkBidding_default", request);
                xQuery.put("artwork_id", artwork.getId());
                List<ArtworkBidding> artworkBiddingList = (List<ArtworkBidding>) baseManager.listObject(xQuery);
                //出价次数 当前价格 几分钟前
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                String str1 = sdf.format(new Date());
                if (artworkBiddingList != null && !artworkBiddingList.isEmpty()) {
                    artwork.setAuctionNum(artworkBiddingList.size());
                    artwork.setNewBidingPrice(artworkBiddingList.get(0).getPrice());
                    String str2 = sdf.format(artworkBiddingList.get(0).getCreateDatetime());
                    artwork.setNewBiddingDate(TimeUtil.getDistanceTimes(str1, str2));
                }

//                ArtWorkBean artWorkBean = new ArtWorkBean();
//                artWorkBean.setArtwork(artwork);
//               // artWorkBean.setMaster((Master)baseManager.getObject(Master.class.getName(),artwork.getAuthor().getId()));
//                objectList.add(artWorkBean);

                if ("3".equals(artwork.getType()) && "32".equals(artwork.getStep())) {//拍卖已经结束

                    //ArtworkBidding artworkBidding = (ArtworkBidding)baseManager.getUniqueObjectByConditions(AppConfig.GET_ART_WORK_WINNER,param);
                  /*  ArtworkBidding artworkBidding = (ArtworkBidding) xdoDao.getSession().createSQLQuery(AppConfig.GET_ART_WORK_WINNER).addEntity(ArtworkBidding.class).setString("artworkId", artwork.getId()).uniqueResult();
                    if (artworkBidding != null && artworkBidding.getId() != null) {
                        artwork.setWinner(artworkBidding.getCreator()); //设置竞拍得主
                    } else {
                        artwork.setWinner(new User()); //设置竞拍得主为空
                    }*/
                    if (artwork.getWinner()== null || artwork.getWinner().getId()==null){
                        artwork.setWinner(null); //设置竞拍得主为空
                    }
                } else {
                    artwork.setWinner(null); //设置竞拍得主为空
                }

            }
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("objectList", artworkList);
        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**
     * 拍卖详情页
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artWorkAuctionView.do", method = RequestMethod.POST)
    @ResponseBody
    public Map artWorkAuctionView(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = null;
        TreeMap treeMap = new TreeMap();
        List<Artwork> artworkList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkAuctionView");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("currentUserId", jsonObj.getString("currentUserId"));
            treeMap.put("artWorkId", jsonObj.getString("artWorkId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            //项目信息
            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), jsonObj.getString("artWorkId"));
            //增加浏览数
            if(artwork.getViewNum() == null){
                artwork.setViewNum(1);
            }else {
                artwork.setViewNum(artwork.getViewNum() + 1);
            }
            baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
//                ArtWorkBean artWorkBean = new ArtWorkBean();
//                artWorkBean.setArtwork(artwork);
//               // artWorkBean.setMaster((Master)baseManager.getObject(Master.class.getName(),artwork.getAuthor().getId()));
            //竞价记录
            XQuery xQuery = new XQuery("listArtworkBidding_default", request);
            xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
            List<ArtworkBidding> artworkBiddingList = (List<ArtworkBidding>) baseManager.listObject(xQuery);
            //最新竞价记录
            artwork.setNewBidingPrice(artworkBiddingList.get(0).getPrice());
            //有效竞价次数
            Integer num = 0;
            if (artworkBiddingList != null) {
                num = artworkBiddingList.size();
            }
            artwork.setAuctionNum(num);
            //项目动态
            xQuery = new XQuery("listArtworkMessage_default", request);
            xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
            List<ArtworkMessage> artworkMessageList = (List<ArtworkMessage>) baseManager.listObject(xQuery);

            //判断是否交付保证金
            String isSubmitDepositPrice = "1";

            if (true){
                isSubmitDepositPrice = "0";
            }
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("artwork", artwork);
            resultMap.put("artWorkBidding", artworkBiddingList);
            resultMap.put("artWorkMessage", artworkMessageList);
            resultMap.put("isSubmitDepositPrice", isSubmitDepositPrice);
        } catch (Exception e) {
            e.printStackTrace();

            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**
     * 拍卖出价
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artworkBid.do")
    @ResponseBody
    public Map artWorkBidOnAuction(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap;
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkBidOnAuction");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("userId")) || "".equals(jsonObj.getString("artworkId")) || "".equals(jsonObj.getString("money"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("currentUserId", jsonObj.getString("currentUserId"));
            treeMap.put("artworkId", jsonObj.getString("artworkId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            treeMap.put("price", jsonObj.getString("price"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            resultMap = artworkAuctionManager.artworkBidOnAuction(request,jsonObj,logBean);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    @RequestMapping(value = "/app/artWorkAuctionPayDeposit.do")
    @ResponseBody
    public Map artWorkAuctionPayDeposit(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap;
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkAuctionPayDeposit");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("userId")) || "".equals(jsonObj.getString("artworkId")) || "".equals(jsonObj.getString("money"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("currentUserId", jsonObj.getString("currentUserId"));
            treeMap.put("artworkId", jsonObj.getString("artworkId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            resultMap = artworkAuctionManager.artWorkAuctionPayDeposit(request, jsonObj, logBean);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**
     * 拍卖订单列表
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/getListOrder.do")
    @ResponseBody
    public Map getListOrder(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<>();
        TreeMap treeMap = new TreeMap();
        List<AuctionOrder> auctionOrderList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("getListOrder");
            boolean flag = CommonUtil.jsonObject(jsonObj);
            if (!flag) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("type", jsonObj.getString("type"));
            treeMap.put("currentUserId", jsonObj.getString("currentUserId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);

            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            //获取订单(2、待付款 3、待收货 4、已完成 1、全部)
            if("2".equals(jsonObj.getString("type"))){
                XQuery xQuery = new XQuery("listAuctionOrder_default1", request);
                xQuery.put("user_id", jsonObj.getString("currentUserId"));
                auctionOrderList = (List<AuctionOrder>) baseManager.listObject(xQuery);
            }else if ("3".equals(jsonObj.getString("type"))){
                XQuery xQuery = new XQuery("listAuctionOrder_default2", request);
                xQuery.put("user_id", jsonObj.getString("currentUserId"));
                auctionOrderList = (List<AuctionOrder>) baseManager.listObject(xQuery);
            }else if ("4".equals(jsonObj.getString("type"))){
                XQuery xQuery = new XQuery("listAuctionOrder_default3", request);
                xQuery.put("user_id", jsonObj.getString("currentUserId"));
                auctionOrderList = (List<AuctionOrder>) baseManager.listObject(xQuery);
            }else {
                XQuery xQuery = new XQuery("listAuctionOrder_default", request);
                xQuery.put("user_id", jsonObj.getString("currentUserId"));
                auctionOrderList = (List<AuctionOrder>) baseManager.listObject(xQuery);
            }

            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "订单获取成功");
            resultMap.put("auctionOrderList", auctionOrderList);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**
     * 拍卖订单详情
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/viewOrder.do")
    @ResponseBody
    public Map viewOrder(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkBidOnAuction");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("artWorkOrderId", jsonObj.getString("artWorkOrderId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            AuctionOrder auctionOrder = (AuctionOrder) baseManager.getObject(AuctionOrder.class.getName(),jsonObj.getString("artWorkOrderId"));
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "订单详情获取成功");
            resultMap.put("auctionOrder", auctionOrder);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**
     * 拍卖记录接口
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/plistArtworkBidding.do")
    @ResponseBody
    public Map listArtworkBidding(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<>();
        TreeMap treeMap = new TreeMap();
        List<ArtworkBidding> artworkBiddingList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("listArtworkBidding");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("artWorkId", jsonObj.getString("artWorkId"));
            treeMap.put("pageSize", jsonObj.getString("pageSize"));
            treeMap.put("pageIndex", jsonObj.getString("pageIndex"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (!verify) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            XQuery xQuery = new XQuery("listArtworkBidding_default", request);
            xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
            PageEntity pageEntity = new PageEntity();
            pageEntity.setSize(jsonObj.getInteger("pageSize"));
            pageEntity.setIndex(jsonObj.getInteger("pageIndex"));
            xQuery.setPageEntity(pageEntity);
            artworkBiddingList = baseManager.listPageInfo(xQuery).getList();

            XQuery xQuery1 = new XQuery("plistArtworkBidding_default1", request);
            xQuery1.put("artwork_id", jsonObj.getString("artWorkId"));
            PageEntity pageEntity1 = new PageEntity();
            pageEntity1.setIndex(1);
            pageEntity1.setSize(3);
            List<ArtworkBidding> biddingTopThree = baseManager.listPageInfo(xQuery1).getList();

            //竞价记录
            XQuery xQuery2 = new XQuery("listArtworkBidding_default", request);
            xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
            List<ArtworkBidding> artworkBiddings = (List<ArtworkBidding>) baseManager.listObject(xQuery2);
            Integer auctionNum = 0;
            if(artworkBiddings != null && artworkBiddings.size() > 0){
                auctionNum = artworkBiddings.size();
            }
            //竞拍人数
            long biddingUsersNum = artworkBiddings.stream().map(artworkBidding -> artworkBidding.getCreator().getId()).distinct().count();
            int num = (int) biddingUsersNum;
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "查询成功");
            resultMap.put("biddingUsersNum", num);
            resultMap.put("auctionNum", auctionNum);
            resultMap.put("artworkBiddingList", artworkBiddingList);
            resultMap.put("biddingTopThree", biddingTopThree);
        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**
     * 浏览次数增加接口
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/addViewNum.do")
    @ResponseBody
    public Map addViewNum(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkAuctionPayDeposit");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("artworkId", jsonObj.getString("artworkId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), jsonObj.getString("artworkId"));
            if(artwork.getViewNum()==null){
                artwork.setViewNum(1);
            }else {
                artwork.setViewNum(artwork.getViewNum()+1);
            }
            baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "浏览次数增加成功");

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

        /**artWorkAuctionList.do测试加密参数**/
        map.put("artWorkOrderId", "qydeyugqqiugd2");
        //map.put("type", "1");
        /**artWorkAuctionView.do测试加密参数**/
        //map.put("artWorkId","qydeyugqqiugd2");
        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.60:8080/app/viewOrder.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  artWorkAuctionView.do测试 **/
        //String json = "{\"artWorkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  artWorkAuctionList.do测试 **/
        String json = "{\"artWorkOrderId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
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