package com.efeiyi.ec.art.base.util;

/**
 * Created by Administrator on 2015/12/23.
 *
 */
public class AppConfig {
    public static final String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
    public static final String SQL_USER_GET ="from MyUser WHERE username= :username AND status<>'0'";
    public static final String SQL_APP_VERSION_INFO ="from AppVersionUpGrade WHERE platform= :platform AND status<>'0'";
    public static final String SQL_USER_GET_APP ="from User WHERE username= :username AND status<>'0'";
    public static final String SQL_NOTICE_GET_APP ="from Notification WHERE targetUser.id= :userId AND status<>'0' limit :pageNum,:pageSize order by createDatetime desc";
    public static final String SQL_REPLY_GET_APP ="from ArtworkComment WHERE fatherComment.creator.id= :userId AND status<>'0' limit :pageNum,:pageSize order by createDatetime desc";
    public static final String SQL_MESSAGE_GET_APP ="select *,COUNT(case when isWatch='0' then :userId  else null end) as 未读数 FROM Message where targetUser.id = :userId and status = \"1\" GROUP BY fromUser.id ORDER BY createDatetime DESC";
    public static final String SQL_MESSAGE_DETAIL_GET_APP ="from Message where (targetUser.id = :userId and fromUser.id = :fromUserId) or (targetUser.id = :fromUserId and targetUser.id = :fromUserId)  ORDER BY createDatetime DESC";
}
