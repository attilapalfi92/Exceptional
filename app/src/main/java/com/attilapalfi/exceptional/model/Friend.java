package com.attilapalfi.exceptional.model;

import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.Comparator;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;
import com.attilapalfi.exceptional.services.persistent_stores.ImageCache;
import com.google.gson.Gson;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Attila on 2015-06-12.
 */
public class Friend extends RealmObject {
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

    public Friend( ) {
        imageWeakReference = new WeakReference<>( null );
    }

    public Friend( String id, String firstName, String lastName, String imageUrl ) {
        this.id = id;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.imageUrl = imageUrl;
        this.imageWeakReference = new WeakReference<>( null );
    }

    public Friend( String id, String firstName, String lastName, String imageUrl, Bitmap image ) {
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
