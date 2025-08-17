package edu.internet2.middleware.grouper.authentication.plugin.config;

import java.util.Locale;

public enum ClientProviders {
    CAS (CasClientProvider.class),
    OIDC (OidcClientProvider.class),
    SAML (SAML2ClientProvider.class);

    private final Class<? extends ClientProvider> providerClass;

    ClientProviders(Class<? extends ClientProvider> clazz) {
        this.providerClass = clazz;
    }

    public Class<? extends ClientProvider> getProviderClass() {
        return this.providerClass;
    }

    public static ClientProviders fromString(String name) {
        return ClientProviders.valueOf(name.toUpperCase(Locale.ENGLISH));
    }
}