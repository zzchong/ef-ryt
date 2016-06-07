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
import com.efeiyi.ec.art.organization.model.AddressCity;
import com.efeiyi.ec.art.organization.model.AddressDistrict;
import com.efeiyi.ec.art.organization.model.AddressProvince;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2016/1/29.
 */
@Controller
public class AddressController extends BaseController {
    private static Logger logger = Logger.getLogger(AddressController.class);

    @Autowired
    private MessageDao messageDao;

    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    AliOssUploadManager aliOssUploadManager;

    @Autowired
    private ArtworkManager artworkManager;


    /**
     * 查看收货地址 接口
     *
     * @param request
     * @return
     */

    @RequestMapping(value = "/app/addressView.do")
    @ResponseBody
    public Map getAddressList(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        List<Artwork> artworkList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("addressView");
            if (CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
//            //校验数字签名
//            String signmsg = jsonObj.getString("signmsg");
//            treeMap.put("pageSize", jsonObj.getString("pageSize"));
//            treeMap.put("pageNum", jsonObj.getString("pageNum"));
//            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            Map<String, Object> data = new HashMap<>();

            PageEntity pageEntity = new PageEntity();
            pageEntity.setIndex(jsonObj.getInteger("pageIndex"));
            pageEntity.setSize(jsonObj.getInteger("pageSize"));
            XQuery xQuery = new XQuery("plistConsumerAddress_default", request);
            xQuery.put("consumer_id", jsonObj.getString("userId"));
            xQuery.setPageEntity(pageEntity);
            PageInfo pageInfo = baseManager.listPageInfo(xQuery);

            data.put("addressList", pageInfo.getList());

            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);

            resultMap.put("data", data);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 保存编辑 收货地址
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/saveAddress.do", method = RequestMethod.POST)
    @ResponseBody
    public Map saveAddress(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        List objectList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("saveAddress");
            if (!CommonUtil.jsonObject(jsonObj, "addressId")) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            ConsumerAddress consumerAddress = null;
            if (StringUtils.isEmpty(jsonObj.getString("addressId")))
                consumerAddress = new ConsumerAddress();
            else
                consumerAddress = (ConsumerAddress) baseManager.getObject(ConsumerAddress.class.getName(), jsonObj.getString("addressId"));

            consumerAddress.setEmail(jsonObj.getString("email"));
            consumerAddress.setStatus(jsonObj.getString("status"));
//            consumerAddress.setCity((AddressCity)baseManager.getObject(AddressCity.class.getName(),jsonObj.getString("cityId")));
            consumerAddress.setConsignee(jsonObj.getString("consignee"));
            consumerAddress.setConsumer((User) baseManager.getObject(User.class.getName(), jsonObj.getString("userId")));
            consumerAddress.setDetails(jsonObj.getString("details"));
//            consumerAddress.setDistrict((AddressDistrict)baseManager.getObject(AddressCity.class.getName(),jsonObj.getString("districtId")));
            consumerAddress.setPhone(jsonObj.getString("phone"));
//            consumerAddress.setPost(jsonObj.getString("post"));
//            consumerAddress.setProvince((AddressProvince) baseManager.getObject(AddressCity.class.getName(),jsonObj.getString("provinceId")));
            baseManager.saveOrUpdate(ConsumerAddress.class.getName(), consumerAddress);

            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);

            resultMap.put("object", consumerAddress);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**收货地址列表接口（不带分页）
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/listAddress.do")
    @ResponseBody
    public Map listAddress(HttpServletRequest request) {
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
            treeMap.put("currentUserId", jsonObj.getString("currentUserId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            XQuery xQuery = new XQuery("listAddress_default", request);
            xQuery.put("consumer_id", jsonObj.getString("currentUserId"));
            List<ConsumerAddress> consumerAddressList = baseManager.listObject(xQuery);
            resultMap.put("consumerAddressList", consumerAddressList);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "获取地址列表成功");

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**创建地址接口
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/addAddress.do")
    @ResponseBody
    public Map addAddress(HttpServletRequest request) {
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
            treeMap.put("currentUserId", jsonObj.getString("currentUserId"));
            treeMap.put("provinceStr", jsonObj.getString("provinceStr"));
            treeMap.put("cityStr", jsonObj.getString("cityStr"));
            treeMap.put("districtStr", jsonObj.getString("districtStr"));
            treeMap.put("details", jsonObj.getString("details"));
            treeMap.put("phone", jsonObj.getString("phone"));
            treeMap.put("consignee", jsonObj.getString("consignee"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            ConsumerAddress consumerAddress = new ConsumerAddress();
            consumerAddress.setConsumer((User) baseManager.getObject(User.class.getName(), jsonObj.getString("currentUserId")));
            consumerAddress.setProvinceStr(jsonObj.getString("provinceStr"));
            consumerAddress.setCityStr(jsonObj.getString("cityStr"));
            consumerAddress.setDistrictStr(jsonObj.getString("districtStr"));
            consumerAddress.setDetails(jsonObj.getString("details"));
            consumerAddress.setPhone(jsonObj.getString("phone"));
            consumerAddress.setConsignee(jsonObj.getString("consignee"));
            consumerAddress.setStatus("1");
            baseManager.saveOrUpdate(ConsumerAddress.class.getName(), consumerAddress);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "地址增加成功");

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**设置默认地址
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/setDefaultAddress.do")
    @ResponseBody
    public Map setDefaultAddress(HttpServletRequest request) {
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
            treeMap.put("currentUserId", jsonObj.getString("currentUserId"));
            treeMap.put("consumerAddressId", jsonObj.getString("consumerAddressId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            XQuery xQuery = new XQuery("listAddress_default1", request);
            xQuery.put("consumer_id", jsonObj.getString("currentUserId"));
            List<ConsumerAddress> consumerAddressList = baseManager.listObject(xQuery);
            if(consumerAddressList != null && consumerAddressList.size()>0){
                for(ConsumerAddress consumerAddress:consumerAddressList){
                    consumerAddress.setStatus("1");
                    baseManager.saveOrUpdate(ConsumerAddress.class.getName(), consumerAddress);
                }
            }
            ConsumerAddress consumerAddress = (ConsumerAddress) baseManager.getObject(ConsumerAddress.class.getName(), jsonObj.getString("consumerAddressId"));
            consumerAddress.setStatus("2");
            baseManager.saveOrUpdate(ConsumerAddress.class.getName(), consumerAddress);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "设置默认地址成功");

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**获取默认地址
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/getDefaultAddress.do")
    @ResponseBody
    public Map getDefaultAddress(HttpServletRequest request) {
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
            treeMap.put("currentUserId", jsonObj.getString("currentUserId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            XQuery xQuery = new XQuery("listAddress_default1", request);
            xQuery.put("consumer_id", jsonObj.getString("currentUserId"));
            List<ConsumerAddress> consumerAddressList = baseManager.listObject(xQuery);
            if (consumerAddressList.size() != 1){
                resultMap.put("resultCode", "10005");
                resultMap.put("resultMsg", "数据查询异常");
            }else {
                resultMap.put("defaultAddress", consumerAddressList.get(0));
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "查询默认地址成功");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

}