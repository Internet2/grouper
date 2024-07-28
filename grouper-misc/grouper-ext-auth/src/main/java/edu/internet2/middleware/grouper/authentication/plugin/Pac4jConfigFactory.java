package edu.internet2.middleware.grouper.authentication.plugin;

import edu.internet2.middleware.grouper.authentication.plugin.config.ClientProvider;
import edu.internet2.middleware.grouper.authentication.plugin.config.ClientProviders;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.matching.matcher.PathMatcher;

import java.lang.reflect.InvocationTargetException;

public class Pac4jConfigFactory implements ConfigFactory {
    private static final Log LOGGER = GrouperAuthentication.getLogFactory().getInstance(Pac4jConfigFactory.class);

    @Override
    public Config build(Object... parameters) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            ConfigPropertiesCascadeBase grouperConfig = ConfigUtils.getBestGrouperConfiguration();

            String provider;
            if (grouperConfig.containsKey("external.authentication.mechanism")) {
                LOGGER.warn("you're using the deprecated key `external.authentication.mechanism`; please update to `external.authentication.provider`");
                provider = grouperConfig.propertyValueString("external.authentication.mechanism");
            } else {
                provider = grouperConfig.propertyValueString("external.authentication.provider");
            }
            Client client = getClient(provider);

            String callbackUrl = grouperConfig.propertyValueString("external.authentication.grouperContextUrl")
                    + grouperConfig.propertyValueString("external.authentication.callbackUrl", "/callback");
            final Clients clients = new Clients(callbackUrl, client);

            final Config config = new Config(clients);

            PathMatcher pathMatcher = new PathMatcher();

            String securityExclusionsPaths = grouperConfig.propertyValueString("external.authentication.exclusions", "/status");
            if (securityExclusionsPaths != null) {
                for (String exclusion : securityExclusionsPaths.split(",")) {
                    pathMatcher.excludeBranch(StringUtils.trim(exclusion));
                }
            }
            config.addMatcher("securityExclusions", pathMatcher);
            return config;
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("problem configuring pac4j", e);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    //TODO: can this be checked
    @SuppressWarnings("unchecked")
    private static Client getClient(String provider) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
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
        return providerClass.getDeclaredConstructor().newInstance().getClient();
    }
}
