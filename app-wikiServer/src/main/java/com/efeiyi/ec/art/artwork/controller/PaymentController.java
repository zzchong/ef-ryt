package com.efeiyi.ec.art.artwork.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.PaymentManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.Account;
import com.efeiyi.ec.art.model.RechargeRecord;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.util.CookieTool;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2016/4/12.
 *
 */
@Controller
public class PaymentController  extends BaseController {
    private static Logger logger = Logger.getLogger(PaymentController.class);
    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    BaseManager baseManager;

    @Autowired
    PaymentManager paymentManager;

    @RequestMapping(value = "/app/checkOrderPay.do", method = RequestMethod.POST)
    @ResponseBody
    public void checkOrderPay(HttpServletRequest request, HttpServletResponse response) {

        LogBean logBean = new LogBean();
        logBean.setApiName("checkOrderPay");
        Map<String, String> resultMap = new HashMap<String, String>();
        StringBuffer json = new StringBuffer();
        String line = null;

        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        net.sf.json.JSONObject jsonObj = net.sf.json.JSONObject.fromObject(json.toString());

        String sign = jsonObj.getString("sign");
        String timestamp = jsonObj.getString("timestamp");

        boolean status = paymentManager.verifySign(sign, timestamp);

        PrintWriter out=null;
        try{
             out = response.getWriter();//给客户端返回数据
            if (status) { //验证成功
        //  获取订单Id
        //1 校验金额
        //2 校验账户
        //3 校验关键字
        //4 校验详情

        /*   此处需要验证购买的产品与订单金额是否匹配:
         验证购买的产品与订单金额是否匹配的目的在于防止黑客反编译了iOS或者Android app的代码，
         将本来比如100元的订单金额改成了1分钱，开发者应该识别这种情况，避免误以为用户已经足额支付。
         Webhook传入的消息里面应该以某种形式包含此次购买的商品信息，比如title或者optional里面的某个参数说明此次购买的产品是一部iPhone手机，
         开发者需要在客户服务端去查询自己内部的数据库看看iPhone的金额是否与该Webhook的订单金额一致，仅有一致的情况下，才继续走正常的业务逻辑。
         如果发现不一致的情况，排除程序bug外，需要去查明原因，防止不法分子对你的app进行二次打包，对你的客户的利益构成潜在威胁。
         如果发现这样的情况，请及时与我们联系，我们会与客户一起与这些不法分子做斗争。而且即使有这样极端的情况发生，
         只要按照前述要求做了购买的产品与订单金额的匹配性验证，在你的后端服务器不被入侵的前提下，你就不会有任何经济损失。
         处理业务逻辑*/

             out.println("success"); //请不要修改或删除

            } else { //验证失败
                out.println("fail");
            }

         }catch (Exception e){
            out.println("fail");
        }


    }

    //生成充值记录详情
    @RequestMapping(value = "/app/makeRecharge.do", method = RequestMethod.POST)
    @ResponseBody
    public Map makeRecharge(HttpServletRequest request, HttpServletResponse response) {

        LogBean logBean = new LogBean();
        logBean.setApiName("makeRecharge");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg"))
                    || "".equals(jsonObj.getString("userId"))
                    || "".equals(jsonObj.getString("timestamp"))
                    || "".equals(jsonObj.getString("payWay"))
                    || "".equals(jsonObj.getString("price"))) {
                return resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("userId", jsonObj.getString("userId"));
            treeMap.put("price", jsonObj.getString("price"));
            treeMap.put("payWay", jsonObj.getString("payWay"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            // 查出账户ID
            // 生成充值记录
            LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
            param.put("userId", jsonObj.getString("userId"));
            Account account =  (Account)baseManager.getUniqueObjectByConditions(AppConfig.SQL_GET_USER_ACCOUNT, param);
            RechargeRecord rechargeRecord = new RechargeRecord();
            rechargeRecord.setUser(account.getUser());
            rechargeRecord.setCreateDatetime(new Date());
            rechargeRecord.setType(jsonObj.getString("payWay"));
            rechargeRecord.setCurrentBalance(new BigDecimal(jsonObj.getString("price")));
            rechargeRecord.setAccount(account);
            rechargeRecord.setStatus("2");
            rechargeRecord.setDetails(account.getUser().getUsername()+":"+account.getId()+":"+jsonObj.getString("price"));
            baseManager.saveOrUpdate(RechargeRecord.class.getName(),rechargeRecord);
            resultMap = resultMapHandler.handlerResult("0","成功",logBean);
            resultMap.put("rechargeRecord",rechargeRecord);
        } catch(Exception e){
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
        return resultMap;

    }
}
