package com.attilapalfi.exceptional.ui.main;

import javax.inject.Inject;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.interfaces.FirstStartFinishedListener;
import com.attilapalfi.exceptional.interfaces.PointChangeListener;
import com.attilapalfi.exceptional.persistence.FriendStore;
import com.attilapalfi.exceptional.persistence.ImageCache;
import com.attilapalfi.exceptional.persistence.MetadataStore;

/**
 */
public class MainFragment extends Fragment implements FirstStartFinishedListener, PointChangeListener {
    private View view;
    @Inject
    FriendStore friendStore;
    @Inject ImageCache imageCache;
    @Inject MetadataStore metadataStore;

    @Override
    public void onCreate( @Nullable Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        Injector.INSTANCE.getApplicationComponent().inject( this );
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        metadataStore.addFirstStartFinishedListener( this );
        metadataStore.addPointChangeListener( this );
        view = inflater.inflate( R.layout.fragment_main, container, false );
        return view;
    }

    @Override
    public void onResume( ) {
        super.onResume();
        if ( metadataStore.isFirstStartFinished() ) {
            setViews();
        }
    }

    @Override
    public void onDestroyView( ) {
        super.onDestroyView();
        metadataStore.removeFirstStartFinishedListener( this );
        metadataStore.removePointChangeListener( this );
    }

    private void setViews( ) {
        if ( metadataStore.getUser() != null ) {
            setImageView();
            setNameView();
            setPointView();
        }
    }

    private void setImageView( ) {
        ImageView imageView = (ImageView) view.findViewById( R.id.myMainImageView );
        imageCache.setImageToView( metadataStore.getUser(), imageView );
    }

    private void setNameView( ) {
        TextView nameView = (TextView) view.findViewById( R.id.mainNameTextView );
        String nameText = getResources().getString( R.string.main_welcome_before_name )
                + " " + metadataStore.getUser().getFirstName().trim()
                + "!";
        nameView.setText( nameText );
    }

    private void setPointView( ) {
        TextView pointView = (TextView) view.findViewById( R.id.mainPointTextView );
        String pointText = getString( R.string.main_point_view_pre ) + " "
                + metadataStore.getPoints()
                + " " + getString( R.string.main_point_view_post );
        pointView.setText( pointText );
    }

    @Override
    public void onFirstStartFinished( boolean state ) {
        if ( state ) {
            setViews();
        }
    }

    @Override
    public void onPointsChanged( ) {
        setPointView();
    }
}
