package com.attilapalf.exceptional.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.exception.*;
import com.attilapalf.exceptional.exception.Exception;

import java.util.Calendar;
import java.util.List;

/**
 */
public class ExceptionsFragment extends ListFragment implements OnSharedPreferenceChangeListener {

    private MyAdapter adapter;



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ExceptionPreferences exceptionPreferences = ExceptionPreferences.
                getInstance(getActivity().getApplicationContext());

        List<Exception> values = exceptionPreferences.getExceptionList();
        adapter = new MyAdapter(getActivity().getApplicationContext(), values);
        //adapter.sort(new Exception.DateComparator());
        adapter.notifyDataSetChanged();
        setListAdapter(adapter);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exceptions, null);
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        SharedPreferences sharedPreferences = activity.getSharedPreferences(getString(R.string.exception_preferences),
                Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //adapter.sort(new Exception.DateComparator());
        adapter.notifyDataSetChanged();
    }

    private static class MyAdapter extends ArrayAdapter<com.attilapalf.exceptional.exception.Exception> {
        private Context context;
        private List<Exception> values;

        public MyAdapter(Context context, List<Exception> values) {
            super(context, R.layout.exc_row_layout, values);
            this.context = context;
            this.values = values;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RowViewHolder viewHolder;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //View rowView = inflater.inflate(R.layout.exc_row_layout, parent, false);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.exc_row_layout, parent, false);
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
            private TextView dateView;

            public RowViewHolder(View rowView) {
                nameView = (TextView) rowView.findViewById(R.id.excName);
                descView = (TextView) rowView.findViewById(R.id.excDesc);
                dateView = (TextView) rowView.findViewById(R.id.excDate);

                nameView.setTextSize(20);
                descView.setTextSize(15);
                dateView.setTextSize(15);
                nameView.setTextColor(Color.BLACK);
                descView.setTextColor(Color.BLACK);
                dateView.setTextColor(Color.BLUE);
            }

            public void bindRow(Exception model) {
                nameView.setText(model.getName());
                descView.setText(model.getDescription());
                String date = model.getDate().get(Calendar.HOUR_OF_DAY) + ":" +
                        model.getDate().get(Calendar.MINUTE) + ":" +
                        model.getDate().get(Calendar.SECOND);
                dateView.setText(date);
            }
        }
    }

}
