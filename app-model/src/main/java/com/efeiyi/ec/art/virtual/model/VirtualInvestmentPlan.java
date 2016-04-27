package com.efeiyi.ec.art.virtual.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.sql.Time;
import java.util.List;

/**
 * Created by Administrator on 2015/11/25.
 */
@Entity
@Table(name = "virtual_investment_plan")
public class VirtualInvestmentPlan extends VirtualPlan{

    private List<VirtualArtwork> virtualArtworkList;
//    private List<VirtualInvestorPlan> virtualInvestorPlanList;
    private VirtualInvestorPlan virtualInvestorPlan;
//    private int userCount;
    private Time peakTime;//均值小时
    private Integer standardDeviation; //标准差小时
    private String url;//模拟请求的服务端地址

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "virtualInvestmentPlan")
    public List<VirtualArtwork> getVirtualArtworkList() {
        return virtualArtworkList;
    }

    public void setVirtualArtworkList(List<VirtualArtwork> virtualArtworkList) {
        this.virtualArtworkList = virtualArtworkList;
    }


//    @Column(name = "user_count")
//    public int getUserCount() {
//        return userCount;
//    }
//
//    public void setUserCount(int userCount) {
//        this.userCount = userCount;
//    }

    @Column(name = "peak_time")
    public Time getPeakTime() {
        return peakTime;
    }

    public void setPeakTime(Time peakTime) {
        this.peakTime = peakTime;
    }

    @Column(name = "standard_deviation")
    public Integer getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(Integer standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    @Column(name = "server_url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
//
//    @OneToMany(fetch = FetchType.LAZY,mappedBy = "virtualInvestmentPlan")
//    public List<VirtualInvestorPlan> getVirtualInvestorPlanList() {
//        return virtualInvestorPlanList;
//    }
//
//    public void setVirtualInvestorPlanList(List<VirtualInvestorPlan> virtualInvestorPlanList) {
//        this.virtualInvestorPlanList = virtualInvestorPlanList;
//    }


    @ManyToOne
    @JoinColumn(name = "virtual_investor_plan_id")
    @NotFound(action = NotFoundAction.IGNORE)
    public VirtualInvestorPlan getVirtualInvestorPlan() {
        return virtualInvestorPlan;
    }

    public void setVirtualInvestorPlan(VirtualInvestorPlan virtualInvestorPlan) {
        this.virtualInvestorPlan = virtualInvestorPlan;
    }
}
