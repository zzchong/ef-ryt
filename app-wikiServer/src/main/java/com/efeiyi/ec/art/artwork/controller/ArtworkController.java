package com.efeiyi.ec.art.artwork.controller;


import com.efeiyi.ec.art.model.Artwork;
import com.ming800.core.base.controller.BaseController;
import com.ming800.core.does.model.PageInfo;
import com.ming800.core.does.model.XQuery;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by Administrator on 2016/1/29.
 *
 */
@Controller
public class ArtworkController extends BaseController {
    private static Logger logger = Logger.getLogger(ArtworkController.class);


    @RequestMapping(value = "/app/getArtWorkList.do", method = RequestMethod.POST)
    @ResponseBody
    public Map getArtWorkList(HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
      try{
          XQuery query = new XQuery("plistArtwork_default", request);
          PageInfo pageInfo = baseManager.listPageInfo(query);
          List<Artwork> list = pageInfo.getList();
          if (list!= null && !list.isEmpty()){
              resultMap.put("responseInfo",list);
          }else {
              resultMap.put("responseInfo",null);
          }
          resultMap.put("resultCode","0");
          resultMap.put("resultMsg","成功");
        } catch(Exception e){
            e.printStackTrace();
            resultMap.put("resultCode", "10004");
            resultMap.put("resultMsg", "未知错误，请联系管理员");
            return resultMap;
        }

        return resultMap;
    }
}
