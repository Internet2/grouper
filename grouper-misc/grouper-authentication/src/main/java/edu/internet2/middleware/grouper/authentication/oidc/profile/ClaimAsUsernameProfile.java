package edu.internet2.middleware.grouper.authentication.oidc.profile;

import org.pac4j.oidc.profile.OidcProfile;

public class ClaimAsUsernameProfile extends OidcProfile {
    private final String claimAsUsername;

    public ClaimAsUsernameProfile(final String claimAsUsername) {
        this.claimAsUsername = claimAsUsername;
    }

    @Override
    public String getUsername() {
        return (String)this.getAttribute(this.claimAsUsername);
    }
}
