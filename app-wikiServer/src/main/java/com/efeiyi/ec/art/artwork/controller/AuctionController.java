package com.efeiyi.ec.art.artwork.controller;


import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtworkAuctionManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.*;
import com.efeiyi.ec.art.message.dao.MessageDao;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.efeiyi.ec.art.organization.util.CommonUtil;
import com.efeiyi.ec.art.organization.util.TimeUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.taglib.PageEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2016/1/29.
 */
@Controller
public class AuctionController extends BaseController {

    @Autowired
    private MessageDao messageDao;

    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    BaseManager baseManager;


    @Autowired
    private ArtworkAuctionManager artworkAuctionManager;

    /**
     * 拍卖首页
     */
    @RequestMapping(value = "/app/artWorkAuctionList.do", method = RequestMethod.POST)
    @ResponseBody
    public Map ArtWorkCreation(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap;
        TreeMap<String, Object> treeMap = new TreeMap<>();
        List artworkList;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkAuctionList");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            if (!DigitalSignatureUtil.verify2(jsonObj)) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            Map paramMap = new HashMap<>();
            //项目信息
            String hql = "from Artwork WHERE 1=1 and status = '1' and type = '3' order by investStartDatetime asc";
            artworkList = messageDao.getPageList(hql, (jsonObj.getInteger("pageIndex") - 1) * (jsonObj.getInteger("pageSize")), jsonObj.getInteger("pageSize"));
            for (Object artwork : artworkList) {
                Artwork artworkTemp = (Artwork) artwork;
//                if(artworkTemp.getPicture_url()!=null) {
//                    artworkTemp.setHeight(ImgUtil.getHeight(artworkTemp.getPicture_url()));
//                    artworkTemp.setWidth(ImgUtil.getWidth(artworkTemp.getPicture_url()));
//                }

                //是否点赞
                Boolean isPraise = false;
                if (!StringUtils.isEmpty(AuthorizationUtil.getUser())) {
                    XQuery xQuery = new XQuery("listArtWorkPraise_default", request);
                    xQuery.put("artwork_id", artworkTemp.getId());
                    xQuery.put("user_id", AuthorizationUtil.getUser().getId());
                    List<ArtWorkPraise> artWorkPraiseList = baseManager.listObject(xQuery);
                    if (artWorkPraiseList != null) {
                        if (artWorkPraiseList.size() > 0) {
                            isPraise = true;
                        }
                    }
                }
                artworkTemp.setPraise(isPraise);

                XQuery xQuery = new XQuery("listArtworkBidding_default", request);
                xQuery.put("artwork_id", artworkTemp.getId());
                List artworkBiddingList = baseManager.listObject(xQuery);
                //出价次数 当前价格 几分钟前
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                String str1 = sdf.format(new Date());
                if (artworkBiddingList != null && !artworkBiddingList.isEmpty()) {
                    artworkTemp.setAuctionNum(artworkBiddingList.size());
                    ArtworkBidding artworkBiddingTemp = ((ArtworkBidding) artworkBiddingList.get(0));
                    artworkTemp.setNewBidingPrice(artworkBiddingTemp.getPrice());
                    String str2 = sdf.format(artworkBiddingTemp.getCreateDatetime());
                    artworkTemp.setNewBiddingDate(TimeUtil.getDistanceTimes(str1, str2));
                }
                if ("3".equals(artworkTemp.getType()) && "32".equals(artworkTemp.getStep())) {//拍卖已经结束
                    if (artworkTemp.getWinner() == null || artworkTemp.getWinner().getId() == null) {
                        artworkTemp.setWinner(null); //设置竞拍得主为空
                    }
                } else {
                    artworkTemp.setWinner(null); //设置竞拍得主为空
                }
            }

            paramMap.put("artworkList",artworkList);
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("data", paramMap);
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
    @RequestMapping(value = "/app/artWorkAuctionView.do")
    @ResponseBody
    public MappingJacksonValue artWorkAuctionView(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkAuctionView");
            /*if (!CommonUtil.jsonObject(jsonObj)) {
                return new MappingJacksonValue(resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean));
            }
            if (!DigitalSignatureUtil.verify2(jsonObj)) {
                return new MappingJacksonValue(resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean));
            }*/
            //项目信息
             Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), jsonObj.getString("artWorkId"));

            //当前用户

            //增加浏览数
//            if (artwork.getViewNum() == null) {
//                artwork.setViewNum(1);
//            } else {
//                artwork.setViewNum(artwork.getViewNum() + 1);
//            }
//            baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
            //竞价记录
            XQuery xQuery = new XQuery("listArtworkBidding_default", request);
            xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
            //最新竞价记录
            List<ArtworkBidding> artworkBiddingList = (List<ArtworkBidding>) baseManager.listObject(xQuery);
            //有效竞价次数
            Integer num = 0;
            if (artworkBiddingList != null && !artworkBiddingList.isEmpty()) {
                num = artworkBiddingList.size();
                artwork.setNewBidingPrice(artworkBiddingList.get(0).getPrice());
            } else {
                artwork.setNewBidingPrice(artwork.getStartingPrice());
            }
            artwork.setAuctionNum(num);
            //项目动态
            xQuery = new XQuery("listArtworkMessage_default", request);
            xQuery.put("artwork_id", jsonObj.getString("artWorkId"));
            List artworkMessageList = baseManager.listObject(xQuery);

            //判断是否交付保证金
            LinkedHashMap<String, Object> queryMap = new LinkedHashMap<>();
            queryMap.put("userId", AuthorizationUtil.getUser()==null?"":AuthorizationUtil.getUser().getId());
            queryMap.put("artworkId", jsonObj.getString("artWorkId"));
            MarginAccount marginAccount = (MarginAccount) baseManager.getUniqueObjectByConditions("From MarginAccount a WHERE a.account.user.id = :userId AND a.artwork.id = :artworkId AND a.status='0'" , queryMap);
            String isSubmitDepositPrice = "1";
            if (marginAccount != null && "0".equals(marginAccount.getStatus())) {
                isSubmitDepositPrice = "0";
            }

            //是否关注
            Boolean isFollowed = false;
            if (!StringUtils.isEmpty(AuthorizationUtil.getUser())) {
                XQuery xQuery1 = new XQuery("listArtUserFollowed_isFollowed", request);
                xQuery1.put("user_id", AuthorizationUtil.getUser().getId());
                xQuery1.put("follower_id", artwork.getAuthor().getId());
                List<ArtUserFollowed> artUserFollowedList = baseManager.listObject(xQuery1);
                if (artUserFollowedList != null) {
                    if (artUserFollowedList.size() > 0) {
                        isFollowed = true;
                    }
                }
            }
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("artwork", artwork);
            resultMap.put("artWorkBidding", artworkBiddingList);
            resultMap.put("artWorkMessage", artworkMessageList);
            resultMap.put("isSubmitDepositPrice", isSubmitDepositPrice);
            resultMap.put("isFollowed", isFollowed);
            resultMap.put("nowDate", new Date());
        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResultType(request, resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
        }

        return resultMapHandler.handlerResultType(request, resultMap);
    }

    /**
     * 拍卖出价
     *
     */
    @RequestMapping(value = "/app/artworkBid.do")
    @ResponseBody
    public MappingJacksonValue artWorkBidOnAuction(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkBidOnAuction");
            /*if (!CommonUtil.jsonObject(jsonObj)) {
                return new MappingJacksonValue(resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean));
            }
            //校验数字签名
            if (!DigitalSignatureUtil.verify2(jsonObj)) {
                return new MappingJacksonValue(resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean));
            }*/

            resultMap = artworkAuctionManager.artworkBidOnAuction(request, jsonObj, logBean);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResultType(request, resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
        }

        return resultMapHandler.handlerResultType(request, resultMap);
    }

    @RequestMapping(value = "/app/artWorkAuctionPayDeposit.do")
    @ResponseBody
    public Map artWorkAuctionPayDeposit(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map resultMap;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkAuctionPayDeposit");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("userId")) || "".equals(jsonObj.getString("artworkId")) || "".equals(jsonObj.getString("money"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            if (!DigitalSignatureUtil.verify2(jsonObj)) {
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
     */
    @RequestMapping(value = "/app/getListOrder.do")
    @ResponseBody
    public Map getListOrder(HttpServletRequest request, HttpServletResponse response) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<>();
        List auctionOrderList;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("getListOrder");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
           /* //校验数字签名
            if (!DigitalSignatureUtil.verify2(jsonObj)) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }*/

            String userId = AuthorizationUtil.getUser()==null?"":AuthorizationUtil.getUser().getId();

            //获取订单(2、待付款 3、待收货 4、已完成 1、全部 5、待发货)
            if ("2".equals(jsonObj.getString("type"))) {
                XQuery xQuery = new XQuery("listAuctionOrder_default1", request);
                xQuery.put("user_id", userId);
                auctionOrderList = baseManager.listObject(xQuery);
            } else if ("3".equals(jsonObj.getString("type"))) {
                XQuery xQuery = new XQuery("listAuctionOrder_default2", request);
                xQuery.put("user_id", userId);
                auctionOrderList = baseManager.listObject(xQuery);
            } else if ("4".equals(jsonObj.getString("type"))) {
                XQuery xQuery = new XQuery("listAuctionOrder_default3", request);
                xQuery.put("user_id", userId);
                auctionOrderList = baseManager.listObject(xQuery);
            }else if ("5".equals(jsonObj.getString("type"))){
                XQuery xQuery = new XQuery("listAuctionOrder_default4", request);
                xQuery.put("user_id", userId);
                auctionOrderList = baseManager.listObject(xQuery);
            }else {
                XQuery xQuery = new XQuery("listAuctionOrder_default", request);
                xQuery.put("user_id", userId);
                auctionOrderList = baseManager.listObject(xQuery);
            }
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "订单获取成功");
            resultMap.put("auctionOrderList", auctionOrderList);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        response.addHeader("Cache-Control", "no-cache");

        return resultMap;
    }

    /**
     * 拍卖订单详情
     *
     */
    @RequestMapping(value = "/app/viewOrder.do")
    @ResponseBody
    public Map viewOrder(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<>();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkBidOnAuction");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            if (!DigitalSignatureUtil.verify2(jsonObj)) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            AuctionOrder auctionOrder = (AuctionOrder) baseManager.getObject(AuctionOrder.class.getName(), jsonObj.getString("artWorkOrderId"));
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
     */
    @RequestMapping(value = "/app/plistArtworkBidding.do")
    @ResponseBody
    public MappingJacksonValue listArtworkBidding(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<>();
        List artworkBiddingList;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson3(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("listArtworkBidding");
            /*if (!CommonUtil.jsonObject(jsonObj)) {
                return new MappingJacksonValue(resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean));
            }
            //校验数字签名
            if (!DigitalSignatureUtil.verify2(jsonObj)) {
                return new MappingJacksonValue(resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean));
            }
*/
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
            List biddingTopThree = baseManager.listPageInfo(xQuery1).getList();

            //竞价记录
            XQuery xQuery2 = new XQuery("listArtworkBidding_default", request);
            xQuery2.put("artwork_id", jsonObj.getString("artWorkId"));
            List<ArtworkBidding> artworkBiddings = baseManager.listObject(xQuery2);
            Integer auctionNum = 0;
            if (artworkBiddings != null && artworkBiddings.size() > 0) {
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
            return resultMapHandler.handlerResultType(request, resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
        }

        return resultMapHandler.handlerResultType(request, resultMap);
    }

    /**
     *确认收货接口
     */
    @RequestMapping(value = "/app/confirmReceipt.do")
    @ResponseBody
    public Map confirmReceipt(HttpServletRequest request){
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("confirmReceipt");
            String auctionOrderId = jsonObj.getString("auctionOrderId");
            AuctionOrder auctionOrder = (AuctionOrder) baseManager.getObject(AuctionOrder.class.getName(), auctionOrderId);
            if (auctionOrder.getType().equals("3")){
                auctionOrder.setType("2");
                baseManager.saveOrUpdate(AuctionOrder.class.getName(), auctionOrder);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "成功");
                return resultMap;
            }else {
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "订单参数不正确");
                return resultMap;
            }
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
    }


}