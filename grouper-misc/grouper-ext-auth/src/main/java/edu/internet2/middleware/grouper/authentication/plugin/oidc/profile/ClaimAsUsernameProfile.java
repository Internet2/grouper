package edu.internet2.middleware.grouper.authentication.plugin.oidc.profile;

import org.pac4j.oidc.profile.OidcProfile;

public class ClaimAsUsernameProfile extends OidcProfile {
    private final String claimAsUsername;

    public ClaimAsUsernameProfile(final String claimAsUsername) {
        this.claimAsUsername = claimAsUsername;
    }

    @Override
    public String getUsername() {
        return this.getAttribute(this.claimAsUsername).toString();
    }
}