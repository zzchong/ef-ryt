package com.efeiyi.ec.art.artwork.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.PaymentManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.model.User;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.service.AutoSerialManager;
import com.ming800.core.util.CookieTool;
import net.sf.json.JSON;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
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
 */
@Controller
public class PaymentController extends BaseController {
    private static Logger logger = Logger.getLogger(PaymentController.class);
    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    BaseManager baseManager;

    @Autowired
    private AutoSerialManager autoSerialManager;

    @Autowired
    PaymentManager paymentManager;

    private final static String TITLE_AUCTION = "融易投-竞价";

    private final static String TITLE_ADD = "融易投-充值";

    private final static String TITLE_INVEST = "融易投-融资";

    @RequestMapping(value = "/app/webhoot.do", method = RequestMethod.POST)
    @ResponseBody
    public void checkOrderPay(HttpServletRequest request, HttpServletResponse response) {

        LogBean logBean = new LogBean();
        logBean.setApiName("webhoot");
//        Map<String, String> resultMap = new HashMap<String, String>();
        StringBuffer json = new StringBuffer();
        String line;

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

        PrintWriter out = null;
        try {
            out = response.getWriter();//给客户端返回数据
            if (!status) {
                out.println("fail");
                return;
            }//验证成功
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

            //先生成充值记录，status=2，再等待Beecloud的webhook回调，
            // 待测
            //optional参数
            JSONObject jsonObject = (JSONObject)JSONObject.toJSON(jsonObj.get("optional"));
            //竞拍 融资 充值
            String action = jsonObject.getString("action");
            //订单号 bill_no
            String id = (String) jsonObj.get("transaction_id");
            //金额
            BigDecimal transactionFee = new BigDecimal((double) jsonObj.get("transaction_fee") / 100);
            if(action.equals("auction")){
                ArtworkBidding artworkBidding = (ArtworkBidding) baseManager.getObject(ArtworkBidding.class.getName(), id);
                if (artworkBidding == null) {
                    out.println("fail");
                    return;
                }
                if(!artworkBidding.getPrice().equals(transactionFee)){
                    out.println("fail");
                    return;
                }
                artworkBidding.setStatus("1");
                BigDecimal balance = artworkBidding.getAccount().getCurrentBalance();
                BigDecimal usableBalance = artworkBidding.getAccount().getCurrentUsableBalance();
                artworkBidding.getAccount().setCurrentBalance(balance.add(transactionFee));
                artworkBidding.getAccount().setCurrentUsableBalance(usableBalance.add(transactionFee));
                artworkBidding.setCreateDatetime(new Date());
                baseManager.saveOrUpdate(ArtworkBidding.class.getName(), artworkBidding);
            }else if(action.equals("add")){
                RechargeRecord rechargeRecord = (RechargeRecord) baseManager.getObject(RechargeRecord.class.getName(), id);
                if (rechargeRecord == null) {
                    out.println("fail");
                    return;
                }
                if(!rechargeRecord.getCurrentBalance().equals(transactionFee)){
                    out.println("fail");
                    return;
                }
                rechargeRecord.setStatus("1");
                rechargeRecord.setCreateDatetime(new Date());
                BigDecimal balance = rechargeRecord.getAccount().getCurrentBalance();
                BigDecimal usableBalance = rechargeRecord.getAccount().getCurrentUsableBalance();
                rechargeRecord.getAccount().setCurrentBalance(balance.add(transactionFee));
                rechargeRecord.getAccount().setCurrentUsableBalance(usableBalance.add(transactionFee));
                baseManager.saveOrUpdate(RechargeRecord.class.getName(), rechargeRecord);

            }else if(action.equals("invest")){
                ArtworkInvest artworkInvest = (ArtworkInvest) baseManager.getObject(ArtworkInvest.class.getName(), id);
                if (artworkInvest == null) {
                    out.println("fail");
                    return;
                }
                if(!artworkInvest.getPrice().equals(transactionFee)){
                    out.println("fail");
                    return;
                }
                artworkInvest.setStatus("1");
                artworkInvest.setCreateDatetime(new Date());
                BigDecimal balance = artworkInvest.getAccount().getCurrentBalance();
                BigDecimal usableBalance = artworkInvest.getAccount().getCurrentUsableBalance();
                artworkInvest.getAccount().setCurrentBalance(balance.add(transactionFee));
                artworkInvest.getAccount().setCurrentUsableBalance(usableBalance.add(transactionFee));
                baseManager.saveOrUpdate(ArtworkInvest.class.getName(), artworkInvest);
            }

            out.println("success"); //请不要修改或删除
        } catch (Exception e) {
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
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("userId", jsonObj.getString("userId"));
            treeMap.put("price", jsonObj.getString("price"));
            treeMap.put("payWay", jsonObj.getString("payWay"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }
            // 查出账户ID
            // 生成充值记录
            LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
            param.put("userId", jsonObj.getString("userId"));
            Account account = (Account) baseManager.getUniqueObjectByConditions(AppConfig.SQL_GET_USER_ACCOUNT, param);
            RechargeRecord rechargeRecord = new RechargeRecord();
            rechargeRecord.setUser(account.getUser());
            rechargeRecord.setCreateDatetime(new Date());
            rechargeRecord.setType(jsonObj.getString("payWay"));
            rechargeRecord.setCurrentBalance(new BigDecimal(jsonObj.getString("price")));
            rechargeRecord.setAccount(account);
            rechargeRecord.setStatus("2");
            rechargeRecord.setDetails(account.getUser().getUsername() + ":" + account.getId() + ":" + jsonObj.getString("price"));
            baseManager.saveOrUpdate(RechargeRecord.class.getName(), rechargeRecord);
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("rechargeRecord", rechargeRecord);
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }
        return resultMap;

    }


    //发送支付信息

    /**
     * type 用来判断是融资 充值 竞价 订单
     *
     * @param request
     * @param modelMap
     * @return
     * @throws Exception
     */
    @RequestMapping("/app/pay/main.do")
    public String orderInfo(HttpServletRequest request, ModelMap modelMap) throws Exception {
        String resultHtml;
        String title = "";
        String billNo = "";
        //optional 参数
        Map<String, Object> map = new HashMap<>();
        map.put("action", request.getParameter("action"));
        //金额
        BigDecimal money = new BigDecimal(request.getParameter("money"));
        //支付类型
        String type = request.getParameter("type");
        //用户Id
        String userId = request.getParameter("userId");
        if(StringUtils.isEmpty(userId)){
           return "";
        }

        //用户
        User user = (User) baseManager.getObject(User.class.getName(),userId);
        LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
        param.put("userId", userId);
        //账户
        Account account = (Account) baseManager.getUniqueObjectByConditions(AppConfig.SQL_GET_USER_ACCOUNT, param);

        if (request.getParameter("action").equals("auction")) {

            //竞拍项目Id
            String artWorkId = request.getParameter("artWorkId");
           if(StringUtils.isEmpty(artWorkId))
               return "";

            ArtworkBidding artworkBidding = new ArtworkBidding();
            artworkBidding.setStatus("0");
            artworkBidding.setAccount(account);
            artworkBidding.setCreator(user);
            artworkBidding.setType(type);
            artworkBidding.setArtwork((Artwork)baseManager.getObject(Artwork.class.getName(),artWorkId));
            artworkBidding.setPrice(money);
            baseManager.saveOrUpdate(ArtworkBidding.class.getName(),artworkBidding);
            title = TITLE_AUCTION;
            billNo = artworkBidding.getId();
        } else if (request.getParameter("action").equals("invest")) {

            //融资项目Id
            String artWorkId = request.getParameter("artWorkId");
            if(StringUtils.isEmpty(artWorkId))
                return "";

            ArtworkInvest artworkInvest = new ArtworkInvest();
            artworkInvest.setCreator(user);
            artworkInvest.setPrice(money);
            artworkInvest.setStatus("0");
            artworkInvest.setArtwork((Artwork)baseManager.getObject(Artwork.class.getName(),artWorkId));
            artworkInvest.setType(type);
            artworkInvest.setAccount(account);
            baseManager.saveOrUpdate(ArtworkInvest.class.getName(),artworkInvest);
            title = TITLE_INVEST;
            billNo = artworkInvest.getId();
        } else if (request.getParameter("action").equals("add")) {

            RechargeRecord rechargeRecord = new RechargeRecord();
            rechargeRecord.setAccount(account);
            rechargeRecord.setStatus("0");
            rechargeRecord.setType(type);
            rechargeRecord.setCurrentBalance(money);
            rechargeRecord.setUser(user);
            baseManager.saveOrUpdate(RechargeRecord.class.getName(),rechargeRecord);
            title = TITLE_ADD;
            billNo = rechargeRecord.getId();
        }
        resultHtml = paymentManager.payBCOrder(billNo, title, money, map);
        modelMap.put("resultHtml", resultHtml);
        return "pay";
    }

    @RequestMapping("/app/pay/paysuccess.do")
    public String paysuccess(HttpServletRequest request) {
        return "paySuccess";
    }

}



