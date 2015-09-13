package com.attilapalfi.exceptional.model;

import java.lang.ref.WeakReference;
import java.util.Comparator;

import android.graphics.Bitmap;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by palfi on 2015-09-13.
 */
public class Yourself extends RealmObject {
    @Index
    @PrimaryKey
    private String id;
    private String firstName;
    private String lastName;
    private String imageUrl;
    @Index
    private int points = 100;
    @Ignore
    private transient WeakReference<Bitmap> imageWeakReference;

    public static class NameComparator implements Comparator<Yourself> {
        @Override
        public int compare( Yourself lhs, Yourself rhs ) {
            return lhs.firstName.compareTo( rhs.firstName );
        }
    }

    public static class PointComparator implements Comparator<Yourself> {
        @Override
        public int compare( Yourself lhs, Yourself rhs ) {
            return lhs.points < rhs.points ? 1 : ( lhs.points == rhs.points ? 0 : -1 );
        }
    }

    public Yourself( ) {
        imageWeakReference = new WeakReference<>( null );
    }

    public Yourself( String id, String firstName, String lastName, String imageUrl ) {
        this.id = id;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.imageUrl = imageUrl;
        this.imageWeakReference = new WeakReference<>( null );
    }

    public Yourself( String id, String firstName, String lastName, String imageUrl, Bitmap image ) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageUrl = imageUrl;
        this.imageWeakReference = new WeakReference<>( image );
    }

    public String getId( ) {
        return id;
    }

    public void setId( String id ) {
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

    public WeakReference<Bitmap> getImageWeakReference( ) {
        return imageWeakReference;
    }

    public void setImageWeakReference( WeakReference<Bitmap> imageWeakReference ) {
        this.imageWeakReference = imageWeakReference;
    }
}
