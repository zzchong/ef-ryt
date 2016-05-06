package com.efeiyi.ec.virtual.model.task;

import com.efeiyi.ec.art.model.UserBrief;
import com.efeiyi.ec.art.organization.model.User;
import com.efeiyi.ec.art.virtual.model.Fans;
import com.efeiyi.ec.art.virtual.model.VirtualInvestorPlan;
import com.efeiyi.ec.art.virtual.model.VirtualPlan;
import org.hibernate.CacheMode;
import org.hibernate.exception.GenericJDBCException;

import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2015/11/26.
 */
@Deprecated
public class VirtualUserGenerator extends BaseTimerTask {

    public VirtualUserGenerator(VirtualInvestorPlan virtualInvestorPlan) {
        super();
    }

    @Override
    public void setVirtualPlan(VirtualPlan virtualPlan) {

    }

    public void execute(List<VirtualPlan> virtualPlanList) {
        if (session == null || !session.isOpen()) {
            session = sessionFactory.openSession();
        }
        session.setCacheMode(CacheMode.IGNORE);
        List<Fans> fansList = session.createQuery("from Fans").list();
        for (Fans fan : fansList) {
            User user = new User();
            user.setName(fan.getName());
            user.setStatus("8");
            user.setCreateDatetime(new Date());
            user.setPictureUrl(fan.getPicture_url());
            UserBrief userBrief = new UserBrief();
            userBrief.setUser(user);
            userBrief.setStatus("0");
            userBrief.setType("2");
            userBrief.setSigner(fan.getSignature());
            session.saveOrUpdate(user);
            session.saveOrUpdate(userBrief);
        }
        session.flush();
    }

    @Override
    public void run() {
        try {
            if (session == null || !session.isOpen()) {
                session = sessionFactory.openSession();
            }
            execute(null);
        } catch (GenericJDBCException jdbcE) {
            retrieveSessionFactory();
            execute(null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

}
