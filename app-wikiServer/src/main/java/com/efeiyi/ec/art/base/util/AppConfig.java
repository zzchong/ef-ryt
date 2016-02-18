package com.efeiyi.ec.art.base.util;

/**
 * Created by Administrator on 2015/12/23.
 *
 */
public class AppConfig {
    public static final String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
    public static final String SQL_MYUSER_GET ="from MyUser WHERE username= :username AND status<>'0'";
    public static final String SQL_USER_GET ="from User WHERE username= :username AND status<>'0'";
    public static final String SQL_APP_VERSION_INFO ="from AppVersionUpGrade WHERE platform= :platform AND status<>'0'";
    public static final String SQL_USER_GET_APP ="from User WHERE username= :username AND status<>'0'";
    public static final String SQL_NOTICE_GET_APP ="from Notice WHERE targetUser.id= :userId AND status<>'0' limit :pageNum,:pageSize";
    public static final String SQL_REPLY_GET_APP ="from Reply WHERE targetUser.id= :userId AND status<>'0' limit :pageNum,:pageSize";
    public static final String SQL_MESSAGE_GET_APP ="from Message WHERE targetUser.id= :userId AND status<>'0' limit :pageNum,:pageSize";
}
