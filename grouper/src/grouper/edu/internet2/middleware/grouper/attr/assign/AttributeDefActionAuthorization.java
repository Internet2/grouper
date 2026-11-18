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
package edu.internet2.middleware.grouper.attr.assign;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Authorization for changing the action list of an attribute definition (adding, removing or
 * replacing actions).  Changing actions is an ATTR_ADMIN operation: removing an action also
 * deletes, as root, every attribute assignment that uses it, so for a permission definition it
 * changes who has which permission.
 * <p>
 * Before this check existed, AttributeDefActionDelegate wrote action rows with no privilege
 * check, and callers such as the WS assignAttributeDefActions operation only reached the
 * ATTR_ADMIN check in AttributeDef.store() after the actions were already changed and
 * committed.  A subject with only ATTR_VIEW could therefore delete any action and its
 * assignments.
 * </p>
 * <p>
 * The check is in its own class so the delegate (and older releases of it) only needs a one
 * line call, and so every release gets the same logic.  It uses only APIs that exist in every
 * Grouper 5.x, 6.x and 7.x release.
 * </p>
 * <p>
 * Callers that must run without the check (XML import via internal_addAction, and the default
 * "assign" action added when an attribute definition is created) use
 * AttributeDefActionDelegate.internal_addAction, which stays unchecked.
 * </p>
 */
public class AttributeDefActionAuthorization {

  /**
   * no instances, static utility
   */
  private AttributeDefActionAuthorization() {
  }

  /**
   * Make sure the subject of the current (static) grouper session may change the actions of
   * the attribute definition, i.e. is root, wheel, or has ATTR_ADMIN on it.
   * Fails closed: if there is no static grouper session, GrouperSession.staticGrouperSession()
   * throws, so nothing is written.
   * @param attributeDef whose actions are about to change
   * @throws InsufficientPrivilegeException if the subject cannot ATTR_ADMIN the definition
   */
  public static void assertCanEditActions(AttributeDef attributeDef) {

    // throws if there is no session, so an unauthenticated code path cannot change actions
    Subject subject = GrouperSession.staticGrouperSession().getSubject();

    // root and wheel can do anything (canAttrAdmin also allows them, this just avoids a query)
    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return;
    }

    // same rule the UI (UiV2AttributeDefAction) and AttributeDef.store() use
    if (attributeDef.getPrivilegeDelegate().canAttrAdmin(subject)) {
      return;
    }

    throw new InsufficientPrivilegeException(GrouperUtil.subjectToString(subject)
        + " is not attrAdmin on attributeDef, cannot change its actions: " + attributeDef.getName());
  }

}
