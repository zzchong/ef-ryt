package com.efeiyi.ec.art.model;

import com.efeiyi.ec.art.organization.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/1/25.
 */
@Entity
@Table(name = "app_bill_detail")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class Bill implements Serializable {
    private String id;
    private String title;//标题
    private String detail;//账单详情
    private String status;  //0 废弃  1 可用
    private BigDecimal money;//账单金额
    private User author;
    private Date createDatetime;
    private String type;//账单类型  1 投资 2 拍卖订单  3 支付保证金 4 返还保证金 5 返利 60 退款余额中 61余额退款成功
    private String outOrIn;//1 进账 0 出账
    private String number;//支付订单唯一标示
    private String payWay;//支付方式 1 支付宝 2 微信
    private String flowAccount;//流水账号


    private String payNumber;//支付宝账号 退款余额时使用
    private String payName;//支付宝名称

    @Id
    @GenericGenerator(name = "id", strategy = "com.ming800.core.p.model.M8idGenerator")
    @GeneratedValue(generator = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    @Column(name = "create_datetime")
    public Date getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }


    @Column(name = "detail")
    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Column(name = "money")
    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "out_or_in")
    public String getOutOrIn() {
        return outOrIn;
    }

    public void setOutOrIn(String outOrIn) {
        this.outOrIn = outOrIn;
    }

    @Column(name = "number")
    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Column(name = "pay_way")
    public String getPayWay() {
        return payWay;
    }

    public void setPayWay(String payWay) {
        this.payWay = payWay;
    }

    @Column(name = "flow_account")
    public String getFlowAccount() {
        return flowAccount;
    }

    public void setFlowAccount(String flowAccount) {
        this.flowAccount = flowAccount;
    }

    @Column(name = "pay_number")
    public String getPayNumber() {
        return payNumber;
    }

    public void setPayNumber(String payNumber) {
        this.payNumber = payNumber;
    }

    @Column(name = "pay_name")
    public String getPayName() {
        return payName;
    }

    public void setPayName(String payName) {
        this.payName = payName;
    }
}
