package com.attilapalfi.exceptional.services.rest.messages;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

/**
 * Created by Attila on 2015-06-11.
 */
public class AppStartRequestBody extends BaseExceptionRequestBody {
    private String deviceId;
    private String gcmId;
    private Collection<BigInteger> friendsFacebookIds;
    private int exceptionVersion;
    private String firstName, lastName;
    private String deviceName;

    public AppStartRequestBody() {
    }

    public AppStartRequestBody(String deviceId, String gcmId, Collection<BigInteger> friendsFacebookIds) {
        this.deviceId = deviceId;
        this.gcmId = gcmId;
        this.friendsFacebookIds = friendsFacebookIds;
    }

    public AppStartRequestBody(BigInteger userId, List<com.attilapalfi.exceptional.model.Exception> exceptionList,
                               String deviceId, String gcmId, Collection<BigInteger> friendsFacebookIds,
                               List<BigInteger> deletedFacebookFriendIds, List<BigInteger> newFacebookFriendIds) {
        super(userId, exceptionList);
        this.deviceId = deviceId;
        this.gcmId = gcmId;
        this.friendsFacebookIds = friendsFacebookIds;
    }

    public AppStartRequestBody(String deviceId, BigInteger userId,
                               List<BigInteger> friendsFacebookIds, List<BigInteger> exceptionIds,
                               List<BigInteger> deletedFacebookFriendIds, List<BigInteger> newFacebookFriendIds) {
        this.deviceId = deviceId;
        this.userFacebookId = userId;
        this.friendsFacebookIds = friendsFacebookIds;
        this.knownExceptionIds = exceptionIds;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public BigInteger getUserFacebookId() {
        return userFacebookId;
    }

    public void setUserFacebookId(BigInteger userId) {
        this.userFacebookId = userId;
    }

    public Collection<BigInteger> getFriendsFacebookIds() {
        return friendsFacebookIds;
    }

    public void setFriendsFacebookIds(Collection<BigInteger> friendsFacebookIds) {
        this.friendsFacebookIds = friendsFacebookIds;
    }

    public List<BigInteger> getKnownExceptionIds() {
        return knownExceptionIds;
    }

    public void setKnownExceptionIds(List<BigInteger> exceptionIds) {
        this.knownExceptionIds = exceptionIds;
    }

    public String getGcmId() {
        return gcmId;
    }

    public void setGcmId(String gcmId) {
        this.gcmId = gcmId;
    }

    public int getExceptionVersion() {
        return exceptionVersion;
    }

    public void setExceptionVersion(int exceptionVersion) {
        this.exceptionVersion = exceptionVersion;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
