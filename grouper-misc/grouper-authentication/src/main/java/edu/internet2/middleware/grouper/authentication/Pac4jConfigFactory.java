package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.authentication.config.ClientProvider;
import edu.internet2.middleware.grouper.authentication.config.ClientProviders;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import org.apache.log4j.Logger;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.matching.matcher.PathMatcher;

public class Pac4jConfigFactory implements ConfigFactory {
    private static final Logger LOGGER = Logger.getLogger(Pac4jConfigFactory.class);

    @Override
    public Config build(Object... parameters) {
        try {
            String provider;
            if (GrouperUiConfig.retrieveConfig().containsKey("external.authentication.mechanism")) {
                LOGGER.warn("you're using the deprecated key `external.authentication.mechanism`; please update to `external.authentication.provider`");
                provider = GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.mechanism");
            } else {
                provider = GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.provider");
            }
            Client client = getClient(provider);

            String callbackUrl = GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.grouperContextUrl")
                    + GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.callbackUrl", "/callback");
            final Clients clients = new Clients(callbackUrl, client);

            final Config config = new Config(clients);
            config.addMatcher("excludePathServices", new PathMatcher().excludeBranch("/services"));
            config.addMatcher("excludePathServicesRest", new PathMatcher().excludeBranch("/servicesRest"));
            return config;
        } catch (IllegalAccessException|InstantiationException e) {
            throw new RuntimeException("problem configuring pac4j", e);
        }
    }

    private static Client getClient(String provider) throws IllegalAccessException, InstantiationException {
        Class<? extends ClientProvider> providerClass;
        //TODO: might be a better way of doing this
        try {
            providerClass = ClientProviders.fromString(provider).getProviderClass();
        } catch (IllegalArgumentException e) {
            try {
                providerClass = (Class<? extends ClientProvider>) Class.forName(provider);
            } catch (ClassNotFoundException classNotFoundException) {
                throw new RuntimeException(classNotFoundException);
            }
        }
        return providerClass.newInstance().getClient();
    }
}
