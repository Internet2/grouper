package edu.internet2.middleware.grouper.app.config.check.provisioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2ProvisionerConfiguration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/** Check the selection capabilities needed for explicitly requested SCIM user groups. */
public class ScimUserGroupsConfigurationCheck extends GrouperConfigurationCheck {

  private static final Pattern PROPERTY_PATTERN =
      Pattern.compile("^provisioner\\.([^.]+)\\.scimUserGroupsRequireAttributes$");

  @Override
  public String getName() {
    return "scimUserGroups";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {
    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    Map<String, String> properties = config.propertiesMap(PROPERTY_PATTERN);
    for (String propertyName : properties.keySet()) {
      if (!GrouperUtil.booleanValue(properties.get(propertyName), false)) {
        continue;
      }
      Matcher matcher = PROPERTY_PATTERN.matcher(propertyName);
      if (!matcher.matches()) {
        continue;
      }
      String prefix = "provisioner." + matcher.group(1) + ".";
      String error = GrouperScim2ProvisionerConfiguration.userGroupsConfigurationError(
          config.propertyValueBoolean(prefix + "scimRetrieveMembershipsByUser", true),
          config.propertyValueBoolean(prefix + "scimRetrieveMembershipsByGroup", true),
          config.propertyValueString(prefix + "membershipStrategy"),
          "Github".equals(config.propertyValueString(prefix + "scimType")));
      if (error != null) {
        results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
            ConfigFileName.GROUPER_LOADER_PROPERTIES, propertyName, "true", error,
            "Configure membership retrieval by user only, with the individual-user membership strategy "
                + "(or leave membershipStrategy unset). Disable " + propertyName
                + " if this target does not support GET /Users/{id}?attributes=groups."));
      }
    }
    return results;
  }
}
