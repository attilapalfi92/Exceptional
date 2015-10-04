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
    private volatile boolean hasData = false;

    private Book database;
    private Handler handler;
    private final Map<String, List<ExceptionType>> exceptionTypeStore = Collections.synchronizedMap( new HashMap<>() );
    private final List<ExceptionType> votedExceptionTypeList = Collections.synchronizedList( new LinkedList<>() );
    private Set<VotedTypeListener> votedTypeListeners = new HashSet<>();

    public ExceptionTypeStore( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        database = Paper.book( TYPE_DATABASE );
        handler = new Handler( Looper.getMainLooper() );
        synchronized ( exceptionTypeStore ) {
            if ( database.read( HAS_DATA, false ) ) {
                initExceptionTypeStore();
                sortExceptionStore();
                hasData = true;
            }
        }
    }

    public void addExceptionTypes( List<ExceptionType> exceptionTypes ) {
        synchronized ( exceptionTypeStore ) {
            saveExceptionTypeStore( exceptionTypes );
        }
        hasData = true;
        database.write( HAS_DATA, true );
    }

    private void saveExceptionTypeStore( List<ExceptionType> exceptionTypes ) {
        int maxId = 0;
        for ( ExceptionType exception : exceptionTypes ) {
            if ( !exceptionTypeStore.containsKey( exception.getType() ) ) {
                exceptionTypeStore.put( exception.getType(), Collections.synchronizedList( new LinkedList<>() ) );
            }
            exceptionTypeStore.get( exception.getType() ).add( exception );
            database.write( Integer.toString( exception.getId() ), exception );
            maxId = exception.getId() > maxId ? exception.getId() : maxId;
        }
        database.write( MAX_ID, maxId );
        sortExceptionStore();
    }

    public void setVotedExceptionTypes( List<ExceptionType> exceptionTypes ) {
        synchronized ( votedExceptionTypeList ) {
            votedExceptionTypeList.clear();
            votedExceptionTypeList.addAll( exceptionTypes );
            sortVotedExceptionList();
        }
        handler.post( this::notifyVotedTypeListeners );
    }

    private void initExceptionTypeStore( ) {
        int minId = 1;
        int maxId = database.read( MAX_ID, 0 );
        for ( int i = minId; i <= maxId; i++ ) {
            ExceptionType exceptionType = database.read( Integer.toString( i ), EMPTY_TYPE );
            if ( !exceptionTypeStore.containsKey( exceptionType.getType() ) ) {
                exceptionTypeStore.put( exceptionType.getType(), Collections.synchronizedList( new LinkedList<>() ) );
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
        Map<String, List<ExceptionType>> tempMap = new HashMap<>( exceptionTypeStore );
        for ( List<ExceptionType> exceptionTypeList : new ArrayList<>( tempMap.values() ) ) {
            for ( ExceptionType exceptionType : exceptionTypeList ) {
                if ( id == exceptionType.getId() ) {
                    return exceptionType;
                }
            }
        }
        return new ExceptionType();
    }

    public Set<String> getExceptionTypes( ) {
        Map<String, List<ExceptionType>> tempMap = new HashMap<>( exceptionTypeStore );
        return new HashSet<>( tempMap.keySet() );
    }

    public List<ExceptionType> getExceptionTypeListByName( String typeName ) {
        Map<String, List<ExceptionType>> tempMap = new HashMap<>( exceptionTypeStore );
        if ( tempMap.containsKey( typeName ) ) {
            return new ArrayList<>( tempMap.get( typeName ) );
        }
        return new ArrayList<>();
    }

    public void wipe( ) {
        exceptionTypeStore.clear();
        votedExceptionTypeList.clear();
        database.destroy();
    }

    public List<ExceptionType> getVotedExceptionTypeList( ) {
        return new ArrayList<>( votedExceptionTypeList );
    }

    public void updateVotedType( ExceptionType votedType ) {
        synchronized ( votedExceptionTypeList ) {
            int index = votedExceptionTypeList.indexOf( votedType );
            ExceptionType listElementException = votedExceptionTypeList.get( index );
            listElementException.setVoteCount( votedType.getVoteCount() );
        }
        notifyVotedTypeListeners();
    }

    public void addVotedType( ExceptionType submittedType ) {
        synchronized ( votedExceptionTypeList ) {
            votedExceptionTypeList.add( submittedType );
        }
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

    public boolean hasData() { return hasData; }
}
