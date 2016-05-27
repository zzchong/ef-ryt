package com.efeiyi.ec.art.message.dao.hibernate;


import com.efeiyi.ec.art.message.dao.MessageDao;
import com.efeiyi.ec.art.organization.dao.UserDao;
import com.efeiyi.ec.art.organization.model.MyUser;
import com.ming800.core.base.dao.hibernate.BaseDaoSupport;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MessageDaoHibernate implements MessageDao{

    @Autowired
    @Qualifier("sessionFactory")
    private SessionFactory sessionFactory;

    public Session getSession() {
//        sessionFactory
        return sessionFactory.getCurrentSession();
    }


    @Override
    public List getPageList(String hql, Integer index, Integer size) {
        Session session = this.getSession();
        Query query = session.createQuery(hql);
        query.setMaxResults(size);
        query.setFirstResult(index);
        return query.list();
    }

    @Override
    public List getPageList2(String hql, Integer index, Integer size,LinkedHashMap<String, Object> params) {
        Session session = this.getSession();
        Query query = session.createQuery(hql);
        query = setQueryParams(query,params);
        query.setMaxResults(size);
        query.setFirstResult(index);
        return query.list();
    }

    @Override
    public List getPageList2(String hql, Integer index, Integer size,Object... params) {
        Session session = this.getSession();
        Query query = session.createQuery(hql);
        query = setParameters(query,params);
        query.setMaxResults(size);
        query.setFirstResult(index);
        return query.list();
    }

    protected Query setParameters(Query query, Object[] paramlist) {
        if (paramlist != null) {
            for (int i = 0; i < paramlist.length; i++) {
                if (paramlist[i] instanceof Date) {
                    //TODO 难道这是bug 使用setParameter不行？？
                    query.setTimestamp(i, (Date) paramlist[i]);
                } else {
                    query.setParameter(i, paramlist[i]);
                }
            }
        }
        return query;
    }

    protected Query setQueryParams(Query query, LinkedHashMap<String, Object> queryParamMap) {
        if (queryParamMap != null && queryParamMap.size() > 0) {
            for (String paramName : queryParamMap.keySet()) {
                if (queryParamMap.get(paramName) instanceof Object[]) {
                    query.setParameterList(paramName, (Object[]) queryParamMap.get(paramName));
                } else if (queryParamMap.get(paramName) instanceof Collection) {
                    query.setParameterList(paramName, (Collection) queryParamMap.get(paramName));
                } else {
                    query.setParameter(paramName, queryParamMap.get(paramName));
                }
            }
        }

        return query;
    }
}
