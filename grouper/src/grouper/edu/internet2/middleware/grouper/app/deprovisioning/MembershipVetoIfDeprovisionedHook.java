package edu.internet2.middleware.grouper.app.deprovisioning;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;

public class MembershipVetoIfDeprovisionedHook extends MembershipHooks {
  
  /**
   * 
   */
  public static void clearHook() {
    registered = false;
  }

  /**
   * if this hook is registered
   */
  private static boolean registered = false;
  
  /**
   * see if this is configured in the grouper.properties, if so, register this hook
   */
  public static void registerHookIfNecessary() {
    
    if (registered) {
      return;
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperHook.MembershipVetoIfDeprovisionedHook.autoRegister", false)) {
      //register this hook
      GrouperHooksUtils.addHookManual(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
          MembershipVetoIfDeprovisionedHook.class);
    }
    
    registered = true;

  }

  /**
   * 
   */
  public MembershipVetoIfDeprovisionedHook() {
  }

  @Override
  public void membershipPreAddMember(HooksContext hooksContext, HooksMembershipChangeBean preAddMemberBean) {
    checkMembershipEligibility(preAddMemberBean.getMembership(), hooksContext);
  }

  /**
   * @param membership
   * @param hooksContext
   */
  protected static void checkMembershipEligibility(Membership membership, HooksContext hooksContext) {
    
    final String[] ownerArray = new String[1];
    boolean shouldAddSubject = (boolean)GrouperSession.internal_callbackRootGrouperSession(true, new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        return GrouperDeprovisioningLogic.shouldAddSubject(grouperSession, hooksContext, 
            membership.getMember().getSubject(), 
            membership.getOwnerGroupId(), membership.getOwnerAttrDefId(), membership.getOwnerStemId(), ownerArray);
      }
    });
    
    if (!shouldAddSubject) {
      throw new HookVeto("veto.membershipVeto.deprovisioned",
          "Entity "+ SubjectHelper.getPretty(membership.getMember().getSubject()) +" has been deprovisioned and is not eligible to have a membership or privilege in " + ownerArray[0]);
    }
  }

}
