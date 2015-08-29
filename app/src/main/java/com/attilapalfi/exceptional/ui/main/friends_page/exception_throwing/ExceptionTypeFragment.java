package com.attilapalfi.exceptional.ui.main.friends_page.exception_throwing;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.model.ExceptionType;

import java.util.List;

public class ExceptionTypeFragment extends Fragment {
    private List<ExceptionType> exceptionTypes;
    private RecyclerView recyclerView;
    private ExceptionTypeAdapter adapter;

    public ExceptionTypeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exception_type, container, false);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setExceptionTypes(List<ExceptionType> exceptionTypes) {
        this.exceptionTypes = exceptionTypes;
    }

    public static class ExceptionTypeAdapter extends RecyclerView.Adapter<ExceptionTypeAdapter.RowViewHolder> {

        @Override
        public ExceptionTypeAdapter.RowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ExceptionTypeAdapter.RowViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public static class RowViewHolder extends RecyclerView.ViewHolder {
            public RowViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
