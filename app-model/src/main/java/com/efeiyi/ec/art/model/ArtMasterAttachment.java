package com.efeiyi.ec.art.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Administrator on 2016/2/25.
 */

@Entity
@Table(name = "app_master_attachment")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class ArtMasterAttachment implements Serializable {

    private String id;
    private Master master;
    private String url;//图片路径
    private String paperType;//证件类型
    private String paperNo;//证件号码
    private String remark;//备注

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
    @JoinColumn(name = "master_id")
    public Master getMaster() {
        return master;
    }

    public void setMaster(Master master) {
        this.master = master;
    }

    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Column(name = "paper_type")
    public String getPaperType() {
        return paperType;
    }
    public void setPaperType(String paperType) {
        this.paperType = paperType;
    }

    @Column(name = "paper_no")
    public String getPaperNo() {
        return paperNo;
    }
    public void setPaperNo(String paperNo) {
        this.paperNo = paperNo;
    }

    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
