package com.efeiyi.ec.art.model;

import com.efeiyi.ec.art.organization.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/1/25.
 */
@Entity
@Table(name = "app_art_work")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class Artwork implements Serializable {
    private String id;
    private String title;//标题
    private String brief;//简介
    private String description;//描述
    private String status;  //0 废弃  1 可用   2 缓存状态
    private BigDecimal investGoalMoney;//融资目标金额
    private Date investStartDatetime;//融资开始时间
    private Date investEndDatetime;//融资结束时间、创作开始时间
    private Date auctionStartDatetime;//拍卖开始时间
    private Date auctionEndDatetime;//拍卖结束时间
    private User author;//项目作者
    //    private Master master;
    private Date createDatetime;
    private List<ArtworkAttachment> artworkAttachment;//项目附件
    private List<ArtworkComment> artworkComments;//项目评论
    private List<ArtworkInvest> artworkInvests;//项目投资
//    private List<ArtworkInvest> artworkInvestCount;//投资笔数
    private List<ArtworkMessage> artworkMessages;//项目制作动态 //后台制作动态查询应用
    private List<ArtworkBidding> artworkBiddings = new ArrayList<>();//项目竞价记录 //艺术家个人信息统计应用
    private List<ArtWorkPraise> artWorkPraiseList;//点赞数
    private ArtworkDraw artworkDraw;//创作时长（天）
    private String picture_url;
    private String step; //项目阶段状态 100:编辑阶段，尚未提交 10.融资待审核 11.融资审核中 12.融资审核通过 13.融资审核未通过，已驳回
    // 14.融资中 15.融资完成 20.创作前 21.创作中 22.创作延时 23.创作完成待审核 24.创作完成审核中 25.创作完成被驳回 30.拍卖前
    // 31.拍卖中 32.拍卖结束 33.流拍 34.待支付尾款 35.待发放 36.已发放
    private BigDecimal investsMoney;//已筹金额
    private Date creationEndDatetime;//创作完成时间=融资结束时间+30(默认)
    private String type;//1 融资阶段  2 制作阶段  3 拍卖阶段  4 抽奖阶段   0 发起阶段  5驳回   6拍卖结束

    private Date newCreationDate;//最新创作时间

    private Integer auctionNum;//竞价记录次数
    private BigDecimal newBidingPrice=new BigDecimal("0.00");//最新竞价价格
    private String newBiddingDate;//最新出价时间
    private String sorts;//排序
    private User winner;//竞拍得主
    private String feedback;//审批意见
    private Integer duration;//创作时长

    private Artworkdirection artworkdirection;//制作说明和疑难解惑
    private BigDecimal startingPrice;//起拍价格

    private Integer commentNum = 0;//评论数
    private Integer praiseNUm = 0;//点赞数
    private Integer investNum =0;//投资人数

    private boolean isPraise;//冗余字段 在获取点赞项目接口中使用
    private Integer viewNum=0;//浏览次数

    private String pictureBottom;//俯视图
    private String pictureSide;//侧视图
    private Date investRestTime;//融资剩余时间

    private Integer height=0;
    private Integer width=0;

    private String material;//材质
    private String buffer;//是否是缓存状态  “yes”缓存状态  “no”正常状态

    private String isReturnIncome;//是否返还投资收益 0或null:无须返还  1:可返还   2:已返还

    @Column(name = "buffer")
    public String getBuffer() {
        return buffer;
    }

    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }

    @Column(name = "material")
    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
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

    @ManyToOne(fetch = FetchType.LAZY)
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
    @Where(clause = "status=1")
    //@JsonIgnore
    public List<ArtworkAttachment> getArtworkAttachment() {
        return artworkAttachment;
    }

    public void setArtworkAttachment(List<ArtworkAttachment> artworkAttachment) {
        this.artworkAttachment = artworkAttachment;
    }

    @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.REFRESH,
            CascadeType.REMOVE}, mappedBy = "artwork")
    @JsonIgnore
    public List<ArtworkComment> getArtworkComments() {
        return artworkComments;
    }

    public void setArtworkComments(List<ArtworkComment> artworkComments) {
        this.artworkComments = artworkComments;
    }

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "artwork")
    @OrderBy(value = "createDatetime desc")
    @Where(clause = "status=1")
    public List<ArtworkMessage> getArtworkMessages() {
        return artworkMessages;
    }

    public void setArtworkMessages(List<ArtworkMessage> artworkMessages) {
        this.artworkMessages = artworkMessages;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "artwork")
    @OrderBy(value = "createDatetime desc")
    public List<ArtworkBidding> getArtworkBiddings() {
        return artworkBiddings;
    }

    public void setArtworkBiddings(List<ArtworkBidding> artworkBiddings) {
        this.artworkBiddings = artworkBiddings;
    }

    @OneToOne(mappedBy = "artwork")
    @JsonIgnore
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

    @Column(name = "step")
    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "artwork")
    @OrderBy(value = "price desc")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Where(clause = "status=1")
    public List<ArtworkInvest> getArtworkInvests() {
        return artworkInvests;
    }

    public void setArtworkInvests(List<ArtworkInvest> artworkInvests) {
        this.artworkInvests = artworkInvests;
    }


//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "artwork")
//    @JsonIgnore
//    @LazyCollection(LazyCollectionOption.EXTRA)
//    public List<ArtworkInvest> getArtworkInvestCount() {
//        return artworkInvestCount;
//    }
//
//    public void setArtworkInvestCount(List<ArtworkInvest> artworkInvestCount) {
//        this.artworkInvestCount = artworkInvestCount;
//    }

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "artwork")
    @Where(clause = "status=1")
    public List<ArtWorkPraise> getArtWorkPraiseList() {
        return artWorkPraiseList;
    }

    public void setArtWorkPraiseList(List<ArtWorkPraise> artWorkPraiseList) {
        this.artWorkPraiseList = artWorkPraiseList;
    }

    @Transient
    public Integer getInvestNum() {
        investNum = 0;
        if (artworkInvests != null) {
           this.investNum = artworkInvests.size();
        }
        return investNum;
    }

    public void setInvestNum(Integer investNum) {
        this.investNum = investNum;
    }

    @Transient
    public BigDecimal getInvestsMoney() {
        Double temp = 0.00;
        if (artworkInvests != null) {
            for (ArtworkInvest artworkInvest : artworkInvests) {
                temp += artworkInvest.getPrice().doubleValue();
            }
            investsMoney = new BigDecimal(temp);
        }
        return investsMoney;
    }

    public void setInvestsMoney(BigDecimal investsMoney) {
        this.investsMoney = investsMoney;
    }

    @Column(name = "creation_end_datetime")
    public Date getCreationEndDatetime() {
        return creationEndDatetime;
    }

    public void setCreationEndDatetime(Date creationEndDatetime) {
        this.creationEndDatetime = creationEndDatetime;
    }

    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "sorts")
    public String getSorts() {
        return sorts;
    }

    public void setSorts(String sorts) {
        this.sorts = sorts;
    }

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "author_id")
//    public Master getMaster() {
//        return master;
//    }
//
//    public void setMaster(Master master) {
//        this.master = master;
//    }


    @Transient
    public String getNewBiddingDate() {
        return newBiddingDate;
    }

    public void setNewBiddingDate(String newBiddingDate) {
        this.newBiddingDate = newBiddingDate;
    }

    @Transient
    public Integer getAuctionNum() {
        return auctionNum;
    }

    public void setAuctionNum(Integer auctionNum) {
        this.auctionNum = auctionNum;
    }

    @Transient
    public BigDecimal getNewBidingPrice() {
        return newBidingPrice = artworkBiddings.size() > 0 ? artworkBiddings.get(0).getPrice() : newBidingPrice;
    }

    public void setNewBidingPrice(BigDecimal newBidingPrice) {
        this.newBidingPrice = newBidingPrice;
    }

    @Transient
    public Date getNewCreationDate() {
        return newCreationDate;
    }

    public void setNewCreationDate(Date newCreationDate) {
        this.newCreationDate = newCreationDate;
    }



   // @Transient
   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name="winner_id")
   public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    @Column(name = "feedback")
    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    @Column(name = "duration")
    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "artwork_direction_id")
    @Where(clause = "status=1")
    public Artworkdirection getArtworkdirection() {
        return artworkdirection;
    }

    public void setArtworkdirection(Artworkdirection artworkdirection) {
        this.artworkdirection = artworkdirection;
    }

    @Column(name = "starting_price")
    public BigDecimal getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(BigDecimal startingPrice) {
        this.startingPrice = startingPrice;
    }

    @Transient
    public Integer getCommentNum() {
        if(artworkComments!=null)
            commentNum = artworkComments.size();
        return commentNum;
    }


    public void setCommentNum(Integer commentNum) {
        this.commentNum = commentNum;
    }
    @Transient
    public Integer getPraiseNUm() {
        if (artWorkPraiseList!=null)
           praiseNUm = artWorkPraiseList.size();
        return praiseNUm;
    }

    public void setPraiseNUm(Integer praiseNUm) {
        this.praiseNUm = praiseNUm;
    }

    @Transient
    public boolean isPraise() {
        return isPraise;
    }

    public void setPraise(boolean praise) {
        isPraise = praise;
    }

    @Column(name = "view_num")
    public Integer getViewNum() {
        return viewNum;
    }

    public void setViewNum(Integer viewNum) {
        this.viewNum = viewNum;
    }

    @Column(name = "picture_bottom")
    public String getPictureBottom() {
        return pictureBottom;
    }

    public void setPictureBottom(String pictureBottom) {
        this.pictureBottom = pictureBottom;
    }

    @Column(name = "picture_side")
    public String getPictureSide() {
        return pictureSide;
    }

    public void setPictureSide(String pictureSide) {
        this.pictureSide = pictureSide;
    }


    @Transient
    public Date getInvestRestTime() {
        return investRestTime;
    }

    public void setInvestRestTime(Date investRestTime) {
        this.investRestTime = investRestTime;
    }

    @Column(name = "height")
    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    @Column(name = "width")
    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    @Column(name = "is_return_income")
    public String getIsReturnIncome() {
        return isReturnIncome;
    }

    public void setIsReturnIncome(String isReturnIncome) {
        this.isReturnIncome = isReturnIncome;
    }
}
