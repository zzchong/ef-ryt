package com.efeiyi.ec.art.organization.util;


import com.efeiyi.ec.art.organization.model.MyUser;
import com.efeiyi.ec.art.organization.model.Role;
import com.efeiyi.ec.art.organization.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author WuYingbo
 */
public class TimeUtil {


    //时间比较
    public static  String getDistanceTimes(String str1, String str2) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Date one;
        Date two;
        long day = 0;
        long hour = 0;
        long min = 0;
        try {
            one = sdf.parse(str1);
            two = sdf.parse(str2);
            long time1 = one.getTime();
            long time2 = two.getTime();
            long diff ;

            diff = time1 - time2;

            day = diff / (24 * 60 * 60 * 1000);
            hour = (diff / (60 * 60 * 1000) - day * 24);
            min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long[] times = {day, hour, min};
        String time = day+"日"+hour+"时"+min+"分";
        return time;
    }


}
