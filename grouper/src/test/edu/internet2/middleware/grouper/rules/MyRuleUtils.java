/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class MyRuleUtils {

  /**
   * remove a member of a group
   * @param groupId
   * @param memberId
   * @return true if removed, false if not
   */
  public static boolean removeMemberFromGroupId(String groupId, String memberId) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing member: " + memberId + ", from group: " + groupId);
    }
    Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), groupId, true);
    Member member = MemberFinder.findByUuid(GrouperSession.startRootSession(), memberId, true);
    boolean result = group.deleteMember(member, false);
    if (LOG.isDebugEnabled()) {
      LOG.debug("Removing subject: " + member.getSubjectId() 
          + ", from group: " + group.getName() + ", result: " + result);
    }
    return result;
  }
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(MyRuleUtils.class);
  
  
}
