package edu.internet2.middleware.grouper.authentication.plugin;

import edu.internet2.middleware.grouper.authentication.plugin.config.ClientProvider;
import edu.internet2.middleware.grouper.authentication.plugin.config.ClientProviders;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.matching.matcher.PathMatcher;

public class Pac4jConfigFactory implements ConfigFactory {
    // private static final Logger LOGGER = Logger.getLogger(Pac4jConfigFactory.class);
    private static final Log LOGGER;
    static {
        try {
            BundleContext bundleContext = FrameworkUtil.getBundle(Pac4jConfigFactory.class).getBundleContext();
            //TODO: figure out why this is weird
            ServiceReference<LogFactory> logfactoryReference = (ServiceReference<LogFactory>) bundleContext.getAllServiceReferences("org.apache.commons.logging.LogFactory", null)[0];
            LOGGER = bundleContext.getService(logfactoryReference).getInstance(ExternalAuthenticationServletContainerInitializer.class);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Config build(Object... parameters) {
        try {
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

            for (String exclusion : grouperConfig.propertyValueString("external.authentication.exclusions", "/status").split(",")) {
                pathMatcher.excludeBranch(StringUtils.trim(exclusion));
            }

            config.addMatcher("securityExclusions", pathMatcher);
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
