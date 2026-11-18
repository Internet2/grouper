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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Decides whether the visualization (RelationGraph) may run the scripted-group (ABAC JEXL)
 * analysis for a group.  The analysis shows per-reference population counts and, when a
 * subject is named, whether that subject matches each reference, so it is only run for users
 * who could also open the Analyze screen of the group's loader: the same rule as
 * UiV2GrouperLoader.editGrouperLoaderAnalyze (GrouperLoaderContainer.isCanEditLoader() ||
 * isCanEditAbacOrRecentMembershipsLoader()).  Users who can only VIEW the group still see the
 * group as a scripted group in the graph, without the analysis nodes.
 *
 * This is a separate class (not a RelationGraph method) so the same file ships unchanged in
 * every release range; it only uses APIs that exist from 6.2.0 on.
 */
public class RelationGraphAbacAnalysisAccess {

  /**
   * ui config key: if set, members of this group who can VIEW a group may edit its loader
   * (same key GrouperLoaderContainer.isCanEditLoader reads)
   */
  public static final String UI_LOADER_EDIT_IF_IN_GROUP = "uiV2.loader.edit.if.in.group";

  /**
   * Can this subject see the scripted-group analysis of this group in the visualization?
   * @param subject the logged in subject (the visualization session's subject), null means no
   * @param group the scripted group being visualized, null means no
   * @return true if wheel/root, if the subject can edit the group's ABAC or recent memberships
   * loader, or if uiV2.loader.edit.if.in.group is set and the subject is in it and can VIEW the group
   */
  public static boolean canRunAbacAnalysis(final Subject subject, final Group group) {

    // fail closed on missing input
    if (subject == null || group == null) {
      return false;
    }

    // the checks below need to see groups the subject may not see, so run as root; the wheel check
    // is inside the callback too, since isWheelOrRoot needs an open session and callers (or tests)
    // may not have one
    return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new edu.internet2.middleware.grouper.misc.GrouperSessionHandler() {

      public Object callback(GrouperSession rootSession) {

        // wheel or root: as before
        if (PrivilegeHelper.isWheelOrRoot(subject)) {
          return true;
        }

        // ABAC or recent memberships loader editors (ADMIN on the group plus the optional
        // grouper.abac.edit.if.in.group / recent memberships group), as on the Analyze screen
        if (PrivilegeHelper.canEditAbacLoader(subject, group)
            || PrivilegeHelper.canEditRecentMembershipsLoader(subject, group)) {
          return true;
        }

        // general loader editors: VIEW on the group plus membership in uiV2.loader.edit.if.in.group
        String loaderEditGroupName = GrouperUiConfigInApi.retrieveConfig().propertyValueString(UI_LOADER_EDIT_IF_IN_GROUP);
        if (GrouperUtil.isBlank(loaderEditGroupName)) {
          return false;
        }
        if (!group.canHavePrivilege(subject, AccessPrivilege.VIEW.getName(), false)) {
          return false;
        }
        Group loaderEditGroup = GroupFinder.findByName(rootSession, loaderEditGroupName, false);
        return loaderEditGroup != null && loaderEditGroup.hasMember(subject);
      }
    });
  }

}
