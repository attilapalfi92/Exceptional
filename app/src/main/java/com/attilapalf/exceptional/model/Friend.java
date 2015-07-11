package com.attilapalf.exceptional.model;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.attilapalf.exceptional.services.ImageCache;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.Comparator;

/**
 * Created by Attila on 2015-06-12.
 */
public class Friend {
    private long id;
    private String name;
    private String imageUrl;
    private transient WeakReference<Bitmap> imageWeakReference;

    private static Gson gson = new Gson();

    public static class NameComparator implements Comparator<Friend> {
        @Override
        public int compare(Friend lhs, Friend rhs) {
            return lhs.name.compareTo(rhs.name);
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
        return ((Friend)o).id == id;
    }

    public Friend() {
        imageWeakReference = new WeakReference<>(null);
    }

    public Friend(long id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.imageWeakReference = new WeakReference<>(null);
    }

    public Friend(long id, String name, String imageUrl, Bitmap image) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.imageWeakReference = new WeakReference<>(image);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
