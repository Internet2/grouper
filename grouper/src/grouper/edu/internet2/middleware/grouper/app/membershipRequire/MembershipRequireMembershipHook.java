package edu.internet2.middleware.grouper.app.membershipRequire;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer
 *
 */
public class MembershipRequireMembershipHook extends MembershipHooks {

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
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouperHook.MembershipRequireMembershipHook.autoRegister", true)) {
      //register this hook
      GrouperHooksUtils.addHookManual(GrouperHookType.MEMBERSHIP.getPropertyFileKey(), 
          MembershipRequireMembershipHook.class);
    }
    
    registered = true;

  }

  /**
   * 
   */
  public MembershipRequireMembershipHook() {
  }

  @Override
  public void membershipPreAddMember(HooksContext hooksContext, HooksMembershipChangeBean preAddMemberBean) {
    checkMembershipEligibility(preAddMemberBean.getMembership());
  }

  /**
   * 
   * @param membership
   */
  private static void checkMembershipEligibility(Membership membership) {

    if (!FieldType.LIST.equals(membership.getField().getType())) {
      return;
    }

    if (GrouperCheckConfig.inCheckConfig || !GrouperStartup.isFinishedStartupSuccessfully()) {
      return;
    }

    if (!membership.isEnabled()) {
      return;
    }

    if (!GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.membershipRequirement.hookEnable", true)) {
      return;
    }
    
    GrouperSession.internal_callbackRootGrouperSession(true, new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        String groupName = membership.getGroupName();
        
        // not sure why this would happen
        if (StringUtils.isBlank(groupName)) {
          return null;
        }

        Set<MembershipRequireConfigBean> membershipRequireConfigBeans = MembershipRequireEngine.groupNameToConfigBeanAssigned(groupName);
        
        if (GrouperUtil.length(membershipRequireConfigBeans) == 0) {
          return null;
        }

        for (MembershipRequireConfigBean membershipRequireConfigBean : membershipRequireConfigBeans) {
          if (!MembershipRequireEngine.validMember(groupName, membershipRequireConfigBean, membership.getMemberUuid())) {
            throw new HookVeto("veto.membershipVeto.customComposite." + membershipRequireConfigBean.getUiKey(),
                "User is not eligible to be in this group since they are not in: " + GrouperUtil.escapeHtml(membershipRequireConfigBean.getRequireGroupName(), true));
          }
        }
        return null;
      }
    });
  }
}
