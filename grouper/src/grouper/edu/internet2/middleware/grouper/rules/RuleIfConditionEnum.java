/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
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

  /**
   * make sure a group has no immedaite enabled membership
   */
  groupHasNoImmediateEnabledMembership {

    /**
     * 
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
        
        Group group = null;
        if (!StringUtils.isBlank(ruleDefinition.getIfCondition().getIfOwnerId())) {
          group = GroupFinder.findByUuid(rootSession, ruleDefinition.getIfCondition().getIfOwnerId(), false);
        } else if (!StringUtils.isBlank(ruleDefinition.getIfCondition().getIfOwnerName())) {
          group = GroupFinder.findByName(rootSession, ruleDefinition.getIfCondition().getIfOwnerName(), false);
        }
        
        if (group == null) {
          LOG.error("Group doesnt exist in rule! " + ruleDefinition);
        }
        
        Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndFieldAndMemberIdsAndType(
              group.getId(), Group.getDefaultList(), 
              GrouperUtil.toSet(memberId), "immediate", true);
        
        return GrouperUtil.length(memberships) == 0;
        
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
    }

    /**
     * 
     */
    @Override
    public String validate(RuleIfCondition ruleIfCondition) {
      if (StringUtils.isBlank(ruleIfCondition.getIfOwnerId()) && StringUtils.isBlank(ruleIfCondition.getIfOwnerName())) {
        return "This ifConditionEnum " + this.name() + " requires an ifOwnerId or ifOwnerName";
      }
      String result = ruleIfCondition.validateOwnerGroup();
      if (!StringUtils.isBlank(result)) {
        return result;
      }
      return null;
    }
    
  }, 
  
  
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

    /**
     * 
     */
    @Override
    public String validate(RuleIfCondition ruleIfCondition) {
      //this is ok
      return null;
    }
  };
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleIfConditionEnum.class);

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
  
  /**
   * validate the enum
   * @param ruleIfCondition 
   * @return error message or null if ok
   */
  public abstract String validate(RuleIfCondition ruleIfCondition);
}
