package com.attilapalfi.exceptional.persistence;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import javax.inject.Inject;

import android.content.Context;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Looper;
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

    @Inject Context context;
    @Inject ExceptionTypeManager exceptionTypeManager;
    @Inject ExceptionFactory exceptionFactory;
    private Book database;
    private Set<ExceptionChangeListener> exceptionChangeListeners = new HashSet<>();
    private List<Exception> storedExceptions = Collections.synchronizedList( new LinkedList<>() );
    private List<BigInteger> idList;
    private Geocoder geocoder;

    public ExceptionInstanceStore( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        database = Paper.book( INSTANCE_DATABASE );
        geocoder = new Geocoder( context, Locale.getDefault() );
        loadExceptionInstances();
    }


    private void loadExceptionInstances( ) {
        idList = Collections.synchronizedList( database.read( INSTANCE_IDs, new LinkedList<>() ) );

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground( Void... params ) {
                stream( idList ).forEach( id -> {
                    Exception e = database.read( id.toString(), EMPTY_EXCEPTION );
                    e.setExceptionType( exceptionTypeManager.findById( e.getExceptionTypeId() ) );
                    int index = Collections.binarySearch( storedExceptions, e );
                    if ( index < 0 ) {
                        index = -index - 1;
                        storedExceptions.add( index, e );
                    }
                } );
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

    public void saveExceptionListAsync( List<ExceptionInstanceWrapper> wrapperList ) {
        if ( !wrapperList.isEmpty() ) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground( Void... params ) {
                    List<Exception> toBeStored = convertToExceptions();
                    saveExceptions( toBeStored );
                    database.write( INSTANCE_IDs, idList );
                    return null;
                }

                private List<Exception> convertToExceptions( ) {
                    return stream( wrapperList ).map( exceptionFactory::createFromWrapper ).collect( Collectors.toList() );
                }

                private void saveExceptions( List<Exception> toBeStored ) {
                    stream( toBeStored ).forEach( e -> {
                        if ( !storedExceptions.contains( e ) ) {
                            saveWithoutIdListWrite( e );
                        }
                    } );
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
            saveWithoutIdListWrite( e );
            database.write( INSTANCE_IDs, idList );
        }
    }

    private void saveWithoutIdListWrite( Exception e ) {
        setCityForException( e );
        addToListInOrder( e );
    }

    private void addToListInOrder( Exception e ) {
        int index = Collections.binarySearch( storedExceptions, e );
        if ( index < 0 ) {
            index = - index - 1;
            if ( storedExceptions.size() >= STORE_SIZE ) {
                addNewOrKeepOld( e, index );
            } else {
                addTheNewOne( e, index );
            }
        }
    }

    private void addNewOrKeepOld( Exception e, int index ) {
        Exception removeCandidate = storedExceptions.get( storedExceptions.size() - 1 );
        if ( removeCandidate.compareTo( e ) > 0 ) {
            removeTheCandidate();
            addTheNewOne( e, index );
        }
    }

    private void removeTheCandidate( ) {
        Exception removed = storedExceptions.remove( storedExceptions.size() - 1 );
        idList.remove( idList.size() - 1 );
        database.delete( removed.getInstanceId().toString() );
    }

    private void addTheNewOne( Exception e, int index ) {
        storedExceptions.add( index, e );
        idList.add( index, e.getInstanceId() );
        database.write( e.getInstanceId().toString(), e );
    }


    private void setCityForException( Exception e ) {
        try {
            e.setCity( geocoder.getFromLocation( e.getLatitude(), e.getLongitude(), 1 ).get( 0 ).getLocality() );
        } catch ( IOException ioe ) {
            ioe.printStackTrace();
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
