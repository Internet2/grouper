package edu.internet2.middleware.grouper.authentication.config;

import org.pac4j.core.client.Client;

public interface ClientProvider {
    boolean supports(String type);
    Client getClient();
}
