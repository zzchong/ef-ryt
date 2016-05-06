package com.efeiyi.ec.virtual.controller;

import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.virtual.model.VirtualArtwork;
import com.efeiyi.ec.art.virtual.model.VirtualInvestmentPlan;
import com.efeiyi.ec.art.virtual.model.VirtualInvestorPlan;
import com.efeiyi.ec.art.virtual.model.VirtualPlan;
import com.efeiyi.ec.virtual.model.task.*;
import com.efeiyi.ec.virtual.model.timer.SubTimer;
import com.efeiyi.ec.virtual.model.timer.SuperTimer;
import com.efeiyi.ec.virtual.service.VirtualPlanManagerService;
import com.efeiyi.ec.virtual.util.VirtualPlanConstant;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
import com.ming800.core.taglib.PageEntity;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Administrator on 2015/12/9.
 * 虚拟计划 Controller
 */

@Controller
@RequestMapping("/virtualPlan")
public class VirtualPlanController {

    @Autowired
    private BaseManager baseManager;

    @Autowired
    @Qualifier("virtualPlanManagerImpl")
    private VirtualPlanManagerService vpmService;

    @RequestMapping("/generateFans")
    public void tranFans() {
        VirtualUserGenerator virtualUserGenerator = new VirtualUserGenerator(null);
        virtualUserGenerator.execute(null);

    }


    @RequestMapping("/startPlan.do")
    public ModelAndView startPlan(VirtualPlan virtualPlan, ModelMap modelMap, HttpServletRequest request) {

        List<VirtualPlan> virtualPlanList = new ArrayList<>();
        virtualPlan = (VirtualPlan) baseManager.getObject(VirtualPlan.class.getName(), virtualPlan.getId());
        if (VirtualPlanConstant.planStatusInit.equals(virtualPlan.getStatus())) {
            virtualPlanList.add(virtualPlan);
            CoreTaskScheduler.getInstance().execute(virtualPlanList);
        }
        modelMap.addAttribute(virtualPlan);
        return new ModelAndView("redirect:/basic/xm.do?qm=plistAppVirtualPlan_appDefault", modelMap);
    }

    @RequestMapping("/finishPlan.do")
    @ResponseBody
    public boolean finishPlan(VirtualPlan virtualPlan, ModelMap modelMap, HttpServletRequest request) throws Exception {

        List<VirtualPlan> virtualPlanList = new ArrayList<>();
        virtualPlan = (VirtualPlan) baseManager.getObject(VirtualPlan.class.getName(), virtualPlan.getId());
        virtualPlanList.add(virtualPlan);
        Map map = SuperTimer.getInstance().getSubTimerMap();
        if(!map.containsKey(virtualPlan)) {
            BaseTimerTask timerTask = new VirtualInvestmentTaskFinisher();
            timerTask.setVirtualPlan(virtualPlan);
            SubTimer subTimer = new SubTimer(new Timer(), timerTask, new Timer(), new SubTaskStopper(virtualPlan));
            map.put(virtualPlan, subTimer);
            subTimer.getSubTimer().schedule(timerTask, 0);
//            subTimer.getStopperTimer().schedule(subTimer.getStopTimerTask(), 0);
            return true;
        }
        return  false;
    }

    @RequestMapping("/pausePlan.do")
    @ResponseBody
    public boolean pausePlan(VirtualPlan virtualPlan) {

        virtualPlan = (VirtualPlan) baseManager.getObject(VirtualPlan.class.getName(), virtualPlan.getId());
        SubTimer subTimer = SuperTimer.getInstance().getSubTimerMap().get(virtualPlan);
        if (subTimer != null) {
            return subTimer.cancel();
        }
        return false;
    }
    @RequestMapping("/pausePlan2.do")
    public ModelAndView pausePlan2(VirtualPlan virtualPlan) {

        virtualPlan = (VirtualPlan) baseManager.getObject(VirtualPlan.class.getName(), virtualPlan.getId());
        SubTimer subTimer = SuperTimer.getInstance().getSubTimerMap().remove(virtualPlan);
        if (subTimer != null) {
            subTimer.cancel();
        }
        return new ModelAndView("redirect:/basic/xm.do?qm=plistAppVirtualPlan_appDefault");
    }

    @RequestMapping("/getTypeObjectList.do")
    public ModelAndView getTypeObjectList(ModelMap modelMap, HttpServletRequest request) throws Exception {

        //虚拟计划Id
        String planId = request.getParameter("id");
        if (planId.isEmpty() || planId.trim().equals("")) {
            throw new Exception("获取计划完成列表失败:VirtualPlanId为空!");
        }
        modelMap.put("planId", planId);
        //虚拟计划对象类型
        String type = request.getParameter("type");
        modelMap.put("objectType", type);
        //虚拟计划完成列表分页信息
        PageEntity pageEntity = new PageEntity();
        String pageIndex = request.getParameter("pageEntity.index");
        String pageSize = request.getParameter("pageEntity.size");
        if (pageIndex != null) {
            pageEntity.setIndex(Integer.parseInt(pageIndex));
            pageEntity.setSize(Integer.parseInt(pageSize));
        }
        modelMap.put("pageEntity", pageEntity);
        //虚拟计划对象--点赞praise
        if (!type.isEmpty() && type.trim().equals(VirtualPlanConstant.PLAN_TYPE_PRAISE)) {
        }
        //虚拟计划对象--商品product
        if (!type.isEmpty() && type.trim().equals(VirtualPlanConstant.PLAN_TYPE_PRODUCT)) {
        }
        //虚拟计划对象--收藏
        if (!type.isEmpty() && type.trim().equals(VirtualPlanConstant.PLAN_TYPE_COLLECT)) {
        }
        //虚拟计划对象--人气popularity
        if (!type.isEmpty() && type.trim().equals(VirtualPlanConstant.PLAN_TYPE_POPULARITY)) {
        }

        return new ModelAndView("redirect:/basic/xm.do?qm=plistVirtualPlan_default");
    }

    @RequestMapping("/getTypeObjectView.do")
    public ModelAndView getTypeObjectView(ModelMap modelMap, HttpServletRequest request) throws Exception {

        //虚拟计划Id
        String planId = request.getParameter("id");
        if (planId.isEmpty() || planId.trim().equals("")) {
            throw new Exception("获取计划完成列表失败:VirtualPlanId为空!");
        }
        modelMap.put("planId", planId);
        //虚拟计划对象类型
        String type = request.getParameter("type");
        modelMap.put("objectType", type);

        //虚拟计划融资--investment
        if (!type.isEmpty() && type.trim().equals(VirtualPlanConstant.PLAN_TYPE_INVESTMENT)) {
            return virtualInvestmentView(modelMap, request);
        }
        //虚拟计划对象--点赞praise
        if (!type.isEmpty() && type.trim().equals(VirtualPlanConstant.PLAN_TYPE_PRAISE)) {
        }
        //虚拟计划对象--商品product
        if (!type.isEmpty() && type.trim().equals(VirtualPlanConstant.PLAN_TYPE_PRODUCT)) {
        }
        //虚拟计划对象--收藏
        if (!type.isEmpty() && type.trim().equals(VirtualPlanConstant.PLAN_TYPE_COLLECT)) {
        }
        //虚拟计划对象--人气popularity
        if (!type.isEmpty() && type.trim().equals(VirtualPlanConstant.PLAN_TYPE_POPULARITY)) {
        }

        return new ModelAndView("redirect:/basic/xm.do?qm=plistAppVirtualPlan_default");
    }


    @RequestMapping("/saveVirtualInvestmentPlan.do")
    public ModelAndView saveVirtualOrderPlan(HttpServletRequest request) throws Exception {
        String id = request.getParameter("id");
        VirtualInvestmentPlan virtualInvestmentPlan = (VirtualInvestmentPlan) baseManager.getObject(VirtualInvestmentPlan.class.getName(), id);
        if (virtualInvestmentPlan == null) {
            //获取父类virtualPlan基本属性值
            VirtualPlan virtualPlan = (VirtualPlan) baseManager.getObject(VirtualPlan.class.getName(), id);
            virtualInvestmentPlan = new VirtualInvestmentPlan();
            BeanUtils.copyProperties(virtualInvestmentPlan, virtualPlan);
            //删除父类virtualPlan 并制空ID
            vpmService.deleteVirtualPlan(id);
            virtualInvestmentPlan.setId(null);
        }
        //获取除父类外的基本属性值
        getBaseProperty(virtualInvestmentPlan, request);

        return new ModelAndView("redirect:/basic/xm.do?qm=plistAppVirtualPlan_appDefault&virtual");
    }


    @RequestMapping("/removeVirtualPlan.do")
    public ModelAndView removeVirtualPlan(HttpServletRequest request) throws Exception {
        String id = request.getParameter("id");
        if (id == null || id.trim().equals("")) {
            throw new Exception("删除计划失败:计划ID为空");
        }
        vpmService.removeVirtualPlan(id);
         return new ModelAndView("redirect:/basic/xm.do?qm=plistAppVirtualPlan_appDefault");
    }


    private ModelAndView virtualInvestmentView(ModelMap modelMap, HttpServletRequest request) throws Exception {
        String planId = (String) modelMap.get("planId");
        VirtualInvestmentPlan virtualInvestmentPlan = (VirtualInvestmentPlan) baseManager.getObject(VirtualInvestmentPlan.class.getName(), planId);
        if (virtualInvestmentPlan == null) {
            VirtualPlan virtualPlan = (VirtualPlan) baseManager.getObject(VirtualPlan.class.getName(), planId);
            virtualInvestmentPlan = new VirtualInvestmentPlan();
            BeanUtils.copyProperties(virtualInvestmentPlan, virtualPlan);
        } else {
            long time = virtualInvestmentPlan.getCreateDatetime().getTime();
            virtualInvestmentPlan.setCreateDatetime(new Date(time));
        }
        modelMap.put("object", virtualInvestmentPlan);

        //获取虚拟用户计划列表
        XQuery xQuery = new XQuery("listAppVirtualInvestor_appDefault", request);
        List<VirtualInvestorPlan> virtualInvestorPlanList = baseManager.listObject(xQuery);
        modelMap.put("virtualInvestorPlanList", virtualInvestorPlanList);

//        //获取商品列表
        xQuery = new XQuery("listAppArtwork_appDefault", request);
        List<Artwork> artworkList = baseManager.listObject(xQuery);
        modelMap.put("artworkList", artworkList);

        return new ModelAndView("/virtual/appVirtualInvestmentPlanView");
    }


    private VirtualInvestmentPlan getBaseProperty(VirtualInvestmentPlan virtualInvestmentPlan, HttpServletRequest request) throws Exception {
        String serverUrl = request.getParameter("serverUrl");
        virtualInvestmentPlan.setUrl(serverUrl);

//        String virtualInvestorPlanId = request.getParameter("virtualInvestorPlanId");
//        VirtualInvestorPlan virtualInvestorPlan = (VirtualInvestorPlan) baseManager.getObject(VirtualInvestorPlan.class.getName(), virtualInvestorPlanId);
//        virtualInvestmentPlan.setVirtualInvestorPlan(virtualInvestorPlan);

        String artworkId = request.getParameter("artworkId");
        Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), artworkId);
        VirtualArtwork virtualArtwork = new VirtualArtwork();
        virtualArtwork.setArtwork(artwork);
//        sessionFactory.getCurrentSession().merge(virtualArtwork);
        baseManager.saveOrUpdate(VirtualArtwork.class.getName(), virtualArtwork);
        virtualInvestmentPlan.setVirtualArtwork(virtualArtwork);
        virtualInvestmentPlan.setImplementClass("com.efeiyi.ec.virtual.model.task.VirtualInvestmentTaskScheduler");
        virtualInvestmentPlan.setStatus(VirtualPlanConstant.planStatusInit);
        baseManager.saveOrUpdate(VirtualInvestmentPlan.class.getName(), virtualInvestmentPlan);
        virtualArtwork.setVirtualInvestmentPlan(virtualInvestmentPlan);
        baseManager.saveOrUpdate(VirtualArtwork.class.getName(), virtualArtwork);
//        sessionFactory.getCurrentSession().merge(virtualInvestmentPlan);
        return virtualInvestmentPlan;
    }
}
