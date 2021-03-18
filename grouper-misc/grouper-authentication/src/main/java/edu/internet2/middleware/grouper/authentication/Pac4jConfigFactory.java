package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.authentication.config.ClientProvider;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import org.apache.log4j.Logger;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.matching.matcher.PathMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class Pac4jConfigFactory implements ConfigFactory {
    private static final Logger LOGGER = Logger.getLogger(Pac4jConfigFactory.class);

    @Override
    public Config build(Object... parameters) {
        ServiceLoader<ClientProvider> loader = ServiceLoader.load(ClientProvider.class);
        List<Client> clientList = new ArrayList<>();
        String authMechanism = GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.mechanism");
        loader.forEach( clientProvider -> {
            if (clientProvider.supports(authMechanism)) {
                clientList.add(clientProvider.getClient());
            }
        });
        /*
        Client client;
        String authMechanism = GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.mechanism");
        switch (authMechanism) {
            case "oidc": {
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
                // setProperties(configuration, authMechanism);
                client = new OidcClient(configuration);
                break;
            }
            case "cas": {
                final CasConfiguration configuration = new CasConfiguration();
                setProperties(configuration, authMechanism);
                client = new CasClient(configuration);
                break;
            }
            case "saml": {
                final SAML2Configuration configuration = new SAML2Configuration();
                setProperties(configuration, authMechanism);
                client = new SAML2Client(configuration);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + authMechanism);
        }
        ((BaseClient)client).setName("client");
         */

        String callbackUrl = GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.grouperContextUrl")
                           + GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.callbackUrl", "/callback");
        final Clients clients = new Clients(callbackUrl, clientList);

        final Config config = new Config(clients);
        config.addMatcher("excludePathServices", new PathMatcher().excludeBranch("/services"));
        config.addMatcher("excludePathServicesRest", new PathMatcher().excludeBranch("/servicesRest"));
        return config;
    }
}
