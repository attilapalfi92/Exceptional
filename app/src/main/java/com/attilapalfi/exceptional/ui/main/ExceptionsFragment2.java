package com.attilapalfi.exceptional.ui.main;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.interfaces.ExceptionChangeListener;
import com.attilapalfi.exceptional.interfaces.ExceptionRefreshListener;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.services.persistent_stores.ExceptionInstanceManager;

import java.util.List;

/**
 * Created by palfi on 2015-08-24.
 */
public class ExceptionsFragment2 extends Fragment implements ExceptionRefreshListener,
        ExceptionChangeListener, SwipeRefreshLayout.OnRefreshListener {


    private RecyclerView recyclerView;
    private ExceptionAdapter exceptionAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ExceptionInstanceManager.getInstance().addExceptionChangeListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initExceptionAdapter();
        View fragmentView = initRecyclerView(inflater, container);
        return fragmentView;
    }

    @Override
    public void onDetach() {
        ExceptionInstanceManager.getInstance().removeExceptionChangeListener(this);
        super.onDetach();
    }

    private void initExceptionAdapter() {
        if (!ExceptionInstanceManager.getInstance().isInitialized()) {
            ExceptionInstanceManager.getInstance().initialize(getActivity().getApplicationContext());
        }
        List<Exception> values = ExceptionInstanceManager.getInstance().getExceptionList();
        exceptionAdapter = new ExceptionAdapter(getActivity(), values);
        onExceptionsChanged();
    }

    @NonNull
    private View initRecyclerView(LayoutInflater inflater, ViewGroup container) {
        View fragmentView = inflater.inflate(R.layout.fragment_exceptions_2, container, false);
        recyclerView = (RecyclerView) fragmentView.findViewById(R.id.exception_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(exceptionAdapter);
        exceptionAdapter.setRecyclerView(recyclerView);
        return fragmentView;
    }


    private static class ExceptionAdapter extends RecyclerView.Adapter<ExceptionAdapter.RowViewHolder>{
        private Context context;
        private List<Exception> values;
        private RecyclerView recyclerView;

        private final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = recyclerView.getChildPosition(view);
                Exception exception = values.get(itemPosition);
                Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        public ExceptionAdapter(Context context, List<Exception> values) {
            this.context = context;
            this.values = values;
        }

        @Override
        public RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.exception_row_layout_2, parent, false);
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
            private TextView descriptionView;

            public RowViewHolder(View rowView) {
                super(rowView);
                nameView = (TextView) rowView.findViewById(R.id.exceptionNameView);
                descriptionView = (TextView) rowView.findViewById(R.id.exceptionDescriptionView);
                nameView.setTextSize(20);
                descriptionView.setTextSize(15);
                nameView.setTextColor(Color.BLACK);
                descriptionView.setTextColor(Color.BLACK);
            }

            public void bindRow(Exception model) {
                nameView.setText(model.getShortName());
                descriptionView.setText(model.getDescription());
            }
        }

    }


    @Override
    public void onExceptionsChanged() {

    }

    @Override
    public void onExceptionRefreshFinished() {

    }

    @Override
    public void onRefresh() {

    }
}
