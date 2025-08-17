package edu.internet2.middleware.grouper.authentication.plugin.oidc.profile;

import org.pac4j.oidc.profile.OidcProfileDefinition;

public class ClaimAsUsernameProfileDefinition extends OidcProfileDefinition {
    public ClaimAsUsernameProfileDefinition(final String claimAsUsername) {
        super();
        setProfileFactory(x -> new ClaimAsUsernameProfile(claimAsUsername));
    }
}