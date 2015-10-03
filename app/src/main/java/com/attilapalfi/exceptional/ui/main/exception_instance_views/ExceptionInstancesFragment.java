package com.attilapalfi.exceptional.ui.main.exception_instance_views;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.ExceptionChangeListener;
import com.attilapalfi.exceptional.interfaces.ExceptionRefreshListener;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.persistence.ExceptionInstanceStore;
import com.attilapalfi.exceptional.persistence.FriendStore;
import com.attilapalfi.exceptional.persistence.ImageCache;
import com.attilapalfi.exceptional.persistence.MetadataStore;
import com.attilapalfi.exceptional.rest.ExceptionService;
import com.attilapalfi.exceptional.ui.main.friends_page.FriendDetailsActivity;

/**
 * Created by palfi on 2015-08-24.
 */
public class ExceptionInstancesFragment extends Fragment implements ExceptionRefreshListener,
        ExceptionChangeListener, SwipeRefreshLayout.OnRefreshListener {

    private static long lastSyncTime = 0;

    @Inject
    ExceptionService exceptionService;
    @Inject
    ExceptionInstanceStore exceptionInstanceStore;
    @Inject
    MetadataStore metadataStore;
    private Friend friend;
    private RecyclerView recyclerView;
    private ExceptionInstanceAdapter exceptionInstanceAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Injector.INSTANCE.getApplicationComponent().inject( this );
        exceptionInstanceStore.addExceptionChangeListener( this );
        FriendDetailsActivity activity;
        if ( getActivity() instanceof FriendDetailsActivity ) {
            activity = (FriendDetailsActivity) getActivity();
            friend = activity.getFriend();
        }
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {
        initExceptionAdapter();
        View view = initRecyclerView( inflater, container );

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById( R.id.exception_swipe_container );
        swipeRefreshLayout.setOnRefreshListener( this );
        swipeRefreshLayout.setSize( SwipeRefreshLayout.LARGE );
        swipeRefreshLayout.setColorSchemeColors(
                getResources().getColor( R.color.exceptional_blue ),
                getResources().getColor( R.color.exceptional_green ),
                getResources().getColor( R.color.exceptional_red ),
                getResources().getColor( R.color.exceptional_purple )
        );

        return view;
    }

    @Override
    public void onActivityCreated( @Nullable Bundle savedInstanceState ) {
        super.onActivityCreated( savedInstanceState );
        onExceptionsChanged();
    }

    @Override
    public void onDestroy( ) {
        exceptionInstanceStore.removeExceptionChangeListener( this );
        super.onDestroy();
    }

    @Override
    public void onExceptionsChanged( ) {
        exceptionInstanceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh( ) {
        long currentTime = System.currentTimeMillis();
        if ( currentTime > lastSyncTime + 10000 ) {
            lastSyncTime = currentTime;
        }
        actualRefresh();
    }

    private void actualRefresh( ) {
        if ( metadataStore.isLoggedIn() ) {
            exceptionService.refreshExceptions( this );
        } else {
            Toast.makeText( getActivity().getApplicationContext(), "You have to login first!", Toast.LENGTH_SHORT ).show();
            onExceptionRefreshFinished();
        }
    }

    @Override
    public void onExceptionRefreshFinished( ) {
        swipeRefreshLayout.setRefreshing( false );
    }

    private void initExceptionAdapter( ) {
        List<Exception> values = generateValues();
        exceptionInstanceAdapter = new ExceptionInstanceAdapter( values, getActivity().getApplicationContext() );
        onExceptionsChanged();
    }

    private List<Exception> generateValues( ) {
        List<Exception> allValues = exceptionInstanceStore.getExceptionList();
        if ( friend != null ) {
            List<Exception> filteredValues = new ArrayList<>();
            for ( Exception exception : allValues ) {
                if ( exception.getFromWho().equals( friend.getId() ) || exception.getToWho().equals( friend.getId() ) ) {
                    filteredValues.add( exception );
                }
            }
            return filteredValues;
        } else {
            return allValues;
        }
    }

    @NonNull
    private View initRecyclerView( LayoutInflater inflater, ViewGroup container ) {
        View fragmentView = inflater.inflate( R.layout.fragment_exception_instances, container, false );
        recyclerView = (RecyclerView) fragmentView.findViewById( R.id.exception_recycler_view );
        recyclerView.setLayoutManager( new LinearLayoutManager( getActivity() ) );
        recyclerView.setAdapter( exceptionInstanceAdapter );
        exceptionInstanceAdapter.setRecyclerView( recyclerView );
        return fragmentView;
    }
}
