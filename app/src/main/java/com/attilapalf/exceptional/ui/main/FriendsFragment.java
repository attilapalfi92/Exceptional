package com.attilapalf.exceptional.ui.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.rest.BackendConnector;
import com.attilapalf.exceptional.utils.FriendsManager;

import java.util.List;

/**
 */
public class FriendsFragment extends ListFragment implements FriendChangeListener {

    private FriendAdapter adapter;



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        BackendConnector.getInstance().addFriendChangeListener(this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<Friend> values = FriendsManager.getInstance(getActivity().getApplicationContext()).getStoredFriends();
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
        BackendConnector.getInstance().removeFriendChangeListener(this);
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
            private TextView idView;
            private TextView urlView;

            public RowViewHolder(View rowView) {
                nameView = (TextView) rowView.findViewById(R.id.friendName);
                idView = (TextView) rowView.findViewById(R.id.friendId);
                urlView = (TextView) rowView.findViewById(R.id.friendImgUrl);

                nameView.setTextSize(20);
                idView.setTextSize(15);
                urlView.setTextSize(15);

                nameView.setTextColor(Color.BLACK);
                idView.setTextColor(Color.BLACK);
                urlView.setTextColor(Color.BLACK);
            }

            public void bindRow(Friend model) {
                nameView.setText(model.getName());
                idView.setText(Long.toString(model.getId()));
                urlView.setText(model.getImageUrl());
            }
        }

    }
}
