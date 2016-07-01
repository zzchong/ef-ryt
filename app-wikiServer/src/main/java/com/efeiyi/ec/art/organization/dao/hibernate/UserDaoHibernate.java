package com.efeiyi.ec.art.organization.dao.hibernate;



import com.efeiyi.ec.art.organization.dao.UserDao;
import com.efeiyi.ec.art.organization.model.MyUser;
import com.ming800.core.base.dao.hibernate.BaseDaoSupport;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;

@Repository
public class UserDaoHibernate extends BaseDaoSupport<MyUser> implements UserDao {


    @Override
    public MyUser getUniqueMyUserByConditions(String branchName, String queryHql, LinkedHashMap<String, Object> queryParamMap) {

        Session tempSession = super.getSessionFactory().openSession();
        Query listQuery = tempSession.createQuery(queryHql);
        setQueryParams(listQuery, queryParamMap);
        MyUser myUser = (MyUser) listQuery.uniqueResult();
        tempSession.close();

        return myUser; //To change body of implemented methods use File | Settings | File Templates.
    }

}
