package edu.internet2.middleware.grouper.authentication.plugin.config;

import edu.internet2.middleware.grouper.authentication.plugin.ConfigUtils;
import org.pac4j.core.client.Client;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;

public class SAML2ClientProvider implements ClientProvider {
    @Override
    public boolean supports(String type) {
        return "saml".equals(type);
    }

    @Override
    public Client getClient() {
        final SAML2Configuration configuration = new SAML2Configuration();
        ConfigUtils.setProperties(configuration, "saml");
        SAML2Client client = new SAML2Client(configuration);

        //TODO: make configurable
        client.setName("client");
        return client;
    }
}