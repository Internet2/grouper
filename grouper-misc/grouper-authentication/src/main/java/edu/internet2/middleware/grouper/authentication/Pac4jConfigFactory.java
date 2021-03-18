package edu.internet2.middleware.grouper.authentication;

import edu.internet2.middleware.grouper.authentication.oidc.config.ClaimAsUsernameOidcConfiguration;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import org.apache.log4j.Logger;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.matching.matcher.PathMatcher;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.config.SAML2Configuration;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.time.Period;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Pac4jConfigFactory implements ConfigFactory {
    private static final Logger LOGGER = Logger.getLogger(Pac4jConfigFactory.class);

    @Override
    public Config build(Object... parameters) {
        Client client;
        String authMechanism = GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.mechanism");
        switch (authMechanism) {
            case "oidc": {
                OidcConfiguration configuration = new ClaimAsUsernameOidcConfiguration();
                /*
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

                 */
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

        String callbackUrl = GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.grouperContextUrl")
                           + GrouperUiConfig.retrieveConfig().propertyValueString("external.authentication.callbackUrl", "/callback");
        final Clients clients = new Clients(callbackUrl, client);

        final Config config = new Config(clients);
        config.addMatcher("excludePathServices", new PathMatcher().excludeBranch("/services"));
        config.addMatcher("excludePathServicesRest", new PathMatcher().excludeBranch("/servicesRest"));
        return config;
    }

    private void setProperties(Object configuration, String authMechanism) {
        Class<?> clazz = configuration.getClass();
        for (String name : GrouperUiConfig.retrieveConfig().propertyNames()) {
            if (name.startsWith("external.authentication." + authMechanism)) {
                try {
                    Field field = clazz.getDeclaredField(name.substring(name.lastIndexOf('.') + 1, name.length() ));
                    field.setAccessible(true);
                    field.set(configuration, getProperty(field.getType(), name));
                } catch (NoSuchFieldException e) {
                    throw new IllegalStateException("Unexpected property name: " + name);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Unable to access property name: " + name);
                }
            }
        }
    }

    private Object getProperty(Type type, String propName) {
        switch (type.getTypeName()) {
            case "java.lang.String" : {
                return GrouperUiConfig.retrieveConfig().propertyValueString(propName);
            }
            case "int" :
            case "java.lang.Integer" : {
                return GrouperUiConfig.retrieveConfig().propertyValueInt(propName);
            }
            case "long" :
            case "java.lang.Long" : {
                return Long.parseLong(GrouperUiConfig.retrieveConfig().propertyValueString(propName));
            }
            case "double" :
            case "java.lang.Double" : {
                return Double.parseDouble(GrouperUiConfig.retrieveConfig().propertyValueString(propName));
            }
            case "boolean" :
            case "java.lang.Boolean" : {
                return GrouperUiConfig.retrieveConfig().propertyValueBoolean(propName);
            }
            case "java.util.List" :
            case "java.util.Collection" :{
                return Arrays.asList(GrouperUiConfig.retrieveConfig().propertyValueString(propName).split(","));
            }
            case "java.util.Set" : {
                Set set = new HashSet();
                for (String prop : GrouperUiConfig.retrieveConfig().propertyValueString(propName).split(",")) {
                    set.add(prop);
                }
                return set;
            }
            case "java.util.Map" : {
                Map<String, String> map = new HashMap();
                for (String pairs : GrouperUiConfig.retrieveConfig().propertyValueString(propName).split(",")) {
                    String [] keyValue = pairs.split("=");
                    map.put(keyValue[0].trim(),keyValue[1].trim());
                }
                return map;
            }
            case "java.time.Period" : {
                return Period.parse(GrouperUiConfig.retrieveConfig().propertyValueString(propName));
            }
            default:
                throw new IllegalStateException("Unexpected type: " + type.getTypeName());
        }
    }
}
