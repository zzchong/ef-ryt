package com.efeiyi.ec.art.base.service.impl;

import com.efeiyi.ec.art.base.service.ThreadManager;
import com.efeiyi.ec.art.base.thread.ThreadLaunch;
import com.efeiyi.ec.art.model.Artwork;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Administrator on 2015/12/10.
 *
 */
@Service
public class ThreadManagerImpl implements ThreadManager {
    private static Logger log = Logger.getLogger(ThreadManagerImpl.class);

  /*  public void startWork(List<Artwork> artworks) throws Exception{
        ExecutorService pool = Executors.newFixedThreadPool(OrganizationConst.THREAD_POOL_CORE_COUNT);
        log.info("开始创建线程池");
        System.out.println("开始创建线程池...");
        int count = artworks.size(),i = 0;
        for(; i <= count-1; i++) {
            System.out.println("启动第一个任务...");
                pool.execute(new Thread(new UpdateArtWorkStatusThread(artworks.get(0))));
        }


        log.info("创建线程池完成");
        System.out.println("创建线程池结束...");
        pool.shutdown();
        log.info("关闭线程池");
        System.out.println("关闭线程池...");
    }*/

    public void startWork(List<Artwork> artworks) throws Exception{

        //ThreadLaunch.getInstance().artworkQueue = putQueue(artworks);
        //putQueue(artworks);
        ThreadLaunch.getInstance().lock.lock();
        try{
            if (artworks != null && !artworks.isEmpty()) {
                for (Artwork artwork : artworks) {
                    ThreadLaunch.getInstance().artworkQueue.offer(artwork);
                }
            }else {
                ThreadLaunch.getInstance().condition.await();
            }



            if (!ThreadLaunch.getInstance().artworkQueue.isEmpty()) {
                //ThreadLaunch.getInstance().artworkQueue.notifyAll();
                ThreadLaunch.getInstance().condition.signalAll();
            }
            //ThreadLaunch.getInstance().artworkQueue.wait();

            ThreadLaunch.getInstance().condition.await();

            /*if(!ThreadLaunch.getInstance().artworkQueue.isEmpty()) {
                synchronized (ThreadLaunch.getInstance().artworkQueue) {

                    if (!ThreadLaunch.getInstance().artworkQueue.isEmpty()) {
                        ThreadLaunch.getInstance().artworkQueue.notifyAll();
                    }
                    ThreadLaunch.getInstance().artworkQueue.wait();
                }
            }*/
        }catch (Exception e){
            e.printStackTrace();

        }finally {
            ThreadLaunch.getInstance().lock.unlock();
        }


    }

   //将所有待处理的artwort 放入任务队列中
    private  void putQueue(List<Artwork> artworks){
       /* if (artworks!=null && !artworks.isEmpty()){
            synchronized(ThreadLaunch.getInstance().artworkQueue) {
                if (artworks != null && !artworks.isEmpty()) {
                    for (Artwork artwork : artworks) {
                        ThreadLaunch.getInstance().artworkQueue.offer(artwork);
                    }
                }
            }
        }*/
        ThreadLaunch.getInstance().lock.lock();
        try{
            if (artworks != null && !artworks.isEmpty()) {
                for (Artwork artwork : artworks) {
                            ThreadLaunch.getInstance().artworkQueue.offer(artwork);
                }
            }else {
                ThreadLaunch.getInstance().condition.await();
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            ThreadLaunch.getInstance().lock.unlock();
        }
    }

}
