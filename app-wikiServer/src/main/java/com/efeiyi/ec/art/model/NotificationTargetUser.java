package com.efeiyi.ec.art.model;

import com.efeiyi.ec.art.organization.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016/4/8.
 */
@Entity
@Table(name = "app_art_notification_targetUsers")
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler"})
public class NotificationTargetUser implements Serializable {
    private String id;
    private List<User> targetUsers;//通知目标
    private List<Notification> Notifications;//通知目标

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
    @JoinColumn(name = "user_ID")
    public List<User> getTargetUsers() {
        return targetUsers;
    }

    public void setTargetUsers(List<User> targetUsers) {
        this.targetUsers = targetUsers;
    }
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_ID")
    public List<Notification> getNotifications() {
        return Notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        Notifications = notifications;
    }
}
