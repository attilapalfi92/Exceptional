package com.attilapalfi.exceptional.services.persistent_stores;

import javax.inject.Inject;

import android.content.Context;
import com.attilapalfi.exceptional.dependency_injection.Injector;
import com.attilapalfi.exceptional.model.Yourself;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by palfi on 2015-09-13.
 */
public class YourselfRealm {
    @Inject Context context;
    private Yourself yourself;

    public YourselfRealm( ) {
        Injector.INSTANCE.getApplicationComponent().inject( this );
        RealmResults<Yourself> results = Realm.getInstance( context ).allObjects( Yourself.class );
        if (!results.isEmpty()) {
            yourself = results.first();
        }
    }

    public void saveYourself( Yourself yourself ) {
        saveOrUpdateYourself( yourself );
    }

    public void updateYourself( Yourself yourself ) {
        saveOrUpdateYourself( yourself );
    }

    private void saveOrUpdateYourself( Yourself yourself ) {
        Realm realm = Realm.getInstance( context );
        realm.beginTransaction();
        this.yourself = realm.copyToRealmOrUpdate( yourself );
        realm.commitTransaction();
    }

    public Yourself getYourself( ) {
        if ( yourself == null ) {
            yourself = new Yourself( "0", "", "", "" );
        }
        return yourself;
    }
}
