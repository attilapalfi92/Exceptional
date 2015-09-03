package com.attilapalfi.exceptional.model;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.attilapalfi.exceptional.services.persistent_stores.ImageCache;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.util.Comparator;

/**
 * Created by Attila on 2015-06-12.
 */
public class Friend {
    private BigInteger id;
    private String firstName;
    private String lastName;
    private String imageUrl;
    private int points = 100;
    private transient WeakReference<Bitmap> imageWeakReference;

    private static Gson gson = new Gson();

    public static class NameComparator implements Comparator<Friend> {
        @Override
        public int compare(Friend lhs, Friend rhs) {
            return lhs.firstName.compareTo(rhs.firstName);
        }
    }

    public static class PointComparator implements Comparator<Friend> {
        @Override
        public int compare(Friend lhs, Friend rhs) {
            return lhs.points < rhs.points ? 1 : (lhs.points == rhs.points ? 0 : -1);
        }
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    public static Friend fromString(String friendJson) {
        return gson.fromJson(friendJson, Friend.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return ((Friend)o).id.equals(id);
    }

    public Friend() {
        imageWeakReference = new WeakReference<>(null);
    }

    public Friend(BigInteger id, String firstName, String lastName, String imageUrl) {
        this.id = id;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.imageUrl = imageUrl;
        this.imageWeakReference = new WeakReference<>(null);
    }

    public Friend(BigInteger id, String firstName, String lastName, String imageUrl, Bitmap image) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.imageUrl = imageUrl;
        this.imageWeakReference = new WeakReference<>(image);
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setImageToView(final ImageView view) {
        if (imageWeakReference.get() == null) {
            new AsyncImageLoader(this, view).execute();

        } else {
            view.setImageBitmap(imageWeakReference.get());
        }
    }

    public void setImage(Bitmap image) {
        this.imageWeakReference = new WeakReference<>(image);
    }


    private static class AsyncImageLoader extends AsyncTask<Void, Void, Bitmap> {
        private Friend friend;
        private ImageView imageView;

        public AsyncImageLoader(Friend f, ImageView view) {
            friend = f;
            imageView = view;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return ImageCache.getInstance().getImage(friend);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            friend.setImage(bitmap);
            imageView.setImageBitmap(bitmap);
            imageView = null;
            friend = null;
        }
    }
}
