package com.efeiyi.ec.art.base.thread;

import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.ContextUtils;
import com.efeiyi.ec.art.model.Account;
import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.model.ArtworkBidding;
import com.efeiyi.ec.art.model.MarginAccount;
import com.efeiyi.ec.art.organization.model.User;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;


import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/4/8.
 */
@Transactional("transactionManager")
public class UpdateArtWorkStatusThread implements  Runnable {
    private static Logger log = Logger.getLogger(UpdateArtWorkStatusThread.class);
    /*private Artwork artwork;

    private Session session;
    @Override
    public void run() {
        exeBatchUpdate();
    }

        private void exeBatchUpdate(){
        log.info("begin exeBatchInsert:");
        System.out.println("开始处理任务...");

        try{
            session = ((SessionFactory)ContextUtils.getBean("sessionFactory")).openSession();
         if ("3".equals(artwork.getType()) && "31".equals(artwork.getStep())){//符合条件
             ArtworkBidding artworkBidding = (ArtworkBidding)session.createSQLQuery(AppConfig.GET_ART_WORK_WINNER).addEntity(ArtworkBidding.class).setString("artworkId", artwork.getId()).uniqueResult();
             if(artworkBidding.getId()!=null && artworkBidding.getCreator().getId()!= null){//竞拍得主
                  if(artwork.getAuctionEndDatetime().getTime()<=new Date().getTime()){
                      artwork.setStep("32");
                  }else {
                      artwork.setStep("33");
                  }
                 //baseManager.saveOrUpdate(Artwork.class.getName(),artwork);
                 //((BaseManagerImpl)ContextUtils.getBean("bseManagerImpl")).saveOrUpdate(Artwork.class.getName(), artwork);

                 session.saveOrUpdate(Artwork.class.getName(),session.merge(Artwork.class.getName(),artwork));
             }
         }
            System.out.println("处理任务结束...");
        }catch(Exception e){
            log.error(e);
        }finally {
            if(session!=null){
                session.close();
            }
        }
        log.info("end exeBatchInsert:");
    }


    public Artwork getArtworks() {
        return artwork;
    }

    public void setArtworks(Artwork artwork) {
        this.artwork = artwork;
    }

    public UpdateArtWorkStatusThread(Artwork artwork) {
        this.artwork = artwork;
    }
    public UpdateArtWorkStatusThread( ) {
        super();
    }*/
    private String name;
    private Artwork artwork;
    private Session session;
    @Override
    public void run() {
        while (true){
            ThreadLaunch.getInstance().lock.lock();
            try{
                /*if (ThreadLaunch.getInstance().artworkQueue.isEmpty()) {
                    synchronized (ThreadLaunch.getInstance().artworkQueue) {
                        if (ThreadLaunch.getInstance().artworkQueue.isEmpty()) {
                            ThreadLaunch.getInstance().artworkQueue.wait();
                            System.out.println("UpdateArtWorkStatusThread " + name + "wait...");
                        }

                    }
                }*/
                        if (ThreadLaunch.getInstance().artworkQueue.isEmpty()) {
                            //ThreadLaunch.getInstance().artworkQueue.wait();
                            ThreadLaunch.getInstance().condition.await();
                            System.out.println("UpdateArtWorkStatusThread " + name + "wait...");
                        }


                artwork = ThreadLaunch.getInstance().artworkQueue.poll();
                if (artwork!=null)
                    exeBatchUpdate(artwork);
            }catch (InterruptedException e){
                e.printStackTrace();

            }finally {
                ThreadLaunch.getInstance().lock.unlock();
            }
        }
    }

    private void exeBatchUpdate(Artwork artwork){
        log.info("begin exeBatchInsert:");
        System.out.println("开始处理任务...");
       boolean flag =false;
        try{
            session = ((SessionFactory)ContextUtils.getBean("sessionFactory")).openSession();
         if ("3".equals(artwork.getType()) && "31".equals(artwork.getStep())){//符合条件
             ArtworkBidding artworkBidding = (ArtworkBidding)session.createSQLQuery(AppConfig.GET_ART_WORK_WINNER).addEntity(ArtworkBidding.class).setString("artworkId", artwork.getId()).uniqueResult();
             if(artworkBidding.getId()!=null && artworkBidding.getCreator().getId()!= null){//有竞价记录

                  if(artwork.getAuctionEndDatetime().getTime()<=new Date().getTime()){//结束时间已过
                      artwork.setWinner(artworkBidding.getCreator()); //设置竞拍得主
                      artwork.setStep("32");
                      flag = true;
                  }
                 //baseManager.saveOrUpdate(Artwork.class.getName(),artwork);
                 //((BaseManagerImpl)ContextUtils.getBean("bseManagerImpl")).saveOrUpdate(Artwork.class.getName(), artwork);

                 session.saveOrUpdate(Artwork.class.getName(),session.merge(Artwork.class.getName(),artwork));
                 if(flag==true){
                     //解冻用户保证金
                     List<MarginAccount>  marginAccounts = session.createQuery(AppConfig.SQL_MARGIN_ACCOUNT_LIST).setString(1,artwork.getId()).list();
                     if(marginAccounts!= null && !marginAccounts.isEmpty()){
                         for(MarginAccount marginAccount: marginAccounts){
                             Account account = marginAccount.getAccount();
                             if(account.getUser().getId().equals(artwork.getWinner().getId())){//过滤掉竞拍得主
                                 continue;
                             }
                             marginAccount.setEndDatetime(new Date());
                             marginAccount.setStatus("3");//解冻状态
                             account.setCurrentUsableBalance(account.getCurrentUsableBalance().add(marginAccount.getCurrentBalance()));
                             session.saveOrUpdate(Account.class.getName(),session.merge(Account.class.getName(),account));
                             session.saveOrUpdate(MarginAccount.class.getName(),session.merge(MarginAccount.class.getName(),marginAccount));
                         }
                     }
                     /*返回投资收益
                     * 1.查出投资列表
                     * 2.计算投资回报额
                     * 3.返回收益到用户账户
                     */


                 }


             }else {////无竞价记录
                 if(artwork.getAuctionEndDatetime().getTime()<=new Date().getTime()){
                     artwork.setWinner(new User()); //设置流拍
                     artwork.setStep("33");
                 }
             }
         }
            System.out.println("处理任务结束...");
        }catch(Exception e){
            log.error(e);
        }finally {
            if(session!=null){
                session.close();
            }
        }
        log.info("end exeBatchInsert:");
    }

    public UpdateArtWorkStatusThread() {
        super();
    }

    public UpdateArtWorkStatusThread(String name) {
        super();
        this.name = name;
    }

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }
}
