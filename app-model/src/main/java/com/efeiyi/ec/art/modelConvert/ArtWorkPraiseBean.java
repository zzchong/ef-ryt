package com.efeiyi.ec.art.modelConvert;

import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.model.Master;

/**
 * Created by Administrator on 2016/1/25.
 *
 */
public class ArtWorkPraiseBean {

    private Artwork artwork;

    private Boolean isPraise;

    public Artwork getArtwork() {
        return artwork;
    }

    public void setArtwork(Artwork artwork) {
        this.artwork = artwork;
    }

    public Boolean getPraise() {
        return isPraise;
    }

    public void setPraise(Boolean praise) {
        isPraise = praise;
    }
}
