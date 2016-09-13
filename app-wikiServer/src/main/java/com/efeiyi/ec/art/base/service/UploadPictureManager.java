package com.efeiyi.ec.art.base.service;

import net.sf.json.JSONArray;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by Administrator on 2016/9/13.
 */
public interface UploadPictureManager {
    public List uplaodPicture(HttpServletRequest request) throws Exception;
}
