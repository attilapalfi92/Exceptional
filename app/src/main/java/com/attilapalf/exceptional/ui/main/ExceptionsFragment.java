package com.attilapalf.exceptional.ui.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;

import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.attilapalf.exceptional.R;
import com.attilapalf.exceptional.model.Exception;
import com.attilapalf.exceptional.model.Friend;
import com.attilapalf.exceptional.rest.BackendConnector;
import com.attilapalf.exceptional.interfaces.ExceptionChangeListener;
import com.attilapalf.exceptional.interfaces.ExceptionRefreshListener;
import com.attilapalf.exceptional.services.ExceptionManager;
import com.attilapalf.exceptional.services.FacebookManager;
import com.attilapalf.exceptional.services.FriendsManager;

import java.util.List;

/**
 */
public class ExceptionsFragment extends ListFragment implements ExceptionRefreshListener,
        ExceptionChangeListener, SwipeRefreshLayout.OnRefreshListener {

    private ExceptionAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof MainActivity) {
            //((ExceptionSource)getActivity()).addExceptionChangeListener(this);
        }

//        BackendConnector.getInstance().addExceptionChangeListener(this);
        ExceptionManager.getInstance().addExceptionChangeListener(this);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<Exception> values = ExceptionManager.getInstance().getExceptionList();
        adapter = new ExceptionAdapter(getActivity().getApplicationContext(), values);
        onExceptionsChanged();
        setListAdapter(adapter);
    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exceptions, null);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_dark,
                android.R.color.holo_purple,
                android.R.color.holo_green_dark,
                android.R.color.holo_red_dark
        );

        return view;
    }




    @Override
    public void onDetach() {
        if (getActivity() instanceof MainActivity) {
            //((ExceptionSource)getActivity()).removeExceptionChangeListener(this);
        }

//        BackendConnector.getInstance().removeExceptionChangeListener(this);
        ExceptionManager.getInstance().removeExceptionChangeListener(this);

        super.onDetach();
    }



    @Override
    public void onExceptionsChanged() {
        adapter.notifyDataSetChanged();
    }



    @Override
    public void onRefresh() {
        BackendConnector.getInstance().refreshExceptions(this);
    }

    @Override
    public void onExceptionRefreshFinished() {
        swipeRefreshLayout.setRefreshing(false);
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
            private static int yourselfCounter = 0;
            private static boolean toastShown = false;

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

                Friend fromWho = FriendsManager.getInstance().findFriendById(model.getFromWho());
                if (fromWho != null) {
                    fromWhoView.setText("From: " + fromWho.getName());
                    yourselfCounter = 0;

                } else {
                    if (FacebookManager.getInstance().getProfileId() == model.getFromWho()) {
                        fromWhoView.setText("From: yourself");
                        if (++yourselfCounter == 7) {
                            yourselfCounter = 0;
                            if (!toastShown) {
                                Toast.makeText(fromWhoView.getContext(), "You lonely motherfucker", Toast.LENGTH_SHORT).show();
                                toastShown = true;
                            }
                        }
                    } else {
                        fromWhoView.setText("From: unknown :(");
                        yourselfCounter = 0;
                    }
                }


                String date = model.getDate().toString();

                dateView.setText(date);
            }
        }
    }

}
