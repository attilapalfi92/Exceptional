package com.attilapalfi.exceptional.ui.main;

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
        List<Exception> values = generateValues( exceptionInstanceStore.getExceptionList() );
        exceptionInstanceAdapter = new ExceptionInstanceAdapter( values, getActivity().getApplicationContext() );
        onExceptionsChanged();
    }

    private List<Exception> generateValues( List<Exception> allValues ) {
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


    public static class ExceptionInstanceAdapter extends RecyclerView.Adapter<ExceptionInstanceAdapter.RowViewHolder> {
        private Context context;
        private List<Exception> values;
        private RecyclerView recyclerView;
        @Inject
        FriendStore friendStore;
        @Inject
        MetadataStore metadataStore;
        @Inject
        ImageCache imageCache;

        private final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                int itemPosition = recyclerView.getChildPosition( view );
                Exception exception = values.get( itemPosition );
                //Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        public ExceptionInstanceAdapter( List<Exception> values, Context context ) {
            Injector.INSTANCE.getApplicationComponent().inject( this );
            this.values = values;
            this.context = context;
        }

        @Override
        public RowViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
            View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.exception_row_layout, parent, false );
            view.setOnClickListener( onClickListener );
            return new RowViewHolder( view, context, friendStore, metadataStore, imageCache );
        }

        @Override
        public void onBindViewHolder( RowViewHolder holder, int position ) {
            holder.bindRow( values.get( position ) );
        }

        @Override
        public int getItemCount( ) {
            return values != null ? values.size() : 0;
        }

        public void setRecyclerView( RecyclerView recyclerView ) {
            this.recyclerView = recyclerView;
        }


        public static class RowViewHolder extends RecyclerView.ViewHolder {
            private Context context;
            private FriendStore friendStore;
            private MetadataStore metadataStore;
            private ImageCache imageCache;
            private ImageView friendImage;
            private TextView exceptionNameView;
            private TextView descriptionView;
            private TextView friendNameAndCityView;
            private TextView toNameView;
            private TextView dateView;
            private ImageView outgoingImage;
            private ImageView incomingImage;
            private Friend fromWho;
            private Friend toWho;
            private Friend user;

            public RowViewHolder( View rowView, Context context, FriendStore friendStore, MetadataStore metadataStore,
                                  ImageCache imageCache ) {
                super( rowView );
                this.context = context;
                this.friendStore = friendStore;
                this.metadataStore = metadataStore;
                this.imageCache = imageCache;
                friendImage = (ImageView) rowView.findViewById( R.id.exc_row_image );
                exceptionNameView = (TextView) rowView.findViewById( R.id.exc_row_name );
                descriptionView = (TextView) rowView.findViewById( R.id.exc_row_description );
                friendNameAndCityView = (TextView) rowView.findViewById( R.id.exc_row_city_and_friend );
                toNameView = (TextView) rowView.findViewById( R.id.exc_row_to_person );
                dateView = (TextView) rowView.findViewById( R.id.exc_row_date );
                outgoingImage = (ImageView) rowView.findViewById( R.id.exc_row_outgoing_image );
                incomingImage = (ImageView) rowView.findViewById( R.id.exc_row_incoming_image );
            }

            public void bindRow( Exception model ) {
                initBinding( model );
                bindUserInfo( model );
                bindExceptionInfo( model );
                setDirectionImages();
            }

            private void initBinding( Exception model ) {
                initFromWho( model );
                initToWho( model );
                user = metadataStore.getUser();
            }

            private void initFromWho( Exception model ) {
                fromWho = friendStore.findFriendById( model.getFromWho() );
                if ( fromWho.getId().equals( new BigInteger( "0" ) ) ) {
                    fromWho = metadataStore.getUser();
                }
            }

            private void initToWho( Exception model ) {
                toWho = friendStore.findFriendById( model.getToWho() );
                if ( toWho.getId().equals( new BigInteger( "0" ) ) ) {
                    toWho = metadataStore.getUser();
                }
            }

            private void bindUserInfo( Exception model ) {
                toNameView.setText( toWho.getName() );
                bindImage();
                setFromWhoNameAndCity( model );
            }

            private void bindImage( ) {
                if ( user.equals( fromWho ) ) {
                    if ( user.equals( toWho ) ) {
                        imageCache.setImageToView( user, friendImage );
                    } else {
                        if ( toWho.getId().longValue() != 0 ) {
                            imageCache.setImageToView( toWho, friendImage );
                        }
                    }
                } else {
                    if ( fromWho.getId().longValue() != 0 ) {
                        imageCache.setImageToView( fromWho, friendImage );
                    }
                }
            }

            private void setFromWhoNameAndCity( Exception model ) {
                String city = model.getCity();
                String nameAndCity = "";
                if ( fromWho.getId().longValue() != 0 ) {
                    nameAndCity = fromWho.getName();
                    if ( !"".equals( city ) ) {
                        nameAndCity += ( ", " + city );
                    }
                }
                friendNameAndCityView.setText( nameAndCity );
            }

            // TODO: get type information from exceptionTypeManager or what to remove redundant strings
            private void bindExceptionInfo( Exception model ) {
                exceptionNameView.setText( model.getShortName() );
                descriptionView.setText( model.getDescription() );
                dateView.setText( DateFormat.format( "yyyy-MM-dd HH:mm:ss", model.getDate().getTime() ) );
            }

            private void setDirectionImages( ) {
                if ( !fromWho.equals( user ) ) {
                    outgoingImage.setImageBitmap( null );
                } else {
                    outgoingImage.setImageDrawable( context.getResources().getDrawable( R.drawable.outgoing ) );
                }
                if ( !toWho.equals( user ) ) {
                    incomingImage.setImageBitmap( null );
                } else {
                    incomingImage.setImageDrawable( context.getResources().getDrawable( R.drawable.incoming ) );
                }
            }
        }

    }
}
