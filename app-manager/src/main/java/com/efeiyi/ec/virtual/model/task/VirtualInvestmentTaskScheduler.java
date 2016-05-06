package com.efeiyi.ec.virtual.model.task;


import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.virtual.model.VirtualInvestmentPlan;
import com.efeiyi.ec.art.virtual.model.VirtualInvestorPlan;
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
        List<VirtualInvestorPlan> virtualInvestorPlanList = session.createQuery("from VirtualInvestorPlan").list();

        //按模板比例选出参与融资的用户
        randomScreeVirtualUser(virtualInvestorPlanList);

        //遍历所有融资者启动随机时间融资行为
        for(VirtualInvestorPlan virtualInvestorPlan : virtualInvestorPlanList){
            launchInvestAction(virtualInvestorPlan);
        }

        logger.info("Ready to generate virtual investments");

    }

    private void launchInvestAction(VirtualInvestorPlan virtualInvestorPlan) {
        Number[] numbers = setVirtualLevel(virtualInvestorPlan);
        Double percentageOfAmount = (Double) numbers[0];
        Integer fixedInvestmentIncrement = (Integer) numbers[1];
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
        futureCalendar.set(Integer.parseInt(nowArray[0]),
                Integer.parseInt(nowArray[1]) - 1,
                Integer.parseInt(nowArray[2]),
                8,
                0,
                0);
        long future = futureCalendar.getTimeInMillis();
        long futureFromNow = future - now;
        int randomSize = virtualInvestorPlan.getVirtualUserList().size();
        for (Long relativeTimePoint : investList) {
            long timePoint = futureFromNow + relativeTimePoint;
                int randomNumber = random.nextInt(randomSize);
                VirtualUser virtualUser = virtualInvestorPlan.getVirtualUserList().get(randomNumber);
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
                                timePoint < 0 ? 0 : timePoint
//                                    1000
                        );

        }
    }

    private void randomScreeVirtualUser(List<VirtualInvestorPlan> virtualInvestorPlanList) {
        BigDecimal goalMoney = virtualInvestmentPlan.getVirtualArtwork().getArtwork().getInvestGoalMoney();
        Random random = new Random();
        for(int y = 0; y <virtualInvestorPlanList.size(); y++) {
            double percentageOfAmount = 0.0;
            int fixedInvestmentIncrement = 0;
            if (goalMoney.compareTo(new BigDecimal(5001)) < 0) {
                switch (virtualInvestorPlanList.get(y).getGroup()) {
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
                switch (virtualInvestorPlanList.get(y).getGroup()) {
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
                switch (virtualInvestorPlanList.get(y).getGroup()) {
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
            List<VirtualUser> virtualUserList = new ArrayList<>();
            int range = goalMoney.multiply(new BigDecimal(percentageOfAmount)).divide(new BigDecimal(fixedInvestmentIncrement),2,BigDecimal.ROUND_HALF_DOWN).intValue();
            for(int x = 0; x < range; x++){
                virtualUserList.add(virtualInvestorPlanList.get(y).getVirtualUserList().get(random.nextInt(virtualInvestorPlanList.get(y).getVirtualUserList().size())));
            }
            virtualInvestorPlanList.get(y).setVirtualUserList(virtualUserList);
        }
    }

    private Number[] setVirtualLevel(VirtualInvestorPlan virtualInvestorPlan) {
        Double percentageOfAmount = 0.0;
        Integer fixedInvestmentIncrement = 0;
        if (virtualInvestmentPlan.getVirtualArtwork().getArtwork().getInvestGoalMoney().compareTo(new BigDecimal(5001)) < 0) {
            switch (virtualInvestorPlan.getGroup()) {
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
            switch (virtualInvestorPlan.getGroup()) {
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
            switch (virtualInvestorPlan.getGroup()) {
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
        logger.info(" investment arranging.");
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
        logger.info("investment arranged.");
    }

    @Override
    public void setVirtualPlan(VirtualPlan virtualPlan) {
        if (session == null) {
            session = sessionFactory.openSession();
        }
        this.virtualInvestmentPlan = (VirtualInvestmentPlan) session.get(VirtualInvestmentPlan.class, virtualPlan.getId());
    }


}


