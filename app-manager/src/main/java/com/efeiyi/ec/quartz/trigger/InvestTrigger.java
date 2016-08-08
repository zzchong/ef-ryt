package com.efeiyi.ec.quartz.trigger;

//import com.efeiyi.ec.quartz.job.AuctionJob;
import com.efeiyi.ec.quartz.job.InvestJob;
import com.efeiyi.ec.virtual.util.DigitalSignatureUtil;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
/**
 * Created by Administrator on 2016/6/16.
 */
public class InvestTrigger {



    //type  invest 投资   auction 拍卖
    public void execute(String id, Date time,String type){

        try {

            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            Long timestamp = System.currentTimeMillis();
            Map<String, Object> map = new TreeMap<>();
            map.put("id",id);
            map.put("type",type);
            map.put("timestamp", timestamp);

            String signmsg = DigitalSignatureUtil.encrypt(map);


            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("id",id);
            jobDataMap.put("type",type);
            jobDataMap.put("timestamp",timestamp);
            jobDataMap.put("signmsg",signmsg);



            JobDetail job = newJob(InvestJob.class)
                    .withIdentity(id,"group")
                    .setJobData(jobDataMap)
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(id,"group")
                    .forJob(job)
                    .startAt(time)
                    .withSchedule(simpleSchedule()
                    .withRepeatCount(0)
                    )
                    .build();

            scheduler.scheduleJob(job,trigger);
//            scheduler.shutdown();
        }catch (SchedulerException se){
           se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
