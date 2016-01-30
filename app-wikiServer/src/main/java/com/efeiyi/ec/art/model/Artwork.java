package com.efeiyi.ec.art.model;

import com.efeiyi.ec.art.organization.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/1/25.
 *
 */
@Entity
@Table(name = "app_artwork")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class Artwork implements Serializable {
    private String id;
    private String title;
    private String brief;
    private String status;  //融资1    拍卖21     抽奖31
    private BigDecimal investGoalMoney;
    private Date investStartDatetime;
    private Date investEndDatetime;
    private Date auctionStartDatetime;
    private Date auctionEndDatetime;
    private User author;
    private Date createDatetime;
    private List<ArtworkAttachment> artworkAttachment;
    private List<ArtworkComment> artworkComments;


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
    @Column(name = "brief")
    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }
    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    @Column(name = "investGoalMoney")
    public BigDecimal getInvestGoalMoney() {
        return investGoalMoney;
    }

    public void setInvestGoalMoney(BigDecimal investGoalMoney) {
        this.investGoalMoney = investGoalMoney;
    }
    @Column(name = "investStartDatetime")
    public Date getInvestStartDatetime() {
        return investStartDatetime;
    }

    public void setInvestStartDatetime(Date investStartDatetime) {
        this.investStartDatetime = investStartDatetime;
    }
    @Column(name = "investEndDatetime")
    public Date getInvestEndDatetime() {
        return investEndDatetime;
    }

    public void setInvestEndDatetime(Date investEndDatetime) {
        this.investEndDatetime = investEndDatetime;
    }
    @Column(name = "auctionStartDatetime")
    public Date getAuctionStartDatetime() {
        return auctionStartDatetime;
    }

    public void setAuctionStartDatetime(Date auctionStartDatetime) {
        this.auctionStartDatetime = auctionStartDatetime;
    }

    public Date getAuctionEndDatetime() {
        return auctionEndDatetime;
    }

    public void setAuctionEndDatetime(Date auctionEndDatetime) {
        this.auctionEndDatetime = auctionEndDatetime;
    }
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }
    @Column(name = "createDatetime")
    public Date getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "artwork")

    public List<ArtworkAttachment> getArtworkAttachment() {
        return artworkAttachment;
    }

    public void setArtworkAttachment(List<ArtworkAttachment> artworkAttachment) {
        this.artworkAttachment = artworkAttachment;
    }
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "artwork")
    public List<ArtworkComment> getArtworkComments() {
        return artworkComments;
    }

    public void setArtworkComments(List<ArtworkComment> artworkComments) {
        this.artworkComments = artworkComments;
    }
}
