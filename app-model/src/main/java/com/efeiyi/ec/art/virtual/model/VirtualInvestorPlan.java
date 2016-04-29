package com.efeiyi.ec.art.virtual.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.util.List;

/**
 * Created by Administrator on 2015/11/25.
 */
@Entity
@Table(name = "virtual_investor_plan")
public class VirtualInvestorPlan {

    private String id;
    private String group;
    private Integer count;
//    private List <VirtualStrategy> virtualStrategyList;
    private String status;
    private List<VirtualUser> virtualUserList;
    private String groupName;

    @Column(name = "count")
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "virtualInvestorPlan")
    public List<VirtualUser> getVirtualUserList() {
        return virtualUserList;
    }

    public void setVirtualUserList(List<VirtualUser> virtualUserList) {
        this.virtualUserList = virtualUserList;
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

    @Column(name = "group")
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Column(name = "status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

//    @OneToMany(mappedBy = "virtualInvestorPlan")
//    @NotFound(action = NotFoundAction.IGNORE)
//    public List<VirtualStrategy> getVirtualStrategyList() {
//        return virtualStrategyList;
//    }
//
//    public void setVirtualStrategyList(List<VirtualStrategy> virtualStrategyList) {
//        this.virtualStrategyList = virtualStrategyList;
//    }

    @Column(name = "group_name")
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
