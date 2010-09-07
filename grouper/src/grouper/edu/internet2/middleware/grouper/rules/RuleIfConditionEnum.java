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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * built in if condition
 * @author mchyzer
 *
 */
public enum RuleIfConditionEnum {

  /**
   * make sure this group and not the folder has membership
   */
  thisGroupAndNotFolderHasImmediateEnabledMembership {

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
        
        Group group = GroupFinder.findByUuid(rootSession, ruleDefinition.getAttributeAssignType().getOwnerGroupId(), false);
        
        if (group == null) {
          LOG.error("Group doesnt exist in rule! " + ruleDefinition);
          return false;
        }
        
        Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndFieldAndMemberIdsAndType(
              group.getId(), Group.getDefaultList(), 
              GrouperUtil.toSet(memberId), "immediate", true);
        
        //if not in this group, forget it
        if (GrouperUtil.length(memberships) == 0) {
          return false;
        }
        
        Stem stem = null;
        if (!StringUtils.isBlank(ruleDefinition.getCheck().getCheckOwnerId())) {
          stem = StemFinder.findByUuid(rootSession, ruleDefinition.getCheck().getCheckOwnerId(), false);
        } else if (!StringUtils.isBlank(ruleDefinition.getCheck().getCheckOwnerName())) {
          stem = StemFinder.findByName(rootSession, ruleDefinition.getCheck().getCheckOwnerName(), false);
        }
        
        memberships = GrouperDAOFactory.getFactory().getMembership()
          .findAllByStemParentOfGroupOwnerAndFieldAndType(stem.getName() + ":%", Group.getDefaultList(), "immediate", true);
      
        //if not in this group, forget it
        if (GrouperUtil.length(memberships) == 0) {
          return true;
        }
      
        return false;
        
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }
    }

    /**
     * 
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {
      
      //we need the owner stem and if sub or one
      if (StringUtils.isBlank(ruleDefinition.getCheck().getCheckOwnerId()) && StringUtils.isBlank(ruleDefinition.getCheck().getCheckOwnerName())) {
        return "This if condition " + this + " requires a check name or id";
      }
      
      if (StringUtils.isBlank(ruleDefinition.getCheck().getCheckStemScope())) {
        return "This if condition " + this + " requires a check stem scope";
      }
      
      return super.validate(ruleDefinition);
    }
    
  }, 
  
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
          return false;
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
    public String validate(RuleDefinition ruleDefinition) {
      RuleIfCondition ruleIfCondition = ruleDefinition.getIfCondition();
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

  },
  /** if permission def has assignment */
  thisPermissionDefHasAssignment {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleIfConditionEnum#shouldFire(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public boolean shouldFire(final RuleDefinition ruleDefinition, final RuleEngine ruleEngine,
        final RulesBean rulesBean) {
      
      Set<PermissionEntry> permissionEntries = RuleUtils.permissionsForUser(ruleDefinition
          .getAttributeAssignType().getOwnerAttributeDefId(), rulesBean);
      
      return GrouperUtil.length(permissionEntries) > 0;
      
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
   * @param ruleDefinition 
   * @return error message or null if ok
   */
  public String validate(RuleDefinition ruleDefinition) {
    RuleIfCondition ruleIfCondition = ruleDefinition.getIfCondition();
    if (!StringUtils.isBlank(ruleIfCondition.getIfOwnerId()) || !StringUtils.isBlank(ruleIfCondition.getIfOwnerName())) {
      return "This ifConditionEnum " + this.name() + " requires no ifOwnerId or ifOwnerName";
    }
    return null;
  }
}
