/**
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.rules;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * SECURITY: the rules.restrictRulesUiToMembersOfThisGroupName gate for adding, editing and deleting
 * rules in the UI.  The UI beans RulesContainer.isCanAddRule and GuiRuleDefinition.isCanEditRule call
 * this before their ADMIN / STEM_ADMIN check on the owner object.
 *
 * This is its own class, not a method on RuleService, so the fix can be dropped into older releases
 * as whole files: RuleService differs from release to release, and this class only uses APIs that
 * have been stable since 6.0.0.
 */
public class RuleUiRestrictGroup {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleUiRestrictGroup.class);

  /**
   * Whether a subject passes the rules.restrictRulesUiToMembersOfThisGroupName gate.  The restrict
   * group is resolved with a root session: it is usually not VIEWable by the users it is meant to
   * exclude, and a lookup in their own session returns null, which used to skip the gate.  A configured
   * group that cannot be found fails closed.
   * @param subject the logged in subject
   * @return true if the subject may use the rules UI (wheel/root, gate blank, or member of the restrict group)
   */
  public static boolean allowedToEditRulesInUi(final Subject subject) {
    if (subject == null) {
      return false;
    }
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    final String restrictRulesGroupName = GrouperConfig.retrieveConfig().propertyValueString("rules.restrictRulesUiToMembersOfThisGroupName", "");
    if (StringUtils.isBlank(restrictRulesGroupName)) {
      return true;
    }
    return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) {
        Group restrictRulesGroup = GroupFinder.findByName(rootSession, restrictRulesGroupName, false);
        if (restrictRulesGroup == null) {
          // a mistyped or deleted group must not open the rules UI to everyone
          LOG.error("rules.restrictRulesUiToMembersOfThisGroupName is set to '" + restrictRulesGroupName
              + "' and it does not exist, denying rules UI access to non-wheel subjects");
          return false;
        }
        return restrictRulesGroup.hasMember(subject);
      }
    });
  }

}
