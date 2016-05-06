package com.efeiyi.ec.art.virtual.model;

import com.efeiyi.ec.art.model.Artwork;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * Created by Administrator on 2016/4/21.
 */
@Entity
@Table(name = "virtual_artwork")
public class VirtualArtwork {
    private String id;
    private Artwork artwork;
    private VirtualInvestmentPlan virtualInvestmentPlan;
    private VirtualPraisePlan virtualPraisePlan;
//    private String investGoal;//0 融资充足 1 不保证融资充足

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
    @JoinColumn(name = "artwork_id")
    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virtual_investment_plan_id")
    public VirtualInvestmentPlan getVirtualInvestmentPlan() {
        return virtualInvestmentPlan;
    }

    public void setVirtualInvestmentPlan(VirtualInvestmentPlan virtualInvestmentPlan) {
        this.virtualInvestmentPlan = virtualInvestmentPlan;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "virtual_praise_plan_id")
    public VirtualPraisePlan getVirtualPraisePlan() {
        return virtualPraisePlan;
    }

    public void setVirtualPraisePlan(VirtualPraisePlan virtualPraisePlan) {
        this.virtualPraisePlan = virtualPraisePlan;
    }
//    @Column(name = "invest_goal")
//    public String getInvestGoal() {
//        return investGoal;
//    }
//
//    public void setInvestGoal(String investGoal) {
//        this.investGoal = investGoal;
//    }
}
