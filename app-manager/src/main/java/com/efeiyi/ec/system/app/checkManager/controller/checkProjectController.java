package com.efeiyi.ec.system.app.checkManager.controller;

import com.efeiyi.ec.art.model.*;

import com.efeiyi.ec.quartz.job.InvestJob;
import com.efeiyi.ec.quartz.trigger.InvestTrigger;
import com.efeiyi.ec.system.app.checkManager.CheckConstant;
import com.efeiyi.ec.system.app.checkManager.service.CheckProjectManager;
import com.efeiyi.ec.system.service.SmsCheckManager;
import com.efeiyi.ec.system.service.impl.SmsCheckManagerImpl;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.PConst;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by Administrator on 2016/4/7.
 * 项目审核 Controller
 */
@Controller
@RequestMapping("/checkProject")
public class checkProjectController {

    @Autowired
    private BaseManager baseManager;
    @Autowired
    private CheckProjectManager checkProjectManager;

    private static String ENCODING = "UTF-8";

    @RequestMapping("/remove.do")
    public ModelAndView removeCheckProject(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String id = request.getParameter("id");
        if (null == id || "".equals(id.trim())){
            throw new Exception("删除审核项目失败：审核项目Id为空!");
        }

        Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), id);
        artwork.setStatus(CheckConstant.ARTWORK_STATUS_REMOVE);
        baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
        return new ModelAndView("redirect:/basic/xm.do?qm=plistCheckArtwork_checkDefault&checkProject=checkProject");
    }

    @RequestMapping("/checkPass.do")
    public ModelAndView checkPassProject(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SmsCheckManager smsCheckManager = new SmsCheckManagerImpl();
        String smsDescription = null;
        String id = request.getParameter("id");
        String type = request.getParameter("type");
        String resultPage = request.getParameter("resultPage");
        if (null == id || "".equals(id.trim())){
            if (CheckConstant.ARTWORK_STEP_WAIT.equals(type)){
                throw new Exception("待审核项目通过失败：待审核项目Id为空!");
            }
            throw new Exception("审核项目通过失败：审核项目Id为空!");
        }

        Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), id);
        if (CheckConstant.ARTWORK_STEP_WAIT.equals(type)){
            artwork.setStep(CheckConstant.ARTWORK_STEP_CHECKING);
        }
        if(CheckConstant.ARTWORK_STEP_CREATION_WAIT.equals(type)){
            artwork.setStep(CheckConstant.ARTWORK_STEP_CREATION_CHECKING);
        }
        if (CheckConstant.ARTWORK_STEP_CHECKING.equals(type)){
            artwork.setStep(CheckConstant.ARTWORK_STEP_PASS);
            artwork.setType(CheckConstant.ARTWORK_STATUS_FINANCING);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(new Date());
            calendar1.add(Calendar.DAY_OF_MONTH,1);
            artwork.setInvestStartDatetime(calendar1.getTime());
            //当前时间+30天
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH,30);
            artwork.setInvestEndDatetime(calendar.getTime());

            calendar.add(Calendar.DAY_OF_MONTH,artwork.getDuration());
            artwork.setCreationEndDatetime(calendar.getTime());

            InvestTrigger investTrigger = new InvestTrigger();
            investTrigger.execute(artwork.getId(),artwork.getInvestEndDatetime(),"invest");

            smsDescription = "发布审核成功";
        }
        if(CheckConstant.ARTWORK_STEP_CREATION_CHECKING.equals(type)){
            artwork.setStep(CheckConstant.ARTWORK_STEP_CREATION_PASS);
            artwork.setType(CheckConstant.ARTWORK_STATUS_SALE);
            artwork.setAuctionStartDatetime(new Date());
            //当前时间+30天
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            //calendar.add(Calendar.DAY_OF_MONTH,30);
            calendar.add(Calendar.MINUTE, 5);
            artwork.setAuctionEndDatetime(calendar.getTime());
            InvestTrigger investTrigger = new InvestTrigger();
            investTrigger.execute(artwork.getId(),artwork.getAuctionEndDatetime(),"auction");

            smsDescription = "创作审核成功";
        }
        baseManager.saveOrUpdate(Artwork.class.getName(), artwork);

        if(!CheckConstant.ARTWORK_STEP_WAIT.equals(type) && !CheckConstant.ARTWORK_STEP_CREATION_WAIT.equals(type)) {
            String mobile = artwork.getAuthor().getMaster().getPhone();
            String tpl_value = URLEncoder.encode("#title#", ENCODING) + "=" + URLEncoder.encode(artwork.getTitle(), ENCODING) + "&"
                    + URLEncoder.encode("#description#", ENCODING) + "=" + URLEncoder.encode(smsDescription, ENCODING);

            Thread t = new Thread(
                    new Runnable(){
                        @Override
                        public void run() {
                            smsCheckManager.send(mobile, "1617172", tpl_value);
                        }
                    }
            );
            t.start();
        }

        if (null != resultPage && "V".equals(resultPage.trim())){
            return new ModelAndView("redirect:/basic/xm.do?qm=viewCheckArtwork&checkProject=checkProject&id=" + id);
        }
        return new ModelAndView("redirect:/basic/xm.do?qm=plistCheckArtwork_checkDefault&checkProject=checkProject");
    }

    @RequestMapping("/checkReject.do")
    public ModelAndView checkRejectProject(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SmsCheckManager smsCheckManager = new SmsCheckManagerImpl();
        String smsDescription = null;

        String id = request.getParameter("id");
        String type = request.getParameter("type");
        String resultPage = request.getParameter("resultPage");
        String message = request.getParameter("message");
        if (null == id || "".equals(id.trim())){
            throw new Exception("驳回审核项目失败：审核项目Id为空!");
        }

        Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), id);

        if (CheckConstant.ARTWORK_STEP_CHECKING.equals(type)){
            artwork.setStep(CheckConstant.ARTWORK_STEP_REJECT);
            smsDescription = "发布审核未通过";
        } else if(CheckConstant.ARTWORK_STEP_CREATION_CHECKING.equals(type)){
            artwork.setStep(CheckConstant.ARTWORK_STEP_CREATION_REJECT);
            smsDescription = "创作审核未通过";
        }

        artwork.setFeedback(message);
        baseManager.saveOrUpdate(Artwork.class.getName(), artwork);

        String mobile = artwork.getAuthor().getMaster().getPhone();
        String tpl_value = URLEncoder.encode("#title#", ENCODING) + "=" + URLEncoder.encode(artwork.getTitle(), ENCODING) + "&"
                + URLEncoder.encode("#description#", ENCODING) + "=" + URLEncoder.encode(smsDescription, ENCODING);
        Thread t = new Thread(
                new Runnable(){
                    @Override
                    public void run() {
                        smsCheckManager.send(mobile, "1617172", tpl_value);
                    }
                }
        );
        t.start();

        if (null != resultPage && "V".equals(resultPage.trim())){
            return new ModelAndView("redirect:/basic/xm.do?qm=viewCheckArtwork&checkProject=checkProject&id=" + id);
        }
        return new ModelAndView("redirect:/basic/xm.do?qm=plistCheckArtwork_checkDefault&checkProject=checkProject");
    }

    @RequestMapping("returnIncome")
    public ModelAndView returnIncome(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("redirect:/basic/xm.do?qm=plistCheckArtwork_checkDefault&checkProject=checkProject");

        String artworkId = request.getParameter("id");
        try {
            checkProjectManager.returnIncome(artworkId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mv;
    }

}
