package com.efeiyi.ec.art.base.thread;

import org.apache.log4j.Logger;

/**
 * Created by Administrator on 2016/4/8.
 */
public class UpdateArtWorkStatusThread implements  Runnable {
    private static Logger log = Logger.getLogger(UpdateArtWorkStatusThread.class);
    private int beginNum;
    private int endNum;

    public UpdateArtWorkStatusThread(int beginNum, int endNum) {
        this.beginNum = beginNum;
        this.endNum = endNum;
    }

    public int getBeginNum() {
        return beginNum;
    }

    public void setBeginNum(int beginNum) {
        this.beginNum = beginNum;
    }

    public int getEndNum() {
        return endNum;
    }

    public void setEndNum(int endNum) {
        this.endNum = endNum;
    }

    @Override
    public void run() {
        System.out.println("beginNum:"+beginNum+"   "+"endNum:"+endNum);
        //exeBatchInsert();
    }

    private void exeBatchInsert(){
        log.info("begin exeBatchInsert:"+beginNum+"-->"+endNum);
        try{

        }catch(Exception e){
            log.error(e);
        }
        log.info("end exeBatchInsert:"+beginNum+"-->"+endNum);
    }
}
