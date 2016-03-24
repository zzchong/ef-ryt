package com.efeiyi.ec.art.artwork.controller;


import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.message.dao.MessageDao;
import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.model.ArtworkInvest;
import com.efeiyi.ec.art.model.InvestReward;
import com.efeiyi.ec.art.model.Master;
import com.efeiyi.ec.art.modelConvert.ArtWorkBean;
import com.efeiyi.ec.art.modelConvert.ArtWorkInvestBean;
import com.efeiyi.ec.art.organization.model.User;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.does.model.PageInfo;
import com.ming800.core.does.model.XQuery;
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
import java.util.*;

/**
 * Created by Administrator on 2016/1/29.
 *
 */
@Controller
public class ArtworkController extends BaseController {
    private static Logger logger = Logger.getLogger(ArtworkController.class);

    @Autowired
    private MessageDao messageDao;

    @Autowired
    ResultMapHandler resultMapHandler;

    @RequestMapping(value = "/app/getArtWorkList.do", method = RequestMethod.POST)
    @ResponseBody
    public Map getArtWorkList(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
      try{
          XQuery query = new XQuery("plistArtwork_default", request);
          PageInfo pageInfo = baseManager.listPageInfo(query);
          List<Artwork> list = pageInfo.getList();
          if (list!= null && !list.isEmpty()){
              resultMap.put("responseInfo",list);
          }else {
              resultMap.put("responseInfo",null);
          }
          resultMap.put("resultCode","0");
          resultMap.put("resultMsg","成功");
        } catch(Exception e){
            e.printStackTrace();
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
        }

        return resultMap;
    }

    /**
     * 融资首页 接口
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
        try{
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("investorIndex");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("pageSize",jsonObj.getString("pageSize"));
            treeMap.put("pageNum",jsonObj.getString("pageNum"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }

            String hql = "from Artwork WHERE 1=1 and status = '1'  order by investStartDatetime asc";
            artworkList =  (List<Artwork>)messageDao.getPageList(hql,(jsonObj.getInteger("pageNum")-1)*(jsonObj.getInteger("pageSize")),jsonObj.getInteger("pageSize"));
//            List<ArtWorkBean> objectList = new ArrayList<>();
//            for (Artwork artwork : artworkList){
//                       ArtWorkBean artWorkBean = new ArtWorkBean();
//                       artWorkBean.setArtwork(artwork);
//                       artWorkBean.setMaster((Master)baseManager.getObject(Master.class.getName(),artwork.getAuthor().getId()));
//                       objectList.add(artWorkBean);
//            }
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            if (artworkList!= null && !artworkList.isEmpty()){
                resultMap.put("objectList",artworkList);
            }else {
                resultMap.put("objectList",null);
            }

        } catch(Exception e){
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }

        return resultMap;
    }


    /**
     * 融资项目 详情页
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/investorArtWork.do", method = RequestMethod.POST)
    @ResponseBody
    public Map investorArtWork(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        List objectList = null;
        try{
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("investorArtWork");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("artWorkId",jsonObj.getString("artWorkId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }

            Artwork artwork = (Artwork)baseManager.getObject(Artwork.class.getName(),jsonObj.getString("artWorkId"));
//            ArtWorkBean artWorkBean = new ArtWorkBean();
//            artWorkBean.setArtwork(artwork);
         //   artWorkBean.setMaster((Master)baseManager.getObject(Master.class.getName(),artwork.getAuthor().getId()));

            XQuery xQuery = new XQuery("listArtworkInvest1_default",request);
            xQuery.put("artwork_id",jsonObj.getString("artWorkId"));
            List<ArtworkInvest> artworkInvestList = (List<ArtworkInvest>)baseManager.listObject(xQuery);
            List<User> userList = new ArrayList<>();
            for(ArtworkInvest artworkInvest : artworkInvestList){
                userList.add(artworkInvest.getCreator());
            }
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("object",artwork);
            resultMap.put("investUserList",userList);
        } catch(Exception e){
            e.printStackTrace();
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return  resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }

        return resultMap;
    }


    /**
     * 艺术家页面
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
        try{
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("masterView");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("masterId",jsonObj.getString("masterId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            //艺术家个人介绍
            Master master = (Master)baseManager.getObject(Master.class.getName(),jsonObj.getString("masterId"));
            //关注人数
            XQuery xQuery = new XQuery("listArtUserFollowed_default",request);
            xQuery.put("follower_id",master.getUser().getId());
            Integer followedNum = baseManager.listObject(xQuery).size();

            xQuery = new XQuery("listArtwork_default",request);
            xQuery.put("author_id",master.getUser().getId());
            List<Artwork> artworks = (List<Artwork>)baseManager.listObject(xQuery);
            //投资者
            Integer investsNum = 0;
            //融资金额
            BigDecimal  investsMoney = new BigDecimal(0);
            for(Artwork artwork :artworks){
                 investsNum += artwork.getArtworkInvests().size();
                 investsMoney = investsMoney.add(artwork.getInvestsMoney());
            }
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("master",master);
            resultMap.put("artWorkList",artworks);
            resultMap.put("investsNum",investsNum);
            resultMap.put("investsMoney",investsMoney);
            resultMap.put("followedNum",followedNum);
        } catch(Exception e){
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }

        return resultMap;
    }


    /**
     * 游客页面
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
        try{
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("guestView");
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("timestamp"))) {
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            //校验数字签名
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("userId",jsonObj.getString("userId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            //游客信息
            User user = (User)baseManager.getObject(User.class.getName(),jsonObj.getString("userId"));
            //
            XQuery xQuery = new XQuery("listArtworkInvest_default",request);
            xQuery.put("creator_id",jsonObj.getString("userId"));
            List<ArtworkInvest> artworkInvests = (List<ArtworkInvest>)baseManager.listObject(xQuery);
            //查询数据参数
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("userId", jsonObj.getString("userId"));
            //投资项目
            List<ArtworkInvest> artworkInvests1 = (List<ArtworkInvest>)baseManager.listObject(AppConfig.SQL_INVEST_ARTWORK_APP, map);
            List<BigDecimal> investMoney = (List<BigDecimal>)baseManager.listObject(AppConfig.SQL_INVEST_MONEY_APP, map);
            List<ArtWorkInvestBean> artworks = new ArrayList<>();
            for (int i = 0;i<artworkInvests1.size();i++){
                ArtWorkInvestBean artWorkInvestBean = new ArtWorkInvestBean();
                artWorkInvestBean.setArtwork(artworkInvests1.get(i).getArtwork());
                artWorkInvestBean.setInvestMoney(investMoney.get(i));
            //    artWorkInvestBean.setMaster((Master)baseManager.getObject(Master.class.getName(),artworkInvests1.get(i).getArtwork().getAuthor().getId()));
                artworks.add(artWorkInvestBean);
            }
            //投资金额
            BigDecimal investsMoney = new BigDecimal(0);
            for (ArtworkInvest artworkInvest : artworkInvests){
                investsMoney = investsMoney.add(artworkInvest.getPrice());
            }
            //投资回报
            BigDecimal reward = new BigDecimal(0);
            xQuery = new XQuery("listInvestReward_default",request);
            xQuery.put("investUser_id",jsonObj.getString("userId"));
            List<InvestReward> investRewards = (List<InvestReward>)baseManager.listObject(xQuery);
            for (InvestReward investReward : investRewards){
                reward = reward.add(investReward.getReward());
            }
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("user",user);
            resultMap.put("artworks",artworks);
            resultMap.put("investsMoney",investsMoney);
            resultMap.put("reward",reward);
        } catch(Exception e){
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }

        return resultMap;
    }
    public  static  void  main(String [] arg) throws Exception {


        String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new HashMap<String, Object>();

        /**investorIndex.do测试加密参数**/
//        map.put("pageSize","3");
//        map.put("pageNum","1");
//        map.put("timestamp", timestamp);
        /**investorArtWork.do测试加密参数**/
//        map.put("artWorkId","qydeyugqqiugdi");
//        map.put("timestamp", timestamp);

        /**masterView.do测试加密参数**/
//        map.put("masterId","icjxkedl0000b6i0");
//        map.put("timestamp", timestamp);
        /**guestView.do测试加密参数**/
        map.put("userId","icjxkedl0000b6i0");
        map.put("timestamp", timestamp);
        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://192.168.1.80:8001/app/guestView.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");

        /**json参数  investorArtWork.do测试 **/
//        String json = "{\"artWorkId\":\"qydeyugqqiugdi\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  investorIndex.do测试 **/
//        String json = "{\"pageSize\":\"3\",\"pageNum\":\"1\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  guestView.do测试 **/
        String json = "{\"userId\":\"icjxkedl0000b6i0\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        /**json参数  masterView.do测试 **/
//        String json = "{\"masterId\":\"icjxkedl0000b6i0\",\"signmsg\":\"" + signmsg+"\",\"timestamp\":\""+timestamp+"\"}";
        JSONObject jsonObj = (JSONObject)JSONObject.parse(json);
        String jsonString = jsonObj.toJSONString();


        StringEntity stringEntity = new StringEntity(jsonString,"utf-8");
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

        }catch (Exception e){

        }
    }



}
