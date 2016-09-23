package com.efeiyi.ec.art.artwork.service;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.model.Master;
import com.efeiyi.ec.art.model.MasterWork;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/14.
 *
 */
public interface MasterManager {

    Map<String, Object> saveMasterBasic(HttpServletRequest request, LogBean logBean) throws Exception;

    Map<String, Object> saveMasterIdentity(HttpServletRequest request, LogBean logBean) throws Exception;

    Master getMasterByUserId(String userId) throws Exception;

    Map<String, Object> saveMasterWork(HttpServletRequest request, LogBean logBean) throws Exception;

    Map<String, Object> getMasterWorks(HttpServletRequest request, LogBean logBean) throws Exception;

}
