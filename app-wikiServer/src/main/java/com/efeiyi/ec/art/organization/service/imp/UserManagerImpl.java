package com.efeiyi.ec.art.organization.service.imp;


import com.alibaba.fastjson.JSONObject;
import com.efeiyi.ec.art.base.model.LogBean;
import com.efeiyi.ec.art.base.util.AppConfig;
import com.efeiyi.ec.art.base.util.ResultMapHandler;
import com.efeiyi.ec.art.model.*;
import com.efeiyi.ec.art.organization.dao.UserDao;
import com.efeiyi.ec.art.organization.model.MyUser;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.organization.service.UserManager;
import com.ming800.core.base.dao.XdoDao;
import com.ming800.core.base.dao.hibernate.XdoDaoSupport;
import com.ming800.core.base.service.BaseManager;
import com.ming800.core.does.model.PageInfo;
import com.ming800.core.taglib.PageEntity;
import com.ming800.core.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ming
 * Date: 12-10-15
 * Time: 下午5:02
 * To change this template use File | Settings | File Templates.
 */

@Service
@Transactional
public class UserManagerImpl implements UserManager, UserDetailsService {

    @Autowired
    private BaseManager baseManager;

    @Autowired
    private UserDao userDao;

    @Autowired
    private XdoDaoSupport xdoDao;

    @Autowired
    ResultMapHandler resultMapHandler;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
        System.out.println("loadUserByUsername");
        String queryStr = "SELECT u FROM MyUser u WHERE u.username=:username AND u.status != 0 ";
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("username", username);
        System.out.println("username is " + username);
        MyUser myUser = userDao.getUniqueMyUserByConditions(username, queryStr, queryParamMap);
        if (myUser == null) {
            System.out.println("myuser is null");
            throw new UsernameNotFoundException("user '" + username + "' not found...");
        } else {
            return myUser;
        }
    }


    /**
     * 登录成功
     *
     * @param userId
     * @return
     */
    @Override
    public Map loginSuccess(String userId) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //日志
        LogBean logBean = new LogBean();
        logBean.setApiName("login");
        logBean.setCreateDate(new Date());
        try {
            //当前用户
            User user = (User) baseManager.getObject(User.class.getName(), userId);

            //返回json
            resultMap = resultMapHandler.handlerResult("0", "成功", logBean);
            User userTemp = new User();
            userTemp.setId(user.getId());
            userTemp.setStatus(user.getStatus());
            userTemp.setCreateDatetime(user.getCreateDatetime());
            userTemp.setType(user.getType());
            userTemp.setCityId(user.getCityId());
            userTemp.setFansNum(user.getFansNum());
            //userTemp.setMaster(user.getMaster());
            userTemp.setName(user.getName());
            userTemp.setMasterWorkNum(user.getMasterWorkNum());
            userTemp.setPictureUrl(user.getPictureUrl());
            userTemp.setRole(user.getRole());
            userTemp.setSex(user.getSex());
            userTemp.setSignMessage(user.getSignMessage());
            userTemp.setUsername(user.getUsername());
            resultMap.put("userInfo", userTemp);
            //获取用户的关注数量  粉丝
            LinkedHashMap<String, Object> paramMap = new LinkedHashMap<String, Object>();
            paramMap.put("userId", user.getId());
            List<Long> counts = (List<Long>) baseManager.listObject(AppConfig.SQL_GET_USER_FOLLOWED, paramMap);
            Long count = 0l;
            if (counts != null && !counts.isEmpty()) {
                count = counts.get(0);
            }
            //Long count = (Long)baseManager.listObject(AppConfig.SQL_GET_USER_FOLLOWED, paramMap).get(0);
            List<Long> count1s = (List<Long>) baseManager.listObject(AppConfig.SQL_GET_USER_FOLLOW, paramMap);
            //Long count1 = (Long)baseManager.listObject(AppConfig.SQL_GET_USER_FOLLOW, paramMap).get(0);
            Long count1 = 0l;
            if (count1s != null && !count1s.isEmpty()) {
                count1 = count1s.get(0);
            }
            //获取签名 SQL_GET_USER_SIGNER
            List<UserBrief> userBriefs = (List<UserBrief>) baseManager.listObject(AppConfig.SQL_GET_USER_SIGNER, paramMap);
            UserBrief userBrief = new UserBrief();
            if (userBriefs != null && !userBriefs.isEmpty()) {
                userBrief = userBriefs.get(0);
            }
            //UserBrief userBrief = (UserBrief)baseManager.listObject(AppConfig.SQL_GET_USER_SIGNER, paramMap).get(0);
            resultMap.put("count", count);
            resultMap.put("count1", count1);
            resultMap.put("userBrief", userBrief.getSigner());
            User user1 = (User) baseManager.getObject(User.class.getName(), user.getId());
            BigDecimal investsMoney = new BigDecimal("0.00");
            BigDecimal roiMoney = new BigDecimal("0.00");
            BigDecimal rate = new BigDecimal("0.00");

            BigDecimal investsMoney2 = new BigDecimal("0.00");
            BigDecimal roiMoney2 = new BigDecimal("0.00");
            BigDecimal rate2 = new BigDecimal("0.00");
            if (user1.getMaster() != null && user1.getMaster().getId() != null && "1".equals(user1.getType())) {
                // 2 艺术家
                //项目总金额
                List<Artwork> artworks = (List<Artwork>) baseManager.listObject(AppConfig.SQL_GET_USER_ARTWORK, paramMap);
                for (Artwork artwork : artworks) {
                    investsMoney2 = investsMoney2.add(artwork.getInvestGoalMoney());
                }
                //项目总拍卖金额
                List<Artwork> artworks2 = (List<Artwork>) baseManager.listObject(AppConfig.SQL_GET_USER_ARTWORK_OVER, paramMap);
                for (Artwork artwork : artworks2) {
                    ArtworkBidding artworkBidding = (ArtworkBidding) xdoDao.getSession().createSQLQuery(AppConfig.GET_ART_WORK_WINNER).addEntity(ArtworkBidding.class).setString("artworkId", artwork.getId()).uniqueResult();
                    roiMoney2 = roiMoney2.add(artworkBidding.getPrice());
                }
                //项目拍卖溢价率
                if (investsMoney2.doubleValue() != 0.00 && roiMoney2.doubleValue() != 0.00) {
                    rate2 = roiMoney2.divide(investsMoney2, 2);
                }
                resultMap.put("investsMoney", investsMoney2);
                resultMap.put("roiMoney", roiMoney2);
                resultMap.put("rate", rate2);
                resultMap.put("flag", "2");
            } else {
                // 1 普通用户
                //获取投资金额
                List<ArtworkInvest> artworkInvests = (List<ArtworkInvest>) baseManager.listObject(AppConfig.SQL_INVEST_ARTWORK_APP, paramMap);
                for (ArtworkInvest artworkInvest : artworkInvests) {
                    investsMoney = investsMoney.add(artworkInvest.getPrice());
                }

                //获取投资收益金额 SQL_GET_USER_ROI
                List<ROIRecord> roiRecords = (List<ROIRecord>) baseManager.listObject(AppConfig.SQL_GET_USER_ROI, paramMap);
                for (ROIRecord roiRecord : roiRecords) {
                    roiMoney = roiMoney.add(roiRecord.getCurrentBalance().subtract(roiRecord.getArtworkInvest().getPrice()));
                }
                //投资回报率
                if (investsMoney.doubleValue() != 0.00 && roiMoney.doubleValue() != 0.00) {
                    rate = roiMoney.divide(investsMoney, 2);

                }
                resultMap.put("investsMoney", investsMoney);
                resultMap.put("roiMoney", roiMoney);
                resultMap.put("rate", rate);
                resultMap.put("flag", "1");

            }

            return resultMap;

        } catch (Exception e) {
            e.printStackTrace();
            resultMap = resultMapHandler.handlerResult("10005", "查询数据出现异常:" + e.getMessage(), logBean);
            return resultMap;
        }
    }

    /**
     * 微信，手机登录验证
     *
     * @param jsonObj
     * @return
     */
    @Override
    public Map<String, String> usernameAuthentication(JSONObject jsonObj) {
        Map<String, String> resultMap = new HashMap<String, String>();
        if (!StringUtils.isEmpty(jsonObj.getString("unionid"))) {//微信登录
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("unionid", jsonObj.getString("unionid"));
            MyUser user = null;
            try {
                user = (MyUser) baseManager.getUniqueObjectByConditions(AppConfig.SQL_WX_LOGIN, map);
                if (user != null && user.getId() != null) {
                    if (StringUtils.isEmpty(user.getUsername())) {
                        user.setUsername(System.currentTimeMillis() + "");
                        user.setPassword(StringUtil.encodePassword("rongyitou", "SHA1"));
                        baseManager.saveOrUpdate(User.class.getName(), user);
                    }
                    resultMap.put("username", user.getUsername());
                    resultMap.put("password", "rongyitou");
                    return resultMap;
                } else {
                    user = new MyUser();
                    user.setUsername(System.currentTimeMillis() + "");
                    user.setPassword(StringUtil.encodePassword("rongyitou", "SHA1"));
                    user.setName(jsonObj.getString("nickname"));
                    user.setUnionid(jsonObj.getString("unionid"));
                    user.setPictureUrl(jsonObj.getString("headimgurl"));
                    user.setAccountExpired(false);
                    user.setAccountLocked(false);
                    user.setCredentialsExpired(false);
                    user.setEnabled(true);
                    user.setStatus(1);
                    user.setUtype(2);
                    user.setCreateDatetime(new Date());
                    baseManager.saveOrUpdate(MyUser.class.getName(), user);
                    //给用户绑定一个账户
                    Account account = new Account();
                    account.setCurrentUsableBalance(new BigDecimal("0.00"));
                    account.setCurrentBalance(new BigDecimal("0.00"));
                    account.setCreateDatetime(new Date());
                    account.setStatus("1");

                    User tmpUser = new User();
                    tmpUser.setId(user.getId());
                    if(tmpUser == null) {
                        resultMap.put("errorMsg", "user 为 null");
                        return resultMap;
                    }

                    account.setUser(tmpUser);
                    baseManager.saveOrUpdate(Account.class.getName(), account);
                    resultMap.put("username", user.getUsername());
                    resultMap.put("password", "rongyitou");
                    return resultMap;
                }
            } catch (Exception e) {
                resultMap.put("errorMsg", e.getMessage());
                return resultMap;
            }
        } else {//手机登录
            String username = jsonObj.getString("username");
            String password = jsonObj.getString("password");

            if (StringUtils.isEmpty(username))
                username = "";
            if (StringUtils.isEmpty(password))
                password = "";

            resultMap.put("username", username.trim());
            resultMap.put("password", password);
            return resultMap;

        }
    }

}
