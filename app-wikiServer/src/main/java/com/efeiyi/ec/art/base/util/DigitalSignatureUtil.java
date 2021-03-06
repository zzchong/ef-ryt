package com.efeiyi.ec.art.base.util;

import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Administrator on 2015/12/22.
 */
public class DigitalSignatureUtil {
    // stackoverflow的MD5计算方法，调用了MessageDigest库函数，并把byte数组结果转换成16进制
    public static String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest
                    .getInstance("MD5");
            byte[] array = md.digest(md5.getBytes("utf-8"));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100)
                        .substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    //校验数字签名的正确性
    public static Boolean verify(TreeMap map, String signmsg) throws Exception {
        boolean flag = false;
        StringBuffer str = new StringBuffer();
        for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            Object Value = map.get(key);
            str.append(key).append("=").append(Value).append("&");
        }
        str.append("key=" + AppConfig.appKey);

        String md5Value = MD5(str.toString());
        //System.out.println(str.toString()+"======>"+md5Value);
        if (md5Value.equals(signmsg)) {
            flag = true;
        }
        return flag;
    }


    //校验数字签名的正确性
    public static Boolean verify2(JSONObject jsonObject) throws Exception {
        String signmsg = "";
        TreeMap map = new TreeMap();
        for (Map.Entry me : jsonObject.entrySet()) {
            if ("signmsg".equals(me.getKey())) {
                signmsg = me.getValue().toString();
            } else {
                map.put(me.getKey(), me.getValue());
            }
        }
        boolean flag = false;
        StringBuffer str = new StringBuffer();
        for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            Object Value = map.get(key);
            str.append(key).append("=").append(Value).append("&");
        }
        str.append("key=" + AppConfig.appKey);

        String md5Value = MD5(str.toString());
        //System.out.println(str.toString()+"======>"+md5Value);
        if (md5Value.equals(signmsg)) {
            flag = true;
        }
        return flag;
    }

    //对所有参数进行自然排序并加密处理
    public static String encrypt(Map map) throws Exception {
        TreeMap map2 = new TreeMap();
        for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            Object Value = map.get(key);
            map2.put(key, Value);
        }
        StringBuffer str = new StringBuffer();
        for (Iterator it = map2.keySet().iterator(); it.hasNext(); ) {
            Object key = it.next();
            Object Value = map2.get(key);
            str.append(key).append("=").append(Value).append("&");
        }
        str.append("key=" + AppConfig.appKey);

        String md5Value = MD5(str.toString());
        //System.out.println(str.toString()+"======>"+md5Value);
        return md5Value;
    }

}
