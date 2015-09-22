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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;

import org.apache.commons.logging.Log;

/**
 * <pre>
 * If you want the group or groups in a folder to allow opt out to anyone in group, 
 * then configure this hook in grouper.properties:
 * 
 * hooks.group.class=edu.internet2.middleware.grouper.hooks.examples.AssignSelfOptOutGroupPrivilegeHook
 * hooks.membership.class=edu.internet2.middleware.grouper.hooks.examples.AssignSelfOptOutMembershipPrivilegeRevocationVetoHook
 * 
 * configure an attribute to assign to groups or folders in grouper.properties
 * 
 * grouper.optOutRequired.attributeDefName = a:b:c:assignOptOut
 * 
 * https://bugs.internet2.edu/jira/browse/GRP-1197
 * 
 * grouperSession = GrouperSession.startRootSession(); 
 * String attributeFolderName = "a:b:c"; 
 * attributeDef = new AttributeDefSave(grouperSession).assignName(attributeFolderName + ":assignOptOutDef").assignToStem(true).assignAttributeDefType(AttributeDefType.attr).assignCreateParentStemsIfNotExist(true).assignValueType(AttributeDefValueType.marker).save();
 * attributeDef.getAttributeDefActionDelegate().configureActionList("assign"); 
 * attributeDefName = new AttributeDefNameSave(grouperSession, attributeDef).assignName(attributeFolderName + ":assignOptOut").assignCreateParentStemsIfNotExist(true).save(); 
 * 
 * Test it out
 * 
 * stem = new StemSave(grouperSession).assignName("j:k").assignCreateParentStemsIfNotExist(true).save();
 * stem.getAttributeDelegate().assignAttribute(attributeDefName);
 * groupSub = new GroupSave(grouperSession).assignName("j:k:l:m").assignCreateParentStemsIfNotExist(true).save();
 *
 * </pre>
 */
public class AssignSelfOptOutGroupPrivilegeHook extends GroupHooks {

  /** logger */
  private static final Log logger = GrouperUtil.getLog(AssignSelfOptOutGroupPrivilegeHook.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.GroupHooks#groupPostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean)
   */
  @Override
  public void groupPostCommitInsert(HooksContext hooksContext,
      HooksGroupBean postCommitInsertBean) {
    
    //only care about this if not grouper loader
    if (GrouperContextTypeBuiltIn.GROUPER_LOADER.equals(hooksContext
        .getGrouperContextType())) {
      return;
    }

    Group thisGroup = GroupFinder.findByUuid(GrouperSession.startRootSession(),
        postCommitInsertBean.getGroup().getId(), false);
    if (logger.isDebugEnabled()) {
      logger.debug("The Group: " + thisGroup);
      logger.debug("Group's subject " + thisGroup.toSubject());
    }
    //assign this attribute to a group or folder where opt out is required
    String optoutRequiredAttributeDefName = GrouperConfig.retrieveConfig().propertyValueStringRequired(
        AssignSelfOptOutMembershipPrivilegeRevocationVetoHook.GROUPER_OPT_OUT_REQUIRED_ATTRIBUTE_DEF_NAME);
    
    if (thisGroup.getAttributeDelegate().hasAttributeOrAncestorHasAttribute(optoutRequiredAttributeDefName, false)) {

      //assign opt out priv
      thisGroup.grantPriv(thisGroup.toSubject(), AccessPrivilege.OPTOUT, false);
    }
  }
}
