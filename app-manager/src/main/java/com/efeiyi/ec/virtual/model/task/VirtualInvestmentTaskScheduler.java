package com.efeiyi.ec.virtual.model.task;


import com.efeiyi.ec.art.model.Artwork;
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

    @Override
    public boolean cancel() {
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
        Number[] numbers = setVirtualLevel(percentageOfAmount, fixedInvestmentIncrement);
        percentageOfAmount = (Double) numbers[0];
        fixedInvestmentIncrement = (Integer) numbers[1];
        Artwork artwork = virtualInvestmentPlan.getVirtualArtwork().getArtwork();
        Double investGoal = artwork.getInvestGoalMoney().doubleValue();
        Double subInvestGoal = investGoal * percentageOfAmount;
        int investTimes = subInvestGoal.intValue() / fixedInvestmentIncrement;
        Long[] morningInvests = new Long[investTimes * 2 / 5];
        Long[] afternoonInvests = new Long[investTimes / 5];
        Long[] eveningInvests = new Long[investTimes * 2 / 5];
        Random random = new Random();
        for (int x = 0; x < morningInvests.length;x++) {
            morningInvests[x] = Integer.toUnsignedLong(random.nextInt(2 * 60 * 60 * 1000));
        }
        for (int x = 0; x < afternoonInvests.length;x++) {
            afternoonInvests[x] = Integer.toUnsignedLong(random.nextInt(2 * 60 * 60 * 1000)) + 7 * 60 * 60 * 1000;;
        }for (int x = 0; x < eveningInvests.length;x++) {
            eveningInvests[x] = Integer.toUnsignedLong(random.nextInt(2 * 60 * 60 * 1000)) + 13 * 60 * 60 * 1000;
        }

        List<Long> investList = new ArrayList<>(morningInvests.length + afternoonInvests.length + eveningInvests.length);
        Collections.addAll(investList, morningInvests);
        Collections.addAll(investList, afternoonInvests);
        Collections.addAll(investList, eveningInvests);
        Long now = System.currentTimeMillis();
        DateFormat dateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
        Calendar futureCalendar = Calendar.getInstance();
        String[] nowArray = dateFormat.format(futureCalendar.getTime()).split(",");
//        String[] peakTimeArray = dateFormat.format(virtualInvestmentPlan.getPeakTime()).split(",");
        futureCalendar.set(Integer.parseInt(nowArray[0]),
                Integer.parseInt(nowArray[1]) - 1,
                Integer.parseInt(nowArray[2]),
                8,
                0,
                0);
        long future = futureCalendar.getTimeInMillis();
        long futureFromNow = future - now;
        int randomSize = virtualInvestmentPlan.getVirtualInvestorPlan().getVirtualUserList().size();
        for (Long relativeTimePoint : investList) {
            long timePoint = futureFromNow + relativeTimePoint;
            if(timePoint > 0) {
                int randomNumber = random.nextInt(randomSize);
                VirtualUser virtualUser = virtualInvestmentPlan.getVirtualInvestorPlan().getVirtualUserList().get(randomNumber);
                Map resultMap = new TreeMap();
                resultMap.put("price", fixedInvestmentIncrement);
                resultMap.put("userId", virtualUser.getUserBrief().getUser().getId());
                resultMap.put("artworkId", artwork.getId());
                resultMap.put("timestamp", timePoint);
                try {
                    String msg = DigitalSignatureUtil.encrypt(resultMap);
                    resultMap.put("signmsg", msg);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Encrypt Exception");
                }
                    SuperTimer.getInstance().getSubTimerMap()
                            .get(virtualInvestmentPlan)
                            .getSubTimer()
                            .schedule(new VirtualInvestmentGenerator(resultMap, virtualInvestmentPlan),
                                    timePoint
//                                    1000
                            );

            }
        }
        logger.info("Ready to generate " + investTimes + " virtual investments");

    }

    private Number[] setVirtualLevel(Double percentageOfAmount, Integer fixedInvestmentIncrement) {
        if (virtualInvestmentPlan.getVirtualArtwork().getArtwork().getInvestGoalMoney().compareTo(new BigDecimal(5001)) < 0) {
            switch (virtualInvestmentPlan.getVirtualInvestorPlan().getGroup()) {
                case "1":
                    percentageOfAmount = 0.02;
                    fixedInvestmentIncrement = 20;
                    break;
                case "2":
                    percentageOfAmount = 0.3;
                    fixedInvestmentIncrement = 100;
                    break;
                case "3":
                    percentageOfAmount = 0.15;
                    fixedInvestmentIncrement = 0;
                    break;
                case "4":
                    percentageOfAmount = 0.1;
                    fixedInvestmentIncrement = 10;
                    break;
                case "5":
                    percentageOfAmount = 0.2;
                    fixedInvestmentIncrement = 20;
                    break;
                case "6":
                    percentageOfAmount = 0.03;
                    fixedInvestmentIncrement = 2;
            }
        } else if (virtualInvestmentPlan.getVirtualArtwork().getArtwork().getInvestGoalMoney().compareTo(new BigDecimal(15001)) < 0) {
            switch (virtualInvestmentPlan.getVirtualInvestorPlan().getGroup()) {
                case "1":
                    percentageOfAmount = 0.04;
                    fixedInvestmentIncrement = 80;
                    break;
                case "2":
                    percentageOfAmount = 0.2;
                    fixedInvestmentIncrement = 200;
                    break;
                case "3":
                    percentageOfAmount = 0.2;
                    fixedInvestmentIncrement = 100;
                    break;
                case "4":
                    percentageOfAmount = 0.05;
                    fixedInvestmentIncrement = 20;
                    break;
                case "5":
                    percentageOfAmount = 0.3;
                    fixedInvestmentIncrement = 60;
                    break;
                case "6":
                    percentageOfAmount = 0.01;
                    fixedInvestmentIncrement = 2;
            }
        } else {
            switch (virtualInvestmentPlan.getVirtualInvestorPlan().getGroup()) {
                case "1":
                    percentageOfAmount = 0.04;
                    fixedInvestmentIncrement = 100;
                    break;
                case "2":
                    percentageOfAmount = 0.3;
                    fixedInvestmentIncrement = 300;
                    break;
                case "3":
                    percentageOfAmount = 0.2;
                    fixedInvestmentIncrement = 100;
                    break;
                case "4":
                    percentageOfAmount = 0.05;
                    fixedInvestmentIncrement = 10;
                    break;
                case "5":
                    percentageOfAmount = 0.2;
                    fixedInvestmentIncrement = 80;
                    break;
                case "6":
                    percentageOfAmount = 0.01;
                    fixedInvestmentIncrement = 2;

            }
        }
        Number[] numbers = new Number[2];
        numbers[0] = percentageOfAmount;
        numbers[1] = fixedInvestmentIncrement;
        return numbers;
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


}


