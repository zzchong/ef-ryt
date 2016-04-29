package com.efeiyi.ec.virtual.model.task;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.virtual.model.VirtualInvestmentPlan;
import com.efeiyi.ec.art.virtual.model.VirtualPlan;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.hibernate.exception.GenericJDBCException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/26.
 */
public class VirtualInvestmentGenerator extends BaseTimerTask {

        private VirtualInvestmentPlan virtualInvestmentPlan;
    private Map jsonMap;


    public VirtualInvestmentGenerator(Map jsonMap,VirtualInvestmentPlan virtualInvestmentPlan) {
        this.jsonMap = jsonMap;
        this.virtualInvestmentPlan = virtualInvestmentPlan;
    }

    public void execute(List<VirtualPlan> virtualPlanList) {
        HttpClient httpClient = new DefaultHttpClient();
        String url = virtualInvestmentPlan.getUrl();
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            execute(null);
        } catch (GenericJDBCException jdbcE) {
            execute(null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            session.close();
        }
    }

    @Override
    public void setVirtualPlan(VirtualPlan virtualPlan) {
    }
}
