package com.efeiyi.ec.virtual.model.task;


import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.virtual.model.*;
import com.efeiyi.ec.virtual.model.timer.SuperTimer;
import com.efeiyi.ec.virtual.util.DigitalSignatureUtil;
import com.efeiyi.ec.virtual.util.VirtualPlanConstant;
import javafx.util.converter.IntegerStringConverter;
import org.hibernate.exception.GenericJDBCException;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2015/11/20.
 */
public class VirtualPraiseTaskScheduler extends BaseTimerTask {

    private VirtualPraisePlan virtualPraisePlan;

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
        logger.info("VirtualInvestmentTaskScheduler cancelled.");
        return super.cancel();
    }

    private void resetPlanStatus() {
        virtualPraisePlan = (VirtualPraisePlan) session.get(VirtualPraisePlan.class, virtualPraisePlan.getId());
        virtualPraisePlan.setStatus(VirtualPlanConstant.planStatusInit);
        session.saveOrUpdate(virtualPraisePlan);
        session.flush();
        session.close();
    }

    public void execute(List<VirtualPlan> virtualPlanList) {

        virtualPraisePlan = (VirtualPraisePlan) session.get(VirtualPraisePlan.class, virtualPraisePlan.getId());
        virtualPraisePlan.setStatus(VirtualPlanConstant.planStatusStarted);
        session.saveOrUpdate(virtualPraisePlan);
        session.flush();
        List<VirtualInvestorPlan> virtualInvestorPlanList = session.createQuery("from VirtualInvestorPlan").list();

        //按模板比例选出参与点赞的用户
        randomScreeVirtualUser(virtualInvestorPlanList);

        //遍历所有融资者启动随机时间点赞行为
        for (VirtualInvestorPlan virtualInvestorPlan : virtualInvestorPlanList) {
            launchPraiseAction(virtualInvestorPlan);
        }

        logger.info("Ready to generate virtual praise");

    }

    private void launchPraiseAction(VirtualInvestorPlan virtualInvestorPlan) {
        Artwork artwork = virtualPraisePlan.getVirtualArtwork().getArtwork();
        int size = virtualInvestorPlan.getVirtualUserList().size();
        Long[] morningPraise = new Long[size / 5];
        Long[] afternoonPraise = new Long[size * 2/ 5];
        Long[] eveningPraise = new Long[size * 2 / 5];
        Random random = new Random();
        for (int x = 0; x < morningPraise.length; x++) {
            morningPraise[x] = Integer.toUnsignedLong(random.nextInt(2 * 60 * 60 * 1000));
        }
        for (int x = 0; x < afternoonPraise.length; x++) {
            afternoonPraise[x] = Integer.toUnsignedLong(random.nextInt(2 * 60 * 60 * 1000)) + 7 * 60 * 60 * 1000;
        }
        for (int x = 0; x < eveningPraise.length; x++) {
            eveningPraise[x] = Integer.toUnsignedLong(random.nextInt(2 * 60 * 60 * 1000)) + 13 * 60 * 60 * 1000;
        }

        List<Long> praiseTimePointList = new ArrayList<>(morningPraise.length + afternoonPraise.length + eveningPraise.length);
        Collections.addAll(praiseTimePointList, morningPraise);
        Collections.addAll(praiseTimePointList, afternoonPraise);
        Collections.addAll(praiseTimePointList, eveningPraise);
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
        for (Long relativeTimePoint : praiseTimePointList) {
            long timePoint = futureFromNow + relativeTimePoint;
            int randomNumber = random.nextInt(randomSize);
            VirtualUser virtualUser = virtualInvestorPlan.getVirtualUserList().get(randomNumber);
            Map resultMap = new TreeMap();
            resultMap.put("currentUserId", virtualUser.getUserBrief().getUser().getId());
            resultMap.put("artworkId", artwork.getId());
            resultMap.put("timestamp", timePoint > 0 ? timePoint : 0);
            try {
                String msg = DigitalSignatureUtil.encrypt(resultMap);
                resultMap.put("signmsg", msg);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Encrypt Exception");
            }
            SuperTimer.getInstance().getSubTimerMap()
                    .get(virtualPraisePlan)
                    .getSubTimer()
                    .schedule(new VirtualPraiseGenerator(resultMap, virtualPraisePlan),
                            timePoint < 0 ? 0 : timePoint
//                                    1000
                    );
        }
    }

    private void randomScreeVirtualUser(List<VirtualInvestorPlan> virtualInvestorPlanList) {
        Random random = new Random();
        for (int y = 0; y < virtualInvestorPlanList.size(); y++) {
            double percentage = 0;
            switch (virtualInvestorPlanList.get(y).getGroup()) {
                case "1":
                    percentage = 0.1;
                    break;
                case "2":
                    percentage = 0.0;
                    break;
                case "3":
                    percentage = 0.02;
                    break;
                case "4":
                    percentage = 0.05;
                    break;
                case "5":
                    percentage = 0.05;
                    break;
                case "6":
                    percentage = 0.06;
            }
            List<VirtualUser> virtualUserList = new ArrayList<>();
            int range = (int)(virtualInvestorPlanList.get(y).getVirtualUserList().size() * percentage) ;
            for (int x = 0; x < range; x++) {
                virtualUserList.add(virtualInvestorPlanList.get(y).getVirtualUserList().get(random.nextInt(virtualInvestorPlanList.get(y).getVirtualUserList().size())));
            }
            virtualInvestorPlanList.get(y).setVirtualUserList(virtualUserList);
        }
    }

//    private Number[] setVirtualLevel(VirtualInvestorPlan virtualInvestorPlan) {
//        Double percentageOfAmount = 0.0;
//        Integer fixedInvestmentIncrement = 0;
//        if (virtualPraisePlan.getVirtualArtwork().getArtwork().getInvestGoalMoney().compareTo(new BigDecimal(5001)) < 0) {
//            switch (virtualInvestorPlan.getGroup()) {
//                case "1":
//                    percentageOfAmount = 0.02;
//                    fixedInvestmentIncrement = 20;
//                    break;
//                case "2":
//                    percentageOfAmount = 0.3;
//                    fixedInvestmentIncrement = 100;
//                    break;
//                case "3":
//                    percentageOfAmount = 0.15;
//                    fixedInvestmentIncrement = 30;
//                    break;
//                case "4":
//                    percentageOfAmount = 0.1;
//                    fixedInvestmentIncrement = 10;
//                    break;
//                case "5":
//                    percentageOfAmount = 0.2;
//                    fixedInvestmentIncrement = 20;
//                    break;
//                case "6":
//                    percentageOfAmount = 0.03;
//                    fixedInvestmentIncrement = 2;
//            }
//        } else if (virtualPraisePlan.getVirtualArtwork().getArtwork().getInvestGoalMoney().compareTo(new BigDecimal(15001)) < 0) {
//            switch (virtualInvestorPlan.getGroup()) {
//                case "1":
//                    percentageOfAmount = 0.04;
//                    fixedInvestmentIncrement = 80;
//                    break;
//                case "2":
//                    percentageOfAmount = 0.2;
//                    fixedInvestmentIncrement = 200;
//                    break;
//                case "3":
//                    percentageOfAmount = 0.2;
//                    fixedInvestmentIncrement = 100;
//                    break;
//                case "4":
//                    percentageOfAmount = 0.05;
//                    fixedInvestmentIncrement = 20;
//                    break;
//                case "5":
//                    percentageOfAmount = 0.3;
//                    fixedInvestmentIncrement = 60;
//                    break;
//                case "6":
//                    percentageOfAmount = 0.01;
//                    fixedInvestmentIncrement = 2;
//            }
//        } else {
//            switch (virtualInvestorPlan.getGroup()) {
//                case "1":
//                    percentageOfAmount = 0.04;
//                    fixedInvestmentIncrement = 100;
//                    break;
//                case "2":
//                    percentageOfAmount = 0.3;
//                    fixedInvestmentIncrement = 300;
//                    break;
//                case "3":
//                    percentageOfAmount = 0.2;
//                    fixedInvestmentIncrement = 100;
//                    break;
//                case "4":
//                    percentageOfAmount = 0.05;
//                    fixedInvestmentIncrement = 10;
//                    break;
//                case "5":
//                    percentageOfAmount = 0.2;
//                    fixedInvestmentIncrement = 80;
//                    break;
//                case "6":
//                    percentageOfAmount = 0.01;
//                    fixedInvestmentIncrement = 2;
//            }
//        }
//        Number[] numbers = new Number[2];
//        numbers[0] = percentageOfAmount;
//        numbers[1] = fixedInvestmentIncrement;
//        return numbers;
//    }

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
        this.virtualPraisePlan = (VirtualPraisePlan) session.get(VirtualPraisePlan.class, virtualPlan.getId());
    }


}


