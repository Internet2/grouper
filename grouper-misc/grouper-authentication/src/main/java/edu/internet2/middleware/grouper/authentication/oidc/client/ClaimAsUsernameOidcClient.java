package edu.internet2.middleware.grouper.authentication.oidc.client;

import edu.internet2.middleware.grouper.authentication.oidc.config.ClaimAsUsernameOidcConfiguration;
import edu.internet2.middleware.grouper.authentication.oidc.profile.ClaimAsUsernameProfileCreator;
import org.pac4j.oidc.client.OidcClient;

public class ClaimAsUsernameOidcClient extends OidcClient<ClaimAsUsernameOidcConfiguration> {
    public ClaimAsUsernameOidcClient(final ClaimAsUsernameOidcConfiguration claimAsUsernameOidcConfiguration) {
        super(claimAsUsernameOidcConfiguration);
    }

    @Override
    protected void clientInit() {
        this.defaultProfileCreator(new ClaimAsUsernameProfileCreator(this.getConfiguration(), this));

        super.clientInit();
    }
}
