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
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;

import org.w3c.dom.Text;

/**
 */
public class MainFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_main, container, false);
        return inflater.inflate(R.layout.fragment_main, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageView imageView = (ImageView) getView().findViewById(R.id.myMainImageView);
        FriendsManager.getInstance().getYourself().setImageToView(imageView);
        TextView nameTextView = (TextView) getView().findViewById(R.id.mainNameTextView);
        R.string.welcome_before_name
    }
}
