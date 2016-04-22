package com.efeiyi.ec.virtual.model.task;


import com.efeiyi.ec.art.virtual.model.VirtualArtwork;
import com.efeiyi.ec.art.virtual.model.VirtualInvestmentPlan;
import com.efeiyi.ec.art.virtual.model.VirtualPlan;
import com.efeiyi.ec.virtual.model.timer.SuperTimer;
import com.efeiyi.ec.virtual.util.VirtualPlanConstant;
import org.hibernate.exception.GenericJDBCException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2015/11/20.
 */
public class InvestTaskScheduler extends BaseTimerTask {

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

        //生成ProductModel随机productModel

        Random random = new Random();

        //生成随机时间点
        for (VirtualArtwork virtualArtwork : virtualInvestmentPlan.getVirtualArtworkList()) {
            Long[] randomOrderTimePoint = new Long[virtualInvestmentPlan.getUserCount()];
            DateFormat dateFormat = new SimpleDateFormat("yyyy,MM,dd,HH,mm,ss");
            Calendar futureCalendar = Calendar.getInstance();
            String[] nowArray = dateFormat.format(futureCalendar.getTime()).split(",");
            String[] peakTimeArray = dateFormat.format(virtualInvestmentPlan.getPeakTime()).split(",");
            futureCalendar.set(Integer.parseInt(nowArray[0]), Integer.parseInt(nowArray[1]) - 1, Integer.parseInt(nowArray[2]), Integer.parseInt(peakTimeArray[3]), Integer.parseInt(peakTimeArray[4]), Integer.parseInt(peakTimeArray[5]));
            long now = System.currentTimeMillis();
            long future = futureCalendar.getTimeInMillis();
            long futureFromNow = future - now;

            for (int x = 0; x < randomOrderTimePoint.length; x++) {
                randomOrderTimePoint[x] = (long) (random.nextGaussian() * 60 * 60 * 1000) * virtualInvestmentPlan.getStandardDeviation() + futureFromNow;
            }
            Arrays.sort(randomOrderTimePoint);
            int count = 0;
            double investAmount = 0;
            for (int x = 0; x < randomOrderTimePoint.length; x++) {
                if (randomOrderTimePoint[x] >= 0) {
                    Map investMap = new TreeMap();
                    investMap.put("userId", virtualArtwork);
                    investMap.put("price", random.nextDouble());
                    investMap.put("artworkId", virtualArtwork.getArtwork().getId());
                    SuperTimer.getInstance().getSubTimerMap()
                            .get(virtualInvestmentPlan)
                            .getSubTimer()
                            .schedule(new VirtualInvestmentGenerator(investMap, virtualInvestmentPlan), randomOrderTimePoint[x]);
                    count++;
                    investAmount += (double) investMap.get("price");
                }
            }
            logger.info("Ready to generate " + count + " virtual investments");
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

}


