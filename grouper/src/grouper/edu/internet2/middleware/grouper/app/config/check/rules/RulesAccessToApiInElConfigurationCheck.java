package edu.internet2.middleware.grouper.app.config.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * GRP-7596: rules.accessToApiInEl.group is no longer used.  It used to decide, by the rule's act-as
 * subject, whether rule EL got the full EL API (the live group, member, membership, subject and rule
 * objects).  Now only a Grouper sysadmin (wheel/root) can save rule if/then EL, and
 * rules.allowUnrestrictedEl decides site wide whether it gets the full API.  A site that still sets the
 * property may think it does something it does not, so report an error until it is cleared.  (GRP-7380
 * checked the group's members instead.)
 */
public class RulesAccessToApiInElConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * property this check is about
   */
  public static final String PROPERTY_NAME = "rules.accessToApiInEl.group";

  @Override
  public String getName() {
    return "rulesAccessToApiInEl";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    String groupName = GrouperConfig.retrieveConfig().propertyValueString(PROPERTY_NAME);

    // blank is the expected setting, nothing to flag
    if (StringUtils.isBlank(groupName)) {
      return results;
    }

    results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
        ConfigFileName.GROUPER_PROPERTIES, PROPERTY_NAME, groupName,
        "rules.accessToApiInEl.group is set to '" + groupName + "', but it is no longer used (GRP-7596).  "
            + "Rule if/then expression language can only be saved by a Grouper sysadmin (wheel/root), "
            + "and rules.allowUnrestrictedEl decides whether it has the full EL API, whoever the rule acts as.",
        "Clear rules.accessToApiInEl.group.  If your rules need the full EL API, set rules.allowUnrestrictedEl = true."));

    return results;
  }

}
