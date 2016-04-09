package com.efeiyi.ec.art.base.service.impl;

import com.efeiyi.ec.art.base.service.ThreadManager;
import com.efeiyi.ec.art.base.thread.UpdateArtWorkStatusThread;
import com.efeiyi.ec.art.model.Artwork;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by Administrator on 2015/12/10.
 *
 */
@Service
public class ThreadManagerImpl implements ThreadManager {
    private static Logger log = Logger.getLogger(ThreadManagerImpl.class);

    public void startWork(ExecutorService pool,List<Artwork> artworks) throws Exception{
        log.info("开始创建线程池");
        int count = artworks.size(),i = 0;
        for(; i <= count-1; i++) {
                pool.execute(new Thread(new UpdateArtWorkStatusThread(artworks.get(0))));
        }


        log.info("创建线程池完成");
        pool.shutdown();
        log.info("关闭线程池");
    }
}
