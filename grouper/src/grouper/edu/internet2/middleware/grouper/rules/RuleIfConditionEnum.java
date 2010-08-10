/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * built in if condition
 * @author mchyzer
 *
 */
public enum RuleIfConditionEnum {

  /** if on group which has membership */
  thisGroupHasImmediateEnabledMembership {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleIfConditionEnum#shouldFire(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public boolean shouldFire(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean) {
      
      String memberId = null;
      try {
        memberId = rulesBean.getMemberId();
      } catch (Exception e) {
        //ignore
      }
      
      GrouperSession rootSession = GrouperSession.startRootSession(false);
      try {
        
        if (StringUtils.isBlank(memberId)) {
          
          Member member = MemberFinder.findBySubject(rootSession, rulesBean.getSubject(), false);
          memberId = member == null ? null : member.getUuid();

          if (StringUtils.isBlank(memberId )) {
            return false;
          }
        }
        
        String groupId = ruleDefinition.getAttributeAssignType().getOwnerGroupId();
        
        Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndFieldAndMemberIdsAndType(
              groupId, Group.getDefaultList(), 
              GrouperUtil.toSet(memberId), "immediate", true);
        
        return GrouperUtil.length(memberships) > 0;
        
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
    }
  };
  
  /**
   * should fire
   * @param ruleDefinition
   * @param ruleEngine
   * @param rulesBean
   * @return if should fire
   */
  public abstract boolean shouldFire(RuleDefinition ruleDefinition, 
      RuleEngine ruleEngine, RulesBean rulesBean);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static RuleIfConditionEnum valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(RuleIfConditionEnum.class, 
        string, exceptionOnNull);

  }
  

}
