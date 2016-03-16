package com.efeiyi.ec.art.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/2/25.
 */

@Entity
@Table(name = "app_art_master_attachment")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class ArtMasterAttachment implements Serializable {

    private String id;
    private Master master;
    private String frontPhotoUrl;//正面照片
    private String versoPhotoUrl;//反面照片
    private String workShopPhoto; //工作室照片
    private List<ArtMasterAttachmentPicture> masterWorks; //作品照片
    private List<ArtMasterAttachmentPicture> qualifications; //资格证书

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
    @JoinColumn(name = "product_id")
    public Master getMaster() {
        return master;
    }

    public void setMaster(Master master) {
        this.master = master;
    }

    @Column(name = "front_photo_url")
    public String getFrontPhotoUrl() {
        return frontPhotoUrl;
    }

    public void setFrontPhotoUrl(String frontPhotoUrl) {
        this.frontPhotoUrl = frontPhotoUrl;
    }

    @Column(name = "verso_photo_url")
    public String getVersoPhotoUrl() {
        return versoPhotoUrl;
    }

    public void setVersoPhotoUrl(String versoPhotoUrl) {
        this.versoPhotoUrl = versoPhotoUrl;
    }

    @Column(name = "work_shop_photo")
    public String getWorkShopPhoto() {
        return workShopPhoto;
    }

    public void setWorkShopPhoto(String workShopPhoto) {
        this.workShopPhoto = workShopPhoto;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "attachment")
    public List<ArtMasterAttachmentPicture> getMasterWorks() {
        return masterWorks;
    }

    public void setMasterWorks(List<ArtMasterAttachmentPicture> masterWorks) {
        this.masterWorks = masterWorks;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "attachment")
    public List<ArtMasterAttachmentPicture> getQualifications() {
        return qualifications;
    }

    public void setQualifications(List<ArtMasterAttachmentPicture> qualifications) {
        this.qualifications = qualifications;
    }
}
