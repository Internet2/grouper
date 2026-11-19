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
package edu.internet2.middleware.grouper.hooks.examples;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Authorization for the legacy include/exclude (addIncludeExclude) and requireInGroups
 * "rename the related groups too" logic in Group.store().
 *
 * When a group of such a family is renamed (or its display extension changes), Group.store()
 * also renames the overall group and the helper groups (systemOfRecord, includes, excludes,
 * systemOfRecordAndIncludes).  Those related groups are found by NAME only and written straight
 * through the DAO, so the ADMIN check in store() only ever covered the group being stored.
 * A delegated admin of one helper group could rename the protected overall group, and anyone
 * who can create a group next to a readable group named like a helper (e.g. x_includes) could
 * rename that unrelated group.
 *
 * Group calls assertCanRenameRelatedGroup() right before each related group is written.  The
 * caller must be ADMIN on the related group too (wheel / root, e.g. the loader, always pass).
 * The exception propagates out of the store() transaction, so nothing is changed at all
 * (fail closed, no half-renamed family).
 *
 * This is its own class (not code in Group.java) so the many Group.java variants across
 * releases only need a one line call each.  It uses only APIs present in Grouper 5.0.0+.
 * </pre>
 */
public class IncludeExcludeRenameAuthorization {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(IncludeExcludeRenameAuthorization.class);

  /**
   * no instances
   */
  private IncludeExcludeRenameAuthorization() {
  }

  /**
   * Make sure the subject of the current grouper session may change a group that is about to
   * be renamed (or have its display extension / description changed) as a side effect of
   * storing another group of the include/exclude or requireInGroups family.
   *
   * @param storedGroup the group the caller is storing (already checked for ADMIN by store())
   * @param relatedGroup the related group that would be written through the DAO
   * @throws InsufficientPrivilegeException if the caller is not wheel/root and not ADMIN on
   *   the related group
   */
  public static void assertCanRenameRelatedGroup(Group storedGroup, Group relatedGroup) {

    // nothing to check
    if (relatedGroup == null) {
      return;
    }

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    Subject subject = grouperSession == null ? null : grouperSession.getSubject();

    // no session should not happen inside store(), but do not allow an unattributed change
    if (subject == null) {
      throw new InsufficientPrivilegeException("No grouper session, cannot change group "
          + relatedGroup.getName() + " which is related to group "
          + (storedGroup == null ? null : storedGroup.getName()));
    }

    // root sessions (loader, daemons, GSH as GrouperSystem) and wheel group members keep the
    // existing behavior
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return;
    }

    // the related group is changed too, so the caller needs the same privilege on it that
    // store() requires on the stored group
    if (relatedGroup.hasAdmin(subject)) {
      return;
    }

    String message = GrouperUtil.subjectToString(subject) + " is not admin on group: "
        + relatedGroup.getName() + ", which would also be changed when storing group: "
        + (storedGroup == null ? null : storedGroup.getName())
        + " (include/exclude or requireInGroups rename propagation)";
    LOG.warn(message);
    throw new InsufficientPrivilegeException(message);
  }

}
