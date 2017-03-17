package com.efeiyi.ec.art.organization.controller;

import com.efeiyi.ec.art.organization.service.TestAddressManager;
import com.ming800.core.base.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Administrator on 2016/12/5.
 */
@Controller
@RequestMapping("testAddress")
public class TestAddressController extends BaseController {

    @Autowired
    TestAddressManager addressManager;

    //获取地址列表



}
