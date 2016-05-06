package com.efeiyi.ec.art.virtual.model;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;

/**
 * Created by Administrator on 2015/11/25.
 */
@Entity
@Table(name = "virtual_investment_plan")
public class VirtualInvestmentPlan extends VirtualPlan{

    private VirtualArtwork virtualArtwork;
//    private VirtualInvestorPlan virtualInvestorPlan;
    private String url;//模拟请求的服务端地址

    @ManyToOne
    @JoinColumn(name = "virtual_artwork_id")
    @NotFound(action = NotFoundAction.IGNORE)
    public VirtualArtwork getVirtualArtwork() {
        return virtualArtwork;
    }

    public void setVirtualArtwork(VirtualArtwork virtualArtwork) {
        this.virtualArtwork = virtualArtwork;
    }

    @Column(name = "server_url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

//    @ManyToOne
//    @JoinColumn(name = "virtual_investor_plan_id")
//    @NotFound(action = NotFoundAction.IGNORE)
//    public VirtualInvestorPlan getVirtualInvestorPlan() {
//        return virtualInvestorPlan;
//    }
//
//    public void setVirtualInvestorPlan(VirtualInvestorPlan virtualInvestorPlan) {
//        this.virtualInvestorPlan = virtualInvestorPlan;
//    }
}
