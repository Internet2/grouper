/*
 * @author mchyzer
 * $Id: MembershipHooksImplExample.java,v 1.3 2008-06-26 11:16:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.hooks;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.GrouperBuiltinContextType;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextType;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreAddMemberBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class MembershipHooksImplExample extends MembershipHooks {

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.MembershipHooks#membershipPreAddMember(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksMembershipPreAddMemberBean)
   */
  @Override
  public void membershipPreAddMember(HooksContext hooksContext, HooksMembershipPreAddMemberBean preAddMemberBean) {

    //TODO make this unknown if not known
    GrouperContextType grouperContextType = hooksContext.getGrouperContextType();
    
    //only care about this if not grouper loader
    if (!grouperContextType.equals(GrouperBuiltinContextType.GROUPER_LOADER)) {
      
      //if the act as user is is in the wheel group, then just admonish
      if (hooksContext.isSubjectActAsInGroup("penn:etc:sysAdminGroup")) {
        
        //add warning to system
        
      } else {
        
        Group group = preAddMemberBean.getGroup();
        GroupType groupType = null;
        try {
          groupType = GroupTypeFinder.find("grouperLoader");
        } catch (SchemaException se) {
          throw new RuntimeException(se);
        }
        if (group.hasType(groupType)) {
          throw new HookVeto("hook.veto.loader.membership", "the membership of this group is automatically managed and does not permit manual changes");
        }
        
      }
      
      
      
    }
  }

}
