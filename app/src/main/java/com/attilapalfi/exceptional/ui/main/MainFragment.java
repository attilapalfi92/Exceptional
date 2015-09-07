package com.attilapalfi.exceptional.ui.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.interfaces.FirstStartFinishedListener;
import com.attilapalfi.exceptional.interfaces.PointChangeListener;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.services.persistent_stores.MetadataStore;

/**
 */
public class MainFragment extends Fragment implements FirstStartFinishedListener, PointChangeListener {
    private View view;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MetadataStore.addFirstStartFinishedListener(this);
        MetadataStore.addPointChangeListener(this);
        view = inflater.inflate(R.layout.fragment_main, container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (MetadataStore.isFirstStartFinished()) {
            setViews();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MetadataStore.removeFirstStartFinishedListener(this);
        MetadataStore.removePointChangeListener(this);
    }

    private void setViews() {
        if (FriendsManager.getYourself() != null) {
            setImageView();
            setNameView();
            setPointView();
        }
    }

    private void setImageView() {
        ImageView imageView = (ImageView) view.findViewById(R.id.myMainImageView);
        FriendsManager.getYourself().setImageToView(imageView);
    }

    private void setNameView() {
        TextView nameView = (TextView) view.findViewById(R.id.mainNameTextView);
        String nameText = getResources().getString(R.string.main_welcome_before_name)
                + " " + FriendsManager.getYourself().getFirstName().trim()
                + "!";
        nameView.setText(nameText);
    }

    private void setPointView() {
        TextView pointView = (TextView) view.findViewById(R.id.mainPointTextView);
        String pointText = getString(R.string.main_point_view_pre) + " "
                + MetadataStore.getPoints()
                + " " + getString(R.string.main_point_view_post);
        pointView.setText(pointText);
    }

    @Override
    public void onFirstStartFinished(boolean state) {
        if (state) {
            setViews();
        }
    }

    @Override
    public void onPointsChanged() {
        setPointView();
    }
}
