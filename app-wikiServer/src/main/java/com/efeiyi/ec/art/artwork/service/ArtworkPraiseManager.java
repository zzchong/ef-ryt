package com.efeiyi.ec.art.artwork.service;

import com.efeiyi.ec.art.model.ArtworkMessage;
import com.efeiyi.ec.art.organization.model.User;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2016/8/30.
 */
public interface ArtworkPraiseManager {
    boolean isToArtworkMessagePraise(HttpServletRequest request, ArtworkMessage artworkMessage, User user);
}
