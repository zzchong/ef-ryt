package com.efeiyi.ec.art.artwork.controller;


import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtworkManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.*;
import com.efeiyi.ec.art.message.dao.MessageDao;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.efeiyi.ec.art.organization.util.CommonUtil;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.p.service.AliOssUploadManager;
import net.sf.json.JSON;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
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
    public Map getAddressView(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("addressView");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            ConsumerAddress consumerAddress = (ConsumerAddress) baseManager.getObject(ConsumerAddress.class.getName(), jsonObj.getString("addressId"));
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("consumerAddress", consumerAddress);

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
            /*if (!CommonUtil.jsonObject(jsonObj, "addressId")) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }*/

            ConsumerAddress consumerAddress = artworkManager.saveConsumerAddress(jsonObj, request);

            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);

            resultMap.put("object", consumerAddress);

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 收货地址列表接口（不带分页）
     *
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
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkAuctionPayDeposit");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            XQuery xQuery = new XQuery("listAddress_default", request);
            xQuery.put("consumer_id", AuthorizationUtil.getUserId());
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


    /**
     * 删除收货地址
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/removeAddress.do")
    @ResponseBody
    public Map removeAddress(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("removeAddress");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            ConsumerAddress consumerAddress = (ConsumerAddress) baseManager.getObject(ConsumerAddress.class.getName(), jsonObj.getString("addressId"));
            consumerAddress.setStatus("0");
            baseManager.saveOrUpdate(ConsumerAddress.class.getName(), consumerAddress);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "删除成功");

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 创建地址接口
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/addAddress.do")
    @ResponseBody
    public Map addAddress(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<>();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkAuctionPayDeposit");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            //校验数字签名
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            ConsumerAddress consumerAddress = new ConsumerAddress();

            if (jsonObj.getString("id") != null) {
                consumerAddress = (ConsumerAddress) baseManager.getObject(ConsumerAddress.class.getName(), jsonObj.getString("id"));
            }

            consumerAddress.setConsumer((User) baseManager.getObject(User.class.getName(), jsonObj.getString("currentUserId")));
//            consumerAddress.setProvinceStr(jsonObj.getString("provinceStr"));
            consumerAddress.setCityStr(URLDecoder.decode(jsonObj.getString("cityStr"), "utf-8"));
//            consumerAddress.setDistrictStr(jsonObj.getString("districtStr"));
            consumerAddress.setDetails(URLDecoder.decode(jsonObj.getString("details"), "utf-8"));
            consumerAddress.setPhone(jsonObj.getString("phone"));
            consumerAddress.setConsignee(URLDecoder.decode(jsonObj.getString("consignee"), "utf-8"));
            consumerAddress.setStatus(jsonObj.getString("status"));
            baseManager.saveOrUpdate(ConsumerAddress.class.getName(), consumerAddress);
            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "地址增加成功");

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }


    /**
     * 设置默认地址
     *
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
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkAuctionPayDeposit");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }

            XQuery xQuery = new XQuery("listAddress_default1", request);
            xQuery.put("consumer_id", AuthorizationUtil.getUser().getId());
            List<ConsumerAddress> consumerAddressList = baseManager.listObject(xQuery);
            if (consumerAddressList != null && consumerAddressList.size() > 0) {
                for (ConsumerAddress consumerAddress : consumerAddressList) {
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

    /**
     * 获取默认地址
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/getDefaultAddress.do")
    @ResponseBody
    public MappingJacksonValue getDefaultAddress(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("artWorkAuctionPayDeposit");

            XQuery xQuery = new XQuery("listAddress_default1", request);
            xQuery.put("consumer_id", jsonObj.getString("currentUserId"));
            List<ConsumerAddress> consumerAddressList = baseManager.listObject(xQuery);
            if (consumerAddressList.size() > 1) {
                resultMap.put("resultCode", "10005");
                resultMap.put("resultMsg", "数据查询异常");
            } else if (!consumerAddressList.isEmpty()) {
                resultMap.put("defaultAddress", consumerAddressList.get(0));
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "查询默认地址成功");
            } else {
                resultMap.put("defaultAddress", null);
                resultMap.put("resultCode", "0");
                resultMap.put("resultMsg", "查询默认地址成功");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return resultMapHandler.handlerResultType(request, resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean));
        }

        return resultMapHandler.handlerResultType(request, resultMap);
    }

    /**
     *获取所有省及市
     */
    @RequestMapping(value = "/app/getProvinceInfo.do")
    @ResponseBody
    public Map getAllProvinceInformation(HttpServletRequest request){
        LogBean logBean = new LogBean();
        try {
            logBean.setApiName("getProvinceInfo");
            logBean.setCreateDate(new Date());
            String jsonContext = ContextUtils.ReadFile(Thread.currentThread().getContextClassLoader().getResource("").getPath() + "setting/province.json");
            JSON json = net.sf.json.JSONObject.fromObject(jsonContext);
            return (Map) json;
        }catch (Exception e){
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
    }


}