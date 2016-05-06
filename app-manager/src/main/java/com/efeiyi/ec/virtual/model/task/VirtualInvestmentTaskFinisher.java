package com.efeiyi.ec.virtual.model.task;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.model.ArtworkInvest;
import com.efeiyi.ec.art.virtual.model.VirtualInvestmentPlan;
import com.efeiyi.ec.art.virtual.model.VirtualInvestorPlan;
import com.efeiyi.ec.art.virtual.model.VirtualPlan;
import com.efeiyi.ec.art.virtual.model.VirtualUser;
import com.efeiyi.ec.virtual.util.DigitalSignatureUtil;
import com.ming800.core.util.ApplicationContextUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by Administrator on 2016/5/3.
 */
public class VirtualInvestmentTaskFinisher extends BaseTimerTask {

    private VirtualInvestmentPlan virtualInvestmentPlan;
    private SessionFactory sessionFactory = ((SessionFactory) ApplicationContextUtil.getApplicationContext().getBean("scheduleSessionFactory"));
    private Session session;
private boolean running = true;
    @Override
    public void setVirtualPlan(VirtualPlan virtualPlan) {
        this.virtualInvestmentPlan = (VirtualInvestmentPlan) virtualPlan;
    }

    @Override
    public boolean cancel() {
        logger.info("VirtualInvestmentTaskFinisher cancelled.");
        running = false;
        return super.cancel();
    }

    @Override
    public void execute(List<VirtualPlan> virtualPlanList) {
        Random random = new Random();
        List<VirtualInvestorPlan> virtualInvestorPlanList = session.createQuery("from VirtualInvestorPlan").list();
        List<VirtualUser> virtualUserList = new ArrayList<>();
        for(VirtualInvestorPlan virtualInvestorPlan : virtualInvestorPlanList){
            virtualUserList.addAll(virtualInvestorPlan.getVirtualUserList());
        }
        while (running) {
            virtualInvestmentPlan = (VirtualInvestmentPlan) session.get(VirtualInvestmentPlan.class.getName(), virtualInvestmentPlan.getId());
//            List<VirtualUser> virtualUserList = virtualInvestmentPlan.getVirtualInvestorPlan().getVirtualUserList();
            HttpClient httpClient = new DefaultHttpClient();
            String url = virtualInvestmentPlan.getUrl();
            HttpPost httppost = new HttpPost(url);
            httppost.setHeader("Content-Type", "application/json;charset=utf-8");
            Map jsonMap = new TreeMap();
            VirtualUser virtualUser = virtualUserList.get(random.nextInt(virtualUserList.size()));
            Integer fixedInvestmentIncrement = (Integer) setVirtualLevel(virtualUser);
            jsonMap.put("price", fixedInvestmentIncrement);
            jsonMap.put("userId", virtualUser.getUserBrief().getUser().getId());
            jsonMap.put("artworkId", virtualInvestmentPlan.getVirtualArtwork().getArtwork().getId());
            jsonMap.put("timestamp", System.currentTimeMillis());
            String msg = null;
            try {
                msg = DigitalSignatureUtil.encrypt(jsonMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            jsonMap.put("signmsg", msg);
            String jsonString = JSONObject.toJSONString(jsonMap);
            StringEntity stringEntity = new StringEntity(jsonString, "utf-8");
            stringEntity.setContentType("text/json");
            stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httppost.setEntity(stringEntity);
            System.out.println("url:  " + url);
            try {
                byte[] b = new byte[(int) stringEntity.getContentLength()];
                System.out.println(stringEntity);
                stringEntity.getContent().read(b);
                System.out.println("报文:" + new String(b, "utf-8"));
                HttpResponse response = httpClient.execute(httppost);
                HttpEntity entity = response.getEntity();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        entity.getContent(), "UTF-8"));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                System.out.println(stringBuilder);
                //
                JSONObject resultJson = JSONObject.parseObject(stringBuilder.toString());
                if(!"0".equals(resultJson.get("resultCode"))){
                    throw new Exception(stringBuilder.toString());
                }
                Thread.sleep(random.nextInt(10) * 1000);

                List<ArtworkInvest> investList = virtualInvestmentPlan.getVirtualArtwork().getArtwork().getArtworkInvests();
                BigDecimal amount = new BigDecimal(0);
                for (ArtworkInvest invest : investList) {
                    amount = amount.add(invest.getPrice());
                }
                if (amount.compareTo(virtualInvestmentPlan.getVirtualArtwork().getArtwork().getInvestGoalMoney()) >= 0) {
                    this.cancel();
                    break;
                }
                session.clear();
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel();
                break;
            }
        }
    }

    @Override
    public void run() {
        if (session == null) {
            session = sessionFactory.openSession();
        }
        logger.info(" VirtualInvestment Finisher arranging.");
        execute(null);
        if (session != null && session.isOpen()) {
            session.close();
        }
        logger.info(" VirtualInvestment Finisher arranged.");
    }


    private Number setVirtualLevel(VirtualUser virtualUser) {
        Integer fixedInvestmentIncrement = 0;
        if (virtualInvestmentPlan.getVirtualArtwork().getArtwork().getInvestGoalMoney().compareTo(new BigDecimal(5001)) < 0) {
            switch (virtualUser.getVirtualInvestorPlan().getGroup()) {
                case "1":
                    fixedInvestmentIncrement = 20;
                    break;
                case "2":
                    fixedInvestmentIncrement = 100;
                    break;
                case "3":
                    fixedInvestmentIncrement = 0;
                    break;
                case "4":
                    fixedInvestmentIncrement = 10;
                    break;
                case "5":
                    fixedInvestmentIncrement = 20;
                    break;
                case "6":
                    fixedInvestmentIncrement = 2;
            }
        } else if (virtualInvestmentPlan.getVirtualArtwork().getArtwork().getInvestGoalMoney().compareTo(new BigDecimal(15001)) < 0) {
            switch (virtualUser.getVirtualInvestorPlan().getGroup()) {
                case "1":
                    fixedInvestmentIncrement = 80;
                    break;
                case "2":
                    fixedInvestmentIncrement = 200;
                    break;
                case "3":
                    fixedInvestmentIncrement = 100;
                    break;
                case "4":
                    fixedInvestmentIncrement = 20;
                    break;
                case "5":
                    fixedInvestmentIncrement = 60;
                    break;
                case "6":
                    fixedInvestmentIncrement = 2;
            }
        } else {
            switch (virtualUser.getVirtualInvestorPlan().getGroup()) {
                case "1":
                    fixedInvestmentIncrement = 100;
                    break;
                case "2":
                    fixedInvestmentIncrement = 300;
                    break;
                case "3":
                    fixedInvestmentIncrement = 100;
                    break;
                case "4":
                    fixedInvestmentIncrement = 10;
                    break;
                case "5":
                    fixedInvestmentIncrement = 80;
                    break;
                case "6":
                    fixedInvestmentIncrement = 2;

            }
        }
        return fixedInvestmentIncrement;
    }
}
