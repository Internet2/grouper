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
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Resolves the "related groups" that the group summary screens (viewGroupSummary and
 * viewGroupSummaryMore) list and count: direct group members, groups that hold privileges on the
 * viewed group, groups the viewed group holds privileges on, and ABAC groups whose scripts use
 * the viewed group.</p>
 *
 * <p>Why: those group ids come from raw SQL with no privilege filter, and
 * <code>new GroupFinder().assignGroupIds(..).findGroups()</code> does not filter by VIEW unless
 * privileges are assigned.  READ or ADMIN on the viewed group does not authorize seeing the
 * name, path or description of a related group, so the summary would name private groups.
 * This class keeps only the related groups the user of the current grouper session can VIEW,
 * and the summary counts are taken from that filtered set (except for users who can VIEW every
 * group, where the raw SQL counts are the same and cheaper).</p>
 */
public class GroupSummaryRelatedGroups {

  /**
   * no instances
   */
  private GroupSummaryRelatedGroups() {
  }

  /**
   * wheel, root and view-only root can VIEW every group, so the raw SQL counts are correct for
   * them and the screen does not need to resolve every related group just to count it
   * @param subject logged in subject
   * @return true if the subject can VIEW every group
   */
  public static boolean canViewAllGroups(Subject subject) {
    return PrivilegeHelper.isWheelOrRootOrViewonlyRoot(subject);
  }

  /**
   * resolve group ids to the groups that the subject of the current (static) grouper session
   * can VIEW.  Groups that are not found or not viewable are left out without saying so.
   * @param groupIds ids of related groups, can be null or empty
   * @return the viewable groups sorted (never null)
   */
  public static Set<Group> viewableGroups(Collection<String> groupIds) {
    Set<Group> result = new TreeSet<Group>();
    if (GrouperUtil.length(groupIds) == 0) {
      return result;
    }
    // assignPrivileges makes the finder filter by VIEW for the session subject (the logged in user)
    result.addAll(new GroupFinder().assignGroupIds(groupIds)
        .assignPrivileges(AccessPrivilege.VIEW_PRIVILEGES).findGroups());
    return result;
  }

}
