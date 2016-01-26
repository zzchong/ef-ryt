package com.efeiyi.ec.art.jpush.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.jpush.EfeiyiPush;
import com.efeiyi.ec.art.model.Message;
import com.efeiyi.ec.organization.model.MyUser;
import com.efeiyi.ec.organization.model.User;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.base.service.BaseManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Administrator on 2016/1/25.
 *
 */
@Controller
public class EPushController extends BaseController {
    private static Logger logger = Logger.getLogger(EPushController.class);

    private static final String appKey ="adc8111aff90e4f46f525f17";
    private static final String masterSecret = "68ced939e490df0ce2b19901";


    @Autowired
    BaseManager baseManager;
    @RequestMapping(value = "/app/push.do", method = RequestMethod.POST)
    @ResponseBody
    public Map login(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());//************记录请求报文
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("content"))
                    || "".equals(jsonObj.getString("targetUserId"))
                    || "".equals(jsonObj.getString("fromUserId")) || "".equals(jsonObj.getString("timestamp"))) {
                logBean.setResultCode("10001");
                logBean.setMsg("必选参数为空，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                resultMap.put("resultCode", "10001");
                resultMap.put("resultMsg", "必选参数为空，请仔细检查");
                return resultMap;
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("content", jsonObj.getString("content"));
            treeMap.put("targetUserId", jsonObj.getString("targetUserId"));
            treeMap.put("fromUserId", jsonObj.getString("fromUserId"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                logBean.setResultCode("10002");
                logBean.setMsg("参数校验不合格，请仔细检查");
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                resultMap.put("resultCode", "10002");
                resultMap.put("resultMsg", "参数校验不合格，请仔细检查");
                return resultMap;
            }

            Message message = new Message();
            message.setContent(jsonObj.getString("content"));
            message.setCreateDatetime(new Date());
            User fromUser,targetUser;
            try {
                 fromUser = (User)baseManager.getObject(User.class.getName(),jsonObj.getString("fromUserId"));
                 targetUser = (User)baseManager.getObject(User.class.getName(),jsonObj.getString("targetUserId"));
            }catch (Exception e){
                logBean.setResultCode("10007");
                logBean.setMsg("用户不存在");
                baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                resultMap.put("resultCode", "10007");
                resultMap.put("resultMsg", "用户不存在");
                return resultMap;
            }
            message.setFromUser(fromUser);
            message.setTargetUser(targetUser);
            baseManager.saveOrUpdate(Message.class.getName(),message);
                    logBean.setResultCode("0");
                    logBean.setMsg("成功");
                    baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
                    resultMap.put("resultCode", "0");
                    resultMap.put("resultMsg", "成功");

            EfeiyiPush.SendPush(appKey, masterSecret);

        } catch (Exception e) {
            logBean.setResultCode("10004");
            logBean.setMsg("未知错误，请联系管理员");
            baseManager.saveOrUpdate(LogBean.class.getName(),logBean);
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMap;
        }
        return resultMap;
    }








    public  static void main(String[] args){
        EfeiyiPush.SendPush(appKey, masterSecret);

    }
}
