package com.efeiyi.ec.art.virtual.model;

import com.efeiyi.ec.art.model.UserBrief;
import com.efeiyi.ec.art.organization.model.User;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by Administrator on 2015/11/25.
 */
@Entity
@Table(name = "virtual_user")
public class VirtualUser {

    private String id;
    private UserBrief userBrief;
    private VirtualInvestorPlan virtualInvestorPlan;

    @Id
    @GenericGenerator(name = "id", strategy = "com.ming800.core.p.model.M8idGenerator")
    @GeneratedValue(generator = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_brief_id")
    public UserBrief getUserBrief() {
        return userBrief;
    }

    public void setUserBrief(UserBrief userBrief) {
        this.userBrief = userBrief;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virtual_investor_plan_id")
    public VirtualInvestorPlan getVirtualInvestorPlan() {
        return virtualInvestorPlan;
    }

    public void setVirtualInvestorPlan(VirtualInvestorPlan virtualInvestorPlan) {
        this.virtualInvestorPlan = virtualInvestorPlan;
    }
}
