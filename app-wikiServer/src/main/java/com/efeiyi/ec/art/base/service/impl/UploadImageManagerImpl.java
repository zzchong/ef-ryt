package com.efeiyi.ec.art.base.service.impl;

import com.efeiyi.ec.art.base.service.UploadImageManager;
import com.ming800.core.p.service.AliOssUploadManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by Administrator on 2016/9/13.
 */

@Service
public class UploadImageManagerImpl implements UploadImageManager {

    @Autowired
    private AliOssUploadManager aliOssUploadManager;

    @Override
    public List uplaodImage(HttpServletRequest request) throws Exception {
        List<Map<String, Object>> list = new ArrayList();

        //创建一个通用的多部分解析器
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());

        //判断 request 是否有文件上传,即多部分请求
        if (multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;

            Map<String, MultipartFile> fileMap = multiRequest.getFileMap();
            for(String key : fileMap.keySet()) {
                MultipartFile file = fileMap.get(key);
                BufferedImage bi = ImageIO.read(file.getInputStream());
                int width = 0;
                int height = 0;
                if(bi != null) {
                    width = bi.getWidth();
                    height = bi.getHeight();
                }
                String fileName = file.getOriginalFilename();

                if(!"".equals(fileName.trim())) {
                    StringBuilder url = new StringBuilder("master/");

                    url.append("picture/" + new Date().getTime() + fileName);

                    String pictureUrl = "http://rongyitou2.efeiyi.com/" + url.toString();

                    //将图片上传至阿里云
                    aliOssUploadManager.uploadFile(file, "ec-efeiyi2", url.toString());

                    Map<String, Object> map = new HashMap<>();
                    map.put("pictureUrl", pictureUrl);
                    map.put("width", width);
                    map.put("height", height);
                    list.add(map);
                }
            }
        }

        return list;
    }

}
