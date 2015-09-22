/*******************************************************************************
 * Copyright 2015 Internet2
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
package edu.internet2.middleware.grouper.hooks.examples;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSourceAdapter;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * assign READ to an admins group based on attribute assignment to a parent folder
 * 
 * configure in grouper.properties:
 * 
 * hooks.group.class=edu.internet2.middleware.grouper.hooks.examples.AssignReadonlyAdminPrivilegeGroupHook
 * hooks.membership.class=edu.internet2.middleware.grouper.hooks.examples.AssignReadonlyAdminPrivilegeVetoMembershipHook
 * 
 * grouper.readonlyAdminEnforced.attributeDefName = a:b:c:reaodnlyAdmin
 * grouper.readonlyAdminEnforced.groupName = c:d:readonlyAdmins
 * 
 * setup objects in GSH:
 * 
 * grouperSession = GrouperSession.startRootSession();
 * String attributeFolderName = "a:b:c";
 * attributeDef = new AttributeDefSave(grouperSession).assignName(attributeFolderName + ":readonlyAdminDef").assignToStem(true).assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
 * attributeDef.getAttributeDefActionDelegate().configureActionList("assign");
 * attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef).assignName(attributeFolderName + ":readonlyAdmin").assignCreateParentStemsIfNotExist(true).save();
 * groupAdmin = new GroupSave(grouperSession).assignName("c:d:readonlyAdmins").assignCreateParentStemsIfNotExist(true).save();
 * 
 * make a group to test:
 * 
 * stem = new StemSave(grouperSession).assignName("l:m").assignCreateParentStemsIfNotExist(true).save();
 * stem.getAttributeDelegate().assignAttribute(attributeDefName);
 * groupSub = new GroupSave(grouperSession).assignName("l:m:n:o").assignCreateParentStemsIfNotExist(true).save();
 * groupNotSub = new GroupSave(grouperSession).assignName("l:p").assignCreateParentStemsIfNotExist(true).save();
 * 
 * </pre>
 */
public class AssignReadonlyAdminPrivilegeVetoMembershipHook extends MembershipHooks {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log logger = GrouperUtil
      .getLog(AssignReadonlyAdminPrivilegeVetoMembershipHook.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreRemoveMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
   */
  @Override
  public void membershipPreRemoveMember(final HooksContext hooksContext,
      final HooksMembershipChangeBean preDeleteMemberBean) {

    //if we are in group delete, then allow
    if (Group.deleteOccuring()) {
      return;
    }

    Field field = preDeleteMemberBean.getMembership().getField();

    if (AccessPrivilege.READ.getField().getName().equals(field.getName())
        && StringUtils.equals(preDeleteMemberBean.getMember().getSubjectSourceId(), GrouperSourceAdapter.groupSourceId())) {
      
      Group thisGroup = preDeleteMemberBean.getGroup();

      // ReadOnly Admin Enforced Attribute DefName, if found enforce this hook
      final String READONLY_ADMIN_ENFORCED_ATTRIBUTE_DEF_NAME = GrouperConfig
          .retrieveConfig().propertyValueStringRequired(
              "grouper.readonlyAdminEnforced.attributeDefName");

      if (thisGroup.getAttributeDelegate().hasAttributeOrAncestorHasAttribute(
          READONLY_ADMIN_ENFORCED_ATTRIBUTE_DEF_NAME, false)) {
        Group membershipGroup = preDeleteMemberBean.getMember().toGroup();

        // ReadOnly Admin Group
        final String READONLY_ADMIN_GROUP = GrouperConfig.retrieveConfig()
            .propertyValueStringRequired("grouper.readonlyAdminEnforced.groupName");

        if (READONLY_ADMIN_GROUP.equalsIgnoreCase(membershipGroup.getName())) {
          throw new HookVeto("readonlyAdmin.remove.veto",
              "Cannot remove read-only admin's READ privilege.");
        }
      }
    }
  }
}