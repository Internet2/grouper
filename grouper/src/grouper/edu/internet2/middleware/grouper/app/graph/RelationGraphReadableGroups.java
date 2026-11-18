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

package edu.internet2.middleware.grouper.app.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * Decides which group nodes of a relation graph the current session subject may see
 * membership counts for.
 *
 * VIEW lets a subject discover a group (so it can appear as a node in the graph), but
 * membership-derived data, including the total and direct member counts, needs READ, the same
 * as the group summary and membership screens.  RelationGraph computes the counts with a raw
 * HQL aggregate that does no privilege checking, so it asks this class first and only counts
 * the groups returned here.
 *
 * This is its own class (not a method on RelationGraph) so the same file can be dropped into
 * every release; RelationGraph differs from release to release.  It only uses APIs that exist
 * from Grouper 5.0.0 on.
 */
public class RelationGraphReadableGroups {

  /**
   * no instances
   */
  private RelationGraphReadableGroups() {
  }

  /**
   * Return the ids of the given group nodes on which the subject of the current static grouper
   * session has READ.  Wheel / root keeps every group.  With no session nothing is readable
   * (fail closed).  Non-group nodes are never returned.
   *
   * @param groupNodes graph nodes that are groups (others are ignored)
   * @return the group ids (GraphNode.getGrouperObjectId()) that may have counts shown, never null
   */
  public static Set<String> readableGroupIds(Collection<GraphNode> groupNodes) {

    Set<String> result = new HashSet<String>();

    if (groupNodes == null || groupNodes.size() == 0) {
      return result;
    }

    // the graph is built under the caller's session, so its subject is the one to check
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    if (grouperSession == null || grouperSession.getSubject() == null) {
      return result;
    }
    Subject subject = grouperSession.getSubject();

    // wheel and root can read everything, skip the per-group checks
    boolean wheelOrRoot = PrivilegeHelper.isWheelOrRoot(subject);

    for (GraphNode graphNode : groupNodes) {
      if (graphNode == null || !graphNode.isGroup()) {
        continue;
      }
      GrouperObject grouperObject = graphNode.getGrouperObject();
      if (!(grouperObject instanceof Group)) {
        continue;
      }
      if (wheelOrRoot) {
        result.add(graphNode.getGrouperObjectId());
        continue;
      }
      // secure=false: just ask whether the subject has the privilege; the caller is not
      // required to be able to see the privilege list
      if (((Group)grouperObject).canHavePrivilege(subject, AccessPrivilege.READ.getName(), false)) {
        result.add(graphNode.getGrouperObjectId());
      }
    }
    return result;
  }
}
