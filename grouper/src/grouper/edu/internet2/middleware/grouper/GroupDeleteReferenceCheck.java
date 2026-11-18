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
package edu.internet2.middleware.grouper;

import java.util.List;

import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * SECURITY: helpers for the referential integrity check at the start of Group.delete() (the
 * scripted groups and privacy realms that reference the group being deleted).
 *
 * That check used to run before the ADMIN check, so any caller who could VIEW a group (for
 * example through WS groupDelete) got an error naming every scripted group that uses it,
 * including groups the caller cannot see.  Group.delete() now calls
 * {@link #assertCanAdminBeforeReferenceCheck(Group, String)} first, and builds the scripted group
 * part of the message with {@link #addDependentGroupReferences(List, List)}, which names only the
 * groups the caller can VIEW and counts the rest.
 *
 * This is its own class so the Group.java change stays a few lines: Group.java differs in most
 * releases, and this class only uses APIs that have been stable since 6.0.0.
 */
public class GroupDeleteReferenceCheck {

  /**
   * Throw InsufficientPrivilegeException unless the subject of the current static session can
   * ADMIN the group.  This is the same check (and message) Group.delete() does inside its
   * transaction; it is repeated here so it happens before the reference check, which would
   * otherwise tell a non-admin which groups reference this one.
   * @param group the group being deleted
   * @param errorMessageSuffix the suffix Group.delete() appends to its error messages
   */
  public static void assertCanAdminBeforeReferenceCheck(Group group, String errorMessageSuffix) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    GrouperSession.validate(grouperSession);
    if (!PrivilegeHelper.canAdmin(grouperSession, group, grouperSession.getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_ADMIN + errorMessageSuffix);
    }
  }

  /**
   * Add one "scripted group '...'" entry per dependent group the caller can VIEW to
   * referencingConfigs, and one entry counting the dependent groups the caller cannot VIEW (so
   * the delete is still blocked, and the caller knows why, without learning hidden group names).
   * Wheel and root see every name, as before.
   * @param referencingConfigs list of reference descriptions for the error message, added to
   * @param dependentGroupNames names of the scripted groups that depend on the group being deleted
   */
  public static void addDependentGroupReferences(List<String> referencingConfigs, List<String> dependentGroupNames) {

    final GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    final Subject subject = grouperSession == null ? null : grouperSession.getSubject();

    // wheel / root: same message as before this fix
    if (subject != null && PrivilegeHelper.isWheelOrRoot(subject)) {
      for (String groupName : GrouperUtil.nonNull(dependentGroupNames)) {
        referencingConfigs.add("scripted group '" + groupName + "'");
      }
      return;
    }

    int hiddenCount = 0;
    for (final String groupName : GrouperUtil.nonNull(dependentGroupNames)) {

      // look the group up as root: the dependency rows come from raw SQL with no privilege filter,
      // so decide visibility here with the caller's own VIEW privilege
      Group dependentGroup = null;
      if (groupName != null && subject != null) {
        dependentGroup = (Group)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

          public Object callback(GrouperSession rootSession) {
            return GroupFinder.findByName(rootSession, groupName, false);
          }
        });
      }

      if (dependentGroup != null && PrivilegeHelper.canView(grouperSession, dependentGroup, subject)) {
        referencingConfigs.add("scripted group '" + groupName + "'");
      } else {
        // not visible to the caller (or no longer exists): count it, do not name it
        hiddenCount++;
      }
    }

    if (hiddenCount == 1) {
      referencingConfigs.add("1 scripted group you cannot view");
    } else if (hiddenCount > 1) {
      referencingConfigs.add(hiddenCount + " scripted groups you cannot view");
    }
  }

}
