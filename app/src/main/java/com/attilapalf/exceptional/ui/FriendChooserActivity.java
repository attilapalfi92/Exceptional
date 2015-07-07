package com.attilapalf.exceptional.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.ExceptionType;
import com.attilapalf.exceptional.model.Friend;
import com.attilapalf.exceptional.services.ExceptionFactory;
import com.attilapalf.exceptional.services.FriendsManager;
import com.attilapalf.exceptional.ui.main.FriendsFragment;

import java.util.List;

public class FriendChooserActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_chooser);

        ListView friendListView = (ListView) findViewById(R.id.send_friend_list);
        friendListView.setOnItemClickListener(this);

        adapter = new MyAdapter(this.getApplicationContext(), FriendsManager.getInstance().getStoredFriends());
        friendListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

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
                nameView.setText(model.getName());
                pointsView.setText("Points: " + Long.toString(model.getId()));
                model.setImageToView(imageView);
            }
        }
    }
}
