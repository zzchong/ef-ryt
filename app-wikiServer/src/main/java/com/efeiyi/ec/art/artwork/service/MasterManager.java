package com.efeiyi.ec.art.artwork.service;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2016/4/14.
 *
 */
public interface MasterManager {


    boolean  saveMasterWork(HttpServletRequest request, MultipartFile multipartFile) throws Exception;


}
