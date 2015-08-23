package com.attilapalfi.exceptional.ui.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.model.*;
import com.attilapalfi.exceptional.rest.BackendServiceImpl;
import com.attilapalfi.exceptional.interfaces.FriendChangeListener;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;

import java.util.List;

/**
 */
public class FriendsFragment extends ListFragment implements FriendChangeListener {

    private FriendAdapter adapter;



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        BackendServiceImpl.getInstance().addFriendChangeListener(this);
        FriendsManager.getInstance().setFriendChangeListener(this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!FriendsManager.getInstance().isInitialized()) {
            FriendsManager.getInstance().initialize(getActivity().getApplicationContext());
        }
        List<Friend> values = FriendsManager.getInstance().getStoredFriends();
        adapter = new FriendAdapter(getActivity().getApplicationContext(), values);
        onFriendsChanged();
        setListAdapter(adapter);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, null);
    }




    @Override
    public void onDetach() {
        BackendServiceImpl.getInstance().removeFriendChangeListener(this);
        super.onDetach();
    }



    @Override
    public void onFriendsChanged() {
        adapter.notifyDataSetChanged();
    }

    private static class FriendAdapter extends ArrayAdapter<Friend> {
        Context context;
        List<Friend> values;

        public FriendAdapter(Context context, List<Friend> values) {
            super(context, R.layout.friend_row_layout, values);
            this.context = context;
            this.values = values;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RowViewHolder viewHolder;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.friend_row_layout, parent, false);
                viewHolder = new RowViewHolder(convertView);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (RowViewHolder)convertView.getTag();
            }

            viewHolder.bindRow(values.get(position));

            return convertView;
        }


        private static class RowViewHolder {
            private TextView nameView;
            private TextView pointsView;
            private ImageView imageView;

            public RowViewHolder(View rowView) {
                nameView = (TextView) rowView.findViewById(R.id.friendNameView);
                pointsView = (TextView) rowView.findViewById(R.id.friendPointsView);
                imageView = (ImageView) rowView.findViewById(R.id.friendImageView);

                nameView.setTextSize(20);
                pointsView.setTextSize(15);

                nameView.setTextColor(Color.BLACK);
                pointsView.setTextColor(Color.BLACK);
            }

            public void bindRow(Friend model) {
                nameView.setText(model.getFirstName());
                pointsView.setText("Points: " + model.getId().toString());
                model.setImageToView(imageView);
            }
        }

    }
}
