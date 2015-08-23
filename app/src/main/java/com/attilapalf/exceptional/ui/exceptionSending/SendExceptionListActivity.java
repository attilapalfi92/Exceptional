package com.attilapalf.exceptional.ui.exceptionSending;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.ExceptionType;
import com.attilapalf.exceptional.services.persistent_stores.ExceptionTypeManager;

import java.util.List;

public class SendExceptionListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_exception_list);
        ListView exceptionListView = (ListView) findViewById(R.id.send_exception_list);
        exceptionListView.setOnItemClickListener(this);
        adapter = new MyAdapter(this.getApplicationContext(), ExceptionTypeManager.getInstance().getExceptionTypesByName());
        exceptionListView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ExceptionType exceptionType = adapter.getItem(position);
        Intent intent = new Intent(this, FriendChooserActivity.class);
        intent.putExtra("exceptionTypeId", exceptionType.getId());
        startActivity(intent);
        finish();
    }


    private static class MyAdapter extends ArrayAdapter<ExceptionType> {
        private Context context;
        private List<ExceptionType> values;

        public MyAdapter(Context context, List<ExceptionType> values) {
            super(context, R.layout.exception_chooser_row, R.id.exceptionChooserName, values);
            this.context = context;
            this.values = values;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            super.getView(position, convertView, parent);

            RowViewHolder viewHolder;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.exception_chooser_row, parent, false);
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
            private TextView descView;

            public RowViewHolder(View rowView) {
                nameView = (TextView) rowView.findViewById(R.id.exceptionChooserName);
                descView = (TextView) rowView.findViewById(R.id.exceptionChooserDescription);
                nameView.setTextSize(20);
                descView.setTextSize(15);
                nameView.setTextColor(Color.BLACK);
                descView.setTextColor(Color.BLACK);
            }

            public void bindRow(ExceptionType model) {
                nameView.setText(model.getPrefix() + "\n" + model.getShortName());
                descView.setText(model.getDescription());
            }
        }
    }

}
