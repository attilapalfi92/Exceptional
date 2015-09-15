package com.attilapalfi.exceptional.services.persistent_stores;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.inject.Inject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Friend;

import static java8.util.stream.StreamSupport.stream;

/**
 * Created by 212461305 on 2015.07.06..
 */
public class ImageCache {
    private LruCache<BigInteger, Bitmap> imageWarehouse;
    private String filePath;
    @Inject
    Context context;

    public ImageCache( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        try {
            filePath =
                    Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() ) ||
                            !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                            context.getCacheDir().getPath();

            final int maxMemory = (int) ( Runtime.getRuntime().maxMemory() / 1024 );
            final int cacheSize = maxMemory / 8;
            imageWarehouse = new LruCache<BigInteger, Bitmap>( cacheSize ) {
                @Override
                protected int sizeOf( BigInteger key, Bitmap value ) {
                    int bitmapByteCount = value.getRowBytes() * value.getHeight(); // The cache size will be measured in kilobytes rather than number of items.
                    return bitmapByteCount / 1024;
                }
            };

        } catch ( NullPointerException npe ) {
            Log.d( "NullPointerException", npe.getMessage() );
        }
    }

    public void setImageToView( Friend friend, final ImageView view ) {
        Bitmap bitmap = imageWarehouse.get( friend.getId() );
        if ( bitmap != null ) {
            view.setImageBitmap( bitmap );
        } else {
            new AsyncImageLoader( friend, view ).execute();
        }
    }

    public void updateImageAsync( Friend newFriendState, Friend oldFriendState ) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground( Void... params ) {
                try {
                    removeImage( oldFriendState );
                    Bitmap bitmap = getFromInternet( newFriendState );
                    saveBitmapToDiskAndMem( newFriendState, bitmap );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    private class AsyncImageLoader extends AsyncTask<Void, Void, Bitmap> {
        private Friend friend;
        private ImageView imageView;

        public AsyncImageLoader( Friend friend, ImageView imageView ) {
            this.friend = friend;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground( Void... params ) {
            return getImageForFriend( friend );
        }

        @Override
        protected void onPostExecute( Bitmap bitmap ) {
            imageView.setImageBitmap( bitmap );
            imageView = null;
            friend = null;
        }

        private Bitmap getImageForFriend( Friend friend ) {
            Bitmap bitmap = imageWarehouse.get( friend.getId() ); // getting image from memory cache, if available
            if ( bitmap == null ) {
                if ( imageFileExists( friend ) ) {
                    bitmap = getFromDisk( friend ); // getting from disk, if available
                    imageWarehouse.put( friend.getId(), bitmap );
                } else {
                    try {
                        bitmap = getFromInternet( friend );
                        saveBitmapToDiskAndMem( friend, bitmap );
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                }
            }
            return bitmap;
        }
    }

    private void saveBitmapToDiskAndMem( final Friend friend, final Bitmap image ) {
        imageWarehouse.put( friend.getId(), image );
        String pictureFilePath = getFriendsImageFilePath( friend );
        if ( pictureFilePath != null ) {
            saveImageToFile( image, pictureFilePath );
        }
    }

    private void saveImageToFile( Bitmap image, String pictureFilePath ) {
        File pictureFile = new File( pictureFilePath );
        try {
            FileOutputStream fos = new FileOutputStream( pictureFile );
            image.compress( Bitmap.CompressFormat.JPEG, 95, fos );
            fos.close();
        } catch ( IOException ioe ) {
            Log.d( "IOException", ioe.getMessage() );
        }
    }

    private boolean isNotInMemory( Friend friend ) {
        return imageWarehouse != null && imageWarehouse.get( friend.getId() ) == null;
    }


    private boolean imageFileExists( Friend friend ) {
        String filePath = getFriendsImageFilePath( friend );
        File file = new File( filePath );
        return file.exists() && !file.isDirectory();
    }

    private String getFriendsImageFilePath( Friend friend ) {
        try {
            File mediaStorageDir = makeStorageDirectory();
            if ( mediaStorageDir == null ) return null;
            String mImageName = String.valueOf( friend.getId() ) + ".jpg";
            return mediaStorageDir.getPath() + File.separator + mImageName;
        } catch ( NullPointerException e ) {
            return null;
        }
    }

    @Nullable
    private File makeStorageDirectory( ) {
        File mediaStorageDir = new File( filePath );
        if ( !mediaStorageDir.exists() ) {
            if ( !mediaStorageDir.mkdirs() ) {
                return null;
            }
        }
        return mediaStorageDir;
    }

    private Bitmap getFromInternet( Friend friend ) throws IOException {
        URL url = new URL( friend.getImageUrl() );
        URLConnection connection = url.openConnection();
        connection.setUseCaches( true );
        InputStream inputStream = (InputStream) connection.getContent();
        return BitmapFactory.decodeStream( inputStream );
    }

    private Bitmap getFromDisk( Friend friend ) {
        String filePath = getFriendsImageFilePath( friend );
        if ( filePath != null ) {
            File file = new File( filePath );
            return BitmapFactory.decodeFile( file.getAbsolutePath() );
        }

        return null;
    }

    private void removeImage( Friend friend ) {
        imageWarehouse.remove( friend.getId() );
        deleteFromDisk( friend );
    }

    public void wipe( List<Friend> friendList ) {
        imageWarehouse.evictAll();
        stream( friendList ).forEach( this::deleteFromDisk );
    }

    private void deleteFromDisk( Friend friend ) {
        String filePath = getFriendsImageFilePath( friend );
        File imageFile = new File( filePath );
        if ( imageFile.exists() ) {
            imageFile.delete();
        }
    }
}
