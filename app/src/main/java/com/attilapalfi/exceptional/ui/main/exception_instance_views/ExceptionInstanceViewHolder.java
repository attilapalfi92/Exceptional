package com.attilapalfi.exceptional.ui.main.exception_instance_views;

import java.math.BigInteger;

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
import com.attilapalfi.exceptional.persistence.FriendStore;
import com.attilapalfi.exceptional.persistence.ImageCache;
import com.attilapalfi.exceptional.persistence.MetadataStore;
import com.attilapalfi.exceptional.ui.helpers.Converter;

/**
 * Created by palfi on 2015-10-03.
 */
public class ExceptionInstanceViewHolder extends RecyclerView.ViewHolder {
    @Inject Context context;
    @Inject FriendStore friendStore;
    @Inject MetadataStore metadataStore;
    @Inject ImageCache imageCache;
    @Inject Converter converter;
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
        initFromWho( model );
        initToWho( model );
        user = metadataStore.getUser();
    }

    private void initFromWho( Exception model ) {
        fromWho = friendStore.findFriendById( model.getFromWho() );
        if ( fromWho.getId().equals( new BigInteger( "0" ) ) ) {
            fromWho = metadataStore.getUser();
        }
    }

    private void initToWho( Exception model ) {
        toWho = friendStore.findFriendById( model.getToWho() );
        if ( toWho.getId().equals( new BigInteger( "0" ) ) ) {
            toWho = metadataStore.getUser();
        }
    }

    private void bindUserInfo( Exception model ) {
        toNameView.setText( toWho.getName() );
        bindImage();
        setFromWhoNameAndCity( model );
    }

    private void bindImage( ) {
        if ( user.equals( fromWho ) ) {
            if ( user.equals( toWho ) ) {
                imageCache.setImageToView( user, friendImage );
            } else {
                if ( toWho.getId().longValue() != 0 ) {
                    imageCache.setImageToView( toWho, friendImage );
                }
            }
        } else {
            if ( fromWho.getId().longValue() != 0 ) {
                imageCache.setImageToView( fromWho, friendImage );
            }
        }
    }

    private void setFromWhoNameAndCity( Exception model ) {
        String nameAndCity = converter.nameAndCityFromException( model );
        friendNameAndCityView.setText( nameAndCity );
    }

    // TODO: get type information from exceptionTypeStore or what to remove redundant strings
    private void bindExceptionInfo( Exception model ) {
        exceptionNameView.setText( model.getShortName() );
        descriptionView.setText( model.getDescription() );
        dateView.setText( DateFormat.format( "yyyy-MM-dd HH:mm:ss", model.getDate().getTime() ) );
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
