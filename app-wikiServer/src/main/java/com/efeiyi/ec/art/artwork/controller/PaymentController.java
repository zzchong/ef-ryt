package com.efeiyi.ec.art.artwork.controller;

import cn.beecloud.bean.BCOrder;
import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.PaymentManager;
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
import com.ming800.core.does.model.XQuery;
import com.ming800.core.p.service.AutoSerialManager;
import com.ming800.core.taglib.PageEntity;
import com.ming800.core.util.CookieTool;
import com.ming800.core.util.StringUtil;
import net.sf.json.JSON;
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
import org.junit.Test;
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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
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

    private final static String TITLE_AUCTION = "项目尾款";

    private final static String TITLE_ADD = "保证金";

    private final static String TITLE_INVEST = "投资";

    private final static String ACCOUNT_INVEST = "余额投资";

    @RequestMapping(value = "/app/webhoot.do", method = RequestMethod.POST)
    @ResponseBody
    public void checkOrderPay(HttpServletRequest request, HttpServletResponse response) {

        LogBean logBean = new LogBean();
        logBean.setApiName("webhoot");
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
        System.out.println(jsonObj);
        String sign = jsonObj.getString("sign");
        String timestamp = jsonObj.getString("timestamp");
        System.out.println("签名："+sign+"--------"+"时间戳："+timestamp);
        boolean status = paymentManager.verifySign(sign, timestamp);
        System.out.println("校验:"+status);
        PrintWriter out = null;
        try {
            out = response.getWriter();//给客户端返回数据
            if (!status) {
                out.println("status is false");
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
            //账单Id
            String billId = jsonObject.getString("Bill_id");
            //是否融资成功
            boolean isOk = jsonObject.getBoolean("isOk");


            //订单号 bill_no
            String id = (String) jsonObj.get("transaction_id");

            System.out.println("jsonObject:"+jsonObject+"----action:"+action+"---billId:"+billId+"------isOk:"+isOk);
            System.out.println("money is :"+jsonObj.getDouble("transaction_fee"));
            //金额
            DecimalFormat df=new DecimalFormat("0.00");
            BigDecimal transactionFee = new BigDecimal(df.format(jsonObj.getDouble("transaction_fee") / 100));
            System.out.println(transactionFee);
            if(action.equals("auction")){
                AuctionOrder auctionOrder = (AuctionOrder) baseManager.getObject(AuctionOrder.class.getName(), id);
                if (auctionOrder == null) {
                    out.println("order is null");
                    return;
                }
                if(!auctionOrder.getFinalPayment().equals(transactionFee)){
                    out.println("money is not equal");
                    return;
                }
                auctionOrder.setType("1");
                auctionOrder.setPayWay("1");
                auctionOrder.setPayStatus("0");
                auctionOrder.setStatus("1");
                baseManager.saveOrUpdate(AuctionOrder.class.getName(), auctionOrder);
            }else if(action.equals("payMargin")){
                MarginAccount marginAccount = (MarginAccount) baseManager.getObject(MarginAccount.class.getName(), id);
                if (marginAccount == null) {
                    out.println("payMargin is null");
                    return;
                }
                if(!marginAccount.getCurrentBalance().equals(transactionFee)){
                    out.println("money is not rich");
                    return;
                }
                marginAccount.setStatus("1");
                marginAccount.setCreateDatetime(new Date());
//                BigDecimal balance = rechargeRecord.getAccount().getCurrentBalance();
//                BigDecimal usableBalance = rechargeRecord.getAccount().getCurrentUsableBalance();
//                rechargeRecord.getAccount().setCurrentBalance(balance.add(transactionFee));
//                rechargeRecord.getAccount().setCurrentUsableBalance(usableBalance.add(transactionFee));
                baseManager.saveOrUpdate(MarginAccount.class.getName(), marginAccount);

            }else if(action.equals("invest")){
                System.out.println("come in");
                ArtworkInvest artworkInvest = (ArtworkInvest) baseManager.getObject(ArtworkInvest.class.getName(), id);
                if (artworkInvest == null) {
                    System.out.println("artworkInvest is null");
                    out.println("artworkInvest is null");
                    return;
                }
                if(!artworkInvest.getPrice().equals(transactionFee)){
                    System.out.println("money is not equal");
                    out.println("money is not equal");
                    return;
                }
                artworkInvest.setStatus("1");
//                artworkInvest.setCreateDatetime(new Date());
                baseManager.saveOrUpdate(ArtworkInvest.class.getName(), artworkInvest);

                if(isOk){
                    String artworkId = jsonObject.getString("artworkId");
                    Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(),artworkId);
                    artwork.setType("2");
                    artwork.setStep("21");
                    baseManager.saveOrUpdate(Artwork.class.getName(),artwork);
                }
            }else if(action.equals("account")){
                Account account = (Account) baseManager.getObject(Account.class.getName(),id);
                account.setCurrentBalance(account.getCurrentBalance().add(transactionFee));
                baseManager.saveOrUpdate(Account.class.getName(),account);
            }

            Bill bill = (Bill) baseManager.getObject(Bill.class.getName(),billId);
            bill.setCreateDatetime(new Date());
            bill.setStatus("1");
            baseManager.saveOrUpdate(Bill.class.getName(),bill);
            System.out.println("success");
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
                    || "".equals(jsonObj.getString("timestamp"))
                    || "".equals(jsonObj.getString("payWay"))
                    || "".equals(jsonObj.getString("price"))) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            String signmsg = jsonObj.getString("signmsg");
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
            param.put("userId", AuthorizationUtil.getUser()==null?"":AuthorizationUtil.getUser().getId());
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


   /* //发送支付信息

    *//**
     * action 用来判断是投资支付(invest) 支付保证金(payMargin) 拍卖尾款支付(auction)
     * 充值(暂不支持)
     * @param request
     * @param modelMap
     * @return
     * @throws Exception
     *//*
    @RequestMapping("/app/pay/main.do")
    @ResponseBody
    public Map payMain(HttpServletRequest request, ModelMap modelMap) throws Exception {
        String url;
        String title = "";//订单名称
        String billNo = "";//订单号

        boolean isOk = false;//投资时使用 是否投资完成
        JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);

        LogBean logBean = new LogBean();
        logBean.setApiName("makeRecharge");
        logBean.setCreateDate(new Date());
        logBean.setRequestMessage(jsonObj.toString());

        Map<String,Object> data = new HashMap<>();
        //optional 参数
        Map<String, Object> map = new HashMap<>();
        map.put("action", jsonObj.getString("action"));

        //支付类型
        String type = jsonObj.getString("type");
        //用户Id
        String userId = AuthorizationUtil.getUser()==null?"":AuthorizationUtil.getUser().getId();
        if(StringUtils.isEmpty(userId)){
           return resultMapHandler.handlerResult("10002", "用户为空", logBean);
        }
        //用户
//        User user = (User) baseManager.getObject(User.class.getName(),userId);
        User user = AuthorizationUtil.getUser();
        //支付金额
        BigDecimal money = new BigDecimal("0.00");
        //获取账户信息参数
        LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
        param.put("userId", userId);
        //账户
        Account account = (Account) baseManager.getUniqueObjectByConditions(AppConfig.SQL_GET_USER_ACCOUNT, param);
        //生成支出账单
        Bill bill = new Bill();
        bill.setStatus("0");
        bill.setAuthor(user);
        bill.setPayWay("1");//支付方式为"支付宝"
        bill.setOutOrIn("0");
        bill.setCreateDatetime(new Date());
        //项目Id
        String artWorkId = jsonObj.getString("artWorkId");
        Artwork artwork = null;
        if(!StringUtils.isEmpty(artWorkId))
            artwork = (Artwork)baseManager.getObject(Artwork.class.getName(),artWorkId);

        if (jsonObj.getString("action").equals("auction")) {//拍卖订单尾款支付


            //收货地址
            ConsumerAddress consumerAddress = null;
            XQuery xQuery = new XQuery("listAddress_default1",request);
            xQuery.put("consumer_id",userId);
            List<ConsumerAddress> consumerAddressList = baseManager.listObject(xQuery);
            if(consumerAddressList!=null && consumerAddressList.size()>0)
                consumerAddress = consumerAddressList.get(0);


            if(consumerAddress==null)
                return resultMapHandler.handlerResult("10002", "收货地址为空", logBean);

            //保证金
            xQuery = new XQuery("listMarginAccount_default2",request);
            xQuery.put("artwork_id",jsonObj.getString("artWorkId"));
            xQuery.put("user_id",userId);
            List<MarginAccount> marginAccountList = baseManager.listObject(xQuery);

            //项目订单Id
            AuctionOrder auctionOrder = null;
            if(!StringUtils.isEmpty(jsonObj.getString("orderId"))) {
                auctionOrder = (AuctionOrder) baseManager.getObject(AuctionOrder.class.getName(), jsonObj.getString("orderId"));
                if(auctionOrder==null)
                    return resultMapHandler.handlerResult("10002", "项目订单为空", logBean);
                money = auctionOrder.getFinalPayment();

            }
            else {

                //拍卖金额
                BigDecimal auctionMoney = new BigDecimal("0.00");
                if(artwork!=null)
                    auctionMoney = artwork.getNewBidingPrice();

                if(marginAccountList!=null && marginAccountList.size()>0)
                    money = auctionMoney.subtract(marginAccountList.get(0).getCurrentBalance());

                auctionOrder = new AuctionOrder();
                auctionOrder.setArtwork(artwork);
                auctionOrder.setFinalPayment(money);
                auctionOrder.setAmount(auctionMoney);
                auctionOrder.setType("0");
                auctionOrder.setStatus("1");
                auctionOrder.setPayWay("0");
                auctionOrder.setPayStatus("3");
                auctionOrder.setUser(user);

            }
            auctionOrder.setConsumerAddress(consumerAddress);
            auctionOrder.setCreateDatetime(new Date());
            baseManager.saveOrUpdate(AuctionOrder.class.getName(), auctionOrder);


            title = TITLE_AUCTION+"-"+auctionOrder.getArtwork().getTitle();
            billNo = auctionOrder.getId();

            bill.setDetail(user.getName()+"向项目<<"+auctionOrder.getArtwork().getTitle()+">>支付尾款:"+money);
            bill.setType("2");
            bill.setRestMoney(account.getCurrentUsableBalance());
            bill.setMoney(money);
        } else if (jsonObj.getString("action").equals("invest")) {//投资

             money = new BigDecimal(jsonObj.getString("money"));

            if(StringUtils.isEmpty(artWorkId))
                return resultMapHandler.handlerResult("10002", "项目为空", logBean);

            if(artwork.getInvestsMoney().add(money).compareTo(artwork.getInvestGoalMoney())==1){
                return resultMapHandler.handlerResult("10002", "最多只能投资"+artwork.getInvestGoalMoney().subtract(artwork.getInvestsMoney()), logBean);
            }
            if(artwork.getInvestsMoney().add(money).compareTo(artwork.getInvestGoalMoney())==0){
                isOk = true;
            }

            ArtworkInvest artworkInvest = new ArtworkInvest();
            artworkInvest.setCreator(user);
            artworkInvest.setPrice(money);
            artworkInvest.setStatus("0");
            artworkInvest.setArtwork((Artwork)baseManager.getObject(Artwork.class.getName(),artWorkId));
            artworkInvest.setType(type);
            artworkInvest.setAccount(account);
            artworkInvest.setCreateDatetime(new Date());
            baseManager.saveOrUpdate(ArtworkInvest.class.getName(),artworkInvest);

            title = TITLE_INVEST+"-"+artwork.getTitle();
            billNo = artworkInvest.getId();

            bill.setDetail(user.getName()+"向项目<<"+artwork.getTitle()+">>投资:"+money);
            bill.setType("1");
            bill.setRestMoney(account.getCurrentUsableBalance());
            bill.setMoney(money);
        } else if (jsonObj.getString("action").equals("payMargin")) {//支付保证金

            money = new BigDecimal(jsonObj.getString("money"));
            //投入保证金的项目Id
//            String artWorkId = jsonObj.getString("artWorkId");
//            Artwork artwork = (Artwork)baseManager.getObject(Artwork.class.getName(),artWorkId);
            if(StringUtils.isEmpty(artWorkId))
                return resultMapHandler.handlerResult("10002", "项目为空", logBean);

            MarginAccount marginAccount = null;
            XQuery xQuery = new XQuery("listMarginAccount_default2",request);
            xQuery.put("artwork_id",jsonObj.getString("artWorkId"));
            xQuery.put("user_id",userId);
            List<MarginAccount> marginAccountList = baseManager.listObject(xQuery);

            if(marginAccountList!=null && marginAccountList.size()>0){
                data.put("resultCode","100022");
                data.put("resultMsg","已缴纳保证金,重复提交");
                return data;
            }
            else{
                marginAccount =  new MarginAccount();
            }

            marginAccount.setStatus("0");
            marginAccount.setAccount(account);
            marginAccount.setArtwork(artwork);
            marginAccount.setCurrentBalance(money);
            marginAccount.setUser(user);
            baseManager.saveOrUpdate(MarginAccount.class.getName(),marginAccount);
            title = TITLE_ADD+"-"+artwork.getTitle();
            billNo = marginAccount.getId();

            bill.setDetail(user.getName()+"向项目<<"+artwork.getTitle()+">>支付竞拍保证金："+money);
            bill.setType("3");
            bill.setRestMoney(account.getCurrentUsableBalance());
            bill.setMoney(money);
        }else if(jsonObj.getString("action").equals("account")){

            money = new BigDecimal(jsonObj.getString("money"));

            title = "充值";
            billNo = account.getId();

            bill.setDetail("充值："+money);
            bill.setType("0");
            bill.setRestMoney(account.getCurrentUsableBalance());
            bill.setMoney(money);
        }else if(jsonObj.getString("action").equals("investAccount")){//余额投资s
            money = new BigDecimal(jsonObj.getString("money"));
            System.out.println(account.getCurrentBalance().compareTo(money));
            if(account.getCurrentBalance().compareTo(money)<0) {
                 data.put("resultCode","100015");
                 data.put("resultMsg","账户余额不足");
                 return data;
            }

            if(artwork.getInvestsMoney().add(money).compareTo(artwork.getInvestGoalMoney())==1){
                return resultMapHandler.handlerResult("10002", "最多只能投资"+artwork.getInvestGoalMoney().subtract(artwork.getInvestsMoney()), logBean);
            }
            if(artwork.getInvestsMoney().add(money).compareTo(artwork.getInvestGoalMoney())==0){
                isOk = true;
            }



            account.setCurrentBalance(account.getCurrentBalance().subtract(money));
            baseManager.saveOrUpdate(Account.class.getName(),account);

            if(isOk){
                artwork.setType("2");
                artwork.setStep("21");
                baseManager.saveOrUpdate(Artwork.class.getName(),artwork);
            }

            ArtworkInvest artworkInvest = new ArtworkInvest();
            artworkInvest.setCreator(user);
            artworkInvest.setPrice(money);
            artworkInvest.setStatus("1");
            artworkInvest.setArtwork(artwork);
            artworkInvest.setType(type);
            artworkInvest.setAccount(account);
            artworkInvest.setCreateDatetime(new Date());
            baseManager.saveOrUpdate(ArtworkInvest.class.getName(),artworkInvest);


            bill.setDetail(user.getName()+"向项目<<"+artwork.getTitle()+">>投资:"+money);
            bill.setType("1");
            bill.setRestMoney(account.getCurrentUsableBalance());
            bill.setMoney(money);
            bill.setTitle(ACCOUNT_INVEST+"-"+artwork.getTitle());
            baseManager.saveOrUpdate(Bill.class.getName(),bill);

            data.put("resultCode","0");
            data.put("resultMsg","成功");
            return data;
        }
         baseManager.saveOrUpdate(Bill.class.getName(),bill);
         map.put("Bill_id",bill.getId());
         map.put("isOk",isOk);
         if(isOk)
            map.put("artworkId",artwork.getId());
         BCOrder  bcOrder = paymentManager.payBCOrder(billNo, title, money, map ,jsonObj.getString("action"));
          bill.setNumber(bcOrder.getObjectId());
          bill.setTitle(title);
          bill.setFlowAccount(bcOrder.getChannelTradeNo());
          baseManager.saveOrUpdate(Bill.class.getName(),bill);
          data.put("url", bcOrder.getUrl().replace(" ","%20"));
          data.put("resultCode","0");
          data.put("resultMsg","成功");
          modelMap.put("resultHtml",bcOrder.getHtml());
        return data;
    }*/

    //发送支付信息

    /**
     * action 用来判断是投资支付(invest) 支付保证金(payMargin) 拍卖尾款支付(auction)
     * 充值(暂不支持)
     * @param request
     * @param modelMap
     * @return
     * @throws Exception
     */
    @RequestMapping("/app/pay/main.do")
    @ResponseBody
    public Map payMain(HttpServletRequest request, ModelMap modelMap) throws Exception {
        JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);

        LogBean logBean = new LogBean();
        logBean.setApiName("makeRecharge");
        logBean.setCreateDate(new Date());
        logBean.setRequestMessage(jsonObj.toString());

        Map<String,Object> data = new HashMap<>();
        //optional 参数
        Map<String, Object> map = new HashMap<>();
        map.put("action", jsonObj.getString("action"));

        //支付类型
        String type = jsonObj.getString("type");
        //用户Id
        String userId = AuthorizationUtil.getUser()==null?"":AuthorizationUtil.getUser().getId();
        if(StringUtils.isEmpty(userId)){
            return resultMapHandler.handlerResult("10002", "用户为空", logBean);
        }
        //用户
        User user = AuthorizationUtil.getUser();

        //获取账户信息参数
        LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
        param.put("userId", userId);
        //账户
        Account account = (Account) baseManager.getUniqueObjectByConditions(AppConfig.SQL_GET_USER_ACCOUNT, param);
        Artwork artwork = null;
        if (jsonObj.getString("action").equals("account")){
            Map<String, Object> resMap = paymentManager.payByAction(request, artwork, jsonObj.getString("action"), type, user, account, jsonObj);
            BCOrder bcOrder = (BCOrder)resMap.get("bcOrder");
            map.put("url", bcOrder.getUrl().replace(" ","%20"));
            map.put("resultCode","0");
            map.put("resultMsg","成功");
            modelMap.put("resultHtml", bcOrder.getHtml());
            return map;
        }else {
            //项目Id
            String artWorkId = jsonObj.getString("artWorkId");

            if(!StringUtils.isEmpty(artWorkId)){
                artwork = (Artwork)baseManager.getObject(Artwork.class.getName(),artWorkId);
            }
            //校验项目是否为空
            if (artwork == null){
                data.put("resultCode","1002");
                data.put("resultMsg","项目为空");
                return data;
            }
            data = paymentManager.payByAction(request, artwork, jsonObj.getString("action"), type, user, account, jsonObj);
            return data;
        }
    }

    /**支付宝打款接口
     * action 用来判断是)  返还保证金(restoreMargin)  返利(reward)
     * 充值(暂不支持)
     * @param request
     * @param modelMap
     * @return
     * @throws Exception
     */
    @RequestMapping("/app/restore/main.do")
    @ResponseBody
    public Map restoreMain(HttpServletRequest request, ModelMap modelMap) throws Exception {
        String url = "";
        String batchNo = System.currentTimeMillis()+autoSerialManager.nextSerial("BatchNo");//批量付款批号
        JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
        Map<String,Object> data = new HashMap<>();


        //完成拍卖/进行返利的项目Id
        Artwork artwork = (Artwork)baseManager.getObject(Artwork.class.getName(),jsonObj.getString("artWorkId"));
        if(artwork==null)
            return null;

        if (jsonObj.getString("action").equals("restoreMargin")) {//返还保证金

            //项目竞拍得主Id
            String userId = jsonObj.getString("userId");
            if(StringUtils.isEmpty(userId)){
                return null;
            }
            //获取保证金表信息参数
            LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
            param.put("userId", userId);
            param.put("artWorkId",artwork.getId());
            //用户
            User user = (User) baseManager.getObject(User.class.getName(),userId);

            //返还保证金的list
            List<MarginAccount> marginAccountList = (List<MarginAccount>)baseManager.listObject(AppConfig.SQL_Margin_RESTORE,param);
            if(marginAccountList==null)
                return null;
            url = paymentManager.batchReturnMoney(batchNo,marginAccountList,"restoreMargin");

        } else if (jsonObj.getString("action").equals("reward")) {//返利

            //获取保证金表信息参数
            LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
            param.put("artWorkId",artwork.getId());
            List<ROIRecord> roiRecordList = baseManager.listObject(AppConfig.SQL_Margin_REWARD,param);
            if(roiRecordList==null)
                return null;
            url = paymentManager.batchReturnMoney(batchNo,roiRecordList,"reward");


        }
        data.put("url",url);

        return data;
    }

    @RequestMapping("/app/pay/paysuccess.do")
    public String paysuccess(HttpServletRequest request) {
        String billId = request.getParameter("bill_id");
        Bill bill = null;

        if (billId != null){
            bill = (Bill) baseManager.getObject(Bill.class.getName(), billId);
            bill.setStatus("1");
            baseManager.saveOrUpdate(Bill.class.getName(), bill);
        }
        String userId = AuthorizationUtil.getUser().getId();
        //获取账户信息参数
        LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
        param.put("userId", userId);
        //账户
        Account account = (Account) baseManager.getUniqueObjectByConditions(AppConfig.SQL_GET_USER_ACCOUNT, param);
        account.setCurrentBalance(account.getCurrentBalance().add(bill.getMoney()));
        account.setCurrentUsableBalance(account.getCurrentUsableBalance().add(bill.getMoney()));

        return "/paySuccess";
    }


    /**
     * 交易记录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/transactionRecord.do", method = RequestMethod.POST)
    @ResponseBody
    public Map transactionRecord(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        List objectList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("transactionRecord");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }

            String userId = AuthorizationUtil.getUser()==null?"":AuthorizationUtil.getUser().getId();
            LinkedHashMap<String,Object> map = new LinkedHashMap<>();
            map.put("userId",userId);
            //总投资
            BigDecimal investMoney = new BigDecimal("0.00");
            Object tempInvestMoney =  baseManager.getUniqueObjectByConditions(AppConfig.SQL_INVEST_TOTAL,map);
            if(tempInvestMoney!=null){
                investMoney = investMoney.add(new BigDecimal(tempInvestMoney.toString()));
            }

            //总收益
//            BigDecimal rewardMoney = new BigDecimal("0.00");
//            Object tempRewardMoney =  baseManager.getUniqueObjectByConditions(AppConfig.SQL_REWARD_TOTAL,map);
//            if(tempRewardMoney!=null)
//                rewardMoney.add(new BigDecimal(tempRewardMoney.toString()));

            //投资收益
            BigDecimal rewardMoney = new BigDecimal("0.00");
            XQuery xquery = new XQuery("listROIRecord_default",request);
            xquery.put("user_id",userId);
            List<ROIRecord> roiRecordList = (List<ROIRecord>)baseManager.listObject(xquery);
            for (ROIRecord roiRecord : roiRecordList){
                rewardMoney = rewardMoney.add(roiRecord.getCurrentBalance());
            }
            //余额
            BigDecimal restMoney = new BigDecimal("0.00");
            XQuery xQuery1 = new XQuery("listAccount_default",request);
            xQuery1.put("user_id",userId);
            List<Account> accountList = baseManager.listObject(xQuery1);
            if(accountList!=null && accountList.size()>0){
                restMoney = accountList.get(0).getCurrentUsableBalance();
            }

            data.put("restMoney",restMoney);
            data.put("investMoney",investMoney);
            data.put("rewardMoney",rewardMoney);

            XQuery xQuery = new XQuery("plistBill_default", request);
            xQuery.put("author_id",userId);
            PageEntity pageEntity = new PageEntity();
            pageEntity.setSize(jsonObj.getInteger("pageSize"));
            pageEntity.setIndex(jsonObj.getInteger("pageIndex"));
            xQuery.setPageEntity(pageEntity);
            List<Bill> billList = baseManager.listPageInfo(xQuery).getList();

            data.put("billList",billList);

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
     * 保存提现余额
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/getMoney.do", method = RequestMethod.POST)
    @ResponseBody
    public Map getMoney(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        List objectList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("getMoney");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            Account account = null;

//            User user =(User)baseManager.getObject(User.class.getName(),jsonObj.getString("userId"));
            User user = AuthorizationUtil.getUser();
            if(user==null)
                return resultMapHandler.handlerResult("10007", "用户不存在", logBean);
            //余额
            BigDecimal restMoney = new BigDecimal("0.00");

            String hql = "select s from com.efeiyi.ec.art.model.Account s where s.user.id = :userId and s.status <> '0'";
            LinkedHashMap params = new LinkedHashMap<>();
            params.put("userId", user.getId());
            //XQuery xQuery1 = new XQuery("listAccount_default",request);
            //xQuery1.put("user_id",jsonObj.getString("userId"));
            List<Account> accountList = baseManager.listObject(hql, params);
            if(accountList.size()==0){
                return resultMapHandler.handlerResult("10007", "用户不存在", logBean);
            }
            BigDecimal money = new BigDecimal(jsonObj.getString("money"));
            account = accountList.get(0);
            if(money.doubleValue()>account.getCurrentUsableBalance().doubleValue())
                return resultMapHandler.handlerResult("100015", "账户余额不足", logBean);

            //修改账户金额
            account.setCurrentBalance(account.getCurrentBalance().subtract(money));
            account.setCurrentUsableBalance(account.getCurrentUsableBalance().subtract(money));

            Bill bill = new Bill();
            bill.setCreateDatetime(new Date());
            bill.setStatus("1");
            bill.setType("60");
            bill.setDetail("提现金额");
            bill.setOutOrIn("0");
            bill.setPayName(jsonObj.getString("name"));
            bill.setPayNumber(jsonObj.getString("number"));
            bill.setAuthor(user);
            bill.setMoney(money);
            bill.setTitle("融艺投-余额提现");
            bill.setRestMoney(account.getCurrentUsableBalance().subtract(money));
            baseManager.saveOrUpdate(Bill.class.getName(),bill);

            baseManager.saveOrUpdate(Account.class.getName(),account);
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    /**
     * 提现跳转
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/app/toGetMoney.do", method = RequestMethod.POST)
    @ResponseBody
    public Map toGetMoney(HttpServletRequest request) {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        List objectList = null;
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参
            logBean.setCreateDate(new Date());//操作时间
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            logBean.setApiName("toGetMoney");
            if (!CommonUtil.jsonObject(jsonObj)) {
                return resultMapHandler.handlerResult("10001", "必选参数为空，请仔细检查", logBean);
            }
            boolean verify = DigitalSignatureUtil.verify2(jsonObj);
            if (verify != true) {
                return resultMapHandler.handlerResult("10002", "参数校验不合格，请仔细检查", logBean);
            }

            Account account = null;

//            User user =(User)baseManager.getObject(User.class.getName(),jsonObj.getString("userId"));
            User user = AuthorizationUtil.getUser();
            if(user==null)
                return resultMapHandler.handlerResult("10007", "用户不存在", logBean);
            //余额
            BigDecimal restMoney = new BigDecimal("0.00");
            XQuery xQuery1 = new XQuery("listAccount_default",request);
            xQuery1.put("user_id",jsonObj.getString("userId"));
            List<Account> accountList = baseManager.listObject(xQuery1);
            if(accountList==null && accountList.size()==0){
                return resultMapHandler.handlerResult("10007", "账户不存在", logBean);
            }
            account = accountList.get(0);
            restMoney = account.getCurrentUsableBalance();

            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            resultMap.put("object",restMoney);
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    @Test
    public void testArtworkView() throws Exception {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> map = new TreeMap<>();

        map.put("artWorkId", "in5z7r5f2w2f73so");
        map.put("userId","iovebhfg2tf3h0mb");
        map.put("action","restoreMargin");
//        String signmsg = DigitalSignatureUtil.encrypt(map);
        HttpClient httpClient = new DefaultHttpClient();
        String url = "http://ryt.efeiyi.com/app/pay/main.do";
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");
        JSONObject jsonObj = (JSONObject)JSONObject.toJSON(map);
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



