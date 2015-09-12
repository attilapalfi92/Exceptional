package com.attilapalfi.exceptional.services.persistent_stores;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import javax.inject.Inject;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Looper;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.ExceptionChangeListener;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.services.ExceptionFactory;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionInstanceWrapper;
import java8.util.stream.Collectors;

import static java8.util.stream.StreamSupport.stream;


/**
 * Created by Attila on 2015-06-08.
 */
public class ExceptionInstanceManager {
    public final int STORE_SIZE = Integer.MAX_VALUE;

    @Inject Context context;
    @Inject ExceptionTypeManager exceptionTypeManager;
    @Inject ExceptionFactory exceptionFactory;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String PREFS_NAME;
    private Set<ExceptionChangeListener> exceptionChangeListeners = new HashSet<>();
    private List<Exception> storedExceptions = Collections.synchronizedList( new LinkedList<>() );
    private Geocoder geocoder;
    private List<BigInteger> knownIdsOnStart = new LinkedList<>();

    public ExceptionInstanceManager( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        initPreferences( context );
        geocoder = new Geocoder( context, Locale.getDefault() );
        loadExceptionInstances();
    }

    private void initPreferences( Context context ) {
        PREFS_NAME = context.getString( R.string.exception_preferences );
        sharedPreferences = context.getSharedPreferences( PREFS_NAME, Context.MODE_PRIVATE );
        editor = sharedPreferences.edit();
        editor.apply();
    }

    private void loadExceptionInstances( ) {
        Map<String, ?> store = sharedPreferences.getAll();
        Set<String> instanceIds = store.keySet();
        knownIdsOnStart.addAll( stream( instanceIds ).map( BigInteger::new ).collect( Collectors.toList() ) );
        new AsyncExceptionLoader( storedExceptions, store ).execute();
    }

    public void wipe( ) {
        storedExceptions.clear();
        editor.clear().apply();
        stream( exceptionChangeListeners ).forEach( ExceptionChangeListener::onExceptionsChanged );
    }

    public boolean isInitialized( ) {
        return sharedPreferences != null;
    }


    public int exceptionCount( ) {
        return storedExceptions.size();
    }

    public BigInteger getLastKnownId( ) {
        if ( storedExceptions.isEmpty() ) {
            return new BigInteger( "0" );
        }

        return storedExceptions.get( 0 ).getInstanceId();
    }

    public void addExceptionAsync( final Exception e ) {
        if ( !storedExceptions.contains( e ) ) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground( Void... params ) {
                    if ( !storedExceptions.contains( e ) ) {
                        setCityForException();
                        addToMemoryStore();
                        editor.putString( e.getInstanceId().toString(), e.toString() );
                        editor.apply();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute( Void aVoid ) {
                    notifyListeners();
                }

                private void setCityForException( ) {
                    try {
                        e.setCity( geocoder.getFromLocation( e.getLatitude(), e.getLongitude(), 1 ).get( 0 ).getLocality() );
                    } catch ( IOException ioe ) {
                        ioe.printStackTrace();
                    }
                }

                private void addToMemoryStore( ) {
                    if ( storedExceptions.size() >= STORE_SIZE ) {
                        removeLast();
                    }
                    storedExceptions.add( 0, e );
                }
            }.execute();
        }
    }


    public void saveExceptionListAsync( List<ExceptionInstanceWrapper> wrapperList ) {
        if ( !wrapperList.isEmpty() ) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground( Void... params ) {
                    stream( wrapperList ).forEach( wrapper -> addException( exceptionFactory.createFromWrapper( wrapper ) ) );
                    Collections.sort( storedExceptions, new Exception.DateComparator() );
                    return null;
                }

                @Override
                protected void onPostExecute( Void aVoid ) {
                    notifyListeners();
                }

                private void addException( Exception e ) {
                    if ( !storedExceptions.contains( e ) ) {
                        setCityForException( e );
                        addToMemoryStore( e );
                        editor.putString( e.getInstanceId().toString(), e.toString() );
                        editor.apply();
                    }
                }

                private void setCityForException( Exception e ) {
                    try {
                        e.setCity( geocoder.getFromLocation( e.getLatitude(), e.getLongitude(), 1 ).get( 0 ).getLocality() );
                    } catch ( IOException ioe ) {
                        ioe.printStackTrace();
                    }
                }

                private void addToMemoryStore( Exception e ) {
                    if ( storedExceptions.size() >= STORE_SIZE ) {
                        removeLast();
                    }
                    storedExceptions.add( 0, e );
                }
            }.execute();
        }
    }


    private void removeLast( ) {
        Exception removed = storedExceptions.remove( storedExceptions.size() - 1 );
        editor.remove( removed.getInstanceId().toString() );
    }


    public void removeException( Exception e ) {
        editor.remove( e.getInstanceId().toString() );
        editor.apply();
        for ( int i = 0; i < storedExceptions.size(); i++ ) {
            if ( storedExceptions.get( i ).getInstanceId().equals( e.getInstanceId() ) ) {
                storedExceptions.remove( i );
                return;
            }
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
        return knownIdsOnStart;
    }

    private class AsyncExceptionLoader extends AsyncTask<Void, Void, Void> {
        private List<Exception> resultList;
        private Map<String, ?> store;

        public AsyncExceptionLoader( List<Exception> resultList, Map<String, ?> store ) {
            this.resultList = resultList;
            this.store = store;
        }

        @Override
        protected Void doInBackground( Void... params ) {
            Set<String> instanceIds = store.keySet();
            for ( String s : instanceIds ) {
                parseExceptionToMemoryList( store, s );
            }
            Collections.sort( resultList, new Exception.DateComparator() );
            return null;
        }

        private void parseExceptionToMemoryList( Map<String, ?> store, String instanceId ) {
            String excJson = (String) store.get( instanceId );
            Exception e = Exception.fromString( excJson );
            e.setExceptionType( exceptionTypeManager.findById( e.getExceptionTypeId() ) );
            if ( !resultList.contains( e ) ) {
                resultList.add( resultList.size(), e );
            }
        }
    }
}
