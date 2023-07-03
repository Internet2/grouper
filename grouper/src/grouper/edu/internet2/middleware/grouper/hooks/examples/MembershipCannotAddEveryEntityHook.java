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
import edu.internet2.middleware.grouper.SubjectFinder;
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
 * Hook allows only sysadmins to add every entity to group or privileges
 */
public class MembershipCannotAddEveryEntityHook extends MembershipHooks {

  /**
   * 
   */
  public static void clearHook() {
    registered = false;
  }

  /**
   * 
   */
  public static final String HOOK_VETO_CANNOT_ADD_EVERY_ENTITY = "hook.veto.cannotAddEveryEntity";

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(MembershipCannotAddEveryEntityHook.class);

  /**
   * if this feature is enabled
   * @return true if enabled
   */
  public static boolean cannotAddEveryEntityEnabled() {
    return GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.enable.rule.cannotAddEveryEntity", false);
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
   */
  @Override
  public void membershipPreAddMember(HooksContext hooksContext,
      final HooksMembershipChangeBean preAddMemberBean) {

    if (GrouperCheckConfig.inCheckConfig || !GrouperStartup.isFinishedStartupSuccessfully()) {
      return;
    }

    //if not every entity, dont worry about it
    if (!SubjectHelper.eq(preAddMemberBean.getMember().getSubject(), SubjectFinder.findAllSubject())) {
      return;
    }

    
    // wheel or root is ok
    if (PrivilegeHelper.isWheelOrRoot(GrouperSession.staticGrouperSession().getSubject())) {
      return;
    }
    
    
    throw new HookVeto(HOOK_VETO_CANNOT_ADD_EVERY_ENTITY, "Error: you cannot add EveryEntity to a group or privilege.  Only a system administrator can do this.");
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
        MembershipCannotAddEveryEntityHook.class);

    
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
