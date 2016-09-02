package com.efeiyi.ec.art.artwork.service.impl;

import com.efeiyi.ec.art.artwork.service.ArtworkPraiseManager;
import com.efeiyi.ec.art.model.ArtWorkPraise;
import com.efeiyi.ec.art.model.ArtworkMessage;
import com.efeiyi.ec.art.organization.model.User;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Administrator on 2016/8/30.
 */
@Service
public class ArtworkPraiseManagerImpl implements ArtworkPraiseManager {
    @Autowired
    BaseManager baseManager;

    @Override
    public boolean isToArtworkMessagePraise(HttpServletRequest request, ArtworkMessage artworkMessage, User user) {
        XQuery xQuery = null;
        try {
            xQuery = new XQuery("listArtWorkPraise_byArtworkMessage", request);
            xQuery.put("artworkMessage_id", artworkMessage.getId());
            xQuery.put("user_id", user.getId());
            List<ArtWorkPraise> artWorkPraiseList = baseManager.listObject(xQuery);
            if (artWorkPraiseList != null && artWorkPraiseList.size()>0){
                return true;
            }else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
