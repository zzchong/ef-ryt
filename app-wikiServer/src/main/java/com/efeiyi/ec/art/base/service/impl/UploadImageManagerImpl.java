package com.efeiyi.ec.art.base.service.impl;

import com.efeiyi.ec.art.base.service.UploadImageManager;
import com.ming800.core.p.service.AliOssUploadManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2016/9/13.
 */

@Service
public class UploadImageManagerImpl implements UploadImageManager {

    @Autowired
    private AliOssUploadManager aliOssUploadManager;

    @Override
    public List uplaodImage(HttpServletRequest request) throws Exception {
        List list = new ArrayList();

        //创建一个通用的多部分解析器
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());

        //判断 request 是否有文件上传,即多部分请求
        if (multipartResolver.isMultipart(request)) {
            //转换成多部分request
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            //取得request中的所有文件名
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                //取得上传文件
                MultipartFile file = multiRequest.getFile(iter.next());

                //取得当前上传文件的文件名称
                String myFileName = file.getOriginalFilename();
                //如果名称不为“”,说明该文件存在，否则说明该文件不存在
                if (myFileName.trim() != "") {
                    //重命名上传后的文件名
                    StringBuilder url = new StringBuilder("master/");

                    url.append("picture/" + new Date().getTime() + myFileName);

                    String pictureUrl = "http://rongyitou2.efeiyi.com/" + url.toString();

                    //将图片上传至阿里云
                    aliOssUploadManager.uploadFile(file, "ec-efeiyi2", url.toString());

                    list.add(pictureUrl);
                }
            }
        }

        return list;
    }
}
