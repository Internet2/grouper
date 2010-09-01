/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * built in if condition
 * @author mchyzer
 *
 */
public enum RuleThenEnum {

  /** remove the member (the current one being acted on) from the owner group */
  removeMemberFromOwnerGroup {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Object fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean, StringBuilder logDataForThisDefinition) {
      
      Group group = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), 
          ruleDefinition.getAttributeAssignType().getOwnerGroupId(), true);
      Member member = MemberFinder.findByUuid(GrouperSession.staticGrouperSession(), rulesBean.getMemberId(), true);
      return group.deleteMember(member, false);
    }
    
  };
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static RuleThenEnum valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(RuleThenEnum.class, 
        string, exceptionOnNull);

  }

  /**
   * fire this rule
   * @param ruleDefinition
   * @param ruleEngine
   * @param rulesBean
   * @param logDataForThisDefinition is null if not logging, and non null if things should be appended
   * @return something for log
   */
  public abstract Object fireRule(RuleDefinition ruleDefinition, 
      RuleEngine ruleEngine, RulesBean rulesBean, StringBuilder logDataForThisDefinition);
  
}
