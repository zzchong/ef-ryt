package com.efeiyi.ec.art.base.thread;

import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.ContextUtils;
import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.model.ArtworkBidding;
import com.ming800.core.base.service.impl.BaseManagerImpl;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;


import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

/**
 * Created by Administrator on 2016/4/8.
 */
@Transactional("transactionManager")
public class UpdateArtWorkStatusThread implements  Runnable {
    private static Logger log = Logger.getLogger(UpdateArtWorkStatusThread.class);
    private Artwork artwork;

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
    }


}
