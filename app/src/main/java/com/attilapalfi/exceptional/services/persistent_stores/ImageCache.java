package com.attilapalfi.exceptional.services.persistent_stores;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.annimon.stream.Stream;
import com.attilapalfi.exceptional.model.Friend;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by 212461305 on 2015.07.06..
 */
public class ImageCache implements Wipeable {
    private LruCache<BigInteger, Bitmap> imageWarehouse;
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
        imageWarehouse = new LruCache<BigInteger, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(BigInteger key, Bitmap value) {
                int bitmapByteCount = value.getRowBytes() * value.getHeight(); // The cache size will be measured in kilobytes rather than number of items.
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


    /**
     * Adds the friend and it's image to the ram and disk cache
     * @param friend who's picture is added
     * @param image the picture that is added
     */
    public void addImage(final Friend friend, final Bitmap image) {
        if (isNotInMemory(friend)) {
            imageWarehouse.put(friend.getId(), image);
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    String pictureFilePath = getFriendsImageFilePath(friend);
                    if (pictureFilePath != null) {
                        saveImageToFile(pictureFilePath);
                    }
                    return null;
                }

                private void saveImageToFile(String pictureFilePath) {
                    File pictureFile = new File(pictureFilePath);
                    try {
                        FileOutputStream fos = new FileOutputStream(pictureFile);
                        image.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                        fos.close();
                    } catch (IOException ioe) {
                        Log.d("IOException", ioe.getMessage());
                    }
                }
            }.execute();
        }
    }

    private boolean isNotInMemory(Friend friend) {
        return imageWarehouse != null && imageWarehouse.get(friend.getId()) == null;
    }


    private boolean imageFileExists(Friend friend) {
        String filePath = getFriendsImageFilePath(friend);
        File file = new File(filePath);
        return file.exists() && !file.isDirectory();
    }

    private String getFriendsImageFilePath(Friend friend){
        try {
            File mediaStorageDir = makeStorageDirectory();
            if (mediaStorageDir == null) return null;
            String mImageName = String.valueOf(friend.getId()) + ".jpg";
            return mediaStorageDir.getPath() + File.separator + mImageName;
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Nullable
    private File makeStorageDirectory() {
        File mediaStorageDir = new File(filePath);
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }
        return mediaStorageDir;
    }


    /**
     * Must be called from an async task
     * @param friend who's image
     * @return image of friend or null
     */
    public Bitmap getImageForFriend(Friend friend) {
        Bitmap bitmap = imageWarehouse.get(friend.getId()); // getting image from memory cache, if available
        if (bitmap == null) {
            if (imageFileExists(friend)) {
                bitmap = getFromDisk(friend); // getting from disk, if available
                if (bitmap == null) {
                    try {
                        bitmap = getBitmapFromInternet(friend);
                        addImage(friend, bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    imageWarehouse.put(friend.getId(), bitmap);
                }
            }
        }

        return bitmap;
    }

    private Bitmap getBitmapFromInternet(Friend friend) throws IOException {
        URL url = new URL(friend.getImageUrl());
        URLConnection connection = url.openConnection();
        connection.setUseCaches(true);
        InputStream inputStream = (InputStream) connection.getContent();
        return BitmapFactory.decodeStream(inputStream);
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
        deleteFromDisk(friend);
    }

    @Override
    public void wipe() {
        imageWarehouse.evictAll();
        if (!FriendsManager.getInstance().isInitialized()) {
            FriendsManager.getInstance().initialize(applicationContext);
        }
        Stream.of(FriendsManager.getInstance().getStoredFriends())
                .forEach(this::deleteFromDisk);
    }

    private void deleteFromDisk(Friend friend) {
        String filePath = getFriendsImageFilePath(friend);
        File imageFile = new File(filePath);
        if (imageFile.exists()) {
            imageFile.delete();
        }
    }
}
