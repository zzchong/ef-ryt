package com.efeiyi.ec.art.modelConvert;

import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.model.Master;
import com.efeiyi.ec.art.organization.model.User;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2016/1/25.
 *
 */
public class ArtWorkInvestTopBean {

    private User user;

    private BigDecimal money;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }
}
