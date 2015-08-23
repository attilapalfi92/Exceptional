package com.attilapalfi.exceptional.services;

import com.attilapalfi.exceptional.model.*;
import com.attilapalfi.exceptional.model.Exception;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by palfi on 2015-08-19.
 */
public class Converter {

    public static List<BigInteger> fromFriendsToLongs(Collection<Friend> friends) {
        List<BigInteger> ids = new ArrayList<>(friends.size());
        for (Friend f : friends) {
            ids.add(f.getId());
        }
        return ids;
    }

    public static List<BigInteger> fromExceptionTypesToLongs(Collection<com.attilapalfi.exceptional.model.Exception> exceptions) {
        List<BigInteger> ids = new ArrayList<>(exceptions.size());
        for (Exception e : exceptions) {
            ids.add(e.getInstanceId());
        }
        return ids;
    }

}
