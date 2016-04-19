package com.efeiyi.ec.art.model;

import com.efeiyi.ec.art.organization.model.User;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2016/4/19.
 *
 */
@Entity
@Table(name = "app_auction_order")
public class AuctionOrder implements Serializable {
    private String id;//订单编号
    private Artwork artwork;//关联项目
    private User user;//下单用户
    private ConsumerAddress consumerAddress;//收货地址
    private Date createDatetime;//创建时间
    private String payWay;// 0 账户余额 1 微信支付 2 支付宝支付
    private String status;
    private String type;// 0 待付尾款 1 代发货 2交易成功
    //private


}
