package com.efeiyi.ec.art.modelConvert;

import com.efeiyi.ec.art.model.Artwork;
import com.efeiyi.ec.art.organization.model.User;

/**
 * Created by Administrator on 2016/1/25.
 *
 */
public class UserFollowedBean {

    private User user;

    private Boolean isFollowed;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getFollowed() {
        return isFollowed;
    }

    public void setFollowed(Boolean followed) {
        isFollowed = followed;
    }
}
