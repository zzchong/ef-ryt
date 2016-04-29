package com.efeiyi.ec.virtual.model.task;


import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.virtual.model.VirtualArtwork;
import com.efeiyi.ec.art.virtual.model.VirtualInvestmentPlan;
import com.efeiyi.ec.art.virtual.model.VirtualPlan;
import com.efeiyi.ec.art.virtual.model.VirtualUser;
import com.efeiyi.ec.virtual.model.timer.SuperTimer;
import com.efeiyi.ec.virtual.util.DigitalSignatureUtil;
import com.efeiyi.ec.virtual.util.VirtualPlanConstant;
import org.hibernate.exception.GenericJDBCException;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2015/11/20.
 */
public class VirtualInvestmentTaskScheduler extends BaseTimerTask {

    private VirtualInvestmentPlan virtualInvestmentPlan;
//    private List<ProductModel> productModelList;

    @Override
    public boolean cancel() {
        //暂停时保存进度，先放弃了，太啰嗦
//        SuperTimer.getInstance().getSubTaskTempStoreMap().put(virtualOrderPlan, productModelList);
        try {
            if (session == null || !session.isOpen()) {
                session = sessionFactory.openSession();
            }
            resetPlanStatus();
        } catch (GenericJDBCException jdbcE) {
            retrieveSessionFactory();
            resetPlanStatus();
        }
        logger.info("PurchaseOrderTaskScheduler cancelled.");
        return super.cancel();
    }

    private void resetPlanStatus() {
        virtualInvestmentPlan = (VirtualInvestmentPlan) session.get(VirtualInvestmentPlan.class, virtualInvestmentPlan.getId());
        virtualInvestmentPlan.setStatus(VirtualPlanConstant.planStatusInit);
        session.saveOrUpdate(virtualInvestmentPlan);
        session.flush();
        session.close();
    }

    public void execute(List<VirtualPlan> virtualPlanList) {

        virtualInvestmentPlan = (VirtualInvestmentPlan) session.get(VirtualInvestmentPlan.class, virtualInvestmentPlan.getId());
        virtualInvestmentPlan.setStatus(VirtualPlanConstant.planStatusStarted);
        session.saveOrUpdate(virtualInvestmentPlan);
        session.flush();


        Double percentageOfAmount = 0.0;
        Integer fixedInvestmentIncrement = 0;
        setVirtualLevel(percentageOfAmount,fixedInvestmentIncrement);

        Artwork artwork = virtualInvestmentPlan.getVirtualArtwork().getArtwork();
        Double investGoal = artwork.getInvestGoalMoney().doubleValue();
        Double subInvestGoal = investGoal * percentageOfAmount;
        int investTimes = subInvestGoal.intValue() / fixedInvestmentIncrement;
        Long[] morningInvests = new Long[investTimes * 2 / 5];
        Long[] afternoonInvests = new Long[investTimes / 5];
        Long[] eveningInvests = new Long[investTimes * 2 / 5];
        Random random = new Random(2 * 60 * 60 * 1000);
        for(Long morningInvest : morningInvests){
            morningInvest = random.nextLong();
        }
        for(Long afternoonInvest : afternoonInvests){
            afternoonInvest = random.nextLong() / 2 + 7 * 60 * 60 * 1000;
        }
        for(Long eveningInvest : eveningInvests){
            eveningInvest = random.nextLong() + 13 * 60 * 60 * 1000;
        }
        List<Long> investList = new ArrayList<>(morningInvests.length + afternoonInvests.length + eveningInvests.length);
        Collections.addAll(investList,morningInvests);
        Collections.addAll(investList,afternoonInvests);
        Collections.addAll(investList,eveningInvests);
//        DateFormat dateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
//        Calendar futureCalendar = Calendar.getInstance();
//        String[] nowArray = dateFormat.format(futureCalendar.getTime()).split(",");
////        String[] peakTimeArray = dateFormat.format(virtualInvestmentPlan.getPeakTime()).split(",");
////        futureCalendar.set(Integer.parseInt(nowArray[0]), Integer.parseInt(nowArray[1]) - 1, Integer.parseInt(nowArray[2]), Integer.parseInt(peakTimeArray[3]), Integer.parseInt(peakTimeArray[4]), Integer.parseInt(peakTimeArray[5]));
//        long now = System.currentTimeMillis();
//        long future = futureCalendar.getTimeInMillis();
//        long futureFromNow = future - now;

//        for (int x = 0; x < randomOrderTimePoint.length; x++) {
//            randomOrderTimePoint[x] = (long) (random.nextGaussian() * 60 * 60 * 1000) * virtualInvestmentPlan.getStandardDeviation() + futureFromNow;
//        }
//        Arrays.sort(randomOrderTimePoint);
//        int count = 0;
//        double investAmount = 0;
        Long now = System.currentTimeMillis();
        for(Long relativeTimePoint : investList){
            int randomSize = virtualInvestmentPlan.getVirtualInvestorPlan().getVirtualUserList().size();
            Random randomUser = new Random(randomSize);
            VirtualUser virtualUser = virtualInvestmentPlan.getVirtualInvestorPlan().getVirtualUserList().get(randomUser.nextInt());
Map resultMap = new TreeMap();
            resultMap.put("price",fixedInvestmentIncrement);
            resultMap.put("userId",virtualUser.getUserBrief().getUser().getId());
            resultMap.put("artworkId",artwork.getId());
            resultMap.put("timestamp",now + relativeTimePoint);
            try {
                resultMap.put("signmsg",DigitalSignatureUtil.encrypt(resultMap));
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Encrypt Exception");
            }
            SuperTimer.getInstance().getSubTimerMap()
                    .get(virtualInvestmentPlan)
                    .getSubTimer()
                    .schedule(new VirtualInvestmentGenerator(resultMap, virtualInvestmentPlan), now + relativeTimePoint);
        }
        logger.info("Ready to generate " + investTimes + " virtual investments");

    }

    private void setVirtualLevel(Double percentageOfAmount, Integer fixedInvestmentIncrement) {
        if (virtualInvestmentPlan.getVirtualArtwork().getArtwork().getInvestGoalMoney().compareTo(new BigDecimal(5001)) < 0){
            switch (virtualInvestmentPlan.getVirtualInvestorPlan().getGroup()){
                case "1":{
                    percentageOfAmount = 0.02;
                    fixedInvestmentIncrement = 20;
                }
                case "2":{
                    percentageOfAmount = 0.3;
                    fixedInvestmentIncrement = 100;
                }
                case "3":{
                    percentageOfAmount = 0.15;
                    fixedInvestmentIncrement = 0;
                }
                case "4":{
                    percentageOfAmount = 0.1;
                    fixedInvestmentIncrement = 10;
                }
                case "5":{
                    percentageOfAmount = 0.2;
                    fixedInvestmentIncrement = 20;
                }
                case "6":{
                    percentageOfAmount = 0.03;
                    fixedInvestmentIncrement = 2;
                }

            }
        }
        else if(virtualInvestmentPlan.getVirtualArtwork().getArtwork().getInvestGoalMoney().compareTo(new BigDecimal(15001)) < 0){
            switch (virtualInvestmentPlan.getVirtualInvestorPlan().getGroup()){
                case "1":{
                    percentageOfAmount = 0.04;
                    fixedInvestmentIncrement = 80;
                }
                case "2":{
                    percentageOfAmount = 0.2;
                    fixedInvestmentIncrement = 200;
                }
                case "3":{
                    percentageOfAmount = 0.2;
                    fixedInvestmentIncrement = 100;
                }
                case "4":{
                    percentageOfAmount = 0.05;
                    fixedInvestmentIncrement = 20;
                }
                case "5":{
                    percentageOfAmount = 0.3;
                    fixedInvestmentIncrement = 60;
                }
                case "6":{
                    percentageOfAmount = 0.01;
                    fixedInvestmentIncrement = 2;
                }

            }
        }
        else{
            switch (virtualInvestmentPlan.getVirtualInvestorPlan().getGroup()){
                case "1":{
                    percentageOfAmount = 0.04;
                    fixedInvestmentIncrement = 100;
                }
                case "2":{
                    percentageOfAmount = 0.3;
                    fixedInvestmentIncrement = 300;
                }
                case "3":{
                    percentageOfAmount = 0.2;
                    fixedInvestmentIncrement = 100;
                }
                case "4":{
                    percentageOfAmount = 0.05;
                    fixedInvestmentIncrement = 10;
                }
                case "5":{
                    percentageOfAmount = 0.2;
                    fixedInvestmentIncrement = 80;
                }
                case "6":{
                    percentageOfAmount = 0.01;
                    fixedInvestmentIncrement = 2;
                }

            }
        }
    }

    @Override
    public void run() {
        logger.info(" Purchase order arranging.");
        if (session == null || !session.isOpen()) {
            session = sessionFactory.openSession();
        }
        try {
            execute(null);
        } catch (GenericJDBCException jdbcE) {
            retrieveSessionFactory();
            execute(null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        logger.info("Purchase arranged.");
    }

    @Override
    public void setVirtualPlan(VirtualPlan virtualPlan) {
        if (session == null) {
            session = sessionFactory.openSession();
        }
        this.virtualInvestmentPlan = (VirtualInvestmentPlan) session.get(VirtualInvestmentPlan.class, virtualPlan.getId());
    }


    private void switchGroup(int percentageValueOfInvestGoal,int fixedInvestAmount){

    }
}


