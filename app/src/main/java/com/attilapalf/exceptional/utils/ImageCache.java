package com.attilapalf.exceptional.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.attilapalf.exceptional.model.Friend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by 212461305 on 2015.07.06..
 */
public class ImageCache {
    private LruCache<Long, Bitmap> imageWarehouse;
    private Context applicationContext;
    private String filePath;

    private static ImageCache instance;

    public static ImageCache getInstance() {
        if (instance == null) {
            instance = new ImageCache();
        }

        return instance;
    }


    private ImageCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        imageWarehouse = new LruCache<Long, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Long key, Bitmap value) {
                // The cache size will be measured in kilobytes rather than number of items.
                int bitmapByteCount = value.getRowBytes() * value.getHeight();

                return bitmapByteCount / 1024;
            }
        };
    }


    public void initialize(Context context) {
        applicationContext = context;

        try {
            filePath =
                    Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                            !Environment.isExternalStorageRemovable() ? applicationContext.getExternalCacheDir().getPath() :
                            applicationContext.getCacheDir().getPath();

        } catch (NullPointerException npe) {
            Log.d("NullPointerException", npe.getMessage());
        }
    }


    public void addImage(final Friend friend, final Bitmap image) {
        if (imageWarehouse != null && imageWarehouse.get(friend.getId()) == null) {
            imageWarehouse.put(friend.getId(), image);
            // TODO: add to the disk storage
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    String pictureFilePath = getFriendsImageFilePath(friend);
                    if (pictureFilePath != null) {
                        File pictureFile = new File(pictureFilePath);
                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            image.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                            fos.close();

                        } catch (IOException ioe) {
                            Log.d("IOException", ioe.getMessage());
                        }
                    }

                    return null;
                }
            }.execute();
        }
    }


    private boolean imageFileExists(Friend friend) {
        String filePath = getFriendsImageFilePath(friend);
        File file = new File(filePath);

        return file.exists() && !file.isDirectory();
    }


    /** Create a File for saving an image or video */
    private String getFriendsImageFilePath(Friend friend){
        try {
            File mediaStorageDir = new File(filePath);

            // This location works best if you want the created images to be shared
            // between applications and persist after your app has been uninstalled.

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()){
                if (!mediaStorageDir.mkdirs()){
                    return null;
                }
            }

            String mImageName = String.valueOf(friend.getId()) + ".jpg";
            return mediaStorageDir.getPath() + File.separator + mImageName;

        } catch (NullPointerException e) {
            return null;
        }
    }



    /**
     * Must be called from an async task
     * @param friend who's image
     * @return image of friend or null
     */
    public Bitmap getImage(Friend friend) {

        Bitmap bitmap = imageWarehouse.get(friend.getId());

        if (bitmap == null) {
            // TODO: check the disk storage for the image
            if (imageFileExists(friend)) {
                bitmap = getFromDisk(friend);
                if (bitmap != null) {
                    imageWarehouse.put(friend.getId(), bitmap);
                }
            }
        }

        return bitmap;
    }


    private Bitmap getFromDisk(Friend friend) {
        String filePath = getFriendsImageFilePath(friend);
        if (filePath != null) {
            File file = new File(filePath);
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }

        return null;
    }





    public void removeImage(Friend friend) {
        imageWarehouse.remove(friend.getId());

        // TODO: remove from disk storage
    }


//    public void loadBitmap(Friend friend, ImageView imageView) {
//        final Bitmap bitmap = getImage(friend);
//
//        if (bitmap != null) {
//            imageView.setImageBitmap(bitmap);
//        } else {
//            new BitmapWorkerTask(friend, imageView);
//        }
//    }
//
//
//    private static class BitmapWorkerTask extends AsyncTask<Void, Void, Void> {
//
//        private Friend friend;
//        private ImageView imageView;
//        private Bitmap bitmap;
//
//        public BitmapWorkerTask(Friend friend, ImageView imageView) {
//            this.friend = friend;
//            this.imageView = imageView;
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            try {
//                URL url = new URL(friend.getImageUrl());
//                URLConnection connection = url.openConnection();
//                InputStream inputStream = (InputStream) connection.getContent();
//                bitmap = BitmapFactory.decodeStream(inputStream);
//
//                ImageCache.getInstance().addImage(friend, bitmap);
//
//                return null;
//
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//                return null;
//            } catch (IOException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }
//
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            imageView.setImageBitmap(bitmap);
//        }
//    }

}
