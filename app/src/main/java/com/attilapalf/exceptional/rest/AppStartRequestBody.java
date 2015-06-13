package com.attilapalf.exceptional.rest;

import java.util.Collection;
import java.util.List;

/**
 * Created by Attila on 2015-06-11.
 */
public class AppStartRequestBody {
    private long deviceId, userId;
    private Collection<Long> friendsIds;
    private List<Long> exceptionIds;

    public AppStartRequestBody() {
    }

    public AppStartRequestBody(long deviceId, long userId, List<Long> friendsIds) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.friendsIds = friendsIds;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Collection<Long> getFriendsIds() {
        return friendsIds;
    }

    public void setFriendsIds(Collection<Long> friendsIds) {
        this.friendsIds = friendsIds;
    }

    public List<Long> getExceptionIds() {
        return exceptionIds;
    }

    public void setExceptionIds(List<Long> exceptionIds) {
        this.exceptionIds = exceptionIds;
    }
}
