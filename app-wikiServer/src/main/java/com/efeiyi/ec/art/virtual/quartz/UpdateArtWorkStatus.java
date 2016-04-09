package com.efeiyi.ec.art.virtual.quartz;

import com.efeiyi.ec.art.base.service.ThreadManager;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.organization.OrganizationConst;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2016/4/8.
 *
 */
@Component("updateArtWorkStatus")
public class UpdateArtWorkStatus {
    @Autowired
    BaseManager baseManager;

    @Autowired
    ThreadManager threadManager;

    @Autowired
    ResultMapHandler resultMapHandler;
    private ExecutorService pool = Executors.newFixedThreadPool(OrganizationConst.THREAD_POOL_CORE_COUNT);
    public void execute() throws  Exception{//每5分钟执行一次
        //查找状态为31 即拍卖中的项目
        XQuery query = new XQuery("listProjectFollowed_isShow", resultMapHandler.getRequest());
        List<Artwork> artworks = baseManager.listObject(query);
        if(artworks!= null && artworks.isEmpty()){
            threadManager.startWork(pool,artworks);
        }

    }
}
