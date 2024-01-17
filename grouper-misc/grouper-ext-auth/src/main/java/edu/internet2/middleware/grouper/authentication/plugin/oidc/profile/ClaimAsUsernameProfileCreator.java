package edu.internet2.middleware.grouper.authentication.plugin.oidc.profile;

import edu.internet2.middleware.grouper.authentication.plugin.oidc.config.ClaimAsUsernameOidcConfiguration;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.profile.creator.OidcProfileCreator;

public class ClaimAsUsernameProfileCreator extends OidcProfileCreator {
    public ClaimAsUsernameProfileCreator(OidcConfiguration configuration, OidcClient client) {
        super(configuration, client);
    }

    @Override
    protected void internalInit(boolean forceReinit) {
        CommonHelper.assertNotNull("claimAsUsername", ((ClaimAsUsernameOidcConfiguration)this.configuration).getClaimAsUsername());

        defaultProfileDefinition(new ClaimAsUsernameProfileDefinition(((ClaimAsUsernameOidcConfiguration)this.configuration).getClaimAsUsername()));

        super.internalInit(forceReinit);
    }
}