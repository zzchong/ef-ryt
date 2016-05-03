package com.efeiyi.ec.virtual.handler;

import com.efeiyi.ec.art.virtual.model.VirtualInvestorPlan;
import com.efeiyi.ec.art.virtual.model.VirtualPlanElement;
import com.efeiyi.ec.art.virtual.model.VirtualUser;
import com.efeiyi.ec.virtual.service.VirtualPlanManagerService;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.does.service.DoHandler;
import com.ming800.core.util.ApplicationContextUtil;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/12/9.
 * 获取虚拟计划  关联数量及完成数量
 */
public class VirtualUserListHandler implements DoHandler {

    private BaseManager baseManager = (BaseManager) ApplicationContextUtil.getApplicationContext().getBean("baseManagerImpl");
//    private VirtualPlanManagerService vpService = (VirtualPlanManagerService) ApplicationContextUtil.getApplicationContext().getBean("virtualPlanManagerImpl");

    @Override
    public ModelMap handle(ModelMap modelMap, HttpServletRequest request) throws Exception {
        modelMap.put("virtualUserBriefList", getAllUsers(request));
        modelMap.put("selectedVirtualUserBriefList", getSelectedUsers(modelMap));
        return modelMap;
    }

    private List getAllUsers(HttpServletRequest request) throws Exception {
        XQuery xQuery = new XQuery("listAppVirtualUserBrief_appDefault", request);
        return baseManager.listObject(xQuery);
    }

    private List getSelectedUsers(ModelMap modelMap) throws Exception {
        VirtualInvestorPlan virtualInvestorPlan = (VirtualInvestorPlan) modelMap.get("object");
        if (virtualInvestorPlan.getVirtualUserList() != null) {
            List<String> idList = new ArrayList<>(virtualInvestorPlan.getVirtualUserList().size());
            for (VirtualUser virtualUser : virtualInvestorPlan.getVirtualUserList()) {
                idList.add(virtualUser.getId());
            }
            return idList;
        }
        return null;
    }

}
