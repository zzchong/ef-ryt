package com.efeiyi.ec.art.model;

import com.efeiyi.ec.art.organization.model.User;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by Administrator on 2016/4/19.
 */
@Entity
@Table(name = "app_auction_order")
public class AuctionOrder implements Serializable {
    private String id;//订单编号
    private Artwork artwork;//关联项目
    private User user;//下单用户
    private ConsumerAddress consumerAddress;//收货地址
    private Date createDatetime;//创建时间
    private String payWay;// 0 账户余额 1 支付宝 2 微信
    private String status;// 1 可用 0 作废
    private String type;// 0 待付尾款 1 代发货 2交易成功  3待收货
    private BigDecimal finalPayment;//尾款金额
    private String payStatus;// 0 支付成功 1 支付失败 3未支付
    private BigDecimal amount;//拍卖金额

    @Id
    @GenericGenerator(name = "id", strategy = "com.ming800.core.p.model.M8idGenerator")
    @GeneratedValue(generator = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "artwork_id")
    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumerAddress_id")
    public ConsumerAddress getConsumerAddress() {
        return consumerAddress;
    }

    public void setConsumerAddress(ConsumerAddress consumerAddress) {
        this.consumerAddress = consumerAddress;
    }

    @Column(name = "createDatetime")
    public Date getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }

    @Column(name = "payWay")
    public String getPayWay() {
        return payWay;
    }

    public void setPayWay(String payWay) {
        this.payWay = payWay;
    }

    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "finalPayment")
    public BigDecimal getFinalPayment() {
        return finalPayment;
    }

    public void setFinalPayment(BigDecimal finalPayment) {
        this.finalPayment = finalPayment;
    }

    @Column(name = "payStatus")
    public String getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(String payStatus) {
        this.payStatus = payStatus;
    }

    @Column(name = "amount")
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
