package com.attilapalfi.exceptional.services.persistent_stores;

import java.util.*;

import javax.inject.Inject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.VotedTypeListener;
import com.attilapalfi.exceptional.model.ExceptionType;

import static java8.util.stream.StreamSupport.stream;

/**
 * Created by Attila on 2015-06-09.
 */
public class ExceptionTypeManager {
    private final String PREFS_NAME = "exception_types";
    private final String MIN_ID = "minId";
    private final String MAX_ID = "maxId";
    private final String HAS_DATA = "hasData";

    @Inject Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Map<String, List<ExceptionType>> exceptionTypeStore;
    private List<ExceptionType> votedExceptionTypeList;
    private Set<VotedTypeListener> votedTypeListeners = new HashSet<>();

    public ExceptionTypeManager( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        initPreferences( context );
        exceptionTypeStore = new HashMap<>();
        if ( sharedPreferences.getBoolean( HAS_DATA, false ) ) {
            initExceptionTypeStore();
            sortExceptionStore();
        }
    }

    public void addExceptionTypes( List<ExceptionType> exceptionTypes ) {
        loadExceptionTypeStore( exceptionTypes );
        editor.putBoolean( HAS_DATA, true );
        editor.apply();
    }

    private void loadExceptionTypeStore( List<ExceptionType> exceptionTypes ) {
        int maxId = 0;
        for ( ExceptionType exception : exceptionTypes ) {
            if ( !exceptionTypeStore.containsKey( exception.getType() ) ) {
                exceptionTypeStore.put( exception.getType(), new ArrayList<>() );
            }
            exceptionTypeStore.get( exception.getType() ).add( exception );
            editor.putString( Integer.toString( exception.getId() ), exception.toString() );
            maxId = exception.getId() > maxId ? exception.getId() : maxId;
        }
        editor.putInt( MAX_ID, maxId );
        editor.apply();
        sortExceptionStore();
    }

    public void setVotedExceptionTypes( List<ExceptionType> exceptionTypes ) {
        votedExceptionTypeList = Collections.synchronizedList( exceptionTypes );
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ( (Runnable) ( ) -> sortVotedExceptionList() ).run();
            notifyVotedTypeListeners();

        } else {
            sortVotedExceptionList();
        }
    }

    private void initPreferences( Context context ) {
        sharedPreferences = context.getSharedPreferences( PREFS_NAME, Context.MODE_PRIVATE );
        editor = sharedPreferences.edit();
        editor.apply();
    }

    private void initExceptionTypeStore( ) {
        int minId = sharedPreferences.getInt( MIN_ID, 1 );
        int maxId = sharedPreferences.getInt( MAX_ID, 0 );
        for ( int i = minId; i <= maxId; i++ ) {
            ExceptionType exceptionType = ExceptionType.fromString( sharedPreferences.getString( Integer.toString( i ), "" ) );
            if ( !exceptionTypeStore.containsKey( exceptionType.getType() ) ) {
                exceptionTypeStore.put( exceptionType.getType(), new ArrayList<>() );
            }
            exceptionTypeStore.get( exceptionType.getType() ).add( exceptionType );
        }
    }

    private void sortExceptionStore( ) {
        for ( List<ExceptionType> typeList : exceptionTypeStore.values() ) {
            Collections.sort( typeList, new ExceptionType.ShortNameComparator() );
        }
    }

    private void sortVotedExceptionList( ) {
        Collections.sort( votedExceptionTypeList, new ExceptionType.VoteComparator() );
    }

    public ExceptionType findById( int id ) {
        for ( List<ExceptionType> exceptionTypeList : exceptionTypeStore.values() ) {
            for ( ExceptionType exceptionType : exceptionTypeList ) {
                if ( id == exceptionType.getId() ) {
                    return exceptionType;
                }
            }
        }
        return new ExceptionType();
    }

    public Set<String> getExceptionTypes( ) {
        return exceptionTypeStore.keySet();
    }

    public boolean isInitialized( ) {
        return exceptionTypeStore != null;
    }

    public List<ExceptionType> getExceptionTypeListByName( String typeName ) {
        if ( exceptionTypeStore.containsKey( typeName ) ) {
            return new ArrayList<>( exceptionTypeStore.get( typeName ) );
        }
        return new ArrayList<>();
    }

    public void wipe( ) {
        exceptionTypeStore.clear();
        votedExceptionTypeList.clear();
        editor.clear().apply();
    }

    public List<ExceptionType> getVotedExceptionTypeList( ) {
        return votedExceptionTypeList;
    }

    public void updateVotedType( ExceptionType votedType ) {
        int index = votedExceptionTypeList.indexOf( votedType );
        ExceptionType listElementException = votedExceptionTypeList.get( index );
        listElementException.setVoteCount( votedType.getVoteCount() );
        notifyVotedTypeListeners();
    }

    public void addVotedType( ExceptionType submittedType ) {
        votedExceptionTypeList.add( submittedType );
        notifyVotedTypeListeners();
    }

    public void notifyVotedTypeListeners( ) {
        if ( Looper.myLooper() == Looper.getMainLooper() ) {
            stream( votedTypeListeners ).forEach( VotedTypeListener::onVoteListChanged );
        }
    }

    public boolean addVotedTypeListener( VotedTypeListener listener ) {
        return votedTypeListeners.add( listener );
    }

    public boolean removeVotedTypeListener( VotedTypeListener listener ) {
        return votedTypeListeners.remove( listener );
    }
}
