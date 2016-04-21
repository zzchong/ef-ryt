package com.efeiyi.ec.art.personal.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.UserBrief;
import com.efeiyi.ec.art.organization.model.User;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Administrator on 2016/4/21.
 *
 */
public class UserBriefController  extends BaseController {
    private static Logger logger = Logger.getLogger(UserBriefController.class);

    @Autowired
    BaseManager baseManager;
    @Autowired
    ResultMapHandler resultMapHandler;

    @RequestMapping(value = "/app/saveUserBrief.do", method = RequestMethod.POST)//获取投资者排行榜
    @ResponseBody
    public Map getInvestorTopList(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("saveUserBrief");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg"))
                    || "".equals(jsonObj.getString("userId"))
                    ||"".equals(jsonObj.getString("timestamp"))
                  //  || "".equals(jsonObj.getString("content"))
                    || "".equals(jsonObj.getString("type"))
                    ) {
                return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("userId",jsonObj.getString("userId"));
            treeMap.put("type",jsonObj.getString("type"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return  resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }
            User user = (User)baseManager.getObject(User.class.getName(),jsonObj.getString("userId"));
            UserBrief userBrief = new UserBrief();
            if(user.getMaster()!= null && user.getMaster().getId()!=null){
                userBrief.setType("1");
            }else{
                userBrief.setType("2");
            }
            userBrief.setUser(user);
            userBrief.setStatus("1");
            if ("1".equals(jsonObj.getString("type"))){
                if("".equals(jsonObj.getString("signer"))){
                    return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
                }
                userBrief.setContent(jsonObj.getString("signer"));//编辑签名
            } else{
                if("".equals(jsonObj.getString("content"))){
                    return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
                }
                userBrief.setContent(jsonObj.getString("content"));
            }

            userBrief.setCreateDatetime(new Date());
            baseManager.saveOrUpdate(UserBrief.class.getName(),userBrief);
            return resultMapHandler.handlerResult("0","成功",logBean);
        } catch(Exception e){
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
    }
}
