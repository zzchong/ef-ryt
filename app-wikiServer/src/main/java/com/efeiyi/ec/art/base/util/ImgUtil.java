package com.efeiyi.ec.art.base.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Administrator on 2016/6/22.
 */
public class ImgUtil {


    /**
     *
     * @param imgUrl 图片地址
     * @return
     */
    public static   BufferedImage  getBufferedImage(String imgUrl) {
        URL url = null;
        InputStream is = null;
        BufferedImage img = null;
        try {
            url = new URL(imgUrl);
            is = url.openStream();
            img = ImageIO.read(is);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return img;
    }

    /**
     *
     * @param imgUrl 图片地址
     * @return
     */
    public static int getWidth(String imgUrl) {
        BufferedImage img = null;
        img = getBufferedImage(imgUrl);

        return img.getWidth();
    }


    /**
     *
     * @param imgUrl 图片地址
     * @return
     */
    public static int getHeight(String imgUrl) {
        BufferedImage img = null;
        img = getBufferedImage(imgUrl);

        return img.getHeight();
    }

}
