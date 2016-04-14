package com.efeiyi.ec.art.artwork.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.organization.model.MyUser;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Administrator on 2016/4/14.
 *
 */
public class ArtworkInvestController extends BaseController {
    private static Logger logger = Logger.getLogger(ArtworkInvestController.class);

    @Autowired
    BaseManager baseManager;
    @Autowired
    ResultMapHandler resultMapHandler;
     private Lock lock = new ReentrantLock();

    /**
     * 用户投资接口，此接口不涉及微信、支付宝
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/artworkInvest.do", method = RequestMethod.POST)
    @ResponseBody
    public Map login(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("artworkInvest");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("userId"))
                    || "".equals(jsonObj.getString("artworkId"))  || "".equals(jsonObj.getString("price"))
                    || "".equals(jsonObj.getString("timestamp"))) {

                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("userId", jsonObj.getString("userId"));
            treeMap.put("artworkId", jsonObj.getString("artworkId"));
            treeMap.put("price", jsonObj.getString("price"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {

                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }


            lock.lock();//加锁 可能会涉及修改项目状态
            try {
                MyUser user = (MyUser) baseManager.getObject(MyUser.class.getName(),jsonObj.getString("userId"));
                if (user == null && user.getId()!= null) {//用户存在
                    //验证项目
                    Artwork artwork = (Artwork)baseManager.getObject(Artwork.class.getName(),jsonObj.getString("artworkId"));
                    if(artwork!= null && artwork.getId() != null){
                        //判断项目是否是融资阶段  是否是融资中 状态是否是可用

                       if ("1".equals(artwork.getType()) && "14".equals(artwork.getStep()) && "0".equals(artwork.getStatus())){
                           BigDecimal price = new BigDecimal(jsonObj.getString("price"));
                           // 查询当前融资总额

                       }
                    }



                }else{

                    resultMap = resultMapHandler.handlerResult("10007","用户名不存在",logBean);
                }
            } catch (Exception e) {

                resultMap = resultMapHandler.handlerResult("10005","查询数据出现异常",logBean);
            }finally {
                lock.unlock();
            }
        } catch (Exception e) {

            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
        return resultMap;
    }
}
