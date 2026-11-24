package edu.internet2.middleware.grouper.app.config.check.membershipRequire;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckResult;
import edu.internet2.middleware.grouper.app.config.check.ConfigurationCheckSeverity;
import edu.internet2.middleware.grouper.app.config.check.GrouperConfigurationCheck;
import edu.internet2.middleware.grouper.app.customComposite.GrouperCustomComposite;
import edu.internet2.middleware.grouper.app.membershipRequire.MembershipRequireAdmin;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Membership requirements (grouper.membershipRequirement.*) and custom composites
 * (grouper.membership.customComposite.*) which are configured but do not work.  The engine skips a
 * requirement with no uiKey, attribute or require group, or whose attribute or group does not exist,
 * with only an error in the log, so it silently removes and vetoes nobody (ERROR).  A custom composite
 * with only some of its three keys, or whose group does not exist, is not offered on the members
 * screen, and one with a type other than complement or intersection fails when picked (WARNING).
 *
 * @author shilen
 */
public class MembershipRequireConfigurationCheck extends GrouperConfigurationCheck {

  @Override
  public String getName() {
    return "membershipRequirementAndCustomComposite";
  }

  @Override
  public List<ConfigurationCheckResult> checkConfiguration() {

    final List<ConfigurationCheckResult> results = new ArrayList<ConfigurationCheckResult>();

    // looking up the require groups needs a session, and this can run with none (e.g. from GSH)
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        for (MembershipRequireAdmin membershipRequireAdmin : MembershipRequireAdmin.retrieveAll()) {
          List<String> problems = requirementProblems(grouperSession, membershipRequireAdmin);
          if (problems.size() == 0) {
            continue;
          }
          String configId = membershipRequireAdmin.getConfigId();
          results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.ERROR, MembershipRequireConfigurationCheck.this.getName(),
              ConfigFileName.GROUPER_PROPERTIES, MembershipRequireAdmin.CONFIG_PREFIX + configId + ".*", null,
              "Membership requirement '" + configId + "' is skipped, so it removes and vetoes nobody: "
                  + GrouperUtil.join(problems.iterator(), " "),
              "Fix or delete it on Miscellaneous > Custom composites and membership requirements, or in "
                  + "the grouper.membershipRequirement." + configId + ".* configuration."));
        }

        for (GrouperCustomComposite grouperCustomComposite : GrouperCustomComposite.retrieveAll()) {
          int index = grouperCustomComposite.getIndex();
          String problem = null;
          if (!grouperCustomComposite.isComplete()) {
            problem = "does not have all of its uiKey, compositeType and groupName, so it is not offered on the members screen.";
          } else if (GroupFinder.findByName(grouperSession, grouperCustomComposite.getGroupName(), false) == null) {
            // the members screen leaves it out without saying why
            problem = "has a group which does not exist, so it is not offered on the members screen.";
          } else if (!Strings.CS.equals("complement", grouperCustomComposite.getCompositeType())
              && !Strings.CS.equals("intersection", grouperCustomComposite.getCompositeType())) {
            // it is offered, and then fails when someone picks it
            problem = "has a compositeType which is not complement or intersection, so it fails when someone picks it on the members screen.";
          }
          if (problem == null) {
            continue;
          }
          results.add(new ConfigurationCheckResult(ConfigurationCheckSeverity.WARNING, MembershipRequireConfigurationCheck.this.getName(),
              ConfigFileName.GROUPER_PROPERTIES, GrouperCustomComposite.CONFIG_PREFIX + "*." + index, null,
              "Custom composite " + index + " " + problem,
              "Fix or delete it on Miscellaneous > Custom composites and membership requirements, or in the "
                  + "grouper.membership.customComposite.*." + index + " configuration."));
        }

        return null;
      }
    });

    return results;
  }

  /**
   * what stops a requirement, in English like the other checks.  the same as
   * {@link MembershipRequireAdmin#retrieveProblems()}, which is UI text for the screen
   * @param grouperSession the session to look up the require group
   * @param membershipRequireAdmin the requirement
   * @return each problem, empty if none
   */
  private static List<String> requirementProblems(GrouperSession grouperSession, MembershipRequireAdmin membershipRequireAdmin) {
    List<String> problems = new ArrayList<String>();
    if (StringUtils.isBlank(membershipRequireAdmin.getUiKey())) {
      problems.add("Its uiKey is missing.");
    }
    if (StringUtils.isBlank(membershipRequireAdmin.getAttributeName())) {
      problems.add("Its attributeName is missing.");
    } else if (membershipRequireAdmin.retrieveAttributeDefName() == null) {
      problems.add("Its attribute " + membershipRequireAdmin.getAttributeName() + " does not exist.");
    }
    if (StringUtils.isBlank(membershipRequireAdmin.getRequireGroupName())) {
      problems.add("Its requireGroupName is missing.");
    } else if (GroupFinder.findByName(grouperSession, membershipRequireAdmin.getRequireGroupName(), false) == null) {
      problems.add("Its required group " + membershipRequireAdmin.getRequireGroupName() + " does not exist.");
    }
    return problems;
  }

}
