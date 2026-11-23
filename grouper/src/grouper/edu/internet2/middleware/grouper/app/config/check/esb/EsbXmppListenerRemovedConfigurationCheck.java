package edu.internet2.middleware.grouper.app.config.check.esb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * SECURITY (GRP-7380, complements GRP-7577): the experimental ESB XMPP listener has been removed.
 * If grouper-loader.properties still has esb.listeners.xmpp.enable=true, GrouperLoader only logs an
 * error and the listener does not start, so whatever relied on it is silently not working (ERROR).
 * Any other esb.listeners.xmpp.* key that is still set is leftover config to delete (WARNING).
 */
public class EsbXmppListenerRemovedConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * the removed switch that used to start the XMPP listener (grouper-loader.properties)
   */
  public static final String ENABLE_PROPERTY_NAME = "esb.listeners.xmpp.enable";

  /**
   * every key of the removed XMPP listener
   */
  private static final Pattern XMPP_LISTENER_KEY_PATTERN = Pattern.compile("^esb\\.listeners\\.xmpp\\..+$");

  @Override
  public String getName() {
    return "esbXmppListenerRemoved";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

    // same read as GrouperLoader: true means someone expects the listener to run, but it will not
    if (grouperLoaderConfig.propertyValueBoolean(ENABLE_PROPERTY_NAME, false)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_LOADER_PROPERTIES, ENABLE_PROPERTY_NAME, "true",
          "esb.listeners.xmpp.enable is true, but the ESB XMPP listener has been removed.  It does not "
              + "start; the daemon only logs an error, so callers that sent changes over XMPP are not served.",
          "Set esb.listeners.xmpp.enable=false (or remove it), delete the other esb.listeners.xmpp.* "
              + "settings, and move those callers to Grouper web services."));
    }

    // any other leftover key with a value is dead config (blank values are harmless, skip them)
    Map<String, String> xmppProperties = grouperLoaderConfig.propertiesMap(XMPP_LISTENER_KEY_PATTERN);
    for (String propertyName : xmppProperties.keySet()) {
      if (ENABLE_PROPERTY_NAME.equals(propertyName)) {
        continue;
      }
      String value = xmppProperties.get(propertyName);
      if (StringUtils.isBlank(value)) {
        continue;
      }
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_LOADER_PROPERTIES, propertyName, value,
          propertyName + " is set, but the ESB XMPP listener has been removed, so this setting is not used.",
          "Delete " + propertyName + " from grouper-loader.properties (or the configuration database)."));
    }

    return results;
  }

}
