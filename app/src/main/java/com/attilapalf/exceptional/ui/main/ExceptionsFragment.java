package com.attilapalf.exceptional.ui.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.rest.BackendConnector;
import com.attilapalf.exceptional.utils.ExceptionManager;

import java.util.List;

/**
 */
public class ExceptionsFragment extends ListFragment implements //OnSharedPreferenceChangeListener,
        ExceptionChangeListener {

    private ExceptionAdapter adapter;



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof MainActivity) {
            ((ExceptionSource)getActivity()).addExceptionChangeListener(this);
        }

        BackendConnector.getInstance().addExceptionChangeListener(this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<Exception> values = ExceptionManager.getExceptionList();
        adapter = new ExceptionAdapter(getActivity().getApplicationContext(), values);
        onExceptionsChanged();
        setListAdapter(adapter);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exceptions, null);
    }




    @Override
    public void onDetach() {
        if (getActivity() instanceof MainActivity) {
            ((ExceptionSource)getActivity()).removeExceptionChangeListener(this);
        }
        BackendConnector.getInstance().removeExceptionChangeListener(this);
        super.onDetach();
    }



    @Override
    public void onExceptionsChanged() {
        adapter.notifyDataSetChanged();
    }


    private static class ExceptionAdapter extends ArrayAdapter<Exception> {
        private Context context;
        private List<Exception> values;

        public ExceptionAdapter(Context context, List<Exception> values) {
            super(context, R.layout.exc_row_layout, values);
            this.context = context;
            this.values = values;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RowViewHolder viewHolder;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            private TextView fromWhoView;

            public RowViewHolder(View rowView) {
                nameView = (TextView) rowView.findViewById(R.id.excName);
                descView = (TextView) rowView.findViewById(R.id.excDesc);
                fromWhoView = (TextView) rowView.findViewById(R.id.excFromWho);
                dateView = (TextView) rowView.findViewById(R.id.excDate);


                nameView.setTextSize(20);
                descView.setTextSize(15);
                fromWhoView.setTextSize(15);
                dateView.setTextSize(15);

                nameView.setTextColor(Color.BLACK);
                descView.setTextColor(Color.BLACK);
                fromWhoView.setTextColor(Color.BLACK);
                dateView.setTextColor(Color.BLACK);
            }

            public void bindRow(Exception model) {
                nameView.setText(model.getPrefix() + "\n" + model.getShortName());
                descView.setText(model.getDescription());
                fromWhoView.setText(Long.toString(model.getFromWho()));
                String date =
                        model.getDate().toString();
//                        model.getDate().get(Calendar.HOUR_OF_DAY) + ":" +
//                        model.getDate().get(Calendar.MINUTE) + ":" +
//                        model.getDate().get(Calendar.SECOND);
                dateView.setText(date);
            }
        }
    }

}
