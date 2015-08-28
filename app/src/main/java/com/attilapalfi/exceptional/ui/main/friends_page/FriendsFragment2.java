package com.attilapalfi.exceptional.ui.main.friends_page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        initFriendAdapter();
        View fragmentView = initRecyclerView(inflater, container);
        onFriendsChanged();
        return fragmentView;
    }

    @Override
    public void onDetach() {
        BackendService.getInstance().removeFriendChangeListener(this);
        super.onDetach();
    }

    private void initFriendAdapter() {
        if (!FriendsManager.getInstance().isInitialized()) {
            FriendsManager.getInstance().initialize(getActivity().getApplicationContext());
        }
        List<Friend> values = FriendsManager.getInstance().getStoredFriends();
        friendAdapter = new FriendAdapter(getActivity(), values);
    }

    @NonNull
    private View initRecyclerView(LayoutInflater inflater, ViewGroup container) {
        View fragmentView = inflater.inflate(R.layout.fragment_friends_2, container, false);
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.friend_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(friendAdapter);
        friendAdapter.setRecyclerView(recyclerView);
        return fragmentView;
    }

    @Override
    public void onFriendsChanged() {
        friendAdapter.notifyDataSetChanged();
    }

    private static class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.RowViewHolder>{
        private Context context;
        private List<Friend> values;
        private RecyclerView recyclerView;

        private final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = recyclerView.getChildPosition(view);
                Friend friend = values.get(itemPosition);
                Intent intent = new Intent(context, FriendDetailsActivity.class);
                context.startActivity(intent);
            }
        };

        public FriendAdapter(Context context, List<Friend> values) {
            this.context = context;
            this.values = values;
        }

        @Override
        public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_row_layout_2, parent, false);
            view.setOnClickListener(onClickListener);
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

        public void setRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
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
