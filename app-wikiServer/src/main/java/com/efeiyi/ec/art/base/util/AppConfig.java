package com.efeiyi.ec.art.base.util;

/**
 * Created by Administrator on 2015/12/23.
 */
public class AppConfig {
    public static final String appKey = "BL2QEuXUXNoGbNeHObD4EzlX+KuGc70U";
    public static final String SQL_MYUSER_GET = "from MyUser WHERE username= :username AND status<>'0'";
    public static final String SQL_BIGUSER_GET = "from BigUser WHERE username= :username AND status<>'0'";
    public static final String SQL_USER_GET = "from User WHERE username= :username AND status<>'0'";
    public static final String SQL_ACCOUNT_BY_USER_ID = "from Account a where a.user.id = :userId and status <> '0'";
    public static final String SQL_APP_VERSION_INFO = "from AppVersionUpGrade WHERE platform= :platform AND status<>'0'";
    public static final String SQL_USER_GET_APP = "from User WHERE username= :username AND status<>'0'";
    public static final String SQL_MESSAGE_GET_APP = "select m FROM Message m where targetUser.id = :userId and status = '1'  GROUP BY fromUser.id ORDER BY isWatch asc, createDatetime DESC";
    public static final String SQL_MESSAGE_GET_NUM_APP = "select new map(fromUser.id as fromUserId,COUNT(targetUser.id) as isRead)FROM Message m where targetUser.id = :userId and status = '1' and isWatch='0' GROUP BY fromUser.id ORDER BY isWatch asc,createDatetime DESC";
    public static final String SQL_MESSAGE_DETAIL_GET_APP = "from Message where (targetUser.id = :userId and fromUser.id = :fromUserId) or (targetUser.id = :fromUserId and targetUser.id = :fromUserId)  ORDER BY createDatetime ASC";
    public static final String SQL_INVEST_MONEY_APP = "select SUM(price) FROM ArtworkInvest where creator.id = :userId GROUP BY artwork.id ORDER BY createDatetime DESC";
    public static final String SQL_INVEST_ARTWORK_APP = "FROM ArtworkInvest where creator.id = :userId GROUP BY artwork.id ORDER BY createDatetime DESC";
    public static final String GET_INVESTOR_TOP_LIST = "SELECT a.user_id,d.truename,d.username,d.picture, SUM(a.price) as price,c.rois " +

            "  FROM app_art_work_invest a ,organization_user d, " +
            "  (SELECT b.user_id, SUM(b.currentBalance) as rois ,b.status  FROM  app_art_work_roi_record b WHERE  b.status<>'0' GROUP BY b.user_id) c" +
            "  WHERE a.status<>'0' AND c.status<>'0' AND a.user_id = c.user_id AND  a.user_id = d.id GROUP BY a.user_id  order by c.rois desc limit ";


    public static final String GET_ARTIST_TOP_LIST = "SELECT x.author_id,x.truename,x.username,x.invest_goal_money , aa.turnover from (" +
            "SELECT a.author_id,u.truename,u.username,u.picture, sum(a.invest_goal_money) AS invest_goal_money " +
            "FROM app_art_work a ,organization_user u " +
            "WHERE a.author_id = u.id " +
            "AND a.status <>'0' " +
            "AND a.step <>'13' " +
            "GROUP BY  a.author_id " +
            "ORDER BY  invest_goal_money desc " +
            ") x LEFT JOIN (select b.author_id as author_id, sum(b.invest_goal_money) AS turnover from app_art_work b WHERE b.type='3' AND b.step='32' AND  b.status <>'0' GROUP BY  b.author_id) aa " +
            " ON x.author_id= aa.author_id ORDER BY  aa.turnover desc LIMIT ";


    public static final String GET_ARTIST_TOP_LIST2 = "SELECT aw.author_id,ou.username,ou.truename,((max(ab.price)-aw.invest_goal_money)/aw.invest_goal_money) AS bidding_rate " +
            " from app_art_work aw ,organization_user ou ,app_art_work_bidding ab " +
            " WHERE aw.author_id = ou.id  AND aw.id = ab.art_work_id AND aw.type='3' AND aw.step='32'  AND aw.status<>'0' " +
            " GROUP BY aw.id " +
            " ORDER BY Bidding_rate desc " +
            " LIMIT ";


    public static final String SQL_USER_BINDING_GET = "from PushUserBinding WHERE user.id= :userId ";

    public static final String GET_ART_WORK_WINNER = "select * from app_art_work_bidding a where a.art_work_id=:artworkId and status='1' ORDER BY a.price desc LIMIT 0,1";

    public static final String GET_ART_WORK_BY_ARTIST = "from Artwork where author= :author";

    public static final String SQL_INVEST_MONEY_ARTWORK = "select SUM(price) FROM ArtworkInvest where artwork.id = :artworkId  and status<>'0'";
    //获取保证金列表
    public static final String SQL_MARGIN_ACCOUNT_LIST =" from MarginAccount where artwork.id= :artworkId and status='0'";
    //获取项目投资列表
    public static final String SQL_INVEST_MONEY_ARTWORK_LIST = " FROM ArtworkInvest where artwork.id = :artworkId  and status<>'0'";
   //查询默认收货地址
   public static final String SQL_CONSUMER_ADDRESS = " FROM ConsumerAddress where user.id = :userId  and status='2'";
    //获取账户
    public static final String SQL_GET_USER_ACCOUNT = " FROM Account where user.id = :userId  and status='1'";
    //获取关注数量
    public static final String SQL_GET_USER_FOLLOWED = "select count(1) FROM ArtUserFollowed where user.id = :userId  and status='1'";
    //获取粉丝数量
    public static final String SQL_GET_USER_FOLLOW = "select count(1) FROM ArtUserFollowed where follower.id = :userId  and status='1'";
    //获取用户签名
    public static final String SQL_GET_USER_SIGNER = " FROM UserBrief where user.id = :userId  and status='1'";
    //获取用户收益
    public static final String SQL_GET_USER_ROI = " FROM ROIRecord where user.id = :userId  and status='1'";

    //获取艺术家所有项目
    public static final String SQL_GET_USER_ARTWORK = " FROM Artwork where author.id = :userId  and status !='0'";

    //获取艺术家所有作品
    public static final String SQL_GET_USER_WORK = " FROM MasterWork where user.id = :userId  and status !='0'";

    //获取艺术家所有已拍卖项目
    public static final String SQL_GET_USER_ARTWORK_OVER = " FROM Artwork where author.id = :userId  and status='1'  and type ='3' and step='32'";


    //获取用户简介
    public static final String SQL_GET_USER_BRIEF= " FROM UserBrief where user.id = :userId  and status='1'";

    //获取艺术家信息
    public static final String SQL_GET_MASTER_INFO= " FROM Master where user.id = :userId ";
    // 判断用户是否关注用户或者大师
    public static final String SQL_GET_IS_FOLLOWED= " FROM ArtUserFollowed where user.id = :userId and follower.id= :followerId and status='1'";


    //微信登录
    public static final String SQL_WX_LOGIN = "FROM MyUser where status != '0' and unionid= :unionid ";


    //私信最后一条记录
    public static final String SQL_LSAT_MESSAGE = "FROM Message where (fromUser.id=:fromUserId and targetUser.id=:userId) or (fromUser.id=:userId and targetUser.id=:fromUserId) and status<>'0' order by createDatetime DESC " ;
}
