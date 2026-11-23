package edu.internet2.middleware.grouper.app.config.check.ws;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.ws.GrouperWsConfigInApi;

/**
 * SECURITY (GRP-7380, complements GRP-7577): when grouper-ws.properties
 * ws.security.non-rampart.authentication.class is WsGrouperOauthAuthentication, WS refuses an OAuth
 * access token (HTTP 401) unless the OIDC external system the request uses is enabled, has
 * useForWs=true and claimSource=userInfoEndpoint, and sets wsAccessTokenValidation (jwt and
 * introspection also need wsAccessTokenAudiences or wsAccessTokenAllowedClientIds).  See
 * WsOauthAccessTokenPolicy in grouper-ws, whose rules this check mirrors (core cannot load that class,
 * so the authenticator is matched by class name).
 *
 * The OIDC config id is chosen per request: the grouperOidcConfigId request parameter, else
 * ws.security.authn.oauth.clientVersion.X.Y.Z.grouperOidcConfigId for the client version in the URL.
 * The request parameter cannot be known here, so the config ids named by the clientVersion keys are
 * checked; if there are none, every enabled OIDC external system with useForWs=true is checked for
 * wsAccessTokenValidation.  Each problem is an ERROR, since WS OAuth calls that use it get 401.
 */
public class WsOauthAuthenticatorConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * WS authentication class (grouper-ws.properties)
   */
  public static final String AUTHENTICATION_CLASS_PROPERTY_NAME = "ws.security.non-rampart.authentication.class";

  /**
   * the OAuth authenticator this check is about (matched by name, core cannot load it)
   */
  public static final String OAUTH_AUTHENTICATION_CLASS_NAME = "edu.internet2.middleware.grouper.ws.security.WsGrouperOauthAuthentication";

  /**
   * prefix of OIDC external system config (grouper.properties)
   */
  public static final String OIDC_CONFIG_PREFIX = "grouper.oidcExternalSystem.";

  /**
   * client version to OIDC config id mapping keys (grouper-ws.properties)
   */
  private static final Pattern CLIENT_VERSION_CONFIG_ID_PATTERN = Pattern.compile(
      "^ws\\.security\\.authn\\.oauth\\.clientVersion\\.([0-9]+\\.[0-9]+\\.[0-9]+)\\.grouperOidcConfigId$");

  /**
   * any key of an OIDC external system, group 1 is the config id
   */
  private static final Pattern OIDC_CONFIG_ID_PATTERN = Pattern.compile("^grouper\\.oidcExternalSystem\\.([^.]+)\\..+$");

  @Override
  public String getName() {
    return "wsOauthAuthenticator";
  }

  /**
   * @param authenticationClass value of ws.security.non-rampart.authentication.class
   * @return true if it is the WS OAuth authenticator
   */
  public static boolean isOauthAuthenticator(String authenticationClass) {
    if (StringUtils.isBlank(authenticationClass)) {
      return false;
    }
    String trimmed = authenticationClass.trim();
    return OAUTH_AUTHENTICATION_CLASS_NAME.equals(trimmed) || trimmed.endsWith(".WsGrouperOauthAuthentication")
        || "WsGrouperOauthAuthentication".equals(trimmed);
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    GrouperWsConfigInApi grouperWsConfig = GrouperWsConfigInApi.retrieveConfig();

    String authenticationClass = grouperWsConfig.propertyValueString(AUTHENTICATION_CLASS_PROPERTY_NAME);
    if (!isOauthAuthenticator(authenticationClass)) {
      return results;
    }

    GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();
    Set<String> existingOidcConfigIds = grouperConfig.propertyConfigIds(OIDC_CONFIG_ID_PATTERN);

    // config ids the clientVersion mappings name: config id to the mapping key that names it
    Map<String, String> mappedConfigIdToPropertyName = new LinkedHashMap<String, String>();
    Map<String, String> clientVersionMappings = grouperWsConfig.propertiesMap(CLIENT_VERSION_CONFIG_ID_PATTERN);
    for (String propertyName : clientVersionMappings.keySet()) {
      String configId = StringUtils.trimToNull(clientVersionMappings.get(propertyName));
      if (configId != null && !mappedConfigIdToPropertyName.containsKey(configId)) {
        mappedConfigIdToPropertyName.put(configId, propertyName);
      }
    }

    if (mappedConfigIdToPropertyName.size() > 0) {

      // the ids WS will use for those client versions must pass every gate in WsOauthAccessTokenPolicy
      for (String configId : mappedConfigIdToPropertyName.keySet()) {
        String mappingPropertyName = mappedConfigIdToPropertyName.get(configId);
        if (!existingOidcConfigIds.contains(configId)) {
          results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
              ConfigFileName.GROUPER_WS_PROPERTIES, mappingPropertyName, configId,
              mappingPropertyName + " names OIDC external system '" + configId + "', which is not configured.  "
                  + "WS OAuth calls from that client version get HTTP 401.",
              "Point " + mappingPropertyName + " at an OIDC external system that is configured for WS, or "
                  + "configure grouper.oidcExternalSystem." + configId + "."));
          continue;
        }
        checkOidcConfigAllowedForWs(grouperConfig, configId, results);
        checkWsAccessTokenValidation(grouperConfig, configId, results);
      }
      return results;
    }

    // no mapping: the request parameter picks the config, so check every config that is open to WS
    boolean foundOneForWs = false;
    for (String configId : existingOidcConfigIds) {
      if (!grouperConfig.propertyValueBoolean(OIDC_CONFIG_PREFIX + configId + ".enabled", true)) {
        continue;
      }
      if (!grouperConfig.propertyValueBoolean(OIDC_CONFIG_PREFIX + configId + ".useForWs", false)) {
        continue;
      }
      foundOneForWs = true;
      checkWsAccessTokenValidation(grouperConfig, configId, results);
    }

    if (!foundOneForWs) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_WS_PROPERTIES, AUTHENTICATION_CLASS_PROPERTY_NAME, authenticationClass,
          "WS authentication is WsGrouperOauthAuthentication, but no enabled OIDC external system has "
              + "useForWs=true.  WS OAuth calls get HTTP 401 (callers may still log in with the fallback class).",
          "Set useForWs=true, claimSource=userInfoEndpoint and wsAccessTokenValidation on the OIDC external "
              + "system that WS callers use, or change ws.security.non-rampart.authentication.class."));
    }

    return results;
  }

  /**
   * the gates of WsOauthAccessTokenPolicy.retrieveConfigAllowedForWs: enabled, useForWs, claimSource
   * @param grouperConfig grouper.properties
   * @param configId OIDC external system config id
   * @param results add findings here
   */
  private void checkOidcConfigAllowedForWs(GrouperConfig grouperConfig, String configId,
      List<ConfigurationCheckResult> results) {

    String enabledPropertyName = OIDC_CONFIG_PREFIX + configId + ".enabled";
    if (!grouperConfig.propertyValueBoolean(enabledPropertyName, true)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, enabledPropertyName, "false",
          "OIDC external system '" + configId + "' is used for WS OAuth but is not enabled.  WS OAuth calls "
              + "that use it get HTTP 401.",
          "Set " + enabledPropertyName + "=true, or stop mapping WS clients to '" + configId + "'."));
    }

    String useForWsPropertyName = OIDC_CONFIG_PREFIX + configId + ".useForWs";
    if (!grouperConfig.propertyValueBoolean(useForWsPropertyName, false)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, useForWsPropertyName,
          grouperConfig.propertyValueString(useForWsPropertyName, ""),
          "OIDC external system '" + configId + "' is used for WS OAuth but does not have useForWs=true.  "
              + "WS OAuth calls that use it get HTTP 401.",
          "Set " + useForWsPropertyName + "=true."));
    }

    // same default as GrouperOidcConfig
    String claimSourcePropertyName = OIDC_CONFIG_PREFIX + configId + ".claimSource";
    String claimSource = grouperConfig.propertyValueString(claimSourcePropertyName, "userInfoEndpoint");
    if (!"userInfoEndpoint".equals(claimSource)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, claimSourcePropertyName, claimSource,
          "OIDC external system '" + configId + "' is used for WS OAuth but claimSource is '" + claimSource
              + "'.  WS access tokens need claimSource=userInfoEndpoint, so WS OAuth calls that use it get HTTP 401.",
          "Set " + claimSourcePropertyName + "=userInfoEndpoint, or use a separate OIDC external system for WS."));
    }
  }

  /**
   * the rules of WsOauthAccessTokenPolicy.assertAccessTokenForWs that depend only on config
   * @param grouperConfig grouper.properties
   * @param configId OIDC external system config id
   * @param results add findings here
   */
  private void checkWsAccessTokenValidation(GrouperConfig grouperConfig, String configId,
      List<ConfigurationCheckResult> results) {

    String validationPropertyName = OIDC_CONFIG_PREFIX + configId + ".wsAccessTokenValidation";
    // trimmed, like WsOauthAccessTokenPolicy.configString
    String mode = StringUtils.trimToEmpty(grouperConfig.propertyValueString(validationPropertyName, ""));

    if (StringUtils.isBlank(mode)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, validationPropertyName, mode,
          "OIDC external system '" + configId + "' is used for WS OAuth but wsAccessTokenValidation is not "
              + "set, so every WS access token is refused (HTTP 401).",
          "Set " + validationPropertyName + " to jwt or introspection (with wsAccessTokenAudiences or "
              + "wsAccessTokenAllowedClientIds and the settings that go with the mode).  userInfoOnly also "
              + "works but keeps the older, weaker check."));
      return;
    }

    if ("userInfoOnly".equals(mode)) {
      // works; the weaker check is a separate warning (wsOidcAccessTokenUserInfoOnly)
      return;
    }

    if (!"jwt".equals(mode) && !"introspection".equals(mode)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, validationPropertyName, mode,
          "OIDC external system '" + configId + "' has wsAccessTokenValidation='" + mode + "', which is not "
              + "a valid mode, so every WS access token is refused (HTTP 401).",
          "Set " + validationPropertyName + " to jwt, introspection, or userInfoOnly (not recommended)."));
      return;
    }

    // jwt and introspection need at least one way to tell that the token is for Grouper WS
    String audiences = grouperConfig.propertyValueString(OIDC_CONFIG_PREFIX + configId + ".wsAccessTokenAudiences", "");
    String allowedClientIds = grouperConfig.propertyValueString(OIDC_CONFIG_PREFIX + configId + ".wsAccessTokenAllowedClientIds", "");
    if (StringUtils.isBlank(audiences) && StringUtils.isBlank(allowedClientIds)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, OIDC_CONFIG_PREFIX + configId + ".wsAccessTokenAudiences", audiences,
          "OIDC external system '" + configId + "' has wsAccessTokenValidation=" + mode + " but neither "
              + "wsAccessTokenAudiences nor wsAccessTokenAllowedClientIds is set, so every WS access token is "
              + "refused (HTTP 401).",
          "Set " + OIDC_CONFIG_PREFIX + configId + ".wsAccessTokenAudiences and/or "
              + OIDC_CONFIG_PREFIX + configId + ".wsAccessTokenAllowedClientIds to the audience or client id "
              + "of tokens issued for Grouper WS."));
    }
  }

}
