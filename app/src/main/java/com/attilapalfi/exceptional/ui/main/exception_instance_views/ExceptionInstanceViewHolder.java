package com.attilapalfi.exceptional.ui.main.exception_instance_views;

import javax.inject.Inject;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.attilapalfi.exceptional.R;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.*;
import com.attilapalfi.exceptional.model.Exception;
import com.attilapalfi.exceptional.persistence.MetadataStore;
import com.attilapalfi.exceptional.ui.helpers.ViewHelper;

/**
 * Created by palfi on 2015-10-03.
 */
public class ExceptionInstanceViewHolder extends RecyclerView.ViewHolder {
    @Inject Context context;
    @Inject MetadataStore metadataStore;
    @Inject ViewHelper viewHelper;
    private ImageView friendImage;
    private TextView exceptionNameView;
    private TextView descriptionView;
    private TextView friendNameAndCityView;
    private TextView toNameView;
    private TextView dateView;
    private ImageView outgoingImage;
    private ImageView incomingImage;
    private Friend fromWho;
    private Friend toWho;
    private Friend user;

    public ExceptionInstanceViewHolder( View rowView ) {
        super( rowView );
        Injector.INSTANCE.getApplicationComponent().inject( this );
        friendImage = (ImageView) rowView.findViewById( R.id.exc_row_image );
        exceptionNameView = (TextView) rowView.findViewById( R.id.question_exception_name );
        descriptionView = (TextView) rowView.findViewById( R.id.exc_row_description );
        friendNameAndCityView = (TextView) rowView.findViewById( R.id.exc_row_city_and_friend );
        toNameView = (TextView) rowView.findViewById( R.id.exc_row_to_person );
        dateView = (TextView) rowView.findViewById( R.id.exc_row_date );
        outgoingImage = (ImageView) rowView.findViewById( R.id.exc_row_outgoing_image );
        incomingImage = (ImageView) rowView.findViewById( R.id.exc_row_incoming_image );
    }

    public void bindRow( com.attilapalfi.exceptional.model.Exception model ) {
        initBinding( model );
        bindUserInfo( model );
        bindExceptionInfo( model );
        setDirectionImages();
    }

    private void initBinding( Exception model ) {
        fromWho = viewHelper.initExceptionSender( model );
        toWho = viewHelper.initExceptionReceiver( model );
        user = metadataStore.getUser();
    }

    private void bindUserInfo( Exception model ) {
        toNameView.setText( toWho.getName() );
        viewHelper.bindExceptionImage( fromWho, toWho, friendImage );
        setFromWhoNameAndCity( model );
    }

    private void setFromWhoNameAndCity( Exception model ) {
        String nameAndCity = viewHelper.getNameAndCity( model, fromWho );
        friendNameAndCityView.setText( nameAndCity );
    }

    private void bindExceptionInfo( Exception model ) {
        exceptionNameView.setText( model.getShortName() );
        descriptionView.setText( model.getDescription() );
        dateView.setText( viewHelper.formattedExceptionDate( model ) );
    }

    private void setDirectionImages( ) {
        if ( !fromWho.equals( user ) ) {
            outgoingImage.setImageBitmap( null );
        } else {
            outgoingImage.setImageDrawable( context.getResources().getDrawable( R.drawable.outgoing ) );
        }
        if ( !toWho.equals( user ) ) {
            incomingImage.setImageBitmap( null );
        } else {
            incomingImage.setImageDrawable( context.getResources().getDrawable( R.drawable.incoming ) );
        }
    }
}
