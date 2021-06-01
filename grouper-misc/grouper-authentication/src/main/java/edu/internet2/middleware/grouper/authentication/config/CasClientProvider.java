package edu.internet2.middleware.grouper.authentication.config;

import edu.internet2.middleware.grouper.authentication.ConfigUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.Client;

public class CasClientProvider implements ClientProvider {
    @Override
    public boolean supports(String type) {
        return "cas".equals(type);
    }

    @Override
    public Client getClient() {
        final CasConfiguration configuration = new CasConfiguration();
        ConfigUtils.setProperties(GrouperUiConfigInApi.retrieveConfig(), configuration, "cas");
        CasClient client = new CasClient(configuration);
        //TODO: make configurable
        client.setName("client");
        return client;
    }
}
