package edu.internet2.middleware.grouper.app.config.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.subject.Subject;

/**
 * SECURITY (GRP-7380): membership in rules.accessToApiInEl.group grants the full EL API
 * (grouperUtil/forName) in rule EL evaluated as the actAs subject.  Warn when the configured group is
 * "broad" -- it includes EveryEntity (all subjects) or has more members than
 * {@link #BROAD_GROUP_MEMBER_THRESHOLD} -- and flag as an error when it points at a group that does
 * not exist.
 */
public class RulesAccessToApiInElConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * property this check is about
   */
  public static final String PROPERTY_NAME = "rules.accessToApiInEl.group";

  /**
   * a configured group with more than this many members is considered broad and is flagged
   */
  public static final int BROAD_GROUP_MEMBER_THRESHOLD = 50;

  @Override
  public String getName() {
    return "rulesAccessToApiInEl";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    String groupName = GrouperConfig.retrieveConfig().propertyValueString(PROPERTY_NAME);

    // not set means no extra EL API access is granted, nothing to flag
    if (StringUtils.isBlank(groupName)) {
      return results;
    }

    Group group = GroupFinder.findByName(groupName, false);
    if (group == null) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, PROPERTY_NAME, groupName,
          "rules.accessToApiInEl.group is set to '" + groupName + "', but no group with that name exists.",
          "Set rules.accessToApiInEl.group to an existing group, or clear it."));
      return results;
    }

    // a group that contains the EveryEntity subject effectively grants this to everyone
    Subject allSubject = SubjectFinder.findAllSubject();
    if (allSubject != null && group.hasMember(allSubject)) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, PROPERTY_NAME, groupName,
          "rules.accessToApiInEl.group is set to '" + groupName + "', which includes EveryEntity (all "
              + "subjects).  Membership grants the full EL API (grouperUtil/forName) in rule EL evaluated "
              + "as the actAs subject.",
          "Restrict rules.accessToApiInEl.group to a small, trusted power-user group instead of a broad group."));
      return results;
    }

    // otherwise flag if the membership is larger than the broad-group threshold.  This runs on demand
    // (when an admin opens the review screen), so counting the effective members here is acceptable.
    int memberCount = group.getMembers().size();
    if (memberCount > BROAD_GROUP_MEMBER_THRESHOLD) {
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, PROPERTY_NAME, groupName,
          "rules.accessToApiInEl.group is set to '" + groupName + "', which has " + memberCount
              + " members (more than " + BROAD_GROUP_MEMBER_THRESHOLD + ").  Membership grants the full EL "
              + "API (grouperUtil/forName) in rule EL evaluated as the actAs subject.",
          "Restrict rules.accessToApiInEl.group to a small, trusted power-user group."));
    }

    return results;
  }

}
