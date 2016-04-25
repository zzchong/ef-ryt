package com.efeiyi.ec.art.artwork.controller;

import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/4/21.
 *
 */
@RestController
public class PCDController {

    @Autowired
    private BaseManager baseManager;

    @RequestMapping(value = "/app/provinceList.do")
    @ResponseBody
    public Map getProvinceList(HttpServletRequest request) throws Exception {
        XQuery xQuery = new XQuery("listAddressProvince_default", request);
        List<Object> list = baseManager.listObject(xQuery);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if(list!= null && !list.isEmpty()){
            resultMap.put("provinceList",list);
            resultMap.put("resultCode","0");
            resultMap.put("resultMsg","成功");
        }else {
            resultMap.put("resultCode","10008");
            resultMap.put("resultMsg","查无数据，稍后再试");
            resultMap.put("provinceList",new ArrayList<Object>());
        }
        return resultMap;
    }

    @RequestMapping(value ="/app/cityListByProvince.do")
    @ResponseBody
    public Map getCityListByProvince(Model model, HttpServletRequest request) throws Exception {
        XQuery xQuery = new XQuery("listAddressCity_province", request);
        xQuery.addRequestParamToModel(model, request);
        List<Object> list = baseManager.listObject(xQuery);

        Map<String, Object> resultMap = new HashMap<String, Object>();
        if(list!= null && !list.isEmpty()){
            resultMap.put("cityList",list);
            resultMap.put("resultCode","0");
            resultMap.put("resultMsg","成功");
        }else {
            resultMap.put("resultCode","10008");
            resultMap.put("resultMsg","查无数据，稍后再试");
            resultMap.put("cityList",new ArrayList<Object>());
        }
        return resultMap;
    }

    @RequestMapping(value ="/app/districtListByCity.do")
    @ResponseBody
    public Map getDistrictListByCity(Model model, HttpServletRequest request) throws Exception {
        XQuery xQuery = new XQuery("listAddressDistrict_city", request);
        xQuery.addRequestParamToModel(model, request);
        List<Object> list = baseManager.listObject(xQuery);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if(list!= null && !list.isEmpty()){
            resultMap.put("districtList",list);
            resultMap.put("resultCode","0");
            resultMap.put("resultMsg","成功");
        }else {
            resultMap.put("resultCode","10008");
            resultMap.put("resultMsg","查无数据，稍后再试");
            resultMap.put("districtList",new ArrayList<Object>());
        }
        return resultMap;
    }


}
