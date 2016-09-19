package com.efeiyi.ec.art.artwork.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.artwork.service.ArtworkManager;
import com.efeiyi.ec.art.artwork.service.MasterManager;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.JPushConfig;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.jpush.EfeiyiPush;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.util.AuthorizationUtil;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.service.AliOssUploadManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Administrator on 2016/4/14.
 *
 */
@Service
public class MasterManagerImpl implements MasterManager {
    private static Logger logger = Logger.getLogger(MasterManagerImpl.class);

    @Autowired
    private AliOssUploadManager aliOssUploadManager;

    @Autowired
    BaseManager baseManager;

    @Autowired
    ResultMapHandler resultMapHandler;

    @Override
    public Map<String, Object> saveMaster(HttpServletRequest request, LogBean logBean) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        JSONObject jsonObj = null;
        Master master = null;

        jsonObj = JsonAcceptUtil.receiveJson(request);//入参

        User user = AuthorizationUtil.getUser();

        master = getMasterByUserId(user.getId());

        //在（1.待提交 4.审核失败）状态下 , 不需要重新创建master
        if(master == null || !("1".equals(master.getTheStatus())
                || "2".equals(master.getTheStatus())
                || "3".equals(master.getTheStatus())
                || "4".equals(master.getTheStatus()))) {
            master = new Master();
        }

        logBean.setCreateDate(new Date());//操作时间
        logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
        logBean.setApiName("saveMaster");

        String code = (String) request.getSession().getAttribute(jsonObj.getString("phone"));

        if(code == null && !"668866".equals(jsonObj.getString("verificationCode"))){
            return  resultMapHandler.handlerResult("100011","验证码失效，请重新发送",logBean);
        }

        if (code != null && !code.equals(jsonObj.getString("verificationCode"))){
            return  resultMapHandler.handlerResult("100010","验证码验证失败",logBean);
        }

        master.setName(jsonObj.getString("name"));
        master.setEmail(jsonObj.getString("email"));
        master.setPhone(jsonObj.getString("phone"));
        master.setPresentCity(jsonObj.getString("presentCity"));
        master.setPresentAddress(jsonObj.getString("presentAddress"));
        master.setTheStatus("1");
        master.setUser(user);

        baseManager.saveOrUpdate(Master.class.getName(), master);

        resultMap.put("resultCode", "0");
        resultMap.put("resultMsg", "艺术家信息保存成功");
        resultMap.put("master", master);
        return resultMap;
    }

    @Override
    public Master getMasterByUserId(String userId) throws Exception {
        String hql = "select s from com.efeiyi.ec.art.model.Master s where s.user.id = :userId";
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("userId", userId);
        return (Master)baseManager.getUniqueObjectByConditions(hql, params);
    }

    @Override
    public boolean saveMasterWork(HttpServletRequest request) throws Exception {

        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参

            String pictureUrl = jsonObj.getString("pictureUrl");
            String[] urlArr = pictureUrl.split(",");

            MasterWork masterWork = new MasterWork();
            masterWork.setStatus("1");
            masterWork.setCreator(AuthorizationUtil.getUser());
            masterWork.setCreateDatetime(new Date());
            masterWork.setMaterial(jsonObj.getString("material"));
            masterWork.setPictureUrl(urlArr[0]);
            masterWork.setName(jsonObj.getString("name"));
            masterWork.setType(jsonObj.getString("type"));
            masterWork.setCreateYear(jsonObj.getString("createYear"));

            baseManager.saveOrUpdate(MasterWork.class.getName(),masterWork);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }
}


