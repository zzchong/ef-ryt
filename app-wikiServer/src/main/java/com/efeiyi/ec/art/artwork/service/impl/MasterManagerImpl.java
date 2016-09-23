package com.efeiyi.ec.art.artwork.service.impl;

import com.alibaba.fastjson.JSONArray;
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
    public Map<String, Object> saveMasterBasic(HttpServletRequest request, LogBean logBean) throws Exception {
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
        resultMap.put("data", master);
        return resultMap;
    }

    @Override
    public Map<String, Object> saveMasterIdentity(HttpServletRequest request, LogBean logBean) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        User user = AuthorizationUtil.getUser();
        Master master = null;

        JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);//入参

        logBean.setCreateDate(new Date());//操作时间
        logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
        logBean.setApiName("saveMasterIdentity");

        master = getMasterByUserId(user.getId());

        if(master == null) {
            return  resultMapHandler.handlerResult("100010","艺术家信息获取错误",logBean);
        }

        master.setIdentityCardNo(jsonObj.getString("paperNo"));
        master.setIdentityCardType(jsonObj.getString("paperType"));
        master.setRemark(jsonObj.getString("remark"));

        String imageStr = jsonObj.getString("image");
        String[] imageArr = null;
        if(imageStr != null) {
            imageArr = imageStr.split(",");
        }

        if(imageStr != null && imageArr.length == 1) {
            master.setIdentityFront(imageArr[0]);
        }
        if(imageStr != null && imageArr.length == 2) {
            master.setIdentityBack(imageArr[1]);
        }

        String submitMark = jsonObj.getString("submitMark");
        //选择了提交审核 ， 更新状态为待审核
        if("1".equals(submitMark)) {
            master.setTheStatus("2");
        }

        baseManager.saveOrUpdate(Master.class.getName(), master);

        resultMap.put("resultCode", "0");
        resultMap.put("resultMsg", "艺术家信息保存成功");
        resultMap.put("data", master);
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
    public Map saveMasterWork(HttpServletRequest request, LogBean logBean) throws Exception {
        JSONObject jsonObj = null;
        Map<String, Object> resultMap = new HashMap<>();

        try{
            jsonObj = JsonAcceptUtil.receiveJson(request);//入参
        } catch (Exception e) {
            e.printStackTrace();
            return  resultMapHandler.handlerResult("100010","获取传入参数失败",logBean);
        }


        String pictureUrl = jsonObj.getString("pictureUrl");

        JSONArray jsonArr = null;
        try{
            jsonArr = JSONArray.parseArray(pictureUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return  resultMapHandler.handlerResult("100010","解析图片信息失败",logBean);
        }

        MasterWork masterWork = new MasterWork();
        masterWork.setStatus("1");
        masterWork.setCreator(AuthorizationUtil.getUser());
        masterWork.setCreateDatetime(new Date());
        masterWork.setMaterial(jsonObj.getString("material"));

        if(jsonArr != null && jsonArr.size() > 0) {
            masterWork.setPictureUrl(jsonArr.getJSONObject(0).getString("pictureUrl"));
            masterWork.setWidth(jsonArr.getJSONObject(0).getString("width"));
            masterWork.setHeight(jsonArr.getJSONObject(0).getString("height"));
        }

        masterWork.setName(jsonObj.getString("name"));
        masterWork.setType(jsonObj.getString("type"));
        masterWork.setCreateYear(jsonObj.getString("createYear"));

        try{
            baseManager.saveOrUpdate(MasterWork.class.getName(),masterWork);
        } catch (Exception e) {
            e.printStackTrace();
            return  resultMapHandler.handlerResult("100010","保存艺术家作品失败",logBean);
        }

        resultMap.put("resultCode", "0");
        resultMap.put("resultMsg", "作品上传成功");
        resultMap.put("data", masterWork);

        return resultMap;
    }

    @Override
    public Map<String, Object> getMasterWorks(HttpServletRequest request, LogBean logBean) throws Exception {
        String userId = AuthorizationUtil.getUserId();

        Map<String, Object> resultMap = new HashMap<>();

        String hql = "select s from com.efeiyi.ec.art.model.MasterWork s where s.creator.id = :userId and s.status = :status";
        LinkedHashMap<String, Object> params = new LinkedHashMap<>();
        params.put("userId", userId);
        params.put("status", "1");
        List<MasterWork> masterWorks = baseManager.listObject(hql, params);

        resultMap.put("resultCode", "0");
        resultMap.put("resultMsg", "获取大师作品成功");
        resultMap.put("dataList", masterWorks);

        return resultMap;
    }

}


