package com.efeiyi.ec.art.artwork.service.impl;

import com.efeiyi.ec.art.artwork.service.ArtMasterAttachmentManager;
import com.efeiyi.ec.art.model.ArtMasterAttachment;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.p.service.AliOssUploadManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Administrator on 2016/9/7.
 */

@Service
public class ArtMasterAttachmentManagerImpl implements ArtMasterAttachmentManager {

    @Autowired
    BaseManager baseManager;

    @Autowired
    private AliOssUploadManager aliOssUploadManager;

    @Override
    public void saveMasterAttachment(ArtMasterAttachment masterAttachment, MultipartFile picture) throws Exception {

        String url = "masterAttachment/"+System.currentTimeMillis()+picture.getOriginalFilename();

        aliOssUploadManager.uploadFile(picture,"ec-efeiyi2",url);

        masterAttachment.setUrl(url);
        baseManager.saveOrUpdate(ArtMasterAttachment.class.getName(), masterAttachment);
    }
}
