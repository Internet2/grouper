/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.ldap.ldaptive;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.ldap.LdapConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.ldaptive.CompareConnectionValidator;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.LdapURL;
import org.ldaptive.PooledConnectionFactory;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.SearchResultHandler;
import org.ldaptive.props.BindConnectionInitializerPropertySource;
import org.ldaptive.props.BlockingConnectionPoolPropertySource;
import org.ldaptive.props.ConnectionConfigPropertySource;
import org.ldaptive.props.PooledConnectionFactoryPropertySource;
import org.ldaptive.props.SearchConnectionValidatorPropertySource;
import org.ldaptive.props.SearchRequestPropertySource;
import org.ldaptive.props.SslConfigPropertySource;

/**
 * Provides thread safe access to the creation and management of configuration data for LDAP servers.
 */
final class LdaptiveConfiguration {

  /** Logger for this class. */
  private static final Log LOG = GrouperUtil.getLog(LdaptiveConfiguration.class);

  /**
   * What ldaptive properties will be decrypted if their values are Morph files?
   * (We don't decrypt all properties because that would prevent the use of slashes in the property values)
   **/
  private static final String[] ENCRYPTABLE_LDAPTIVE_PROPERTIES = new String[] {"org.ldaptive.bindCredential"};

  /** map of connection name to properties */
  private static final Map<String, Config> configuration = new HashMap<>();

  /**
   * Creates a new pooled connection factory using configuration for the supplied ldap server id.
   *
   * @param ldapServerId to retrieve configuration for
   *
   * @return new pooled connection factory
   */
  static PooledConnectionFactory createPooledConnectionFactory(String ldapServerId) {
    Properties ldaptiveProperties = getConfig(ldapServerId).getProperties();

    // we don't allow the base dn in the URL anymore
    String urlString = ldaptiveProperties.getProperty("org.ldaptive.ldapUrl");
    if (!StringUtils.isBlank(urlString)) {
      LdapURL url = new LdapURL(urlString);
      if (!url.isDefaultBaseDn()) {
        throw new RuntimeException("Base DN not allowed to be configured in the ldap URL: " + urlString);
      }
    }

    ConnectionConfig connConfig = new ConnectionConfig();
    ConnectionConfigPropertySource ccpSource = new ConnectionConfigPropertySource(connConfig, ldaptiveProperties);
    ccpSource.initialize();

    PooledConnectionFactory connectionFactory = new PooledConnectionFactory();
    connectionFactory.setConnectionConfig(connConfig);
    PooledConnectionFactoryPropertySource pcfpSource = new PooledConnectionFactoryPropertySource(connectionFactory, ldaptiveProperties);
    pcfpSource.initialize();
    return connectionFactory;
  }

  static boolean hasConfig(String ldapServerId) {
    synchronized (configuration) {
      return configuration.containsKey(ldapServerId);
    }
  }

  /**
   * Retrieves the {@link Config} for the supplied ldap server id. If the configuration cannot be found a new
   * configuration if created. See {@link #createConfig(String)}.
   *
   * @param ldapServerId to retrieve configuration for
   *
   * @return ldaptive configuration
   */
  static Config getConfig(String ldapServerId) {
    synchronized (configuration) {
      if (!configuration.containsKey(ldapServerId)) {
        configuration.put(ldapServerId, createConfig(ldapServerId));
      }
      return configuration.get(ldapServerId);
    }
  }

  /**
   * Removes the configuration for the supplied ldap server id.
   *
   * @param ldapServerId to remove configuration for
   */
  static void removeConfig(String ldapServerId) {
    synchronized (configuration) {
      configuration.remove(ldapServerId);
    }
  }

  /**
   * Creates a new configuration for the supplied ldap server id.
   *
   * @param ldapServerId to create configuration for
   *
   * @return new ldaptive configuration
   */
  static Config createConfig(String ldapServerId) {
    Properties ldaptiveProperties = new Properties();
    String ldapPropertyPrefix = "ldap." + ldapServerId + ".";

    // load this ldaptive config file before the configs here.  load from classpath
    String configFileFromClasspathParam = ldapPropertyPrefix + "configFileFromClasspath";
    String configFileFromClasspathValue = GrouperLoaderConfig.retrieveConfig().propertyValueString(configFileFromClasspathParam);
    if (!StringUtils.isBlank(configFileFromClasspathValue)) {
      URL url = GrouperUtil.computeUrl(configFileFromClasspathValue, false);
      try {
        ldaptiveProperties.load(url.openStream());
      } catch (IOException ioe) {
        throw new RuntimeException("Error processing classpath file: " + configFileFromClasspathValue, ioe);
      }
    }

    for (String propName : GrouperLoaderConfig.retrieveConfig().propertyNames()) {
      if ( propName.startsWith(ldapPropertyPrefix) ) {
        String propValue = GrouperLoaderConfig.retrieveConfig().propertyValueString(propName, "");

        // Get the part of the property after ldapPropertyPrefix 'ldap.person.'
        String propNameTail = propName.substring(ldapPropertyPrefix.length());

        if (propValue == null) {
          propValue = "";
        }

        // GRP-4484: ldaptive upgrade now uses durations
        if (propNameTail.equalsIgnoreCase("timeout") || propNameTail.equalsIgnoreCase("timeLimit")) {
          if (!StringUtils.isBlank(propValue)) {
            try {
              propValue = Duration.ofMillis(GrouperUtil.longValue(propValue)).toString();
            } catch (Throwable t) {
              // if its not a number, then forget it
              LOG.debug("Error parsing: " + propValue, t);
            }
          }
        }

        // map old property names to current names
        switch (propNameTail) {
          case "url":
            propNameTail = "ldapUrl";
            break;
          case "tls":
            // tls (vtldap) ==> useStartTls
            propNameTail = "useStartTLS";
            break;
          case "user":
            // user (vtldap) ==> bindDn
            propNameTail = "bindDn";
            break;
          case "pass":
            // pass (vtldap) ==> bindCredential
            propNameTail = "bindCredential";
            break;
          case "countLimit":
            // countLimit (vtldap) ==> sizeLimit
            propNameTail = "sizeLimit";
            break;
          case "timeout":
            // timeout (vtldap) ==> connectTimeout
            propNameTail = "connectTimeout";
            break;
          case "pruneTimerPeriod":
            propNameTail = "prunePeriod";
            break;
          case "expirationTime":
            propNameTail = "idleTime";
            break;
        }

        // convert properties to ldaptive domain
        if (ConnectionConfigPropertySource.getProperties().contains(propNameTail)) {
          ldaptiveProperties.put("org.ldaptive." + propNameTail, propValue);
        } else if (SslConfigPropertySource.getProperties().contains(propNameTail)) {
          ldaptiveProperties.put("org.ldaptive." + propNameTail, propValue);
        } else if (BindConnectionInitializerPropertySource.getProperties().contains(propNameTail)) {
          ldaptiveProperties.put("org.ldaptive." + propNameTail, propValue);
        } else if (SearchRequestPropertySource.getProperties().contains(propNameTail)) {
          ldaptiveProperties.put("org.ldaptive." + propNameTail, propValue);
        } else if (BlockingConnectionPoolPropertySource.getProperties().contains(propNameTail)) {
          ldaptiveProperties.put("org.ldaptive.pool." + propNameTail, propValue);
        } else if (SearchConnectionValidatorPropertySource.getProperties().contains(propNameTail)) {
          ldaptiveProperties.put("org.ldaptive.pool." + propNameTail, propValue);
        } else {
          LOG.info("Unknown ldap property: " + propNameTail + "=" + propValue);
        }
      }
    }

    addCredentialConfigProperty(ldapServerId, ldaptiveProperties);
    addSaslProperties(ldapServerId, ldaptiveProperties);
    addPruneStrategyProperty(ldapServerId, ldaptiveProperties);
    addValidatorProperty(ldapServerId, ldaptiveProperties);

    // Go through the properties that can be encrypted and decrypt them if they're Morph files
    for (String encryptablePropertyKey : ENCRYPTABLE_LDAPTIVE_PROPERTIES) {
      String value = ldaptiveProperties.getProperty(encryptablePropertyKey);
      value = Morph.decryptIfFile(value);
      if (!StringUtils.isBlank(value)) {
        ldaptiveProperties.put(encryptablePropertyKey, value);
      }
    }
    return new Config(ldaptiveProperties, createEntryHandlers(ldapServerId), createResultHandlers(ldapServerId));
  }

  /**
   * Adds an ldaptive credential config to the supplied properties iff pemCaFile, pemCertFile and pemKeyFile are all
   * specified.
   *
   * @param ldapServerId configuration reference
   * @param props to update
   */
  private static void addCredentialConfigProperty(String ldapServerId, Properties props) {
    String cafile = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".pemCaFile");
    String certfile = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".pemCertFile");
    String keyfile = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".pemKeyFile");
    if (cafile != null && certfile != null && keyfile != null) {
      props.put(
        "org.ldaptive.credentialConfig",
        String.format(
          "edu.internet2.middleware.grouper.ldap.ldaptive.LdapPEMCredentialConfig{{caFile=%1$s}{certFile=%2$s}{keyFile=%3$s}}",
          cafile,
          certfile,
          keyfile));
    }
  }

  /**
   * Adds an ldaptive SASL config to the supplied properties iff saslMechanism is specified.
   *
   * @param ldapServerId configuration reference
   * @param props to update
   */
  private static void addSaslProperties(String ldapServerId, Properties props) {
    String mechanism = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".saslMechanism");
    if (!StringUtils.isBlank(mechanism)) {
      StringBuilder saslConfig = new StringBuilder("org.ldaptive.sasl.SaslConfig{{mechanism=").append(mechanism).append("}");
      String realm = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".saslRealm");
      if (!StringUtils.isBlank(realm)) {
        saslConfig.append("{realm=").append(realm).append("}");
      }
      String authorizationId = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".saslAuthorizationId");
      if (!StringUtils.isBlank(authorizationId)) {
        saslConfig.append("{authorizationId=").append(authorizationId).append("}");
      }
      saslConfig.append("}");
      props.put("org.ldaptive.bindSaslConfig", saslConfig.toString());
    }
  }

  /**
   * Adds an ldaptive pooling prune strategy to the supplied properties. If pruneTimerPeriod or expirationTime is not
   * specified, default values are used.
   *
   * @param ldapServerId configuration reference
   * @param props to update
   */
  private static void addPruneStrategyProperty(String ldapServerId, Properties props) {
    String pruneStrategy = "org.ldaptive.pool.IdlePruneStrategy{{prunePeriod=%1$s}{idleTime=%2$s}}";
    pruneStrategy = String.format(
      pruneStrategy,
      Duration.ofMillis(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + ldapServerId + ".pruneTimerPeriod", 300000)),
      Duration.ofMillis(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + ldapServerId + ".expirationTime", 600000)));
    props.put("org.ldaptive.pool.pruneStrategy", pruneStrategy);
  }

  /**
   * Adds an ldaptive pooling validator to the supplied properties. If no validation is configured, periodic validation
   * will be enabled.
   *
   * @param ldapServerId configuration reference
   * @param props to update
   */
  private static void addValidatorProperty(String ldapServerId, Properties props) {
    String validator;
    String validatorClass = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".validator", "SearchConnectionValidator");
    if (StringUtils.equalsIgnoreCase(validatorClass, CompareConnectionValidator.class.getSimpleName()) ||
      StringUtils.equalsIgnoreCase(validatorClass, "CompareLdapValidator")) {
      validator = "edu.internet2.middleware.grouper.ldap.ldaptive.LdaptiveConnectionValidator{{validatePeriod=%1$s}{dn=%2$s}{name=%3$s}{value=%4$s}}";
      validator = String.format(
        validator,
        Duration.ofMillis(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + ldapServerId + ".validateTimerPeriod", 5*60*1000)),
        GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("ldap." + ldapServerId + ".validatorCompareDn"),
        GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("ldap." + ldapServerId + ".validatorCompareAttribute"),
        GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("ldap." + ldapServerId + ".validatorCompareValue"));
    } else {
      validator = "org.ldaptive.SearchConnectionValidator{{validatePeriod=%1$s}}";
      validator = String.format(
        validator,
        Duration.ofMillis(GrouperLoaderConfig.retrieveConfig().propertyValueInt("ldap." + ldapServerId + ".validateTimerPeriod", 5*60*1000)));
    }
    props.put("org.ldaptive.pool.validator", validator);

    // Make sure some kind of validation is turned on
    boolean poolValidate =
      Boolean.parseBoolean(props.getProperty("org.ldaptive.pool.validateOnCheckIn", "false")) ||
        Boolean.parseBoolean(props.getProperty("org.ldaptive.pool.validateOnCheckOut", "false")) ||
        Boolean.parseBoolean(props.getProperty("org.ldaptive.pool.validatePeriodically", "false"));
    if (!poolValidate) {
      props.put("org.ldaptive.pool.validatePeriodically", "true");
    }
  }

  /**
   * Creates a new list of ldap entry handlers for the supplied ldap server id.
   *
   * @param ldapServerId to create entry handlers for
   *
   * @return entry handlers
   */
  private static LdapEntryHandler[] createEntryHandlers(String ldapServerId) {
    Set<LdapEntryHandler> handlers = new LinkedHashSet<>();

    String handlerNames = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".searchResultHandlers");
    if (!StringUtils.isBlank(handlerNames)) {
      String[] handlerClassNames = GrouperUtil.splitTrim(handlerNames, ",");
      for (String className : handlerClassNames) {
        if (className.equals("edu.internet2.middleware.grouper.ldap.handler.RangeSearchResultHandler")) {
          className = "";
        } else if (className.equals("edu.vt.middleware.ldap.handler.EntryDnSearchResultHandler")) {
          className = "org.ldaptive.handler.DnAttributeEntryHandler";
        }
        if (!StringUtils.isBlank(className)) {
          try {
            Class<LdapEntryHandler> customClass = GrouperUtil.forName(className);
            handlers.add(GrouperUtil.newInstance(customClass));
          } catch (ClassCastException e) {
            LOG.debug("Ignoring ldap entry handler of incorrect type: " + className, e);
          }
        }
      }
    }
    return !handlers.isEmpty() ? handlers.toArray(LdapEntryHandler[]::new) : null;
  }

  /**
   * Creates a new list of ldap entry handlers for the supplied ldap server id.
   *
   * @param ldapServerId to create entry handlers for
   *
   * @return entry handlers
   */
  private static SearchResultHandler[] createResultHandlers(String ldapServerId) {
    Set<SearchResultHandler> handlers = new LinkedHashSet<>();
    boolean isActiveDirectory = LdapConfiguration.getConfig(ldapServerId).isActiveDirectory();

    String handlerNames = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + ldapServerId + ".searchResultHandlers");
    if (StringUtils.isEmpty(handlerNames) && isActiveDirectory) {
      handlerNames = "edu.internet2.middleware.grouper.ldap.ldaptive.GrouperRangeEntryHandler";
    }

    if (!StringUtils.isBlank(handlerNames)) {
      String[] handlerClassNames = GrouperUtil.splitTrim(handlerNames, ",");
      for (String className : handlerClassNames) {
        if (className.equals("edu.internet2.middleware.grouper.ldap.handler.RangeSearchResultHandler")) {
          className = "edu.internet2.middleware.grouper.ldap.ldaptive.GrouperRangeEntryHandler";
        } else if (className.equals("edu.vt.middleware.ldap.handler.EntryDnSearchResultHandler")) {
          className = "";
        }
        if (!StringUtils.isBlank(className)) {
          try {
            Class<SearchResultHandler> customClass = GrouperUtil.forName(className);
            handlers.add(GrouperUtil.newInstance(customClass));
          } catch (ClassCastException e) {
            LOG.debug("Ignoring search result handler of incorrect type: " + className, e);
          }
        }
      }
    }
    return !handlers.isEmpty() ? handlers.toArray(SearchResultHandler[]::new) : null;
  }

  /**
   * Container for objects associated with a specific server configuration.
   */
  static class Config {
    private final Properties properties;
    private final LdapEntryHandler[] ldapEntryHandlers;
    private final SearchResultHandler[] searchResultHandlers;

    Config(Properties props, LdapEntryHandler[] entryHandlers, SearchResultHandler[] resultHandlers) {
      properties = props;
      ldapEntryHandlers = entryHandlers;
      searchResultHandlers = resultHandlers;
    }

    Properties getProperties() {
      return properties;
    }

    LdapEntryHandler[] getLdapEntryHandlers() {
      return ldapEntryHandlers;
    }

    SearchResultHandler[] getSearchResultHandlers() {
      return searchResultHandlers;
    }

    @Override
    public boolean equals(final Object o) {
      if (o == this) {
        return true;
      }
      if (o instanceof Config) {
        Config other = (Config)o;
        return properties.equals(other.properties) &&
          Arrays.equals(ldapEntryHandlers, other.ldapEntryHandlers) &&
          Arrays.equals(searchResultHandlers, other.searchResultHandlers);
      }
      return false;
    }
  }
}
