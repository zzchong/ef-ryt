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
@Table(name = "app_art_work")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class Artwork implements Serializable {
    private String id;
    private String title;
    private String brief;
    private String status;  //融资1 拍卖21 抽奖31
    private BigDecimal investGoalMoney;
    private Date investStartDatetime;
    private Date investEndDatetime;
    private Date auctionStartDatetime;
    private Date auctionEndDatetime;
    private User author;
    private Date createDatetime;
    private List<ArtworkAttachment> artworkAttachment;
    private List<ArtworkComment> artworkComments;
    private ArtworkDraw artworkDraw;
    private String picture_url;
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
    @Column(name = "invest_goal_money")
    public BigDecimal getInvestGoalMoney() {
        return investGoalMoney;
    }

    public void setInvestGoalMoney(BigDecimal investGoalMoney) {
        this.investGoalMoney = investGoalMoney;
    }
    @Column(name = "invest_start_datetime")
    public Date getInvestStartDatetime() {
        return investStartDatetime;
    }

    public void setInvestStartDatetime(Date investStartDatetime) {
        this.investStartDatetime = investStartDatetime;
    }
    @Column(name = "invest_end_datetime")
    public Date getInvestEndDatetime() {
        return investEndDatetime;
    }

    public void setInvestEndDatetime(Date investEndDatetime) {
        this.investEndDatetime = investEndDatetime;
    }
    @Column(name = "auction_start_datetime")
    public Date getAuctionStartDatetime() {
        return auctionStartDatetime;
    }

    public void setAuctionStartDatetime(Date auctionStartDatetime) {
        this.auctionStartDatetime = auctionStartDatetime;
    }
    @Column(name = "auction_end_datetime")
    public Date getAuctionEndDatetime() {
        return auctionEndDatetime;
    }

    public void setAuctionEndDatetime(Date auctionEndDatetime) {
        this.auctionEndDatetime = auctionEndDatetime;
    }
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
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
    @OneToOne(mappedBy = "artwork")
    public ArtworkDraw getArtworkDraw() {
        return artworkDraw;
    }

    public void setArtworkDraw(ArtworkDraw artworkDraw) {
        this.artworkDraw = artworkDraw;
    }
    @Column(name = "picture_url")
    public String getPicture_url() {
        return picture_url;
    }

    public void setPicture_url(String picture_url) {
        this.picture_url = picture_url;
    }
}
