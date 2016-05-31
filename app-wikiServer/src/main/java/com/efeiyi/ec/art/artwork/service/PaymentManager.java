package com.efeiyi.ec.art.artwork.service;


import cn.beecloud.bean.BCException;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by Administrator on 2015/8/3.
 */
public interface PaymentManager {


    void payCallback(String purchaseOrderPaymentId, String transactionNumber);

    boolean verifySign(String sign, String timestamp);

    String payBCOrder(String billNo, String title, BigDecimal money, Map<String,Object> map) throws BCException;

}
