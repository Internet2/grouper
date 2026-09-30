package edu.internet2.middleware.grouper.app.config.check.rules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * SECURITY (GRP-7380): membership in rules.accessToApiInEl.group grants the full EL API
 * (grouperUtil/forName) in rule EL evaluated as the actAs subject, so only Grouper sysadmins
 * (wheel/root) should be members.  Warn when the configured group has any member who is not a
 * sysadmin (this also covers a group that contains EveryEntity), and flag as an error when it points
 * at a group that does not exist.
 */
public class RulesAccessToApiInElConfigurationCheck extends GrouperConfigurationCheck {

  /**
   * property this check is about
   */
  public static final String PROPERTY_NAME = "rules.accessToApiInEl.group";

  /**
   * cap on how many offending subject ids to list in the finding message
   */
  private static final int MAX_SUBJECTS_LISTED = 20;

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

    // only sysadmins (wheel/root) should be in this group.  Any member who is not wheel/root -- or
    // whose subject cannot be resolved to verify -- is flagged.  This runs on demand (when an admin
    // opens the review screen), so walking the effective members here is acceptable.
    int nonSysadminCount = 0;
    StringBuilder listedSubjects = new StringBuilder();
    for (Member member : group.getMembers()) {
      Subject subject = null;
      try {
        subject = member.getSubject();
      } catch (Exception e) {
        // unresolvable subject: cannot confirm it is a sysadmin, so treat it as non-sysadmin
      }
      if (subject != null && PrivilegeHelper.isWheelOrRoot(subject)) {
        continue;
      }
      nonSysadminCount++;
      if (nonSysadminCount <= MAX_SUBJECTS_LISTED) {
        if (listedSubjects.length() > 0) {
          listedSubjects.append(", ");
        }
        listedSubjects.append(member.getSubjectId());
      }
    }

    if (nonSysadminCount > 0) {
      String subjectsText = listedSubjects.toString();
      if (nonSysadminCount > MAX_SUBJECTS_LISTED) {
        subjectsText = subjectsText + ", and " + (nonSysadminCount - MAX_SUBJECTS_LISTED) + " more";
      }
      results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, this.getName(),
          ConfigFileName.GROUPER_PROPERTIES, PROPERTY_NAME, groupName,
          "rules.accessToApiInEl.group is set to '" + groupName + "', which has " + nonSysadminCount
              + " member(s) who are not Grouper sysadmins (wheel/root): " + subjectsText + ".  Membership "
              + "grants the full EL API (grouperUtil/forName) in rule EL evaluated as the actAs subject.",
          "Only Grouper sysadmins (members of the wheel/root group) should be in rules.accessToApiInEl.group.  "
              + "Remove the non-sysadmin members, or clear the property."));
    }

    return results;
  }

}
