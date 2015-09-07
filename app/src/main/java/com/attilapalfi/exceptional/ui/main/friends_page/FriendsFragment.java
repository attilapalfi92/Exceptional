package com.attilapalfi.exceptional.ui.main.friends_page;

import android.app.Activity;
import android.content.Intent;
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
import com.attilapalfi.exceptional.services.rest.BackendService;
import com.attilapalfi.exceptional.services.persistent_stores.FriendsManager;
import com.attilapalfi.exceptional.ui.main.Constants;

import java.util.List;

/**
 * Created by palfi on 2015-08-23.
 */
public class FriendsFragment extends Fragment implements FriendChangeListener {
    private RecyclerView recyclerView;
    private FriendAdapter friendAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        FriendsManager.addFriendChangeListener(this);
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        FriendsManager.removeFriendChangeListener(this);
        super.onDetach();
    }

    private void initFriendAdapter() {
        if (!FriendsManager.isInitialized()) {
            FriendsManager.initialize(getActivity().getApplicationContext());
        }
        List<Friend> values = FriendsManager.getStoredFriends();
        friendAdapter = new FriendAdapter(getActivity(), values);
    }

    @NonNull
    private View initRecyclerView(LayoutInflater inflater, ViewGroup container) {
        View fragmentView = inflater.inflate(R.layout.fragment_friends, container, false);
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

    private static class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.RowViewHolder> {
        private Activity activity;
        private List<Friend> values;
        private RecyclerView recyclerView;
        private final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = recyclerView.getChildPosition(view);
                Friend friend = values.get(itemPosition);
                Intent intent = new Intent(activity, FriendDetailsActivity.class);
                intent.putExtra(Constants.FRIEND_ID, friend.getId().toString());
                activity.startActivity(intent);
            }
        };

        public FriendAdapter(Activity activity, List<Friend> values) {
            this.activity = activity;
            this.values = values;
        }

        @Override
        public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_row_layout, parent, false);
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
            }

            public void bindRow(Friend model) {
                nameView.setText(model.getFirstName() + " " + model.getLastName());
                pointsView.setText("Points: " + model.getPoints());
                model.setImageToView(imageView);
            }
        }

    }
}