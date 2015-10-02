package com.attilapalfi.exceptional.persistence;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.inject.Inject;

import android.content.Context;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.ExceptionChangeListener;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.model.ExceptionFactory;
import com.attilapalfi.exceptional.rest.messages.ExceptionInstanceWrapper;
import io.paperdb.Book;
import io.paperdb.Paper;
import java8.util.stream.Collectors;

import static java8.util.stream.StreamSupport.stream;


/**
 * Created by Attila on 2015-06-08.
 */
public class ExceptionInstanceStore {
    public final int STORE_SIZE = 1000;
    private static final String INSTANCE_DATABASE = "INSTANCE_DATABASE";
    private static final String INSTANCE_IDs = "INSTANCE_IDs";
    private static final Exception EMPTY_EXCEPTION = new Exception();

    @Inject
    Context context;
    @Inject
    ExceptionTypeStore exceptionTypeStore;
    @Inject
    ExceptionFactory exceptionFactory;
    private Book database;
    private Set<ExceptionChangeListener> exceptionChangeListeners = new HashSet<>();
    private final CopyOnWriteArrayList<Exception> storedExceptions = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<BigInteger> idList = new CopyOnWriteArrayList<>();
    private Geocoder geocoder;
    private Handler handler;

    public ExceptionInstanceStore( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        database = Paper.book( INSTANCE_DATABASE );
        handler = new Handler( Looper.getMainLooper() );
        geocoder = new Geocoder( context, Locale.getDefault() );
        loadExceptionInstances();
    }


    private void loadExceptionInstances( ) {
        idList.addAll( database.read( INSTANCE_IDs, new LinkedList<>() ) );

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground( Void... params ) {
                List<Exception> tempList = new LinkedList<>();
                stream( idList ).forEach( id -> {
                    Exception e = database.read( id.toString(), EMPTY_EXCEPTION );
                    e.setExceptionType( exceptionTypeStore.findById( e.getExceptionTypeId() ) );
                    int index = Collections.binarySearch( storedExceptions, e );
                    if ( index < 0 ) {
                        index = -index - 1;
                        tempList.add( index, e );
                    }
                } );
                storedExceptions.addAll( tempList );
                return null;
            }

            @Override
            protected void onPostExecute( Void aVoid ) {
                notifyListeners();
            }

        }.execute();
    }

    public void wipe( ) {
        storedExceptions.clear();
        database.destroy();
        stream( exceptionChangeListeners ).forEach( ExceptionChangeListener::onExceptionsChanged );
    }

    public void addExceptionAsync( final Exception exception ) {
        if ( !storedExceptions.contains( exception ) ) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground( Void... params ) {
                    saveToStore( exception );
                    return null;
                }

                @Override
                protected void onPostExecute( Void aVoid ) {
                    notifyListeners();
                }

            }.execute();
        }
    }

    public void saveExceptionList( List<ExceptionInstanceWrapper> wrapperList ) {
        if ( !wrapperList.isEmpty() ) {
            saveListToStore( wrapperList );
            handler.post( this::notifyListeners );
        }
    }

    public void saveExceptionListAsync( List<ExceptionInstanceWrapper> wrapperList ) {
        if ( !wrapperList.isEmpty() ) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground( Void... params ) {
                    saveListToStore( wrapperList );
                    return null;
                }

                @Override
                protected void onPostExecute( Void aVoid ) {
                    notifyListeners();
                }

            }.execute();
        }
    }

    private void saveToStore( Exception e ) {
        if ( !storedExceptions.contains( e ) ) {
            saveWithCity( e );
            database.write( INSTANCE_IDs, idList );
        }
    }

    private void saveListToStore( List<ExceptionInstanceWrapper> wrapperList ) {
        List<Exception> toBeStored = wrapperListToExceptions( wrapperList );
        storeEachIfNotContained( toBeStored );
    }

    private List<Exception> wrapperListToExceptions( List<ExceptionInstanceWrapper> wrappers ) {
        return stream( wrappers ).map( exceptionFactory::createFromWrapper ).collect( Collectors.toList() );
    }

    private void storeEachIfNotContained( List<Exception> toBeStored ) {
        stream( toBeStored ).forEach( e -> {
            if ( !storedExceptions.contains( e ) ) {
                saveWithCity( e );
            }
        } );
    }

    private void saveWithCity( Exception e ) {
        setCityForException( e );
        addToListInOrder( storedExceptions, e );
    }

    private void addToListInOrder( List<Exception> list, Exception e ) {
        int index = Collections.binarySearch( list, e );
        if ( index < 0 ) {
            index = -index - 1;
            if ( list.size() >= STORE_SIZE ) {
                addNewOrKeepOld( list, e, index );
            } else {
                addTheNewOne( list, e, index );
            }
        }
    }

    private void addNewOrKeepOld( List<Exception> list, Exception e, int index ) {
        Exception removeCandidate = list.get( list.size() - 1 );
        if ( removeCandidate.compareTo( e ) > 0 ) {
            removeTheCandidate( list );
            addTheNewOne( list, e, index );
        }
    }

    private void removeTheCandidate( List<Exception> list ) {
        Exception removed = list.remove( list.size() - 1 );
        idList.remove( idList.size() - 1 );
        database.delete( removed.getInstanceId().toString() );
    }

    private void addTheNewOne( List<Exception> list, Exception e, int index ) {
        list.add( index, e );
        idList.add( index, e.getInstanceId() );
        database.write( e.getInstanceId().toString(), e );
        database.write( INSTANCE_IDs, idList );
    }

    private void setCityForException( Exception e ) {
        try {
            e.setCity( geocoder.getFromLocation( e.getLatitude(), e.getLongitude(), 1 ).get( 0 ).getLocality() );
        } catch ( java.lang.Exception exception ) {
            e.setCity( context.getString( R.string.unknown ) );
            exception.printStackTrace();
        }
    }

    private void notifyListeners( ) {
        if ( Looper.myLooper() == Looper.getMainLooper() ) {
            stream( exceptionChangeListeners ).forEach( ExceptionChangeListener::onExceptionsChanged );
        }
    }

    public List<Exception> getExceptionList( ) {
        return storedExceptions;
    }

    public boolean addExceptionChangeListener( ExceptionChangeListener listener ) {
        return exceptionChangeListeners.add( listener );
    }

    public boolean removeExceptionChangeListener( ExceptionChangeListener listener ) {
        return exceptionChangeListeners.remove( listener );
    }

    public List<BigInteger> getKnownIds( ) {
        return idList;
    }
}
