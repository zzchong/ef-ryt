package com.efeiyi.ec.art.base.service.impl;

import com.efeiyi.ec.art.base.service.ThreadManager;
import com.efeiyi.ec.art.base.thread.UpdateArtWorkStatusThread;
import com.efeiyi.ec.art.organization.OrganizationConst;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2015/12/10.
 *
 */
@Service
public class ThreadManagerImpl implements ThreadManager {
    private static Logger log = Logger.getLogger(ThreadManagerImpl.class);

    public void startWork() throws Exception{
        log.info("开始创建线程池");
        ExecutorService pool = Executors.newFixedThreadPool(OrganizationConst.THREAD_POOL_CORE_COUNT);
        int beginNum =1,endNum =40;
        for(int i = 1; i <= OrganizationConst.THREAD_POOL_CORE_COUNT; i++) {
            pool.execute(new Thread(new UpdateArtWorkStatusThread(beginNum,endNum)));
            beginNum = endNum+1;
            if (i== OrganizationConst.THREAD_POOL_CORE_COUNT-1){
                endNum=373;
            }else {
                endNum = beginNum+40;
            }

        }


/*
        ExecutorService pool = Executors.newFixedThreadPool(1);
        int beginNum =1,endNum =373;
        pool.execute(new Thread(new UpdateArtWorkStatusThread(beginNum,endNum)));*/
        log.info("创建线程池完成");
        pool.shutdown();
        log.info("关闭线程池");
    }
}
