package com.efeiyi.ec.system.app.checkManager.service.impl;

import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.system.app.checkManager.service.CheckProjectManager;
import com.ming800.core.base.service.BaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Administrator on 2016/11/7.
 */
@Service
public class CheckProjectManagerImpl implements CheckProjectManager {
    @Autowired
    BaseManager baseManager;

    @Override
    @Transactional
    public void returnIncome(String artworkId) throws Exception {
        Artwork artwork = (Artwork) baseManager.getObject(Artwork.class.getName(), artworkId);

        //返回投资收益
        String biddingHql = "select s from com.efeiyi.ec.art.model.ArtworkBidding s where s.artwork.id = :artworkId and s.status = '1' ORDER BY s.price desc";
        LinkedHashMap<String, Object> params1 = new LinkedHashMap<>();
        params1.put("artworkId", artworkId);
        List<ArtworkBidding> biddingList = baseManager.listObject(biddingHql, params1);
        BigDecimal roi = biddingList.get(0).getPrice().divide(artwork.getInvestGoalMoney(), 2).subtract(new BigDecimal("1.00"));//溢价率

        String investHql = "select s from com.efeiyi.ec.art.model.ArtworkInvest s where s.artwork.id = :artworkId  and s.status<>'0'";
        List<ArtworkInvest>  artworkInvests = baseManager.listObject(investHql, params1);
        for(ArtworkInvest artworkInvest  :  artworkInvests){
            Account accout = artworkInvest.getAccount();

            //生成投资收益记录
            ROIRecord roiRecord = new ROIRecord();
            roiRecord.setAccount(accout);
            roiRecord.setStatus("1");
            roiRecord.setCurrentBalance(artworkInvest.getPrice().multiply(roi));
            roiRecord.setArtwork(artworkInvest.getArtwork());
            roiRecord.setCreateDatetime(new Date());
            roiRecord.setUser(artworkInvest.getCreator());
            roiRecord.setArtworkInvest(artworkInvest);
            roiRecord.setDetails("投资《"+artworkInvest.getArtwork().getTitle()+"》收益");
            baseManager.saveOrUpdate(ROIRecord.class.getName(),roiRecord);

            accout.setCurrentBalance(accout.getCurrentBalance().add(artworkInvest.getPrice().multiply(roi)));
            accout.setCurrentUsableBalance(accout.getCurrentUsableBalance().add(artworkInvest.getPrice().multiply(roi)));
            baseManager.saveOrUpdate(Account.class.getName(),accout);

            Bill bill = new Bill();
            bill.setDetail("投资《"+artworkInvest.getArtwork().getTitle()+"》的收益");
            bill.setTitle("投资《"+artworkInvest.getArtwork().getTitle()+"》的收益");
            bill.setStatus("1");
            bill.setMoney(artworkInvest.getPrice().multiply(roi).subtract(artworkInvest.getPrice()));
            bill.setAuthor(artworkInvest.getCreator());
            bill.setCreateDatetime(new Date());
            bill.setType("4");
            bill.setOutOrIn("1");
            bill.setPayWay("3");
            baseManager.saveOrUpdate(Bill.class.getName(), bill);
        }

        artwork.setIsReturnIncome("2");
        baseManager.saveOrUpdate(Artwork.class.getName(), artwork);
    }
}
