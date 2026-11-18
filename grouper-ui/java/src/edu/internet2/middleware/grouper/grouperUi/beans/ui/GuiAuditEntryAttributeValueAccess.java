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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * <p>Decides if an attribute value stored in an audit row (attribute assign value add, update,
 * delete) may be shown to the user looking at the audit.</p>
 *
 * <p>Audit rows are selected by the object they are about (a group, folder, subject), so seeing
 * the row only proves access to that object.  The value belongs to an attribute definition with
 * its own ATTR_READ privilege, so it is shown only if the user can read that attribute
 * definition.  The definition is looked up by the immutable attribute def name id stored in the
 * row (so renames do not matter); if it can no longer be found (deleted name or definition, or no
 * id in the row) the value is not shown.</p>
 */
public class GuiAuditEntryAttributeValueAccess {

  /**
   * no instances
   */
  private GuiAuditEntryAttributeValueAccess() {
  }

  /**
   * see if a subject may read the values of an attribute def name (ATTR_READ on its definition)
   * @param attributeDefNameId id of the attribute def name from the audit row
   * @param subject user looking at the audit, null means no
   * @return true if the value may be shown
   */
  public static boolean canReadValue(final String attributeDefNameId, final Subject subject) {

    if (subject == null || attributeDefNameId == null || attributeDefNameId.trim().length() == 0) {
      return false;
    }

    // look up as root: the user may not be able to see the name, which is exactly the case to redact
    return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

        // wheel and root (and read-only root, which can read every attribute) see all values
        if (PrivilegeHelper.isWheelOrRootOrReadonlyRoot(subject)) {
          return true;
        }

        AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, false);
        if (attributeDefName == null) {
          return false;
        }
        AttributeDef attributeDef = null;
        try {
          attributeDef = attributeDefName.getAttributeDef();
        } catch (RuntimeException re) {
          // definition is gone: redact rather than disclose
          attributeDef = null;
        }
        if (attributeDef == null) {
          return false;
        }
        return attributeDef.getPrivilegeDelegate().canAttrRead(subject);
      }
    });
  }
}
