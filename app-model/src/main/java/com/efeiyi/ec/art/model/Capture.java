package com.efeiyi.ec.art.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2015/12/10.
 *
 */
@Component
public class Capture {
    @Value("#{configProperties['isCapture']}")
    private String isCapture;

    public String getIsCapture() {
        return isCapture;
    }

    public void setIsCapture(String isCapture) {
        this.isCapture = isCapture;
    }
}
