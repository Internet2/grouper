package edu.internet2.middleware.grouper.authentication.plugin.oidc.client;

import edu.internet2.middleware.grouper.authentication.plugin.oidc.config.ClaimAsUsernameOidcConfiguration;
import edu.internet2.middleware.grouper.authentication.plugin.oidc.profile.ClaimAsUsernameProfileCreator;
import org.pac4j.oidc.client.OidcClient;

public class ClaimAsUsernameOidcClient extends OidcClient {
    public ClaimAsUsernameOidcClient(final ClaimAsUsernameOidcConfiguration claimAsUsernameOidcConfiguration) {
        super(claimAsUsernameOidcConfiguration);
    }

    @Override
    public void init() {
        this.defaultProfileCreator(new ClaimAsUsernameProfileCreator(this.getConfiguration(), this));

        super.init();
    }
}