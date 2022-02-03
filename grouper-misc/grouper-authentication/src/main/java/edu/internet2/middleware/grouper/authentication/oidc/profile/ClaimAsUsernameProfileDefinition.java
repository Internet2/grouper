package edu.internet2.middleware.grouper.authentication.oidc.profile;

import org.pac4j.oidc.profile.OidcProfileDefinition;

public class ClaimAsUsernameProfileDefinition extends OidcProfileDefinition<ClaimAsUsernameProfile> {
    public ClaimAsUsernameProfileDefinition(final String claimAsUsername) {
        super();
        setProfileFactory(x -> new ClaimAsUsernameProfile(claimAsUsername));
    }
}
