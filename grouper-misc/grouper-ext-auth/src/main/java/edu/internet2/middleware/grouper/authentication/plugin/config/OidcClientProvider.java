package edu.internet2.middleware.grouper.authentication.plugin.config;

import edu.internet2.middleware.grouper.authentication.plugin.ConfigUtils;
import edu.internet2.middleware.grouper.authentication.plugin.GrouperAuthentication;
import edu.internet2.middleware.grouper.authentication.plugin.oidc.client.ClaimAsUsernameOidcClient;
import edu.internet2.middleware.grouper.authentication.plugin.oidc.config.ClaimAsUsernameOidcConfiguration;
import org.apache.commons.logging.Log;
import org.pac4j.core.client.Client;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

import java.lang.reflect.InvocationTargetException;

public class OidcClientProvider implements ClientProvider {
    private static final Log LOGGER = GrouperAuthentication.getLogFactory().getInstance(OidcClientProvider.class);

    @Override
    public boolean supports(String type) {
        return "oidc".equals(type);
    }

    @Override
    public Client getClient() {
        OidcClient client;
        String implementation = ConfigUtils.getBestGrouperConfiguration().propertyValueString("external.authentication.mechanism.oidc.clientImplementation");
        if (implementation != null && !implementation.isEmpty()) {
            try {
                OidcConfiguration configuration = (OidcConfiguration) Class.forName(implementation).getDeclaredConstructor().newInstance();
                client = new OidcClient(configuration);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException |
                     InvocationTargetException e) {
                //TODO: check whether this is actually a good idea and/or approapriate. there are a few cases that might need to be handled differently (e.g., wrong classname)
                LOGGER.warn("problem loading pac4j client implementation; using a default", e);
                client = getClaimAsUsernameOidcClient();
            }
        } else {
            client = getClaimAsUsernameOidcClient();
        }
        ConfigUtils.setProperties(client.getConfiguration(), "oidc");

        //TODO: make configurable
        client.setName("client");
        return client;
    }

    private static ClaimAsUsernameOidcClient getClaimAsUsernameOidcClient() {
        ClaimAsUsernameOidcConfiguration configuration = new ClaimAsUsernameOidcConfiguration();
        String claimAsUsername = ConfigUtils.getBestGrouperConfiguration().propertyValueString("external.authentication.oidc.claimAsUsername");
        if (claimAsUsername != null) {
            configuration.setClaimAsUsername(claimAsUsername);
        }
        ClaimAsUsernameOidcClient client = new ClaimAsUsernameOidcClient(configuration);
        return client;
    }
}
