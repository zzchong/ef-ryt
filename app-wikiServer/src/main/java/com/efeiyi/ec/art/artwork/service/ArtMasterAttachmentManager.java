package com.efeiyi.ec.art.artwork.service;

import com.efeiyi.ec.art.model.ArtMasterAttachment;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Administrator on 2016/9/7.
 */
public interface ArtMasterAttachmentManager {
    void saveMasterAttachment(ArtMasterAttachment masterAttachment, MultipartFile picture) throws Exception;
}
