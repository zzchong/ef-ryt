package com.efeiyi.ec.art.virtual.quartz;

import com.efeiyi.ec.art.base.service.ThreadManager;
import com.efeiyi.ec.art.organization.OrganizationConst;
import com.ming800.core.base.service.BaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/4/8.
 *
 */
@Component("updateArtWorkStatus")
public class UpdateArtWorkStatus {
    @Autowired
    BaseManager baseManager;

    @Autowired
    ThreadManager threadManager;
    private ExecutorService pool = Executors.newFixedThreadPool(OrganizationConst.THREAD_POOL_CORE_COUNT);
    public void execute() throws  Exception{//每5分钟执行一次
        threadManager.startWork(pool);
    }
}
