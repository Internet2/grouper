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
public class AssignSelfOptOutMembershipPrivilegeRevocationVetoHook extends MembershipHooks {

  /**
   * 
   */
  public static final String GROUPER_OPT_OUT_REQUIRED_ATTRIBUTE_DEF_NAME = "grouper.optOutRequired.attributeDefName";

  /** logger */
  @SuppressWarnings("unused")
  private static final Log logger = GrouperUtil.getLog(AssignSelfOptOutMembershipPrivilegeRevocationVetoHook.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreRemoveMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean)
   */
  @Override
  public void membershipPreRemoveMember(HooksContext hooksContext,
      HooksMembershipChangeBean preDeleteMemberBean) {
    
    //if we are in group delete, then allow
    if (Group.deleteOccuring()) {
      return;
    }
    
    Field field = preDeleteMemberBean.getMembership().getField();

    if (AccessPrivilege.OPTOUT.getField().getName().equals(field.getName())
        && StringUtils.equals(preDeleteMemberBean.getMember().getSubjectSourceId(), GrouperSourceAdapter.groupSourceId())) {
      
      Group thisGroup = preDeleteMemberBean.getGroup();
      Group membershipGroup = preDeleteMemberBean.getMember().toGroup();

      if (thisGroup.getUuid().equals(membershipGroup.getUuid())) {

        //assign this attribute to a group or folder where opt out is required
        String optoutRequiredAttributeDefName = GrouperConfig.retrieveConfig().propertyValueStringRequired(GROUPER_OPT_OUT_REQUIRED_ATTRIBUTE_DEF_NAME);
        
        if (thisGroup.getAttributeDelegate().hasAttributeOrAncestorHasAttribute(optoutRequiredAttributeDefName, false)) {

          throw new HookVeto("self.optout.remove.veto",
              "Cannot remove self-assigned OptOut privilege.");
        }
      }
    }
  }
}
