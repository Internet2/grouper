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
package edu.internet2.middleware.grouper.audit;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <p>Writes audit entries for changes to the permission inheritance links:</p>
 * <ul>
 *   <li>role inheritance (grouper_role_set): members of the "if has" role also get the
 *   permissions of the "then has" role</li>
 *   <li>permission name implication (grouper_attribute_def_name_set): holders of the "if has"
 *   permission name also hold the "then has" one</li>
 *   <li>action implication (grouper_attr_assign_action_set): the "if has" action implies the
 *   "then has" action</li>
 * </ul>
 * <p>These links change who effectively holds a permission, but before this class they only
 * wrote change log rows, which do not name the acting user in the audit screens and are purged.
 * The delegates (RoleInheritanceDelegate, AttributeDefNameSetDelegate,
 * AttributeAssignActionSetDelegate) run their set change through this class so the change and
 * its audit entry are in one transaction.</p>
 * <p>Kept in its own class (one version for every release) so the delegate edits stay small.</p>
 */
public class PermissionInheritanceAudit {

  /**
   * the set change to run inside the audited transaction
   */
  public static interface SetChange {

    /**
     * make the change
     * @return true if something was added or removed, false if nothing changed
     */
    public boolean change();
  }

  /**
   * add or remove a role inheritance link, and audit it if it changed
   * @param add true for add, false for remove
   * @param ifHasRole members of this role ...
   * @param thenHasRole ... also get the permissions of this role
   * @param setChange the change to make
   * @return true if changed
   */
  public static boolean roleInheritance(boolean add, Role ifHasRole, Role thenHasRole, SetChange setChange) {
    String description = (add ? "Added" : "Removed") + " role inheritance: members of role "
        + ifHasRole.getName() + " " + (add ? "now also get" : "no longer get") + " the permissions of role "
        + thenHasRole.getName();
    return changeAndAudit(add ? AuditTypeBuiltin.ROLE_INHERITANCE_ADD : AuditTypeBuiltin.ROLE_INHERITANCE_DELETE,
        description, setChange,
        "ifHasRoleId", ifHasRole.getId(), "ifHasRoleName", ifHasRole.getName(),
        "thenHasRoleId", thenHasRole.getId(), "thenHasRoleName", thenHasRole.getName());
  }

  /**
   * add or remove a permission name implication, and audit it if it changed
   * @param add true for add, false for remove
   * @param ifHasAttributeDefName holders of this name ...
   * @param thenHasAttributeDefName ... also hold this name
   * @param setChange the change to make
   * @return true if changed
   */
  public static boolean attributeDefNameInheritance(boolean add, AttributeDefName ifHasAttributeDefName,
      AttributeDefName thenHasAttributeDefName, SetChange setChange) {
    String description = (add ? "Added" : "Removed") + " permission name implication: "
        + ifHasAttributeDefName.getName() + " " + (add ? "now implies " : "no longer implies ")
        + thenHasAttributeDefName.getName();
    return changeAndAudit(add ? AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_INHERITANCE_ADD
        : AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_INHERITANCE_DELETE,
        description, setChange,
        "ifHasAttributeDefNameId", ifHasAttributeDefName.getId(),
        "ifHasAttributeDefNameName", ifHasAttributeDefName.getName(),
        "thenHasAttributeDefNameId", thenHasAttributeDefName.getId(),
        "thenHasAttributeDefNameName", thenHasAttributeDefName.getName());
  }

  /**
   * add or remove an action implication, and audit it if it changed
   * @param add true for add, false for remove
   * @param ifHasAction this action ...
   * @param thenHasAction ... implies this action
   * @param setChange the change to make
   * @return true if changed
   */
  public static boolean actionInheritance(boolean add, AttributeAssignAction ifHasAction,
      AttributeAssignAction thenHasAction, SetChange setChange) {

    // name the attribute def the actions belong to, the action names alone are not unique
    AttributeDef attributeDef = null;
    try {
      attributeDef = ifHasAction.getAttributeDef();
    } catch (RuntimeException re) {
      // the audit still names the action ids
      attributeDef = null;
    }
    String attributeDefId = attributeDef == null ? ifHasAction.getAttributeDefId() : attributeDef.getId();
    String attributeDefName = attributeDef == null ? null : attributeDef.getName();

    String description = (add ? "Added" : "Removed") + " action implication on attribute def "
        + attributeDefName + ": action " + ifHasAction.getName() + " "
        + (add ? "now implies " : "no longer implies ") + thenHasAction.getName();
    return changeAndAudit(add ? AuditTypeBuiltin.ATTRIBUTE_DEF_ACTION_INHERITANCE_ADD
        : AuditTypeBuiltin.ATTRIBUTE_DEF_ACTION_INHERITANCE_DELETE,
        description, setChange,
        "attributeDefId", attributeDefId, "attributeDefName", attributeDefName,
        "ifHasActionId", ifHasAction.getId(), "ifHasActionName", ifHasAction.getName(),
        "thenHasActionId", thenHasAction.getId(), "thenHasActionName", thenHasAction.getName());
  }

  /**
   * run the change in a transaction that will audit; write one audit entry if something changed
   * and the caller does not write its own audit (isCallerWillCreateAudit)
   * @param auditTypeBuiltin type of entry
   * @param description human readable text
   * @param setChange the change
   * @param labelNamesAndValues audit labels and values
   * @return true if changed
   */
  private static boolean changeAndAudit(final AuditTypeBuiltin auditTypeBuiltin, final String description,
      final SetChange setChange, final String... labelNamesAndValues) {

    Object result = HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {

            boolean changed = setChange.change();

            // only audit a real change, and not when an outer caller writes its own audit
            if (changed && !hibernateHandlerBean.isCallerWillCreateAudit()) {
              AuditEntry auditEntry = new AuditEntry(auditTypeBuiltin, labelNamesAndValues);
              auditEntry.setDescription(GrouperUtil.abbreviate(description, 3500));
              auditEntry.saveOrUpdate(true);
            }
            return changed;
          }
        });
    return result instanceof Boolean && (Boolean)result;
  }

}
