package com.efeiyi.ec.art.base.controller;

import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.service.DictionaryManager;
import com.efeiyi.ec.art.base.util.JsonAcceptUtil;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.Dictionary;
import com.ming800.core.base.controller.BaseController;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/2.
 */

@Controller
public class DictionaryController extends BaseController{

    @Autowired
    DictionaryManager dictionaryManager;

    @Autowired
    ResultMapHandler resultMapHandler;

    @RequestMapping("/app/getDictionaryList.do")
    @ResponseBody
    public Map<String, Object> getDictionaryList(HttpServletRequest request) {
        LogBean logBean = new LogBean();
        Map<String, Object> resultMap = new HashMap<String, Object>();

        try{
            JSONObject jsonObj = JsonAcceptUtil.receiveJson(request);
            Integer type = jsonObj.getInteger("type");

            logBean.setApiName("getDictionaryList");
            logBean.setCreateDate(new Date());
            logBean.setRequestMessage(jsonObj.toString());

            List<Dictionary> dicList = dictionaryManager.getDictionaryByType(type);

            resultMap.put("resultCode", "0");
            resultMap.put("resultMsg", "成功");
            resultMap.put("dataList", dicList);
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        return resultMap;
    }

    //github test

    public void test() {
        System.out.print("test");
    }
}
