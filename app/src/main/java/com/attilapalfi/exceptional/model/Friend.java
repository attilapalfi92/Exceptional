package com.attilapalfi.exceptional.model;

import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.Comparator;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.attilapalfi.exceptional.services.persistent_stores.ImageCache;
import com.google.gson.Gson;

/**
 * Created by Attila on 2015-06-12.
 */
public class Friend {
    private BigInteger id;
    private String firstName;
    private String lastName;
    private String imageUrl;
    private int points = 100;

    private static Gson gson = new Gson();

    public static class NameComparator implements Comparator<Friend> {
        @Override
        public int compare( Friend lhs, Friend rhs ) {
            return lhs.firstName.compareTo( rhs.firstName );
        }
    }

    public static class PointComparator implements Comparator<Friend> {
        @Override
        public int compare( Friend lhs, Friend rhs ) {
            return lhs.points < rhs.points ? 1 : ( lhs.points == rhs.points ? 0 : -1 );
        }
    }

    @Override
    public String toString( ) {
        return gson.toJson( this );
    }

    public static Friend fromString( String friendJson ) {
        return gson.fromJson( friendJson, Friend.class );
    }

    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;
        return ( (Friend) o ).id.equals( id );
    }

    public Friend( ) {
    }

    public Friend( BigInteger id, String firstName, String lastName, String imageUrl ) {
        this.id = id;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.imageUrl = imageUrl;
    }

    public BigInteger getId( ) {
        return id;
    }

    public void setId( BigInteger id ) {
        this.id = id;
    }

    public String getFirstName( ) {
        return firstName;
    }

    public String getLastName( ) {
        return lastName;
    }

    public void setLastName( String lastName ) {
        this.lastName = lastName;
    }

    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }

    public String getName( ) {
        return firstName + " " + lastName;
    }

    public String getImageUrl( ) {
        return imageUrl;
    }

    public void setImageUrl( String imageUrl ) {
        this.imageUrl = imageUrl;
    }

    public int getPoints( ) {
        return points;
    }

    public void setPoints( int points ) {
        this.points = points;
    }
}
