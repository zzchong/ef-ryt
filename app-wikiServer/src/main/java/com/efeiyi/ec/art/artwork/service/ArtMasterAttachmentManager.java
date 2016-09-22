package com.efeiyi.ec.art.artwork.service;

import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.model.ArtMasterAttachment;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/7.
 */
public interface ArtMasterAttachmentManager {
    Map saveArtMasterAttachment(HttpServletRequest request, LogBean logBean) throws Exception;

    Map deleteArtMasterAttachment(HttpServletRequest request, LogBean logBean) throws Exception;
}
