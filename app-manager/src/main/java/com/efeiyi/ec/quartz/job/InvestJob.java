package com.efeiyi.ec.quartz.job;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.model.ArtworkBidding;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/6/14.
 */
@DisallowConcurrentExecution
public class InvestJob implements Job {

    @Autowired
    protected BaseManager baseManager;

    private static final String URL = "http://192.168.1.80:8080/app/updateCreationStatus.do";

    //private static final String URL = "http://ryt.efeiyi.com/app/updateCreationStatus.do";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        System.out.println("任务开始执行!");
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(URL);
        httppost.setHeader("Content-Type", "application/json;charset=utf-8");
        String jsonString = JSONObject.toJSONString(jobExecutionContext.getJobDetail().getJobDataMap());
        StringEntity stringEntity = new StringEntity(jsonString, "utf-8");
        stringEntity.setContentType("text/json");
        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httppost.setEntity(stringEntity);
        System.out.println("url:  " + URL);
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
            jobExecutionContext.getScheduler().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("任务结束!");
    }

}
