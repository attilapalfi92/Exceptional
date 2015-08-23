package com.attilapalfi.exceptional.ui.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.interfaces.FriendChangeListener;
import com.attilapalfi.exceptional.model.Friend;
import com.attilapalfi.exceptional.rest.BackendService;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;

import java.util.List;

/**
 * Created by palfi on 2015-08-23.
 */
public class FriendsFragment2 extends Fragment implements FriendChangeListener {
    private RecyclerView recyclerView;
    private FriendAdapter friendAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        BackendService.getInstance().addFriendChangeListener(this);
        FriendsManager.getInstance().setFriendChangeListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (!FriendsManager.getInstance().isInitialized()) {
            FriendsManager.getInstance().initialize(getActivity().getApplicationContext());
        }
        List<Friend> values = FriendsManager.getInstance().getStoredFriends();
        friendAdapter = new FriendAdapter(getActivity().getApplicationContext(), values);

        View fragmentView = inflater.inflate(R.layout.fragment_friends_2, container, false);
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.friend_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(friendAdapter);

        onFriendsChanged();
        return fragmentView;
    }

    @Override
    public void onDetach() {
        BackendService.getInstance().removeFriendChangeListener(this);
        super.onDetach();
    }

    @Override
    public void onFriendsChanged() {
        friendAdapter.notifyDataSetChanged();
    }

    private static class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.RowViewHolder>{
        Context context;
        List<Friend> values;

        public FriendAdapter(Context context, List<Friend> values) {
            this.context = context;
            this.values = values;
        }

        @Override
        public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_friends_2, null);
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_row_layout_2, parent, false);
            return new RowViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RowViewHolder holder, int position) {
            holder.bindRow(values.get(position));
        }

        @Override
        public int getItemCount() {
            return values != null ? values.size() : 0;
        }


        public static class RowViewHolder extends RecyclerView.ViewHolder {
            private TextView nameView;
            private TextView pointsView;
            private ImageView imageView;

            public RowViewHolder(View rowView) {
                super(rowView);
                nameView = (TextView) rowView.findViewById(R.id.friendNameView);
                pointsView = (TextView) rowView.findViewById(R.id.friendPointsView);
                imageView = (ImageView) rowView.findViewById(R.id.friendImageView);
                nameView.setTextSize(20);
                pointsView.setTextSize(15);
                nameView.setTextColor(Color.BLACK);
                pointsView.setTextColor(Color.BLACK);
            }

            public void bindRow(Friend model) {
                nameView.setText(model.getFirstName() + " " + model.getLastName());
                pointsView.setText("Points: " + model.getPoints());
                model.setImageToView(imageView);
            }
        }

    }
}
