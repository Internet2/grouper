package edu.internet2.middleware.grouper.authentication.oidc.config;

import org.pac4j.oidc.config.OidcConfiguration;

public class ClaimAsUsernameOidcConfiguration extends OidcConfiguration {
    private String claimAsUsername;

    public String getClaimAsUsername() {
        return claimAsUsername;
    }

    public void setClaimAsUsername(String claimAsUsername) {
        this.claimAsUsername = claimAsUsername;
    }
}
