package com.efeiyi.ec.art.personal.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.DigitalSignatureUtil;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
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
 * Created by Administrator on 2016/4/6.
 */
@Controller
public class RankListController extends BaseController {
    private static Logger logger = Logger.getLogger(RankListController.class);

    @Autowired
    BaseManager baseManager;
    @Autowired
    ResultMapHandler resultMapHandler;

    @RequestMapping(value = "/app/getInvestorTopList.do", method = RequestMethod.POST)//获取投资者排行榜
    @ResponseBody
    public Map checkUserName(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        logBean.setApiName("getInvestorTopList");
        Map<String, String> resultMap = new HashMap<String, String>();
        TreeMap treeMap = new TreeMap();
        try {
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());
            if ("".equals(jsonObj.getString("signmsg")) || "".equals(jsonObj.getString("pageNum")) ||
                    "".equals(jsonObj.getString("timestamp")) || "".equals(jsonObj.getString("pageSize")) ) {
                return  resultMapHandler.handlerResult("10001","必选参数为空，请仔细检查",logBean);
            }

            String signmsg = jsonObj.getString("signmsg");
            treeMap.put("pageSize",jsonObj.getString("pageSize"));
            treeMap.put("pageNum",jsonObj.getString("pageNum"));
            treeMap.put("timestamp", jsonObj.getString("timestamp"));
            boolean verify = DigitalSignatureUtil.verify(treeMap, signmsg);
            if (verify != true) {
                return  resultMapHandler.handlerResult("10002","参数校验不合格，请仔细检查",logBean);
            }


        } catch(Exception e){
            return resultMapHandler.handlerResult("10004","未知错误，请联系管理员",logBean);
        }
        return  resultMap;
    }
}
