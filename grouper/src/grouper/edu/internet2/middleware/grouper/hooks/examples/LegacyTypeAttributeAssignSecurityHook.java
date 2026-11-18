/**
 * Copyright 2014 Internet2
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
package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.hooks.AttributeAssignHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * <pre>
 * Built in hook, registered by GroupTypeSecurityHook.registerHookIfNecessary() when any
 * security.types.&lt;type&gt;.wheelOnly or security.types.&lt;type&gt;.allowOnlyGroup setting is
 * configured (the same switch as GroupTypeSecurityHook and AttributeSecurityFromTypeHook).
 *
 * A migrated legacy attribute (e.g. grouperLoaderType, grouperLoaderQuery) is stored as:
 *   1. a legacy group type marker assignment on the group (e.g. legacyGroupType_grouperLoader),
 *   2. a nested assignment on that marker (e.g. legacyAttribute_grouperLoaderType),
 *   3. a value on the nested assignment.
 *
 * GroupTypeSecurityHook checks (1) and AttributeSecurityFromTypeHook checks (3), but nothing
 * checked (2), so a caller who is not allowed to edit a protected type could still create or
 * delete an empty nested assignment (e.g. WS assignAttributes with no value).  This hook runs
 * the same GroupTypeSecurityHook.vetoIfNecessary() check for any assignment whose owner is a
 * legacy group type marker assignment, inside the save transaction, so a vetoed assignment is
 * rolled back from every entry point (WS, UI, GSH, API).
 *
 * vetoIfNecessary() returns right away for types with no security.types setting and allows
 * wheel, root, and members of the allowOnlyGroup, so authorized callers are not affected.
 * </pre>
 */
public class LegacyTypeAttributeAssignSecurityHook extends AttributeAssignHooks {

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPostInsert(HooksContext hooksContext,
      HooksAttributeAssignBean postInsertBean) {
    vetoIfLegacyTypeProtected(postInsertBean.getAttributeAssign(), "adding");
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPostUpdate(HooksContext hooksContext,
      HooksAttributeAssignBean postUpdateBean) {
    vetoIfLegacyTypeProtected(postUpdateBean.getAttributeAssign(), "changing");
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPostDelete(HooksContext hooksContext,
      HooksAttributeAssignBean postDeleteBean) {
    vetoIfLegacyTypeProtected(postDeleteBean.getAttributeAssign(), "deleting");
  }

  /**
   * if the assignment is nested on a legacy group type marker assignment, veto it unless the
   * session subject may edit that group type
   * @param attributeAssign the nested assignment being saved or deleted
   * @param summaryForLog adding, changing or deleting
   * @throws HookVeto if the type is protected and the subject is not allowed
   */
  public static void vetoIfLegacyTypeProtected(AttributeAssign attributeAssign, String summaryForLog) throws HookVeto {

    if (attributeAssign == null) {
      return;
    }

    // only assignments on an assignment (nested) can be legacy configuration assignments
    String ownerAttributeAssignId = attributeAssign.getOwnerAttributeAssignId();
    if (ownerAttributeAssignId == null || ownerAttributeAssignId.length() == 0) {
      return;
    }

    // load the owner without security: this is an internal lookup to decide which rule applies,
    // the security decision itself is made by vetoIfNecessary based on the session subject.
    // Not found is fine (e.g. the marker is already gone), there is nothing to protect then.
    AttributeAssign ownerAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(ownerAttributeAssignId, false);
    if (ownerAssign == null) {
      return;
    }

    // null unless the owner is a legacy group type marker on a group
    GroupTypeTuple groupTypeTuple = GroupTypeTuple.internal_getGroupTypeTuple(ownerAssign, false);
    if (groupTypeTuple == null) {
      return;
    }

    String attributeName = attributeAssign.getAttributeDefNameId();
    try {
      AttributeDefName attributeDefName = attributeAssign.getAttributeDefName();
      if (attributeDefName != null) {
        attributeName = attributeDefName.getName();
      }
    } catch (RuntimeException re) {
      // only used for the log / veto message, the id is good enough
    }

    GroupTypeSecurityHook.vetoIfNecessary(groupTypeTuple.getGroupUuid(), groupTypeTuple.getTypeUuid(),
        summaryForLog + " attribute assignment " + attributeName);
  }

}
