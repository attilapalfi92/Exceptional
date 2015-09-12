package com.attilapalfi.exceptional.dependency_injection;

import com.attilapalfi.exceptional.ExceptionalApplication;

/**
 * Created by palfi on 2015-09-12.
 * <p>
 * an Injector class to handle the application-level component
 */
public enum Injector {
    INSTANCE;

    private ApplicationComponent applicationComponent;

    private Injector( ) {
    }

    public void initializeApplicationComponent( ExceptionalApplication application ) {
        ApplicationComponent applicationComponent = DaggerApplicationComponent.builder()
                .appContextModule( new AppContextModule( application ) )
                .build();
        this.applicationComponent = applicationComponent;
    }

    public ApplicationComponent getApplicationComponent( ) {
        return applicationComponent;
    }
}
