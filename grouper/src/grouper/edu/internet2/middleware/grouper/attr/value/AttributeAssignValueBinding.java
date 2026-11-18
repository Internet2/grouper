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
package edu.internet2.middleware.grouper.attr.value;

import java.util.Collection;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * SECURITY: binds an attribute assignment value to the assignment it belongs to.
 *
 * Callers (UI handlers, WS assignAttributes remove_value, AttributeAssignValueDelegate.deleteValues)
 * authorize an attribute assignment, and then operate on a value whose id came from the request.
 * Unless the value is checked to belong to the authorized assignment, a caller who may update one
 * assignment can read, edit or delete a value of any other assignment whose value id they know.
 * The invariant enforced here is:
 *
 *   attributeAssignValue.getAttributeAssignId().equals(attributeAssign.getId())
 *
 * This is its own class so the fix can be dropped into older releases as whole files.  It only uses
 * APIs that have been stable since 5.0.0, and no commons-lang (5.x uses commons-lang 2, 6.x and
 * later commons-lang3), so the same file works in every release.
 */
public class AttributeAssignValueBinding {

  /**
   * fixed error text: it must not include the value id or contents, since the value may belong to
   * an object the caller cannot read
   */
  public static final String NOT_BOUND_MESSAGE = "Attribute assignment value is not owned by the authorized assignment";

  /**
   * no instances
   */
  private AttributeAssignValueBinding() {
  }

  /**
   * Whether a value belongs to an assignment.
   * @param attributeAssign the authorized assignment
   * @param attributeAssignValue the value
   * @return true if both are non-null and the value's attributeAssignId is the assignment's id
   */
  public static boolean isBound(AttributeAssign attributeAssign, AttributeAssignValue attributeAssignValue) {
    if (attributeAssign == null || attributeAssignValue == null) {
      return false;
    }
    String attributeAssignId = attributeAssign.getId();
    // a blank id never matches: an unsaved assignment does not own any stored value
    if (attributeAssignId == null || attributeAssignId.length() == 0) {
      return false;
    }
    return attributeAssignId.equals(attributeAssignValue.getAttributeAssignId());
  }

  /**
   * Look up a value by the id from a request, but only if it belongs to the given (already authorized)
   * assignment.  A nonexistent id and the id of another assignment's value both return null, so the
   * caller cannot tell them apart.
   *
   * The value is loaded with the DAO and then checked, rather than picked out of
   * attributeAssign.getValueDelegate().retrieveValues(), because retrieveValues() also checks READ on
   * the assignment, and the UI edit and delete handlers have only ever required UPDATE.
   * @param attributeAssign the authorized assignment
   * @param attributeAssignValueId value id from the request
   * @return the value, or null if blank, not found, or not bound to the assignment
   */
  public static AttributeAssignValue retrieveBoundValue(AttributeAssign attributeAssign, String attributeAssignValueId) {
    if (attributeAssign == null || attributeAssignValueId == null || attributeAssignValueId.trim().length() == 0) {
      return null;
    }
    AttributeAssignValue attributeAssignValue = GrouperDAOFactory.getFactory().getAttributeAssignValue()
        .findById(attributeAssignValueId, false);
    if (!isBound(attributeAssign, attributeAssignValue)) {
      return null;
    }
    return attributeAssignValue;
  }

  /**
   * Make sure every value belongs to the assignment, before anything is changed.  The whole collection
   * is checked first so a mixed request (some own values, some foreign) changes nothing.
   * @param attributeAssign the authorized assignment
   * @param attributeAssignValues values about to be changed or deleted
   * @throws InsufficientPrivilegeException on the first value that does not belong to the assignment
   */
  public static void assertValuesBound(AttributeAssign attributeAssign, Collection<AttributeAssignValue> attributeAssignValues) {
    if (attributeAssignValues == null) {
      return;
    }
    for (AttributeAssignValue attributeAssignValue : attributeAssignValues) {
      if (!isBound(attributeAssign, attributeAssignValue)) {
        throw new InsufficientPrivilegeException(NOT_BOUND_MESSAGE);
      }
    }
  }

}
