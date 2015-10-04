package com.attilapalfi.exceptional.ui.main.exception_instance_views;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Exception;

/**
 * Created by palfi on 2015-10-03.
 */
public class ExceptionInstanceAdapter extends RecyclerView.Adapter<ExceptionInstanceViewHolder> {
    private List<Exception> values;
    private RecyclerView recyclerView;

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick( View view ) {
            int itemPosition = recyclerView.getChildPosition( view );
            Exception exception = values.get( itemPosition );
            //Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    public ExceptionInstanceAdapter( List<Exception> values ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        this.values = values;
    }

    @Override
    public ExceptionInstanceViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.exception_row_layout, parent, false );
        view.setOnClickListener( onClickListener );
        return new ExceptionInstanceViewHolder( view );
    }

    @Override
    public void onBindViewHolder( ExceptionInstanceViewHolder holder, int position ) {
        holder.bindRow( values.get( position ) );
    }

    @Override
    public int getItemCount( ) {
        return values != null ? values.size() : 0;
    }

    public void setRecyclerView( RecyclerView recyclerView ) {
        this.recyclerView = recyclerView;
    }


}
