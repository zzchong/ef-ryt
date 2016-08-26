package com.efeiyi.ec.art.artwork.service;


import cn.beecloud.bean.BCException;
import cn.beecloud.bean.BCOrder;
import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.model.Account;
import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.organization.model.User;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/8/3.
 */
public interface PaymentManager {


    void payCallback(String purchaseOrderPaymentId, String transactionNumber);

    boolean verifySign(String sign, String timestamp);

    BCOrder payBCOrder(String billNo, String title, BigDecimal money, Map<String,Object> map, String action) throws BCException;

    String batchReturnMoney(String batchNo, List transferDataList, String type) throws Exception;

    Map payByAction(HttpServletRequest request, Artwork artwork, String action, String type, User user, Account account, JSONObject jsonObject);

    int isPayMarginAccount(HttpServletRequest request, User user, Artwork artwork);


}
