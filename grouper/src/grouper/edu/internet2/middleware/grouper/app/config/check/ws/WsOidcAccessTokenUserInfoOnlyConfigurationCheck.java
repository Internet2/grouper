package edu.internet2.middleware.grouper.app.config.check.ws;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * SECURITY (GRP-7380, complements GRP-7577): grouper.oidcExternalSystem.&lt;configId&gt;.wsAccessTokenValidation
 * = userInfoOnly keeps the older WS OAuth check: any access token the provider's userinfo endpoint
 * accepts logs in to WS, including tokens issued to other applications.  It works, but jwt or
 * introspection (which check the audience or client id) is safer (WARNING).
 */
public class WsOidcAccessTokenUserInfoOnlyConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * the weaker validation mode
   */
  public static final String MODE_USER_INFO_ONLY = "userInfoOnly";

  /**
   * wsAccessTokenValidation keys of OIDC external systems (grouper.properties)
   */
  private static final Pattern WS_ACCESS_TOKEN_VALIDATION_PATTERN = Pattern.compile(
      "^grouper\\.oidcExternalSystem\\.([^.]+)\\.wsAccessTokenValidation$");

  @Override
  public String getName() {
    return "wsOidcAccessTokenUserInfoOnly";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    Map<String, String> validationProperties = GrouperConfig.retrieveConfig().propertiesMap(WS_ACCESS_TOKEN_VALIDATION_PATTERN);

    for (String propertyName : validationProperties.keySet()) {
      // trimmed, like WsOauthAccessTokenPolicy
      String mode = StringUtils.trimToEmpty(validationProperties.get(propertyName));
      if (!MODE_USER_INFO_ONLY.equals(mode)) {
        continue;
      }
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, propertyName, mode,
          propertyName + " is userInfoOnly, the older WS OAuth check: WS accepts any access token the "
              + "userinfo endpoint accepts, including tokens issued to other applications.",
          "Set " + propertyName + " to jwt or introspection, with wsAccessTokenAudiences and/or "
              + "wsAccessTokenAllowedClientIds, so only tokens issued for Grouper WS are accepted."));
    }

    return results;
  }

}
