package com.attilapalfi.exceptional.services.persistent_stores;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
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
import com.attilapalfi.exceptional.model.Yourself;
import io.realm.Realm;
import io.realm.RealmResults;

import static java8.util.stream.StreamSupport.stream;

/**
 * Created by 212461305 on 2015.07.06..
 */
public class ImageCache {
    private LruCache<String, Bitmap> imageWarehouse;
    private String filePath;
    @Inject Context context;

    public ImageCache( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        try {
            filePath =
                    Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() ) ||
                            !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                            context.getCacheDir().getPath();

            final int maxMemory = (int) ( Runtime.getRuntime().maxMemory() / 1024 );
            final int cacheSize = maxMemory / 8;
            imageWarehouse = new LruCache<String, Bitmap>( cacheSize ) {
                @Override
                protected int sizeOf( String key, Bitmap value ) {
                    int bitmapByteCount = value.getRowBytes() * value.getHeight(); // The cache size will be measured in kilobytes rather than number of items.
                    return bitmapByteCount / 1024;
                }
            };

        } catch ( NullPointerException npe ) {
            Log.d( "NullPointerException", npe.getMessage() );
        }
    }

    /**
     * Must be called from an async task
     * Adds the friend and it's image to the ram and disk cache
     *
     * @param friend who's picture is added
     * @param image  the picture that is added
     */
    public void saveImageToStore( final Friend friend, final Bitmap image ) {
        if ( isNotInMemory( friend ) ) {
            imageWarehouse.put( friend.getId(), image );
            String pictureFilePath = getFriendsImageFilePath( friend );
            if ( pictureFilePath != null ) {
                File pictureFile = new File( pictureFilePath );
                try {
                    FileOutputStream fos = new FileOutputStream( pictureFile );
                    image.compress( Bitmap.CompressFormat.JPEG, 95, fos );
                    fos.close();
                } catch ( IOException ioe ) {
                    Log.d( "IOException", ioe.getMessage() );
                }
            }
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


    /**
     * Must be called from an async task
     *
     * @param friend who's image
     * @return image of friend or null
     */
    public Bitmap getImageForFriend( Friend friend ) {
        Bitmap bitmap = imageWarehouse.get( friend.getId() ); // getting image from memory cache, if available
        if ( bitmap == null ) {
            if ( imageFileExists( friend ) ) {
                bitmap = getFromDisk( friend ); // getting from disk, if available
                if ( bitmap == null ) {
                    try {
                        bitmap = getBitmapFromInternet( friend );
                        saveImageToStore( friend, bitmap );
                    } catch ( IOException e ) {
                        e.printStackTrace();
                    }
                } else {
                    imageWarehouse.put( friend.getId(), bitmap );
                }
            }
        }

        return bitmap;
    }

    private Bitmap getBitmapFromInternet( Friend friend ) throws IOException {
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

    public void removeImage( Friend friend ) {
        imageWarehouse.remove( friend.getId() );
        deleteFromDisk( friend );
    }

    // TODO: implement
    public void wipe( ) {
        imageWarehouse.evictAll();
        //stream( friendList ).forEach( this::deleteFromDisk );
    }

    private void deleteFromDisk( Friend friend ) {
        String filePath = getFriendsImageFilePath( friend );
        File imageFile = new File( filePath );
        if ( imageFile.exists() ) {
            imageFile.delete();
        }
    }

    public void setImageToView( Yourself yourself, final ImageView view ) {
        /*if ( yourself.getImageWeakReference().get() == null ) {
            new AsyncImageLoader( yourself, view, this ).execute();

        } else {
            view.setImageBitmap( yourself.getImageWeakReference().get() );
        }*/
    }

    public void setImageToView( Friend friend, final ImageView view ) {
        if ( friend.getImageWeakReference().get() == null ) {
            new AsyncImageLoader( friend.getId(), view ).execute();

        } else {
            view.setImageBitmap( friend.getImageWeakReference().get() );
        }
    }

    public void downloadAllFriendsImage( ) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground( Void... params ) {
                try ( Realm realm = Realm.getInstance( context ) ) {
                    RealmResults<Friend> allFriends = realm.allObjects( Friend.class );
                    stream( allFriends ).forEach( friend -> getImageForFriend( friend ) );
                }

                return null;
            }

            @Override
            protected void onPostExecute( Bitmap bitmap ) {

            }
        }.execute();
    }

    private class AsyncImageLoader extends AsyncTask<Void, Void, Bitmap> {
        private String id;
        private ImageView imageView;

        public AsyncImageLoader( String id, ImageView imageView ) {
            this.id = id;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground( Void... params ) {
            try ( Realm realm = Realm.getInstance( context ) ) {
                realm.beginTransaction();
                Friend friend = realm.where( Friend.class )
                        .equalTo( "id", id )
                        .findFirst();
                Bitmap bitmap = getImageForFriend( friend );
                realm.commitTransaction();
                return bitmap;
            }
        }

        @Override
        protected void onPostExecute( Bitmap bitmap ) {
            try ( Realm realm = Realm.getInstance( context ) ) {
                realm.beginTransaction();
                Friend friend = realm.where( Friend.class )
                        .equalTo( "id", id )
                        .findFirst();

                friend.setImageWeakReference( new WeakReference<>( bitmap ) );
                realm.commitTransaction();
                imageView.setImageBitmap( bitmap );
                imageView = null;
                friend = null;
            }
        }
    }
}
