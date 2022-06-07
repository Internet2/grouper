package edu.internet2.middleware.grouper.authentication.plugin.config;

import edu.internet2.middleware.grouper.authentication.plugin.ConfigUtils;
import edu.internet2.middleware.grouper.authentication.plugin.ExternalAuthenticationServletContainerInitializer;
import edu.internet2.middleware.grouper.authentication.plugin.Pac4jConfigFactory;
import edu.internet2.middleware.grouper.authentication.plugin.oidc.client.ClaimAsUsernameOidcClient;
import edu.internet2.middleware.grouper.authentication.plugin.oidc.config.ClaimAsUsernameOidcConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.pac4j.core.client.Client;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

public class OidcClientProvider implements ClientProvider {
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
    public boolean supports(String type) {
        return "oidc".equals(type);
    }

    @Override
    public Client getClient() {
        OidcClient client;
        String implementation = ConfigUtils.getBestGrouperConfiguration().propertyValueString("external.authentication.mechanism.oidc.clientImplementation");
        if (implementation != null && !implementation.isEmpty()) {
            try {
                OidcConfiguration configuration = (OidcConfiguration) Class.forName(implementation).newInstance();
                client = new OidcClient(configuration);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
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
