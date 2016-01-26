package com.efeiyi.ec.art.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by Administrator on 2016/1/26.
 *
 */
@Entity
@Table(name = "app_user_push_binding")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class PushUserBinding implements Serializable {
    private String id;
    private String User;
    private String cid;

    @Id
    @GenericGenerator(name = "id", strategy = "com.ming800.core.p.model.M8idGenerator")
    @GeneratedValue(generator = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    public String getUser() {
        return User;
    }

    public void setUser(String user) {
        User = user;
    }
    @Column(name = "cid")
    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }
}
