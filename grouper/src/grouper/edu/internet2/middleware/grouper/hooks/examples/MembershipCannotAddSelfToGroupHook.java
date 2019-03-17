/**
 * Copyright 2018 Internet2
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

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Hook allows only one membership in a folder at a time
 */
public class MembershipCannotAddSelfToGroupHook extends MembershipHooks {

  /**
   * 
   */
  public static final String HOOK_VETO_CANNOT_ADD_SELF_TO_GROUP = "hook.veto.cannotAddSelfToGroup";

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(MembershipCannotAddSelfToGroupHook.class);

  /**
   * if this feature is enabled
   * @return true if enabled
   */
  public static boolean cannotAddSelfEnabled() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.enable.rule.cannotAddSelfToGroup", false);
  }

  /**
   * if the current group has cannotAddSelf
   * @param group group to check
   * @return if assigned to group
   */
  public static boolean cannotAddSelfAssignedToGroup(final Group group) {
    if (!cannotAddSelfEnabled()) {
      return false;
    }
    return (Boolean)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          
            return group.getAttributeDelegate().hasAttribute(MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName());
          }
        });
  }

  /**
   * assign the attribute to the group which means cannot add self
   * @param group
   * @return if the value was assigned
   */
  public static boolean cannotAddSelfAssign(final Group group) {

    return (Boolean)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

            AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().assignAttribute(MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName());
            return attributeAssignResult.isChanged();
          }
        });
  }

  /**
   * revoke the attribute to the group which means cannot add self
   * @param group
   * @return if the value was revoked
   */
  public static boolean cannotAddSelfRevoke(final Group group) {

    return (Boolean)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

            AttributeAssignResult attributeAssignResult = group.getAttributeDelegate().removeAttribute(MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName());
            return attributeAssignResult.isChanged();
          }
        });

  }

  /**
   * if the current user can assign cannotAddSelf
   * @param group the group to check
   * @param subject the subject to check
   * @return is can optin
   */
  public static boolean cannotAddSelfUserCanEdit(final Group group, final Subject subject) {
    if (!cannotAddSelfEnabled()) {
      return false;
    }

    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    
    return (Boolean)GrouperSession.callbackGrouperSession(
        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
          
          @Override
          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

            boolean isUserGroupAdmin = group.canHavePrivilege(subject, AccessPrivilege.ADMIN.getName(), false);
            if (!isUserGroupAdmin) {
              return false;
            }
            
            boolean assigned = cannotAddSelfAssignedToGroup(group);

            if (assigned && isUserGroupAdmin && GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.cannotAddSelfToGroup.allowRevokeByGroupAdmins", false)) {
              return true;
            }
            if (!assigned && isUserGroupAdmin && GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.cannotAddSelfToGroup.allowAssignByGroupAdmins", true)) {
              return true;
            }
            
            // if assigned, see if user can revoke.  else if not assigned, see if the user can assign
            String groupToCheckName = assigned ? cannotAddSelfRevokeGroupName() : cannotAddSelfAssignGroupName();
            Group groupToCheck = GroupFinder.findByName(grouperSession, groupToCheckName, false);
            if (groupToCheck == null) {
              return false;
            }
            return groupToCheck.hasMember(subject);
            
          }
        });
  }

  /**
   * if the current user can assign cannotAddSelf
   * @param group the group to check
   * @param subject the subject to check
   * @return is can optin
   */
  public static boolean cannotAddSelfUserCanView(final Group group, final Subject subject) {
    if (!cannotAddSelfEnabled()) {
      return false;
    }

    if (PrivilegeHelper.isWheelOrRoot(subject)) {
      return true;
    }
    
    if (group.canHavePrivilege(subject, AccessPrivilege.VIEW.getName(), false)) {
      return true;
    }
    return false;
  }

  /**
   * base stem for these attributes (just in hooks folder)
   * @return the stem name
   */
  public static String cannotAddSelfStemName() {
    return GrouperCheckConfig.attributeRootStemName() + ":cannotAddSelfToGroup";
  }

  /**
   * users who can assign "cannot add self"
   * @return the group name
   */
  public static String cannotAddSelfAssignGroupName() {
    return cannotAddSelfStemName() + ":canAssignCannotAddSelf";
  }

  /**
   * users who can revoke "cannot add self"
   * @return the group name
   */
  public static String cannotAddSelfRevokeGroupName() {
    return cannotAddSelfStemName() + ":canRevokeCannotAddSelf";
  }

  /**
   * attribute def for "cannot add self"
   * @return the group name
   */
  public static String cannotAddSelfNameOfAttributeDef() {
    return cannotAddSelfStemName() + ":cannotAddSelfAttributeDef";
  }

  /**
   * attribute def name for "cannot add self"
   * @return the group name
   */
  public static String cannotAddSelfNameOfAttributeDefName() {
    return cannotAddSelfStemName() + ":cannotAddSelfAttributeDefName";
  }
  
  /**
   * attribute def name for "cannot add self"
   * @return the group name
   */
  public static AttributeDefName cannotAddSelfAttributeDefName() {
    return (AttributeDefName)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return AttributeDefNameFinder.findByName(cannotAddSelfNameOfAttributeDefName(), true);
      }
    });
  }
  
  /**
   * put this attribute on a folder to ensure there is one membership only for any group in folder
   */
  public static final String membershipOneFolderExtensionOfAttributeDefName = "hookMembershipOneInFolder";
  
  /**
   * cache if stem name has the membership one attribute
   */
  private static GrouperCache<String, Boolean> stemHasMembershipOneAttribute = new GrouperCache(
      MembershipCannotAddSelfToGroupHook.class.getName() + ".membershipOneAttribute", 5000, false, 60, 60, false);
  
//  private static 
//  
//  //register the hook
//  if (index > 0) {
//    //register this hook
//    GrouperHooksUtils.addHookManual(GrouperHookType.GROUP.getPropertyFileKey(), GroupAttributeNameValidationHook.class);
//    GrouperHooksUtils.addHookManual(GrouperHookType.ATTRIBUTE.getPropertyFileKey(), GroupAttributeNameValidationAttrHook.class);
//  }
//  
//  registered = true;

  
  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
   */
  @Override
  public void membershipPreAddMember(HooksContext hooksContext,
      final HooksMembershipChangeBean preAddMemberBean) {

    if (!FieldType.LIST.equals(preAddMemberBean.getField().getType())) {
      return;
    }

    if (GrouperCheckConfig.inCheckConfig || !GrouperStartup.isFinishedStartupSuccessfully()) {
      return;
    }

    //if not same subject, dont worry about it
    if (!SubjectHelper.eq(preAddMemberBean.getMember().getSubject(), GrouperSession.staticGrouperSession().getSubject())) {
      return;
    }
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        // its the same subject, are we checking on this object
        if (!preAddMemberBean.getGroup().getAttributeDelegate().hasAttribute(cannotAddSelfAttributeDefName())) {
          return null;
        }

        throw new HookVeto(HOOK_VETO_CANNOT_ADD_SELF_TO_GROUP, "You cannot add yourself to this group: " 
            + GrouperUtil.xmlEscape(preAddMemberBean.getGroup().getName()) + ".  Someone else must add you.");
      }
    });
  }

  /**
   * only register once
   */
  private static boolean registered = false;
  
  /**
   * see if this is configured in the grouper.properties, if so, register this hook
   */
  public static void registerHookIfNecessary() {
    
    if (registered) {
      return;
    }
    
    //register this hook
    GrouperHooksUtils.addHookManual(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
        MembershipCannotAddSelfToGroupHook.class);

    
    registered = true;

  }

  
//  /**
//   * @return attribute def name for this hook
//   */
//  public static AttributeDefName membershipOneInFolderAttributeDefName() {
//    //initObjectsOnce(false);
//    return AttributeDefNameFinder.findByName(membershipOneFolderStemName() + ":" + membershipOneFolderExtensionOfAttributeDefName, true);
//  }

//  /**
//   * pass in the stem and assign attribute and clear cache
//   * @param stem
//   */
//  public static void assignMembershipOneInFolderAttributeDefName(Stem stem) {
//    
//    stem.getAttributeDelegate().assignAttribute(MembershipCannotAddSelfToGroupHook.membershipOneInFolderAttributeDefName());
//    stemHasMembershipOneAttribute.clear();
//  }


}
