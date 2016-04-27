package com.efeiyi.ec.art.virtual.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Administrator on 2015/11/25.
 */
@Entity
@Table(name = "virtual_investor_plan")
public class VirtualInvestorPlan {

    private String id;
    private String group;
    private Integer count;
//    private int investFloorAmount;
//    private int investCeilAmount;
    private String status;
    private List<VirtualUser> virtualUserList;
//    private VirtualInvestmentPlan virtualInvestmentPlan;

    @Column(name = "count")
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "virtualInvestorPlan")
    public List<VirtualUser> getVirtualUserList() {
        return virtualUserList;
    }

    public void setVirtualUserList(List<VirtualUser> virtualUserList) {
        this.virtualUserList = virtualUserList;
    }

    @Id
    @GenericGenerator(name = "id", strategy = "com.ming800.core.p.model.M8idGenerator")
    @GeneratedValue(generator = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "group")
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
//    @Column(name = "invest_floor_amount")
//     public int getInvestFloorAmount() {
//        return investFloorAmount;
//    }
//
//    public void setInvestFloorAmount(int investFloorAmount) {
//        this.investFloorAmount = investFloorAmount;
//    }
//
//    @Column(name = "invest_ceil_amount")
//    public int getInvestCeilAmount() {
//        return investCeilAmount;
//    }
//
//    public void setInvestCeilAmount(int investCeilAmount) {
//        this.investCeilAmount = investCeilAmount;
//    }

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "virtual_investment_plan_id")
//    public VirtualInvestmentPlan getVirtualInvestmentPlan() {
//        return virtualInvestmentPlan;
//    }
//
//    public void setVirtualInvestmentPlan(VirtualInvestmentPlan virtualInvestmentPlan) {
//        this.virtualInvestmentPlan = virtualInvestmentPlan;
//    }

    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
