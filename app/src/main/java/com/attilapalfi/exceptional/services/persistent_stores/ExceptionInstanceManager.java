package com.attilapalfi.exceptional.services.persistent_stores;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Looper;
import com.annimon.stream.Collectors;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.interfaces.ExceptionChangeListener;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.services.rest.messages.ExceptionInstanceWrapper;

import static com.annimon.stream.Stream.of;

/**
 * Created by Attila on 2015-06-08.
 */
public class ExceptionInstanceManager {
    public static final int STORE_SIZE = Integer.MAX_VALUE;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    private static String PREFS_NAME;
    private static Set<ExceptionChangeListener> exceptionChangeListeners = new HashSet<>();
    private static List<Exception> storedExceptions = Collections.synchronizedList( new LinkedList<>() );
    private static Geocoder geocoder;
    private static List<BigInteger> knownIdsOnStart = new LinkedList<>();

    private ExceptionInstanceManager( ) {
    }

    public static void initialize( Context context ) {
        if ( !ExceptionTypeManager.isInitialized() ) {
            ExceptionTypeManager.initialize( context );
        }
        initPreferences( context );
        geocoder = new Geocoder( context, Locale.getDefault() );
        loadExceptionInstances();
    }

    private static void initPreferences( Context context ) {
        PREFS_NAME = context.getString( R.string.exception_preferences );
        sharedPreferences = context.getSharedPreferences( PREFS_NAME, Context.MODE_PRIVATE );
        editor = sharedPreferences.edit();
        editor.apply();
    }

    private static void loadExceptionInstances( ) {
        Map<String, ?> store = sharedPreferences.getAll();
        Set<String> instanceIds = store.keySet();
        knownIdsOnStart.addAll( of( instanceIds ).map( BigInteger::new ).collect( Collectors.toList() ) );
        new AsyncExceptionLoader( storedExceptions, store ).execute();
    }

    public static void wipe( ) {
        storedExceptions.clear();
        editor.clear().apply();
        of( exceptionChangeListeners ).forEach( ExceptionChangeListener::onExceptionsChanged );
    }

    public static boolean isInitialized( ) {
        return sharedPreferences != null;
    }


    public static int exceptionCount( ) {
        return storedExceptions.size();
    }

    public static BigInteger getLastKnownId( ) {
        if ( storedExceptions.isEmpty() ) {
            return new BigInteger( "0" );
        }

        return storedExceptions.get( 0 ).getInstanceId();
    }

    public static void addExceptionAsync( final Exception e ) {
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
                protected void onPostExecute( Void aVoid) {
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


    public static void saveExceptionListAsync( List<ExceptionInstanceWrapper> wrapperList ) {
        if ( !wrapperList.isEmpty() ) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground( Void... params ) {
                    of( wrapperList ).forEach( wrapper -> addException( new Exception( wrapper ) ) );
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


    private static void removeLast( ) {
        Exception removed = storedExceptions.remove( storedExceptions.size() - 1 );
        editor.remove( removed.getInstanceId().toString() );
    }


    public static void removeException( Exception e ) {
        editor.remove( e.getInstanceId().toString() );
        editor.apply();
        for ( int i = 0; i < storedExceptions.size(); i++ ) {
            if ( storedExceptions.get( i ).getInstanceId().equals( e.getInstanceId() ) ) {
                storedExceptions.remove( i );
                return;
            }
        }
    }

    private static void notifyListeners( ) {
        if ( Looper.myLooper() == Looper.getMainLooper() ) {
            of( exceptionChangeListeners ).forEach( ExceptionChangeListener::onExceptionsChanged );
        }
    }

    public static List<Exception> getExceptionList( ) {
        return storedExceptions;
    }

    public static boolean addExceptionChangeListener( ExceptionChangeListener listener ) {
        return exceptionChangeListeners.add( listener );
    }

    public static boolean removeExceptionChangeListener( ExceptionChangeListener listener ) {
        return exceptionChangeListeners.remove( listener );
    }

    public static List<BigInteger> getKnownIds( ) {
        return knownIdsOnStart;
    }

    private static class AsyncExceptionLoader extends AsyncTask<Void, Void, Void> {
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
            e.setExceptionType( ExceptionTypeManager.findById( e.getExceptionTypeId() ) );
            if ( !resultList.contains( e ) ) {
                resultList.add( resultList.size(), e );
            }
        }
    }
}
