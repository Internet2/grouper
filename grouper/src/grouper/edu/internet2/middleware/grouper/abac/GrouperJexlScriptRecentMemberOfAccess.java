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
package edu.internet2.middleware.grouper.abac;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Which groups named in a scripted group's entity.recentMemberOf('group', 'period') calls the
 * user analyzing the script may start membership history for.</p>
 *
 * <p>When a script is analyzed (the Analyze screen, the visualization script preview, the save
 * check), GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml adds a persistent
 * mshipHistory_abac SQL cache dependency for each recentMemberOf group and, if the group has no
 * membership history yet, runs a full membership history sync for it in that request.  That ran
 * before any privilege check, so anybody who could reach an analysis could make Grouper build and
 * keep maintaining history for any cached group, including groups they cannot see.  Only groups
 * the user can READ are kept: the analysis's own checks treat any other group as missing (or
 * refuse it), so its counts never use that history anyway.</p>
 *
 * <p>Saved scripts are not affected: the scripted group daemon adds the history dependencies for
 * the groups of every saved script itself.</p>
 *
 * <p>Uses only APIs present since 5.0.0 and no commons-lang import, so the same file serves every
 * release.</p>
 */
public class GrouperJexlScriptRecentMemberOfAccess {

  /**
   * no instances
   */
  private GrouperJexlScriptRecentMemberOfAccess() {
  }

  /**
   * keep the recentMemberOf group names the user can READ, in the order given
   * @param groupNames group names from the script, may be null
   * @param loggedInSubject the user analyzing the script, null means none are kept (fail closed)
   * @return the names of existing groups the user can READ, never null
   */
  public static Set<String> readableGroupNames(final Collection<String> groupNames, final Subject loggedInSubject) {

    final Set<String> result = new LinkedHashSet<String>();

    if (loggedInSubject == null || groupNames == null || groupNames.size() == 0) {
      return result;
    }

    // as root: the user is checked explicitly, and the user's session cannot find a group it cannot
    // see, which is the point
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        for (String groupName : groupNames) {
          if (groupName == null) {
            continue;
          }
          // not from the finder's caches, and by the current name only (the finder also matches an
          // old name), so the sql cache row found by this name below is this group's
          Group group = GroupFinder.findByName(grouperSession, groupName, false,
              new QueryOptions().secondLevelCache(false));
          if (group != null && groupName.equals(group.getName())
              && group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false)) {
            result.add(groupName);
          }
        }
        return null;
      }
    });

    return result;
  }
}
