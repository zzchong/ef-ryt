package com.efeiyi.ec.art.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;
import com.efeiyi.ec.art.organization.model.User;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/1/25.
 */
@Entity
@Table(name = "app_art_work_message")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class ArtworkMessage implements Serializable {//项目动态
    private String id;
    private String content;
    private User creator;
    private Date createDatetime;
    private Artwork artwork;
    private String status;
    private List<ArtworkMessageAttachment> artworkMessageAttachments;
    private List<ArtWorkPraise> artWorkPraiseList; //点赞
    private List<ArtworkComment> artworkCommentList;//评论

    private Integer commentNum = 0;//评论数
    private Integer praiseNum = 0; //点赞数

    //临时参数
    private String praiseIsOrNot;

    @Transient
    public String getPraiseIsOrNot() {
        return praiseIsOrNot;
    }

    public void setPraiseIsOrNot(String praiseIsOrNot) {
        this.praiseIsOrNot = praiseIsOrNot;
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

    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Column(name = "create_datetime")
    public Date getCreateDatetime() {
        return createDatetime;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "art_work_id")
    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "artworkMessage")
    public List<ArtworkMessageAttachment> getArtworkMessageAttachments() {
        return artworkMessageAttachments;
    }

    public void setArtworkMessageAttachments(List<ArtworkMessageAttachment> artworkMessageAttachments) {
        this.artworkMessageAttachments = artworkMessageAttachments;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "artworkMessage")
    @Where(clause = "status=1")
    public List<ArtWorkPraise> getArtWorkPraiseList() {
        return artWorkPraiseList;
    }

    public void setArtWorkPraiseList(List<ArtWorkPraise> artWorkPraiseList) {
        this.artWorkPraiseList = artWorkPraiseList;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "artworkMessage")
    @OrderBy(value = "createDatetime desc")
    public List<ArtworkComment> getArtworkCommentList() {
        return artworkCommentList;
    }

    public void setArtworkCommentList(List<ArtworkComment> artworkCommentList) {
        this.artworkCommentList = artworkCommentList;
    }

    @Transient
    public Integer getCommentNum() {

        if (artworkCommentList != null)
            commentNum = artworkCommentList.size();
        return commentNum;
    }

    public void setCommentNum(Integer commentNum) {
        this.commentNum = commentNum;
    }

    @Transient
    public Integer getPraiseNum() {
        if (artWorkPraiseList != null)
            praiseNum = artWorkPraiseList.size();
        return praiseNum;
    }

    public void setPraiseNum(Integer praiseNum) {
        this.praiseNum = praiseNum;
    }
}

