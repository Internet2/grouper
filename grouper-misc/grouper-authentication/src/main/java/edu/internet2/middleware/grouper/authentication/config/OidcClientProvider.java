package edu.internet2.middleware.grouper.authentication.config;

import edu.internet2.middleware.grouper.authentication.ConfigUtils;
import edu.internet2.middleware.grouper.authentication.oidc.config.ClaimAsUsernameOidcConfiguration;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import org.apache.log4j.Logger;
import org.pac4j.core.client.Client;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

public class OidcClientProvider implements ClientProvider {
    private static Logger LOGGER = Logger.getLogger(OidcClientProvider.class);

    @Override
    public boolean supports(String type) {
        return "oidc".equals(type);
    }

    @Override
    public Client getClient() {
        OidcConfiguration configuration = new ClaimAsUsernameOidcConfiguration();
        String implementation = GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.mechanism.oidc.clientImplementation");
        if (implementation != null && !implementation.isEmpty()) {
            try {
                configuration = (OidcConfiguration) Class.forName(implementation).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                LOGGER.warn("problem loading pac4j client implementation; using a default", e);
                configuration = new ClaimAsUsernameOidcConfiguration();
            }
        } else {
            configuration = new ClaimAsUsernameOidcConfiguration();
        }
        ConfigUtils.setProperties(configuration, "oidc");
        OidcClient client = new OidcClient(configuration);

        //TODO: make configurable
        client.setName("client");
        return client;
    }
}
