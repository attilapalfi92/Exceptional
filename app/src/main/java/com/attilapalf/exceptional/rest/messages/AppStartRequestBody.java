package com.attilapalf.exceptional.rest.messages;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

/**
 * Created by Attila on 2015-06-11.
 */
public class AppStartRequestBody extends BaseRequestBody {
    private String deviceId;
    private String regId;
    private Collection<Long> friendsIds;

    public AppStartRequestBody() {
    }

    public AppStartRequestBody(String deviceId, long userId, String regId,
                               List<Long> friendsIds, List<Long> exceptionIds) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.regId = regId;
        this.friendsIds = friendsIds;
        this.exceptionIds = exceptionIds;
    }

    public AppStartRequestBody(String deviceId, long userId,
                               List<Long> friendsIds, List<Long> exceptionIds) {
        this.deviceId = deviceId;
        this.userId = userId;
        this.friendsIds = friendsIds;
        this.exceptionIds = exceptionIds;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
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

    public String getRegId() {
        return regId;
    }

    public void setRegId(String regId) {
        this.regId = regId;
    }
}
