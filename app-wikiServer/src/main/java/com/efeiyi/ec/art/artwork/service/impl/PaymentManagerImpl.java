package com.efeiyi.ec.art.artwork.service.impl;


import cn.beecloud.BeeCloud;
import cn.beecloud.bean.*;
import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.PaymentManager;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.model.User;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.p.PConst;
import com.ming800.core.p.service.AutoSerialManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

import java.math.BigDecimal;
import java.util.*;

import org.apache.commons.codec.digest.DigestUtils;
import cn.beecloud.*;
import org.apache.log4j.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2015/8/3.
 */
@Service
public class PaymentManagerImpl implements PaymentManager {

    @Autowired
    private BaseManager baseManager;

    @Autowired
    private AutoSerialManager autoSerialManager;

    private final static  String ACCOUNT_NO = "feiyipark@ich-park.com";//付款方支付宝账号

    private final static  String ACCOUNT_NAME = "永新华韵文化发展有限公司";//付款方支付宝账户名称

    private final static String APPID = "bad9ddf8-b5d8-475d-ae5b-1a244b9b9993";

    private final static String APPSECRET = "dfbedf37-97d9-4d35-aa32-cd8b3d6fed93";

    static {
        BeeCloud.registerApp("bad9ddf8-b5d8-475d-ae5b-1a244b9b9993","4d164cf7-211f-452f-8d85-417556656577","dfbedf37-97d9-4d35-aa32-cd8b3d6fed93","b5b9b602-6e9c-4e5a-8c37-7071974c3720"); //正式环境
    }


    @Override
    public void payCallback(String purchaseOrderPaymentId, String transactionNumber) {

    }


    /* 功能：BeeCloud服务器异步通知页面
      ***********页面功能说明***********
     创建该页面文件时，请留心该页面文件中无任何HTML代码及空格。
     该页面不能在本机电脑测试，请到服务器上做测试。请确保外部可以访问该页面。
     如果没有收到该页面返回的 success 信息，BeeCloud会在36小时内按一定的时间策略重发通知
     */


    Logger log = Logger.getLogger(this.getClass());

    boolean verify(String sign, String text, String key, String input_charset) {
        text = text + key;
        String mysign = DigestUtils.md5Hex(getContentBytes(text, input_charset));
        log.info("mysign:" + mysign);

        long timeDifference = System.currentTimeMillis() - Long.valueOf(key);
        log.info("timeDifference:" + timeDifference);
        if (mysign.equals(sign) && timeDifference <= 300000) {
            return true;
        } else {
            return false;
        }
    }

   public boolean verifySign(String sign, String timestamp) {
        log.info("sign:" + sign);
        log.info("timestamp:" + timestamp);

        return verify(sign, APPID + APPSECRET,
                timestamp, "UTF-8");

    }

    byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

    /**
     *
     * @param billNo 商户订单号
     * @param title  订单标题
     * @param money
     * @return
     */
    //支付参数 通过BCOrder对象接收
    @Override
   public BCOrder payBCOrder(String billNo, String title, BigDecimal money, Map<String,Object> map, String action) throws BCException {
       BigDecimal price = new BigDecimal(money.floatValue() * 100);
       BCOrder bcOrder = new BCOrder(BCEumeration.PAY_CHANNEL.ALI_WAP,price.intValue(),billNo,title);
       bcOrder.setBillTimeout(360);
       bcOrder.setReturnUrl(PConst.RETURN_URL+"/app/pay/paysuccess.do?bill_id="+map.get("Bill_id"));
       bcOrder.setOptional(map);
       bcOrder = BCPay.startBCPay(bcOrder);
       System.out.println(bcOrder.getObjectId());
       return bcOrder;
   }


    /**
     * 批量打款
     */
    @Override
    public  String batchReturnMoney(String batchNo, List transferDataList,String type) throws Exception {
        String url = "";
        List<ALITransferData> list = new ArrayList<>();
        List<Object> billList = new ArrayList<>();
        TransferParameter param = new TransferParameter();
        param.setChannel(BCEumeration.TRANSFER_CHANNEL.ALI_TRANSFER);//支付宝单笔打款
        if(type.equals("restoreMargin")){
            List<MarginAccount> marginAccountList = (List<MarginAccount>)transferDataList;
            for(MarginAccount marginAccount :marginAccountList){

                String  transferNote = "参与《"+marginAccount.getArtwork().getTitle()+"》项目的保证金";
                Integer transferFee = marginAccount.getCurrentBalance().multiply(new BigDecimal(100)).intValue();
                String  transferNo = System.currentTimeMillis()+autoSerialManager.nextSerial("transferId");


                param.setTransferNo(transferNo);//打款单号
                param.setTotalFee(transferFee);//打款金额
                param.setDescription(transferNote);//打款说明
                param.setChannelUserId("362622735@qq.com");
                param.setChannelUserName("离心力");
                param.setAccountName(ACCOUNT_NAME);


                Bill bill = new Bill();
                bill.setStatus("1");
                bill.setTitle("融易投-保证金退还");
                bill.setMoney(marginAccount.getCurrentBalance());
                bill.setAuthor(marginAccount.getUser());
                bill.setPayWay("1");
                bill.setOutOrIn("1");
                bill.setCreateDatetime(new Date());
                bill.setDetail(transferNote);
                bill.setFlowAccount(transferNo);
                bill.setType("4");
                url = BCPay.startTransfer(param);
                System.out.println(url);
                baseManager.saveOrUpdate(Bill.class.getName(),bill);
//                ALITransferData data = new ALITransferData(transferId,"1055303387@qq.com","青石",transferFee,transferNote);
//                list.add(data);
//                billList.add(bill);

            }
        }else if(type.equals("reward")){
            List<ROIRecord> roiRecordList = (List<ROIRecord>)transferDataList;
            for (ROIRecord roiRecord : roiRecordList){
                String  transferNote = "参与《"+roiRecord.getArtwork().getTitle()+"》项目投资的本金"+roiRecord.getInvestMoney()+"及收益"+roiRecord.getCurrentBalance();
                Integer transferFee = roiRecord.getCurrentBalance().add(roiRecord.getInvestMoney()).multiply(new BigDecimal(100)).intValue();
                String  transferNo = System.currentTimeMillis()+autoSerialManager.nextSerial("transferId");

                param.setTransferNo(transferNo);//打款单号
                param.setTotalFee(transferFee);//打款金额
                param.setDescription(transferNote);//打款说明
                param.setChannelUserId("362622735@qq.com");
                param.setChannelUserName("离心力");
                param.setAccountName(ACCOUNT_NAME);

                Bill bill = new Bill();
                bill.setStatus("1");
                bill.setTitle("融易投-保证金退还");
                bill.setMoney(roiRecord.getCurrentBalance().add(roiRecord.getInvestMoney()));
                bill.setAuthor(roiRecord.getUser());
                bill.setPayWay("1");
                bill.setOutOrIn("1");
                bill.setCreateDatetime(new Date());
                bill.setDetail(transferNote);
                bill.setFlowAccount(transferNo);
                bill.setType("5");

                url = BCPay.startTransfer(param);
                System.out.println(url);
                baseManager.saveOrUpdate(Bill.class.getName(),bill);
//                ALITransferData data = new ALITransferData(transferId,"1055303387@qq.com","青石",transferFee,transferNote);
//                list.add(data);
//                billList.add(bill);
            }

        }

//        TransfersParameter para = new TransfersParameter();
//        para.setBatchNo(batchNo);
//        para.setAccountName(ACCOUNT_NO);
//        para.setTransferDataList(list);
//        para.setChannel(BCEumeration.PAY_CHANNEL.ALI);
//        url = BCPay.startTransfers(para);
//        baseManager.batchSaveOrUpdate("save",Bill.class.getName(),billList);
        return url;
    }

    @Override
    public Map payByAction(HttpServletRequest request, Artwork artwork, String action, String type, User user,Account account, JSONObject jsonObject) {
        Map<String, Object> map = new HashMap<>();
        if (action.equals("payMargin")){//余额支付保证金
            try {
                /*ConsumerAddress consumerAddress = null;
                XQuery xQuery = new XQuery("listAddress_default1",request);
                xQuery.put("consumer_id",user.getId());
                List<ConsumerAddress> consumerAddressList = baseManager.listObject(xQuery);
                if(consumerAddressList!=null && consumerAddressList.size()>0)
                    consumerAddress = consumerAddressList.get(0);

                if(consumerAddress==null){
                    map.put("resultCode", "10002");
                    map.put("resultMsg", "收货地址为空");
                    return map;
                }*/

                //保证金
                if (1 == isPayMarginAccount(request,user,artwork)){
                    map.put("resultCode","100022");
                    map.put("resultMsg","已缴纳保证金,重复提交");
                    return map;
                }
                //拍卖金额
                BigDecimal auctionMoney = new BigDecimal("0.00");
                auctionMoney = artwork.getInvestGoalMoney().multiply(new BigDecimal("0.1"));
                //判断账户余额是否足够
                if (account.getCurrentUsableBalance().compareTo(auctionMoney)<0){
                    map.put("resultCode","100015");
                    map.put("resultMsg","账户余额不足");
                    return map;
                }

                MarginAccount marginAccount = new MarginAccount();
                marginAccount.setCurrentBalance(auctionMoney);
                marginAccount.setUser(user);
                marginAccount.setAccount(account);
                marginAccount.setArtwork(artwork);
                marginAccount.setCreateDatetime(new Date());
                marginAccount.setStatus("0");

                account.setCurrentUsableBalance(account.getCurrentUsableBalance().subtract(auctionMoney));
                baseManager.saveOrUpdate(Account.class.getName(),account);

                /*AuctionOrder auctionOrder = new AuctionOrder();
                auctionOrder.setArtwork(artwork);
                auctionOrder.setUser(user);
                auctionOrder.setCreateDatetime(new Date());
                auctionOrder.setStatus("1");
                auctionOrder.setConsumerAddress(consumerAddress);
                auctionOrder.setFinalPayment(auctionMoney);
                auctionOrder.setPayStatus("0");
                auctionOrder.setPayWay("0");
                auctionOrder.setType("4");*/

                Bill bill = new Bill();
                bill.setDetail(user.getName()+"向"+artwork.getTitle()+"支付保证金"+auctionMoney+"元");
                bill.setTitle(user.getName()+"-保证金-"+artwork.getTitle());
                bill.setStatus("1");
                bill.setMoney(auctionMoney);
                bill.setAuthor(user);
                bill.setCreateDatetime(new Date());
                bill.setType("3");
                bill.setOutOrIn("0");
                bill.setPayWay("3");
                baseManager.saveOrUpdate(MarginAccount.class.getName(),marginAccount);
                baseManager.saveOrUpdate(Bill.class.getName(), bill);

                map.put("resultCode","0");
                map.put("resultMsg","成功");
                return map;


            }catch (Exception e){
                map.put("resultCode", "10004");
                map.put("resultMsg", "未知错误，请联系管理员");
                return map;
            }


        }else if (action.equals("auction")){//余额支付尾款
            try {
                //收货地址
                ConsumerAddress consumerAddress = null;
                /*XQuery xQuery = new XQuery("listAddress_default1",request);
                xQuery.put("consumer_id",user.getId());
                List<ConsumerAddress> consumerAddressList = baseManager.listObject(xQuery);
                if(consumerAddressList!=null && consumerAddressList.size()>0){
                    consumerAddress = consumerAddressList.get(0);
                }*/
                String consumerAddressId = jsonObject.getString("consumerAddressId");
                if (consumerAddressId == null || consumerAddressId.equals("")){
                    map.put("resultCode", "10002");
                    map.put("resultMsg", "收货地址为空");
                    return map;
                }
                consumerAddress = (ConsumerAddress) baseManager.getObject(ConsumerAddress.class.getName(), consumerAddressId);
                if (consumerAddress == null){
                    map.put("resultCode", "10002");
                    map.put("resultMsg", "收货地址错误");
                    return map;
                }

                //保证金判断
                if (0 == isPayMarginAccount(request, user, artwork)){
                    map.put("resultCode", "100023");
                    map.put("resultMsg", "未缴纳保证金");
                    return map;
                }
                //判断账户余额是否足够
                if (account.getCurrentUsableBalance().compareTo(artwork.getNewBidingPrice().subtract(artwork.getInvestGoalMoney().multiply(new BigDecimal("0.1"))))<0){
                    map.put("resultCode","100015");
                    map.put("resultMsg","账户余额不足");
                    return map;
                }
                if (!artwork.getType().equals("3")){
                    map.put("resultCode", "100002");
                    map.put("resultMsg", "该项目不在拍卖中");
                    return map;
                }

                AuctionOrder auctionOrder = null;
                XQuery xQuery1 = new XQuery("listAuctionOrder_default5", request);
                List<AuctionOrder> auctionOrderList = baseManager.listObject(xQuery1);
                if (null != auctionOrderList && auctionOrderList.size()>0){
                    auctionOrder = auctionOrderList.get(0);
                }else {
                    auctionOrder = new AuctionOrder();
                    auctionOrder.setArtwork(artwork);
                    auctionOrder.setUser(user);
                    auctionOrder.setCreateDatetime(new Date());
                    auctionOrder.setStatus("1");
                    auctionOrder.setConsumerAddress(consumerAddress);
                    auctionOrder.setAmount(artwork.getNewBidingPrice());
                    auctionOrder.setFinalPayment(artwork.getNewBidingPrice().subtract(artwork.getInvestGoalMoney().multiply(new BigDecimal("0.1"))));
                    auctionOrder.setPayStatus("3");
                    auctionOrder.setType("0");
                }
                baseManager.saveOrUpdate(AuctionOrder.class.getName(), auctionOrder);
                //操作账户
                account.setCurrentUsableBalance(account.getCurrentUsableBalance().subtract(artwork.getNewBidingPrice()));
                account.setCurrentBalance(account.getCurrentBalance().subtract(artwork.getNewBidingPrice()));
                baseManager.saveOrUpdate(Account.class.getName(),account);

                //更新订单状态
                auctionOrder.setPayStatus("0");
                auctionOrder.setPayWay("0");
                auctionOrder.setType("5");
                baseManager.saveOrUpdate(AuctionOrder.class.getName(), auctionOrder);

                //更改项目为拍卖结束
                artwork.setType("6");
                baseManager.saveOrUpdate(Artwork.class.getName(), artwork);

                //生成账单
                Bill bill = new Bill();
                bill.setDetail(user.getName()+"向"+artwork.getTitle()+"支付尾款"+artwork.getNewBidingPrice().subtract(artwork.getInvestGoalMoney().multiply(new BigDecimal("0.1")))+"元");
                bill.setTitle(user.getName()+"-支付尾款-"+artwork.getTitle());
                bill.setStatus("1");
                bill.setMoney(artwork.getNewBidingPrice().subtract(artwork.getInvestGoalMoney().multiply(new BigDecimal("0.1"))));
                bill.setAuthor(user);
                bill.setCreateDatetime(new Date());
                bill.setType("7");
                bill.setOutOrIn("0");
                bill.setPayWay("3");
                baseManager.saveOrUpdate(AuctionOrder.class.getName(), auctionOrder);
                baseManager.saveOrUpdate(Bill.class.getName(), bill);

                map.put("resultCode","0");
                map.put("resultMsg","成功");
                return map;
            }catch (Exception e){
                map.put("resultCode", "10004");
                map.put("resultMsg", "未知错误，请联系管理员");
                return map;
            }

        }else if (action.equals("invest")){//投资
            try {
                //获得投资金额
                BigDecimal money = new BigDecimal(jsonObject.getString("money"));
                if (money.compareTo(new BigDecimal("-1"))==0){
                    money = artwork.getInvestGoalMoney().subtract(artwork.getInvestsMoney());
                }
                if (new BigDecimal("0").compareTo(money)>0){
                    map.put("resultCode", "10002");
                    map.put("resultMsg", "投资金额有误");
                    return map;
                }

                //验证投资金额
                if(artwork.getInvestsMoney().add(money).compareTo(artwork.getInvestGoalMoney())==1){
                    map.put("resultCode", "10002");
                    map.put("resultMsg", "最多只能投资"+artwork.getInvestGoalMoney().subtract(artwork.getInvestsMoney()));
                    return map;
                }
                //判断账户余额是否足够
                if (account.getCurrentUsableBalance().compareTo(money)<0){
                    map.put("resultCode","100015");
                    map.put("resultMsg","账户余额不足");
                    return map;
                }
                account.setCurrentBalance(account.getCurrentBalance().subtract(money));
                account.setCurrentUsableBalance(account.getCurrentUsableBalance().subtract(money));
                baseManager.saveOrUpdate(Account.class.getName(), account);

                if(artwork.getInvestsMoney().add(money).compareTo(artwork.getInvestGoalMoney())==0){
                    artwork.setType("2");
                    artwork.setStep("21");
                    baseManager.saveOrUpdate(Artwork.class.getName(),artwork);
                }
                ArtworkInvest artworkInvest = new ArtworkInvest();
                artworkInvest.setCreator(user);
                artworkInvest.setPrice(money);
                artworkInvest.setStatus("1");
                artworkInvest.setArtwork(artwork);
                artworkInvest.setType("0");
                artworkInvest.setAccount(account);
                artworkInvest.setCreateDatetime(new Date());

                Bill bill = new Bill();
                bill.setDetail(user.getName()+"向"+artwork.getTitle()+"投资"+money+"元");
                bill.setTitle(user.getName()+"-投资-"+artwork.getTitle());
                bill.setStatus("1");
                bill.setMoney(money);
                bill.setAuthor(user);
                bill.setCreateDatetime(new Date());
                bill.setType("1");
                bill.setOutOrIn("0");
                bill.setPayWay("3");

                baseManager.saveOrUpdate(ArtworkInvest.class.getName(),artworkInvest);
                baseManager.saveOrUpdate(Bill.class.getName(), bill);
                map.put("resultCode", "0");
                map.put("resultMsg", "成功");
                return map;
            }catch (Exception e){
                map.put("resultCode", "10004");
                map.put("resultMsg", "未知错误，请联系管理员");
                return map;
            }

        }else if (action.equals("account")){
            try {
                Map<String, Object> map1 = new HashMap<>();
                BigDecimal money = new BigDecimal(jsonObject.getString("money"));

                Bill bill = new Bill();
                bill.setStatus("0");
                bill.setAuthor(user);
                bill.setPayWay("1");//支付方式为"支付宝"
                bill.setOutOrIn("0");
                bill.setCreateDatetime(new Date());
                bill.setDetail("充值："+money);
                bill.setType("0");
                bill.setRestMoney(account.getCurrentUsableBalance());
                bill.setMoney(money);
                baseManager.saveOrUpdate(Bill.class.getName(), bill);

                String title = "充值";
                String billNo = bill.getId();
                map1.put("action", action);
                map1.put("Bill_id",bill.getId());

                BCOrder  bcOrder = payBCOrder(billNo, title, money, map1 ,jsonObject.getString("action"));
                bill.setNumber(bcOrder.getObjectId());
                bill.setTitle(title);
                bill.setFlowAccount(bcOrder.getChannelTradeNo());
                baseManager.saveOrUpdate(Bill.class.getName(),bill);
                map.put("bcOrder", bcOrder);
                map.put("url", bcOrder.getUrl().replace(" ","%20"));
                map.put("resultCode","0");
                map.put("resultMsg","成功");
                return map;

            }catch (Exception e){
                map.put("resultCode", "10004");
                map.put("resultMsg", "未知错误，请联系管理员");
                return map;
            }

        }else {
            map.put("resultCode", "10004");
            map.put("resultMsg", "未知错误，请联系管理员");
            return map;
        }

    }

    //是否缴纳保证金
    @Override
    public int isPayMarginAccount(HttpServletRequest request, User user, Artwork artwork){
        try {
            XQuery xQuery = new XQuery("listMarginAccount_default2",request);
            xQuery.put("artwork_id",artwork.getId());
            xQuery.put("user_id",user.getId());
            List<MarginAccount> marginAccountList = baseManager.listObject(xQuery);

            if(null != marginAccountList && marginAccountList.size()>0){
                return 1;
            }else {
                return 0;
            }
        }catch (Exception e){
            return -1;
        }

    }




}
