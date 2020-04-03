package edu.internet2.middleware.grouper.changeLog.consumer.model;

public class PasswordProfile {
    public final boolean forceChangePasswordNextSignIn;
    public final String password;


    public PasswordProfile(boolean forceChangePasswordNextSignIn, String password) {
        this.forceChangePasswordNextSignIn = forceChangePasswordNextSignIn;
        this.password = password;
    }
}
