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
package edu.internet2.middleware.grouper.permissions.limits;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;

/**
 * SECURITY: authorization for the permission limit screens of the UI (UiV2GroupPermission and
 * UiV2SubjectPermission limitDelete, limitValueDelete, limitValueEdit, limitValueEditSubmit).
 *
 * The UI used to delete a limit first and check privileges afterward, so a caller who could only
 * VIEW the limit removed it and then got an error.  It also never checked that the id was a limit
 * at all, so any viewable assignment (a permission, a deny row, a group attribute) could be deleted
 * that way, and the limit value handlers skipped the permission definition check.
 *
 * Everything here is derived from the limit assignment itself, never from the request:
 *
 * - the assignment is found with the secure finder (the caller must be able to view it),
 * - it must be a limit: an assignment on an assignment (group_asgn or any_mem_asgn) whose attribute
 *   definition is of type limit,
 * - its owner must be a permission assignment (group or any_mem, definition of type perm) on a role,
 * - the caller needs ATTR_UPDATE on the limit definition, GROUP_ATTR_UPDATE on the role, and
 *   ATTR_UPDATE on the permission definition.
 *
 * Errors are reported as existing UI text keys, so no new externalized text is needed.  This class is
 * in the core so it can be unit tested without a UI harness; it only uses APIs that have been stable
 * since 5.0.0 and no commons-lang, so the same file works in every release.
 */
public class PermissionLimitUiAuthorization {

  /** text key: caller cannot update the limit definition */
  public static final String ERROR_CANT_EDIT_ATTRIBUTE_DEF = "simplePermissionUpdate.errorCantEditAttributeDef";

  /** text key: caller cannot manage the role (or the role cannot be resolved) */
  public static final String ERROR_CANT_MANAGE_ROLE = "simplePermissionUpdate.errorCantManageRole";

  /** text key: caller cannot update the permission definition, or the id is not a limit */
  public static final String ERROR_CANT_EDIT_LIMIT = "simplePermissionUpdate.errorCantEditLimit";

  /** the limit assignment, if found */
  private AttributeAssign limitAssign;

  /** the permission assignment the limit is on, if it is a limit */
  private AttributeAssign permissionAssign;

  /** the role the permission is on */
  private Group role;

  /** null if authorized, else the text key of the error to show */
  private String errorTextKey;

  /**
   * use authorize()
   */
  private PermissionLimitUiAuthorization() {
  }

  /**
   * the limit assignment (null if it could not be found or viewed)
   * @return the limit assignment
   */
  public AttributeAssign getLimitAssign() {
    return this.limitAssign;
  }

  /**
   * the permission assignment the limit is on (only set if the id is a limit)
   * @return the permission assignment
   */
  public AttributeAssign getPermissionAssign() {
    return this.permissionAssign;
  }

  /**
   * the role the permission is assigned on (only set if the id is a limit)
   * @return the role
   */
  public Group getRole() {
    return this.role;
  }

  /**
   * null if the caller may change this limit, else the UI text key of the error to show
   * @return the error text key or null
   */
  public String getErrorTextKey() {
    return this.errorTextKey;
  }

  /**
   * true if the caller may change this limit and its values
   * @return true if authorized
   */
  public boolean isAuthorized() {
    return this.errorTextKey == null;
  }

  /**
   * Resolve a limit assignment by id and check that the subject may change it.  Nothing is changed.
   * @param grouperSession session of the logged in subject
   * @param subject logged in subject
   * @param limitAssignId id of the limit assignment, from the request
   * @return the result, check isAuthorized() / getErrorTextKey()
   */
  public static PermissionLimitUiAuthorization authorize(GrouperSession grouperSession, Subject subject, String limitAssignId) {

    PermissionLimitUiAuthorization result = new PermissionLimitUiAuthorization();

    // secure finder: the caller must at least be able to view the assignment and its underlying assignment
    AttributeAssign theLimitAssign = null;
    if (limitAssignId != null && limitAssignId.trim().length() > 0) {
      theLimitAssign = AttributeAssignFinder.findById(limitAssignId, false);
    }
    result.limitAssign = theLimitAssign;
    if (theLimitAssign == null) {
      result.errorTextKey = ERROR_CANT_EDIT_LIMIT;
      return result;
    }

    //  ---- it must really be a limit, else the delete would remove any viewable assignment
    AttributeAssignType limitAssignType = theLimitAssign.getAttributeAssignType();
    if (limitAssignType != AttributeAssignType.group_asgn && limitAssignType != AttributeAssignType.any_mem_asgn) {
      result.errorTextKey = ERROR_CANT_EDIT_LIMIT;
      return result;
    }
    AttributeDef limitDef = theLimitAssign.getAttributeDef();
    if (limitDef == null || limitDef.getAttributeDefType() != AttributeDefType.limit) {
      result.errorTextKey = ERROR_CANT_EDIT_LIMIT;
      return result;
    }

    //  ---- its owner must be a permission assignment on a role
    AttributeAssign thePermissionAssign = theLimitAssign.getOwnerAttributeAssign();
    if (thePermissionAssign == null) {
      result.errorTextKey = ERROR_CANT_EDIT_LIMIT;
      return result;
    }
    AttributeAssignType permissionAssignType = thePermissionAssign.getAttributeAssignType();
    if (permissionAssignType != AttributeAssignType.group && permissionAssignType != AttributeAssignType.any_mem) {
      result.errorTextKey = ERROR_CANT_EDIT_LIMIT;
      return result;
    }
    AttributeDef permissionDef = thePermissionAssign.getAttributeDef();
    if (permissionDef == null || permissionDef.getAttributeDefType() != AttributeDefType.perm) {
      result.errorTextKey = ERROR_CANT_EDIT_LIMIT;
      return result;
    }
    result.permissionAssign = thePermissionAssign;

    //  ---- limit definition: ATTR_UPDATE
    if (!PrivilegeHelper.canAttrUpdate(grouperSession, limitDef, subject)) {
      result.errorTextKey = ERROR_CANT_EDIT_ATTRIBUTE_DEF;
      return result;
    }

    //  ---- role: found in the caller's session, a role, and GROUP_ATTR_UPDATE
    String roleId = thePermissionAssign.getOwnerGroupId();
    Group theRole = roleId == null ? null : GroupFinder.findByUuid(grouperSession, roleId, false);
    if (theRole == null || theRole.getTypeOfGroup() != TypeOfGroup.role || !theRole.hasGroupAttrUpdate(subject)) {
      result.errorTextKey = ERROR_CANT_MANAGE_ROLE;
      return result;
    }
    result.role = theRole;

    //  ---- permission definition: ATTR_UPDATE (the limit value handlers used to skip this)
    // PrivilegeHelper, not the delegate: the delegate first requires the session subject to have ATTR_ADMIN
    // (to read privileges) and throws for ordinary users instead of returning false
    if (!PrivilegeHelper.canAttrUpdate(grouperSession, permissionDef, subject)) {
      result.errorTextKey = ERROR_CANT_EDIT_LIMIT;
      return result;
    }

    return result;
  }

  /**
   * Delete the authorized limit (and, as AttributeAssign.delete() does, its values and any assignments
   * on it) in one transaction.  Only call this after authorize() said yes.
   * @throws InsufficientPrivilegeException if this limit was not authorized
   */
  public void deleteLimit() {
    // defense in depth: never delete something that did not pass authorize()
    if (!this.isAuthorized() || this.limitAssign == null) {
      throw new InsufficientPrivilegeException("Not allowed to delete this permission limit");
    }
    GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {
        PermissionLimitUiAuthorization.this.limitAssign.delete();
        return null;
      }
    });
  }

}
