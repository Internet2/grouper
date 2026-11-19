/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;

/**
 * Request-wide authorization for the external invite submit
 * (UiV2ExternalEntities.inviteSubmit).
 *
 * <p>The submit stores the invitation (and, when inviting by identifier, creates
 * external subjects) inside a root session. Before this check the only authorization
 * was done per group while reading the submitted groups, so a request that sent no
 * group at all checked nothing: any logged in user could create a root-owned,
 * group-less invitation and send mail with their own subject and body, even when
 * inviting is turned off.</p>
 *
 * <p>This class is called after the groups are resolved and before anything is
 * stored or runs as root. It fails closed: the feature must be on, there must be at
 * least one target group, and the logged in user (the static session subject, which
 * is the logged in user at that point) must be able to invite into every target
 * group. The per-group check is GuiGroup.isCanInviteExternalUsers(), the same one
 * the invite screens use in every release, so this class works unchanged on 5.x,
 * 6.x and 7.x.</p>
 *
 * <p>Group-less invitations stay possible from the API
 * (ExternalSubjectAttrFramework.inviteExternalUsers), e.g. for GSH run by an admin;
 * only this UI endpoint, which any user can reach, requires a group.</p>
 */
public class ExternalInviteSubmitAuthorization {

  /** text key when inviting external users is not enabled */
  public static final String TEXT_KEY_NOT_ENABLED = "inviteExternalNotEnabled";

  /** text key when there is no group, or a group the user cannot invite into */
  public static final String TEXT_KEY_CANT_FIND_GROUP = "inviteExternalCantFindGroup";

  /**
   * no instances
   */
  private ExternalInviteSubmitAuthorization() {
  }

  /**
   * see if the logged in user may submit an external invite for these groups.
   * Needs a static grouper session started as the logged in user.
   * @param allGroups the resolved target groups of the invite
   * @return null if allowed, else the text key of the error to show
   */
  public static String denyTextKey(Set<Group> allGroups) {

    // same switch the invite screen checks; the submit never checked it
    if (!GrouperUiConfig.retrieveConfig().propertyValueBoolean("inviteExternalMembers.enableInvitation", false)) {
      return TEXT_KEY_NOT_ENABLED;
    }

    // this workflow invites into groups; with no group nothing below would be checked
    if (allGroups == null || allGroups.isEmpty()) {
      return TEXT_KEY_CANT_FIND_GROUP;
    }

    // every target must pass (deny the whole request, never invite into part of it)
    for (Group group : allGroups) {
      if (group == null || !new GuiGroup(group).isCanInviteExternalUsers()) {
        return TEXT_KEY_CANT_FIND_GROUP;
      }
    }

    return null;
  }

}
