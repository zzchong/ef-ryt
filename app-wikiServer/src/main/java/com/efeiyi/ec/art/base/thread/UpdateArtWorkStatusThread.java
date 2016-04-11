package com.efeiyi.ec.art.base.thread;

import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.model.ArtworkBidding;
import com.ming800.core.base.dao.hibernate.XdoDaoSupport;
import com.ming800.core.base.service.BaseManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Date;

/**
 * Created by Administrator on 2016/4/8.
 */
@Controller
public class UpdateArtWorkStatusThread implements  Runnable {
    private static Logger log = Logger.getLogger(UpdateArtWorkStatusThread.class);
    private Artwork artwork;
    @Autowired
    BaseManager baseManager;

    @Autowired
    private XdoDaoSupport xdoDao;
    @Override
    public void run() {
        exeBatchUpdate();
    }

    private void exeBatchUpdate(){
        log.info("begin exeBatchInsert:");
        try{
         if ("3".equals(artwork.getType()) && "31".equals(artwork.getStep())){//符合条件
             ArtworkBidding artworkBidding = (ArtworkBidding)xdoDao.getSession().createSQLQuery(AppConfig.GET_ART_WORK_WINNER).addEntity(ArtworkBidding.class).setString("artworkId", artwork.getId()).uniqueResult();
             if(artworkBidding.getId()!=null && artworkBidding.getCreator().getId()!= null){//竞拍得主
                  if(artwork.getAuctionEndDatetime().getTime()<=new Date().getTime()){
                      artwork.setStep("32");
                  }else {
                      artwork.setStep("33");
                  }
                 baseManager.saveOrUpdate(Artwork.class.getName(),artwork);
             }
         }
        }catch(Exception e){
            log.error(e);
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
}
