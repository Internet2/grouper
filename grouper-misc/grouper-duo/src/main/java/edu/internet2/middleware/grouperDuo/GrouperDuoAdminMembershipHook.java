package edu.internet2.middleware.grouperDuo;

import edu.internet2.middleware.grouper.*;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.membership.MembershipType;

import java.util.HashMap;

/**
 * Limits a member from being added to two Duo Admin roles.
 *
 * Register hook with grouper.properties:
 * hooks.membership.class = edu.internet2.middleware.grouperDuo.GrouperDuoAdminMembershipHook
 */
public class GrouperDuoAdminMembershipHook extends MembershipHooks {

  @Override
  public void membershipPreAddMember(HooksContext hooksContext,
      HooksMembershipChangeBean preAddMemberBean) {
    HashMap<String, Object> debugMap = new HashMap<String, Object>();
    long startTime = System.nanoTime();

    debugMap.put("method", "membershipPreAddMember");

    GrouperSession grouperSession = hooksContext.grouperSession();
    boolean startedGrouperSession = false;

    try {
      Membership membership = preAddMemberBean.getMembership();
      Field field = membership.getField();

      // Only manage immediate memberships
      if (!MembershipType.IMMEDIATE.equals(membership.getTypeEnum())) {
        return;
      }

      // Only manage group memberships
      if (!FieldType.LIST.equals(field.getType())) {
        return;
      }

      Group group = preAddMemberBean.getGroup();

      if (grouperSession == null) {
        grouperSession = GrouperSession.startRootSession();
        startedGrouperSession = true;
      } else {
        grouperSession = grouperSession.internal_getRootSession();
      }

      if (GrouperDuoUtils.isValidDuoAdminGroup(grouperSession, group.getName())) {
        GrouperDuoLog
            .duoLog("User is added as a member of a valid admin group, checking for existing role.");
        Group existingAdminRole = GrouperDuoUtils.getExistingAdminRole(grouperSession,
            membership.getMember());
        GrouperDuoLog.duoLog(String.format("Existing admin role: %s",
            existingAdminRole == null ? "None." : existingAdminRole.getName()));

        if (existingAdminRole != null) {
          GrouperDuoLog.duoLog("membershipPreAddMember hook - Preventing "
              + membership.getMember().getName() + " membership to " + group.getName()
              + " because user is already a member of " + existingAdminRole.getName());
          throw new HookVeto(this.getClass().getName() + ".membershipPreAdd.membershipRejected",
              "Error: Users may only belong to a single Duo administrator group. "
                  + membership.getMember().getName() + " is already in the "
                  + existingAdminRole.getDisplayName() + " group.");
        }
      }
    } catch (HookVeto e) {
      throw e;
    } catch (Exception e) {
      GrouperDuoLog.logError("Error processing membershipPreAddMember hook.", e);
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
      if (startedGrouperSession)
        GrouperSession.stopQuietly(grouperSession);
    }
  }
}
