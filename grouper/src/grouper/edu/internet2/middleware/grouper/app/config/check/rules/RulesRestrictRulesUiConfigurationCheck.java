package edu.internet2.middleware.grouper.app.config.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;

/**
 * SECURITY (GRP-7380, complements GRP-7359): rules run as GrouperSystem, so gating who can add or
 * edit rules to a power-user group is the safer posture.  Warn when
 * rules.restrictRulesUiToMembersOfThisGroupName is blank, and flag as an error when it points at a
 * group that does not exist (rule editing then fails closed to wheel/root).
 */
public class RulesRestrictRulesUiConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * property this check is about
   */
  public static final String PROPERTY_NAME = "rules.restrictRulesUiToMembersOfThisGroupName";

  @Override
  public String getName() {
    return "rulesRestrictRulesUi";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    String groupName = GrouperConfig.retrieveConfig().propertyValueString(PROPERTY_NAME);

    if (StringUtils.isBlank(groupName)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, PROPERTY_NAME, groupName,
          "rules.restrictRulesUiToMembersOfThisGroupName is blank, so any user who administers an "
              + "object may add or edit its rules.  Rules run as GrouperSystem.",
          "Set rules.restrictRulesUiToMembersOfThisGroupName to a power-user group (for example your "
              + "sysadmin/wheel group) so that only vetted rule editors can add or edit rules."));
      return results;
    }

    Group group = GroupFinder.findByName(groupName, false);
    if (group == null) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, PROPERTY_NAME, groupName,
          "rules.restrictRulesUiToMembersOfThisGroupName is set to '" + groupName + "', but no group "
              + "with that name exists.  Rule editing fails closed, so only wheel/root can edit rules.",
          "Set rules.restrictRulesUiToMembersOfThisGroupName to the name of an existing power-user "
              + "group, or clear it."));
    }

    return results;
  }

}
