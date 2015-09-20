package com.attilapalfi.exceptional.persistence;

import java.util.*;

import android.os.Handler;
import android.os.Looper;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.VotedTypeListener;
import com.attilapalfi.exceptional.model.ExceptionType;
import io.paperdb.Book;
import io.paperdb.Paper;

import static java8.util.stream.StreamSupport.stream;

/**
 * Created by Attila on 2015-06-09.
 */
public class ExceptionTypeStore {
    private static final String TYPE_DATABASE = "TYPE_DATABASE";
    private static final ExceptionType EMPTY_TYPE = new ExceptionType( 0, "", "", "" );
    private final String MAX_ID = "maxId";
    private final String HAS_DATA = "hasData";

    private Book database;
    private Handler handler;
    private Map<String, List<ExceptionType>> exceptionTypeStore;
    private List<ExceptionType> votedExceptionTypeList;
    private Set<VotedTypeListener> votedTypeListeners = new HashSet<>();

    public ExceptionTypeStore( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        exceptionTypeStore = Collections.synchronizedMap( new HashMap<>() );
        database = Paper.book( TYPE_DATABASE );
        handler = new Handler( Looper.getMainLooper() );
        if ( database.read( HAS_DATA, false ) ) {
            initExceptionTypeStore();
            sortExceptionStore();
        }
    }

    public void addExceptionTypes( List<ExceptionType> exceptionTypes ) {
        saveExceptionTypeStore( exceptionTypes );
        database.write( HAS_DATA, true );
    }

    private void saveExceptionTypeStore( List<ExceptionType> exceptionTypes ) {
        int maxId = 0;
        for ( ExceptionType exception : exceptionTypes ) {
            if ( !exceptionTypeStore.containsKey( exception.getType() ) ) {
                exceptionTypeStore.put( exception.getType(), new ArrayList<>() );
            }
            exceptionTypeStore.get( exception.getType() ).add( exception );
            database.write( Integer.toString( exception.getId() ), exception );
            maxId = exception.getId() > maxId ? exception.getId() : maxId;
        }
        database.write( MAX_ID, maxId );
        sortExceptionStore();
    }

    public void setVotedExceptionTypes( List<ExceptionType> exceptionTypes ) {
        votedExceptionTypeList = Collections.synchronizedList( exceptionTypes );
        sortVotedExceptionList();
        handler.post( this::notifyVotedTypeListeners );
    }

    private void initExceptionTypeStore( ) {
        int minId = 1;
        int maxId = database.read( MAX_ID, 0 );
        for ( int i = minId; i <= maxId; i++ ) {
            ExceptionType exceptionType = database.read( Integer.toString( i ), EMPTY_TYPE );
            if ( !exceptionTypeStore.containsKey( exceptionType.getType() ) ) {
                exceptionTypeStore.put( exceptionType.getType(), new LinkedList<>() );
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

    public List<ExceptionType> getExceptionTypeListByName( String typeName ) {
        if ( exceptionTypeStore.containsKey( typeName ) ) {
            return new ArrayList<>( exceptionTypeStore.get( typeName ) );
        }
        return new ArrayList<>();
    }

    public void wipe( ) {
        exceptionTypeStore.clear();
        votedExceptionTypeList.clear();
        database.destroy();
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
