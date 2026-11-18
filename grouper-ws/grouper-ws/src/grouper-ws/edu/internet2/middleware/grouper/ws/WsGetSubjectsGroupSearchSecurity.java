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
package edu.internet2.middleware.grouper.ws;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;

/**
 * <pre>
 * Authorization for the WS getSubjects free-form search filtered by a group
 * (searchString plus wsGroupLookup).  That branch of GrouperServiceLogic.getSubjects
 * reads the membership DAO directly, which does no privilege check, so the caller must
 * be checked here first.  The group itself was already found with the caller's session
 * (VIEW), but VIEW does not allow reading who is in a list of the group.
 *
 * Rules (the same ones getMembers and MemberFinder.findBySubjectsInGroup apply):
 * - the field must be a group list field: a membership list (type list, e.g. members)
 *   or a group privilege list (type access, e.g. admins, updaters).  Folder and
 *   attribute definition fields do not belong to a group and are refused.
 * - the caller needs the read privilege of that field: READ for members, ADMIN for
 *   the privilege lists (Field.getReadPriv()).  Wheel and root pass as usual.
 *
 * A separate small class (not a method on GrouperServiceLogic) so the same file ships
 * unchanged for every release, and the change to the large, release-specific
 * GrouperServiceLogic is one call.
 * </pre>
 */
public class WsGetSubjectsGroupSearchSecurity {

  /**
   * no instances
   */
  private WsGetSubjectsGroupSearchSecurity() {
  }

  /**
   * make sure the caller may list the subjects in this field of the group, before the
   * membership DAO (which does not check privileges) is queried
   * @param grouperSession session of the WS caller (or the act-as subject)
   * @param group the group the search is filtered by, found with the caller's session
   * @param field the field the caller asked for, or null for the default members list
   * @return the field to search (the default list if null was passed)
   * @throws WsInvalidQueryException if the field is not a group list or privilege field
   * @throws InsufficientPrivilegeException if the caller cannot read that field of the group
   */
  public static Field checkCanSearchGroupField(GrouperSession grouperSession, Group group, Field field) {

    if (grouperSession == null || group == null) {
      // the caller only calls this when filtering by a group, so this is a programming error;
      // fail closed
      throw new InsufficientPrivilegeException("getSubjects group search needs a session and a group");
    }

    Field theField = field == null ? Group.getDefaultList() : field;

    // only membership lists and group privilege lists are lists of a group; anything else
    // (naming or attributeDef fields) is not a valid choice for this search
    if (!theField.isGroupListField()) {
      throw new WsInvalidQueryException("fieldName must be a group list or group privilege field: "
          + theField.getName());
    }

    // same check MemberFinder.findBySubjectsInGroup makes for the subject lookup branch:
    // the read privilege of the field (READ for members, ADMIN for admins, etc)
    if (!PrivilegeHelper.canViewMembers(grouperSession, group, theField)) {
      throw new InsufficientPrivilegeException("Not allowed to read list '" + theField.getName()
          + "' of group: " + group.getName());
    }

    return theField;
  }

}
