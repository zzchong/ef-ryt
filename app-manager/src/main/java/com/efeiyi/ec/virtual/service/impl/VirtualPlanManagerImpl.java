package com.efeiyi.ec.virtual.service.impl;

import com.efeiyi.ec.virtual.dao.VirtualPlanDao;
import com.efeiyi.ec.virtual.service.VirtualPlanManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


/**
 * Created by Administrator on 2015/12/9.
 * 虚拟计划 Service 实现类
 */
public class VirtualPlanManagerImpl implements VirtualPlanManagerService {

    @Autowired
    @Qualifier("virtualPlanDao")
    private VirtualPlanDao virtualPlanDao;

//    @Override
//    public Integer getOrderRelation(VirtualInvestmentPlan virtualOrderPlan) throws Exception {
//        return virtualPlanDao.getOrderRelation(virtualOrderPlan);
//    }

//    @Override
//    public List<PurchaseOrderProduct> getOrderProductList(VirtualInvestmentPlan virtualOrderPlan, PageEntity pageEntity) throws Exception {
//        return virtualPlanDao.getOrderProductList(virtualOrderPlan, pageEntity);
//    }

//    @Override
//    public List<VirtualUser> getVirtualUserList(VirtualInvestorPlan virtualUserPlan, PageEntity pageEntity) throws Exception {
//        return virtualPlanDao.getVirtualUserList(virtualUserPlan, pageEntity);
//    }

    @Override
    public void deleteVirtualPlan(String id) throws Exception {
        virtualPlanDao.deleteVirtualPlan(id);
    }

    @Override
    public void removeVirtualPlan(String id) throws Exception {
        virtualPlanDao.removeVirtualPlan(id);
    }
}
