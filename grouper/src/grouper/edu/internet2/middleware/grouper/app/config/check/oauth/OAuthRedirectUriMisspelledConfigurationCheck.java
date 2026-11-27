package edu.internet2.middleware.grouper.app.config.check.oauth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * GRP-7532: the OAuth redirect URI allowlist key was misspelled grouper.oauth.redrectUri.&lt;configId&gt;.regex
 * and is now grouper.oauth.redirectUri.&lt;configId&gt;.regex.  Upgrade task 47 renames the keys in the
 * configuration database, but a key in a grouper.properties file stays misspelled.  GrouperOAuthStore still
 * reads the old spelling for now (with a WARN in the log once per key), but it is deprecated and will be
 * removed, and then those redirect URIs would no longer be allowed.  Every misspelled key that is set is an
 * ERROR, naming the key to rename it to.
 */
public class OAuthRedirectUriMisspelledConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * the misspelled keys, group 1 is the config id
   */
  private static final Pattern MISSPELLED_PATTERN = Pattern.compile("^grouper\\.oauth\\.redrectUri\\.([^.]+)\\.regex$");

  @Override
  public String getName() {
    return "oauthRedirectUriMisspelled";
  }

  /**
   * @param configId
   * @return the correctly spelled key for this config id
   */
  public static String correctPropertyName(String configId) {
    return "grouper.oauth.redirectUri." + configId + ".regex";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    Map<String, String> properties = GrouperConfig.retrieveConfig().propertiesMap(MISSPELLED_PATTERN);

    for (String propertyName : properties.keySet()) {

      Matcher matcher = MISSPELLED_PATTERN.matcher(propertyName);
      if (!matcher.matches()) {
        continue;
      }
      String correctPropertyName = correctPropertyName(matcher.group(1));

      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, propertyName, properties.get(propertyName),
          propertyName + " is misspelled (redrectUri).  Grouper still reads it for now, but the misspelled key is "
              + "deprecated and will be removed, and then this redirect URI allowlist would no longer apply.",
          "Rename it to " + correctPropertyName + " in grouper.properties (upgrade task 47 already renamed the keys "
              + "in the configuration database)."));
    }

    return results;
  }

}
