package com.efeiyi.ec.art.artwork.service;


import cn.beecloud.bean.BCException;
import cn.beecloud.bean.BCOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/8/3.
 */
public interface PaymentManager {


    void payCallback(String purchaseOrderPaymentId, String transactionNumber);

    boolean verifySign(String sign, String timestamp);

    BCOrder payBCOrder(String billNo, String title, BigDecimal money, Map<String,Object> map) throws BCException;

    String batchReturnMoney(String batchNo, List transferDataList, String type) throws Exception;

}
