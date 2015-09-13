package com.attilapalfi.exceptional.services.rest.messages;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

/**
 * Created by Attila on 2015-06-11.
 */
public class AppStartRequest extends BaseExceptionRequest {
    private String deviceId;
    private String gcmId;
    private Collection<String> friendsFacebookIds;
    private int exceptionVersion;
    private String firstName, lastName;
    private String deviceName;

    public AppStartRequest( ) {
    }

    public AppStartRequest( String deviceId, String gcmId, Collection<String> friendsFacebookIds ) {
        this.deviceId = deviceId;
        this.gcmId = gcmId;
        this.friendsFacebookIds = friendsFacebookIds;
    }

    public AppStartRequest( String userId, List<com.attilapalfi.exceptional.model.Exception> exceptionList,
                            String deviceId, String gcmId, Collection<String> friendsFacebookIds,
                            List<String> deletedFacebookFriendIds, List<String> newFacebookFriendIds ) {
        super( userId, exceptionList );
        this.deviceId = deviceId;
        this.gcmId = gcmId;
        this.friendsFacebookIds = friendsFacebookIds;
    }

    public AppStartRequest( String deviceId, String userId,
                            List<String> friendsFacebookIds, List<BigInteger> exceptionIds,
                            List<String> deletedFacebookFriendIds, List<String> newFacebookFriendIds ) {
        this.deviceId = deviceId;
        this.userFacebookId = userId;
        this.friendsFacebookIds = friendsFacebookIds;
        this.knownExceptionIds = exceptionIds;
    }

    public String getDeviceId( ) {
        return deviceId;
    }

    public void setDeviceId( String deviceId ) {
        this.deviceId = deviceId;
    }

    public String getUserFacebookId( ) {
        return userFacebookId;
    }

    public void setUserFacebookId( String userId ) {
        this.userFacebookId = userId;
    }

    public Collection<String> getFriendsFacebookIds( ) {
        return friendsFacebookIds;
    }

    public void setFriendsFacebookIds( Collection<String> friendsFacebookIds ) {
        this.friendsFacebookIds = friendsFacebookIds;
    }

    public List<BigInteger> getKnownExceptionIds( ) {
        return knownExceptionIds;
    }

    public void setKnownExceptionIds( List<BigInteger> exceptionIds ) {
        this.knownExceptionIds = exceptionIds;
    }

    public String getGcmId( ) {
        return gcmId;
    }

    public void setGcmId( String gcmId ) {
        this.gcmId = gcmId;
    }

    public int getExceptionVersion( ) {
        return exceptionVersion;
    }

    public void setExceptionVersion( int exceptionVersion ) {
        this.exceptionVersion = exceptionVersion;
    }

    public String getFirstName( ) {
        return firstName;
    }

    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }

    public String getLastName( ) {
        return lastName;
    }

    public void setLastName( String lastName ) {
        this.lastName = lastName;
    }

    public String getDeviceName( ) {
        return deviceName;
    }

    public void setDeviceName( String deviceName ) {
        this.deviceName = deviceName;
    }
}
