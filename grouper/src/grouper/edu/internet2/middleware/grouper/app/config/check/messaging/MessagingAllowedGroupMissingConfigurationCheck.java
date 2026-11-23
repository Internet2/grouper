package edu.internet2.middleware.grouper.app.config.check.messaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;

/**
 * SECURITY (GRP-7380, complements GRP-7580): grouper.messaging.system.&lt;name&gt;.allowedGroupName
 * (grouper.properties) names the group whose members (besides wheel and root) may send, receive or
 * acknowledge on an external messaging system.  GrouperMessagingExternalAuthorization fails closed
 * when that group is not found, so a name that does not match a group locks out every intended
 * member (ERROR).
 */
public class MessagingAllowedGroupMissingConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * allowedGroupName keys of messaging systems (grouper.properties)
   */
  private static final Pattern ALLOWED_GROUP_NAME_PATTERN = Pattern.compile(
      "^grouper\\.messaging\\.system\\.([^.]+)\\.allowedGroupName$");

  @Override
  public String getName() {
    return "messagingAllowedGroupMissing";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    Map<String, String> allowedGroupProperties = GrouperConfig.retrieveConfig().propertiesMap(ALLOWED_GROUP_NAME_PATTERN);

    for (String propertyName : allowedGroupProperties.keySet()) {

      String configuredValue = allowedGroupProperties.get(propertyName);

      // blank is the default (only wheel or root), not a broken setting
      if (StringUtils.isBlank(configuredValue)) {
        continue;
      }

      // trimmed, like GrouperMessagingExternalAuthorization
      final String groupName = configuredValue.trim();

      // look up as root, like GrouperMessagingExternalAuthorization, so the result does not depend on
      // who is looking at the review
      boolean groupExists = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        public Object callback(GrouperSession rootSession) {
          return GroupFinder.findByName(rootSession, groupName, false) != null;
        }
      });

      if (!groupExists) {
        results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
            ConfigFileName.GROUPER_PROPERTIES, propertyName, configuredValue,
            propertyName + " is set to '" + groupName + "', but no group with that name exists.  Only wheel "
                + "or root can use that messaging system; its members are refused.",
            "Set " + propertyName + " to the name of an existing group, or clear it."));
      }
    }

    return results;
  }

}
