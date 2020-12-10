package edu.internet2.middleware.grouper.azure.model;

public class AzureGraphUser {
    public final String id;
    public final boolean accountEnabled;
    public final String displayName;
    public final String onPremisesImmutableId;
    public final String mailNickName;
    public final AzureGraphPasswordProfile passwordProfile;
    public final String userPrincipalName;

    public static class AzureGraphPasswordProfile {
        public final boolean forceChangePasswordNextSignIn;
        public final String password;


        public AzureGraphPasswordProfile(boolean forceChangePasswordNextSignIn, String password) {
            this.forceChangePasswordNextSignIn = forceChangePasswordNextSignIn;
            this.password = password;
        }
    }

    public AzureGraphUser(String id, boolean accountEnabled, String displayName, String onPremisesImmutableId, String mailNickName, AzureGraphPasswordProfile passwordProfile, String userPrincipalName) {
        this.id = id;
        this.accountEnabled = accountEnabled;
        this.displayName = displayName;
        this.onPremisesImmutableId = onPremisesImmutableId;
        this.mailNickName = mailNickName;
        this.passwordProfile = passwordProfile;
        this.userPrincipalName = userPrincipalName;
    }

    @Override
    public String toString() {
        return "Azure User {id: " + this.id + ", userPrincipalName: " + this.userPrincipalName
                + ", displayName: " + this.displayName + "}";
    }
}
