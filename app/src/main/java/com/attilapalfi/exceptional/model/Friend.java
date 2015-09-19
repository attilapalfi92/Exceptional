package com.attilapalfi.exceptional.model;

import java.math.BigInteger;
import java.util.Comparator;

import com.google.gson.Gson;

/**
 * Created by Attila on 2015-06-12.
 */
public class Friend implements Comparable<Friend> {
    private BigInteger id;
    private String firstName;
    private String lastName;
    private String imageUrl;
    private int points = 100;

    @Override
    public int compareTo( Friend another ) {
        return Integer.valueOf( another.points ).compareTo( points );
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
