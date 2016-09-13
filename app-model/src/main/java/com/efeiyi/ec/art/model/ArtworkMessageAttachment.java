package com.efeiyi.ec.art.model;

import com.efeiyi.ec.art.organization.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2016/4/12.
 *
 */
@Entity
@Table(name = "app_art_work_message_attachment")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class ArtworkMessageAttachment implements Serializable{//项目和动态附件
    private String id;
    private ArtworkMessage artworkMessage;
    private String fileUri;
    private String fileType;
    private String videoPicture;

    @Id
    @GenericGenerator(name = "id", strategy = "com.ming800.core.p.model.M8idGenerator")
    @GeneratedValue(generator = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "FileUri")
    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artworkMessage_id")
    public ArtworkMessage getArtworkMessage() {
        return artworkMessage;
    }

    public void setArtworkMessage(ArtworkMessage artworkMessage) {
        this.artworkMessage = artworkMessage;
    }
    @Column(name = "FileType")
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Column(name = "video_picture")
    public String getVideoPicture() { return videoPicture; }
    public void setVideoPicture(String videoPicture) { this.videoPicture = videoPicture; }
}


