package com.attilapalf.exceptional.ui.exceptionSending;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.*;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.rest.BackendServiceImpl;
import com.attilapalf.exceptional.services.ExceptionTypeManager;
import com.attilapalf.exceptional.services.FriendsManager;
import com.attilapalf.exceptional.services.GpsService;

import java.util.List;

public class FriendChooserActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MyAdapter adapter;
    private int exceptionTypeId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_chooser);

        ListView friendListView = (ListView) findViewById(R.id.send_friend_list);
        friendListView.setOnItemClickListener(this);

        adapter = new MyAdapter(this.getApplicationContext(), FriendsManager.getInstance().getStoredFriends());
        friendListView.setAdapter(adapter);

        exceptionTypeId = getIntent().getIntExtra("exceptionTypeId", exceptionTypeId);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Friend friend = adapter.getItem(position);
        Exception exception = ExceptionTypeManager.getInstance().createException(exceptionTypeId,
                FriendsManager.getInstance().getYourself().getId(),
                friend.getId());

        if (GpsService.getInstance().canGetLocation()) {
            Location location = GpsService.getInstance().getLocation();
            exception.setLongitude(location.getLongitude());
            exception.setLatitude(location.getLatitude());
            BackendServiceImpl.getInstance().sendException(exception);
            finish();
        }
    }


    private static class MyAdapter extends ArrayAdapter<Friend> {
        private Context context;
        private List<Friend> values;

        public MyAdapter(Context context, List<Friend> values) {
            super(context, R.layout.friend_row_layout, R.id.friendNameView, values);
            this.context = context;
            this.values = values;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            super.getView(position, convertView, parent);

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



        // TODO: write last time of getting exception from someone
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
