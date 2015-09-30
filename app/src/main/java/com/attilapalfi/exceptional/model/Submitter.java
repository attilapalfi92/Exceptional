package com.attilapalfi.exceptional.model;

/**
 * Created by palfi on 2015-09-30.
 */
public class Submitter {
    private String firstName;
    private String lastName;

    public Submitter( ) {
    }

    public Submitter( String firstName, String lastName ) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName( ) {
        return firstName;
    }

    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }

    public String getLastName( ) {
        return lastName;
    }

    public void setLastName( String lastName ) {
        this.lastName = lastName;
    }

    public String fullName( ) {
        return firstName + " " + lastName;
    }
}
