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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Visibility checks for the other groups that a group's loader configuration names: the
 * source ("from") group of a recent-memberships loader, and the "and groups" of a SQL or LDAP
 * loader.  Used by GrouperLoaderContainer for the Loader tab and the loader edit form.</p>
 *
 * <p>Why: GrouperLoaderContainer resolves those groups in a root session from the saved
 * configuration.  The Loader tab is shown to ADMINs of the loader group by default
 * (uiV2.loader.view.by.group.admins), which authorizes this group's loader settings, not the
 * other groups they name.  Rendering the real GuiGroup link would put a private group's UUID,
 * name, folder path and description in the response.  So a related group is shown only if the
 * logged in user can VIEW it, or can edit loaders (wheel, root, uiV2.loader.edit.if.in.group:
 * they author these settings and the loader diagnostics need the real groups).  Otherwise
 * GuiGroupRestrictedReference (a "restricted group" placeholder with no group in it) is
 * returned, so the JSPs that render shortLinkWithIcon do not change.  The saved configuration is
 * never changed.</p>
 *
 * <p>The edit form puts the saved recent-memberships source UUID in its combobox.  Saving
 * requires READ on that source, so the UUID is only put there for a user who can READ the
 * source; others get an empty combobox and choose a source they can READ, which is what saving
 * already required of them.</p>
 *
 * <p>Fails closed: if the logged in user cannot be determined or a check throws, the group is
 * not shown.  Only uses APIs that exist in every Grouper 2.6, 4.x, 5.x, 6.x and 7.x release.</p>
 */
public class GrouperLoaderRelatedGroups {

  /**
   * no instances
   */
  private GrouperLoaderRelatedGroups() {
  }

  /**
   * wrap a group that the loader configuration names, for display on the Loader tab or edit form
   * @param group related group (recent-memberships source or an "and group"), can be null if not found
   * @param grouperLoaderContainer container of the loader screen, to see if the user can edit loaders
   * @return the gui group if the logged in user can VIEW the group or can edit loaders, else the
   * restricted placeholder.  A null group gives an empty GuiGroup, the same as before.
   */
  public static GuiGroup guiGroupIfViewable(Group group, GrouperLoaderContainer grouperLoaderContainer) {
    if (group == null) {
      // same as before: not found is an empty gui group (the loader diagnostics report it as not found)
      return new GuiGroup(group);
    }
    boolean canView = false;
    try {
      Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      canView = loggedInSubject != null
          && group.canHavePrivilege(loggedInSubject, AccessPrivilege.VIEW.getName(), false);
      if (!canView && loggedInSubject != null && grouperLoaderContainer != null) {
        // loader editors author these settings (and run the diagnostics, which need the real group)
        canView = grouperLoaderContainer.isCanEditLoader();
      }
    } catch (RuntimeException re) {
      // cannot tell: do not show the group
      canView = false;
    }
    if (canView) {
      return new GuiGroup(group);
    }
    return new GuiGroupRestrictedReference();
  }

  /**
   * the recent-memberships source group UUID to put in the edit form, only if the logged in
   * user can READ that group (saving requires READ on it anyway)
   * @param groupUuid saved or submitted source group UUID, can be blank
   * @return the UUID, or null if the logged in user cannot READ the group.  A blank UUID, or
   * a UUID of a group that does not exist, is returned as is (the save then says it is required
   * or not found, the same as before).
   */
  public static String recentFromGroupUuidIfReadable(final String groupUuid) {
    if (GrouperUtil.isBlank(groupUuid)) {
      return groupUuid;
    }
    try {
      final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();
      if (loggedInSubject == null) {
        return null;
      }
      Boolean readable = (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          Group group = GroupFinder.findByUuid(grouperSession, groupUuid, false);
          if (group == null) {
            // not found: leave it, the save reports it
            return Boolean.TRUE;
          }
          return group.canHavePrivilege(loggedInSubject, AccessPrivilege.READ.getName(), false);
        }
      });
      return (readable != null && readable) ? groupUuid : null;
    } catch (RuntimeException re) {
      // cannot tell: do not put the UUID in the form
      return null;
    }
  }

}
