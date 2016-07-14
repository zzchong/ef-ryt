package com.efeiyi.ec.art.personal.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.efeiyi.ec.art.organization.util.CommonUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.PageInfo;
import com.ming800.core.does.model.XQuery;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2016/4/21.
 *
 */
@RestController
public class UserMainController extends BaseController {
    private static Logger logger = Logger.getLogger(UserMainController.class);

    @Autowired
    BaseManager baseManager;
    @Autowired
    ResultMapHandler resultMapHandler;

    /**
     * 艺术家 主页
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/userMain.do", method = RequestMethod.POST)
    @ResponseBody
    public Map userMain(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("userMain");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if (!CommonUtil.jsonObject(jsonObj)) {
                return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }

            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return  resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }

            String currentId = AuthorizationUtil.getUserId();
            String userId = jsonObj.getString("userId");
            User user = (User)baseManager.getObject(User.class.getName(),jsonObj.getString("userId"));

            //粉丝
            Integer followNum = 0;
            XQuery xQuery = new XQuery("listArtUserFollowed_default",request);
            xQuery.put("follower_id",userId);
            List<ArtUserFollowed> followNumList = baseManager.listObject(xQuery);
            if(followNumList!=null){
                followNum = followNumList.size();
            }
            //关注
            Integer num = 0;
            xQuery = new XQuery("listArtUserFollowed_followed",request);
            xQuery.put("user_id",userId);
            List<ArtUserFollowed> numList = baseManager.listObject(xQuery);
            if(numList!=null){
                num = numList.size();
            }
            //是否关注
            boolean isFollowed = false;
            if(!StringUtils.isEmpty(currentId) && currentId.equals(userId)){
                XQuery xQuery1  = new XQuery("listArtUserFollowed_isFollowed",request);
                xQuery1.put("follower_id",userId);
                xQuery1.put("user_id",currentId);
                List<ArtUserFollowed> artUserFollowedList = (List<ArtUserFollowed>) baseManager.listObject(xQuery1);
                if(artUserFollowedList!=null && artUserFollowedList.size()!=0){
                    isFollowed = true;
                }
            }
            //艺术家
            User master = (User)baseManager.getObject(User.class.getName(),userId);

            //艺术家投资记录
            XQuery xquery = new XQuery("listArtworkInvest_default", request);
            xquery.put("creator_id", userId);
            List<ArtworkInvest> artworkInvests = (List<ArtworkInvest>) baseManager.listObject(xquery);

            //艺术家投资总金额
            BigDecimal sumInvestsMoney = new BigDecimal("0.00");
            for (ArtworkInvest artworkInvest : artworkInvests) {
                sumInvestsMoney = sumInvestsMoney.add(artworkInvest.getPrice());
            }

            //项目总金额
//            BigDecimal money = new BigDecimal("0.00");
//            //拍卖总金额
//            BigDecimal auctionMoney = new BigDecimal("0.00");
//            xQuery = new XQuery("listArtwork_default",request);
//            xQuery.put("author_id",userId);
//            List<Artwork> artworkList = baseManager.listObject(xQuery);
//            if(artworkList!=null){
//                for (Artwork artwork :artworkList){
//                    money.add(artwork.getInvestsMoney());
//                    if(artwork.getNewBidingPrice()!=null)
//                        auctionMoney.add(artwork.getNewBidingPrice());
//                }
//            }

            //投资收益
            BigDecimal reward = new BigDecimal("0.00");
            xQuery = new XQuery("listROIRecord_default",request);
            xQuery.put("user_id",jsonObj.getString("userId"));
            List<ROIRecord> roiRecordList = (List<ROIRecord>)baseManager.listObject(xQuery);
            for (ROIRecord roiRecord : roiRecordList){
                reward = reward.add(roiRecord.getCurrentBalance());
            }

            //项目
            xQuery = new XQuery("plistArtworkPage_default",request);
            xQuery.put("author_id",jsonObj.getString("userId"));
            xQuery.getPageEntity().setSize(jsonObj.getInteger("pageSize"));
            xQuery.getPageEntity().setIndex(jsonObj.getInteger("pageIndex"));
            PageInfo pageInfo = baseManager.listPageInfo(xQuery);
            List<Artwork> artworks = (List<Artwork>) pageInfo.getList();


            data.put("artworkList",artworks);
            data.put("followNum",followNum);
            data.put("num",num);
            data.put("isFollowed",isFollowed);
            data.put("master",master);
            data.put("sumInvestsMoney",sumInvestsMoney);
            data.put("artworkList",artworks);
            data.put("reward",reward);
//            data.put("type",jsonObj.getString("type"));
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("object",data);
            return  resultMap;
        } catch(Exception e){
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
    }


    /**
     * 艺术家 作品
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/userWork.do", method = RequestMethod.POST)
    @ResponseBody
    public Map useWork(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("userWork");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if (!CommonUtil.jsonObject(jsonObj)) {
                return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return  resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            User user = (User)baseManager.getObject(User.class.getName(),jsonObj.getString("userId"));
            XQuery xQuery = new XQuery("plistMasterWork_default",request);
            xQuery.put("creator_id",jsonObj.getString("userId"));
            xQuery.getPageEntity().setSize(jsonObj.getInteger("pageSize"));
            xQuery.getPageEntity().setIndex(jsonObj.getInteger("pageIndex"));
            PageInfo pageInfo = baseManager.listPageInfo(xQuery);
            List<MasterWork> masterWorkList = (List<MasterWork>) pageInfo.getList();
            data.put("masterWorkList",masterWorkList);
//            data.put("type",jsonObj.getString("type"));
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("object",data);
            return  resultMap;
        } catch(Exception e){
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
    }

    /**
     * 艺术家 修改项目
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/editArtWork.do", method = RequestMethod.POST)
    @ResponseBody
    public Map editArtWork(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("editArtWork");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if (!CommonUtil.jsonObject(jsonObj)) {
                return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return  resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(),jsonObj.getString("artworkId"));
            artwork.setDescription(jsonObj.getString("description"));
            baseManager.saveOrUpdate(Artwork.class.getName(),artwork);
            data.put("artwork",artwork);
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("object",data);
            return  resultMap;
        } catch(Exception e){
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
    }

    public static void main(String[] arg) throws Exception {


        String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<String, Object>();

        /**editArtWork.do测试加密参数**/
//        map.put("userId","imhfp1yr4636pj49");
//        map.put("pageSize","4");
//        map.put("pageIndex","1");
        map.put("description","修改后");
        map.put("artworkId","qydeyugqqiugd2");
        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.75:8001/app/editArtWork.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  investorArtWork.do测试 **/
//        String json = "{\"userId\":\"imhfp1yr4636pj49\",\"pageSize\":\"4\",\"pageIndex\":\"1\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
        String json = "{\"description\":\"修改后\",\"artworkId\":\"qydeyugqqiugd2\",\"signmsg\":\"" + signmsg + "\",\"timestamp\":\"" + timestamp + "\"}";
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

}
