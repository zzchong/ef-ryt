package com.efeiyi.ec.art.artwork.controller;

import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.XQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Administrator on 2016/4/21.
 *
 */
@RestController
public class PCDController {

    @Autowired
    private BaseManager baseManager;

    @RequestMapping({"/app/provinceList.do"})
    public List<Object> getProvinceList(HttpServletRequest request) throws Exception {
        XQuery xQuery = new XQuery("listAddressProvince_default", request);
        List<Object> list = baseManager.listObject(xQuery);
        return list;
    }

    @RequestMapping({"/app/cityListByProvince.do"})
    public List<Object> getCityListByProvince(Model model, HttpServletRequest request) throws Exception {
        XQuery xQuery = new XQuery("listAddressCity_province", request);
        xQuery.addRequestParamToModel(model, request);
        List<Object> list = baseManager.listObject(xQuery);
        return list;
    }

    @RequestMapping({"/app/districtListByCity.do"})
    public List<Object> getDistrictListByCity(Model model, HttpServletRequest request) throws Exception {
        XQuery xQuery = new XQuery("listAddressDistrict_city", request);
        xQuery.addRequestParamToModel(model, request);
        List<Object> list = baseManager.listObject(xQuery);
        return list;
    }

}
