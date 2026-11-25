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
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * SECURITY (GRP-7580): provisioner.&lt;configId&gt;.trustTargetToRelinkEntities lets matching move an
 * entity that is already linked to a target account onto a different account that holds its new
 * matching value (e.g. a renamed userName).  That is safe only when Grouper is the only writer of
 * target accounts; otherwise anyone who can create or rename target accounts could pre-claim a
 * value and receive the entity's provisioned access.  Warn for each provisioner that turns it on, so
 * the site confirms that.
 */
public class ProvisionerTrustTargetToRelinkEntitiesConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * suffix of the per-provisioner setting (grouper-loader.properties)
   */
  public static final String PROPERTY_SUFFIX = "trustTargetToRelinkEntities";

  /**
   * matches provisioner.&lt;configId&gt;.trustTargetToRelinkEntities, group 1 is the config id
   */
  private static final Pattern PROPERTY_PATTERN = Pattern.compile("^provisioner\\.([^.]+)\\." + PROPERTY_SUFFIX + "$");

  @Override
  public String getName() {
    return "provisionerTrustTargetToRelinkEntities";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    Map<String, String> properties = GrouperLoaderConfig.retrieveConfig().propertiesMap(PROPERTY_PATTERN);

    for (String propertyName : properties.keySet()) {

      // same parse as GrouperProvisioningConfiguration (blank or not true is the safe default)
      if (!GrouperUtil.booleanValue(properties.get(propertyName), false)) {
        continue;
      }

      Matcher matcher = PROPERTY_PATTERN.matcher(propertyName);
      String configId = matcher.matches() ? matcher.group(1) : propertyName;

      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_LOADER_PROPERTIES, propertyName, "true",
          "Provisioner '" + configId + "' trusts the target to re-link entities: when an entity's matching "
              + "value (e.g. userName) changes to a value another target account already holds, the entity "
              + "and its access move to that account.  If anyone else can create or rename accounts in the "
              + "target, they could pre-claim a value.",
          "Keep this on only if Grouper is the only writer of accounts in this target; otherwise set "
              + propertyName + "=false (the default)."));
    }

    return results;
  }

}
