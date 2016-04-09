package com.efeiyi.ec.art.base.service.impl;

import com.efeiyi.ec.art.base.service.ThreadManager;
import com.efeiyi.ec.art.base.thread.UpdateArtWorkStatusThread;
import com.efeiyi.ec.art.organization.OrganizationConst;
import com.ming800.core.base.service.BaseManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    BaseManager baseManager;
    public void startWork(ExecutorService pool) throws Exception{
        log.info("开始创建线程池");
        //查找状态为31 即拍卖中的项目


        for(int i = 1; i <= OrganizationConst.THREAD_POOL_CORE_COUNT; i++) {


            //pool.execute(new Thread(new UpdateArtWorkStatusThread(beginNum,endNum)));


        }
        log.info("创建线程池完成");
        pool.shutdown();
        log.info("关闭线程池");
    }
}
