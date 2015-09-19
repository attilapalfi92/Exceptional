package com.attilapalfi.exceptional.rest;

import android.content.Context;
import com.attilapalfi.exceptional.R;
import com.google.gson.GsonBuilder;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Created by palfi on 2015-09-13.
 */
public class RestInterfaceFactory {
    public <T> T create( Context context, Class<T> interfaceClass ) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint( context.getString( R.string.backend_address ) )
                .setConverter( new GsonConverter( ( new GsonBuilder().create() ) ) )
                .build();
        return restAdapter.create( interfaceClass );
    }
}
