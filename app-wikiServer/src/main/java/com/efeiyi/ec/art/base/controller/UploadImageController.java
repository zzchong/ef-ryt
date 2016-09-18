package com.efeiyi.ec.art.base.controller;

import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.service.UploadImageManager;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.ming800.core.base.controller.BaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UploadImageController extends BaseController {

    @Autowired
    ResultMapHandler resultMapHandler;

    @Autowired
    UploadImageManager uploadImageManager;

    @RequestMapping("/app/uploadPicture.do")
    @ResponseBody
    public Map uploadPicture(HttpServletRequest request)  {
        LogBean logBean = new LogBean();//日志记录
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List list = null;

        try{
            list = uploadImageManager.uplaodImage(request);
        } catch (Exception e) {
            return resultMapHandler.handlerResult("10004", "未知错误，请联系管理员", logBean);
        }

        resultMap.put("resultCode", "0");
        resultMap.put("resultMsg", "图片上传成功");
        resultMap.put("pictureUrls", list);

        return resultMap;
    }
}
