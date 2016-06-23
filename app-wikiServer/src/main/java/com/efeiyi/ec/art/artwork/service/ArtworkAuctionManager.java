package com.efeiyi.ec.art.artwork.service;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/15.
 */
public interface ArtworkAuctionManager {
    Map<String, Object> artworkBidOnAuction(HttpServletRequest request, JSONObject jsonObj, LogBean logBean);

    Map<String, Object> artWorkAuctionPayDeposit(HttpServletRequest request, JSONObject jsonObj, LogBean logBean);
}
