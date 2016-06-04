package com.efeiyi.ec.art.artwork.service.impl;


import cn.beecloud.BCCache;
import cn.beecloud.BeeCloud;
import cn.beecloud.bean.*;
import com.efeiyi.ec.art.artwork.service.PaymentManager;
import com.efeiyi.ec.art.model.Bill;
import com.efeiyi.ec.art.model.MarginAccount;
import com.efeiyi.ec.art.model.ROIRecord;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.PConst;
import com.ming800.core.p.service.AutoSerialManager;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;

import net.sf.json.JSONObject;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import cn.beecloud.*;
import org.apache.log4j.*;

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

        return verify(sign, BCCache.getAppID() + BCCache.getAppSecret(),
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
   public BCOrder payBCOrder(String billNo, String title, BigDecimal money, Map<String,Object> map) throws BCException {
       BigDecimal price = new BigDecimal(money.floatValue() * 100);
       BCOrder bcOrder = new BCOrder(BCEumeration.PAY_CHANNEL.ALI_WAP,price.intValue(),billNo,title);
       bcOrder.setBillTimeout(360);
       bcOrder.setReturnUrl(PConst.RETURN_URL+"/app/pay/paysuccess.do");
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


}
