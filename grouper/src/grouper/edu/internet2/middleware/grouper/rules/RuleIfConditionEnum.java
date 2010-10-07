/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
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
   * make sure no group in folder has an enabled membership
   */
  noGroupInFolderHasImmediateEnabledMembership {

    /**
     * 
     */
    @Override
    public boolean shouldFire(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean) {
      
      Stem.Scope stemScope = Stem.Scope.valueOfIgnoreCase(ruleDefinition.getIfCondition().getIfStemScope(), true);
      
      boolean folderHasMembership = RuleUtils.folderHasMembership(rulesBean, ruleDefinition.getIfCondition().getIfOwnerId(), 
          ruleDefinition.getIfCondition().getIfOwnerName(), 
          stemScope, null);
      
      if (folderHasMembership) {
        return false;
      }
      
      return true;
    }

    /**
     * 
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {
      
      String error = RuleUtils.validateStem(ruleDefinition.getIfCondition().getIfOwnerId(), 
          ruleDefinition.getIfCondition().getIfOwnerName(), 
          null);

      if (!StringUtils.isBlank(error)) {
        return error;
      }
      
      if (StringUtils.isBlank(ruleDefinition.getIfCondition().getIfStemScope())) {
        return "Stem scope is required in if condition";
      }
      
      return null;
    }
    
  },
  /**
   * make sure the name of the object matches this sql like string (with percent signs and underscores), 
   * e.g. school:folder:whatever:%groupSuffix
   */
  nameMatchesSqlLikeString {

    /**
     * 
     */
    @Override
    public boolean shouldFire(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean) {

      String sqlLikeString = ruleDefinition.getIfCondition().getIfConditionEnumArg0();
      
      if (StringUtils.isBlank(sqlLikeString)) {
        throw new RuntimeException("The like string should be in the if arg0!");
      }

      String name = null;
      if (rulesBean.hasAttributeDefName()) {
        name = rulesBean.getAttributeDefName().getName();
      } else if (rulesBean.hasAttributeDef()) {
        name = rulesBean.getAttributeDef().getName();
      } else if (rulesBean.hasGroup()) {
        name = rulesBean.getGroup().getName();
      } else if (rulesBean.hasStem()) {
        name = rulesBean.getStem().getName();
      }
      
      boolean matches = GrouperUtil.matchSqlString(sqlLikeString, name);
      
      return matches;
      
    }

    /**
     * 
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {
            
      if (StringUtils.isBlank(ruleDefinition.getIfCondition().getIfConditionEnumArg0())) {
        return "ifArg0 is required and is the sql like string e.g. school:folder:%suffix";
      }
      
      return null;
    }
    
  },
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
      
      Group group = RuleUtils.group(ruleDefinition.getIfCondition().getIfOwnerId(), 
          ruleDefinition.getIfCondition().getIfOwnerName(), ruleDefinition.getAttributeAssignType().getOwnerGroupId(), false, true);
      String groupId = group.getId();
      
      
      boolean groupHasMembership = RuleUtils.groupHasImmediateEnabledMembership(rulesBean, 
          groupId);
      
      if (!groupHasMembership) {
        return false;
      }
      
      Stem.Scope stemScope = Stem.Scope.valueOfIgnoreCase(ruleDefinition.getCheck().getCheckStemScope(), true);
      
      boolean folderHasMembership = RuleUtils.folderHasMembership(rulesBean, ruleDefinition.getCheck().getCheckOwnerId(), 
          ruleDefinition.getCheck().getCheckOwnerName(), 
          stemScope, null);
      
      if (folderHasMembership) {
        return false;
      }
      
      return true;
    }

    /**
     * 
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {
      
      //we need the owner stem and if sub or one
      String error = RuleUtils.validateStem(ruleDefinition.getCheck().getCheckOwnerId(), 
          ruleDefinition.getCheck().getCheckOwnerName(), 
          null);

      if (!StringUtils.isBlank(error)) {
        return error;
      }
      
      if (StringUtils.isBlank(ruleDefinition.getCheck().getCheckStemScope())) {
        return "This if condition " + this + " requires a check stem scope";
      }
      
      return super.validate(ruleDefinition);
    }
    
  },
  /** 
   * make sure there is not a membership in folder, but does have an attributeDef
   */
  thisPermissionDefHasAssignmentAndNotFolder {
    /**
     * 
     */
    @Override
    public boolean shouldFire(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean) {
      
      Stem.Scope stemScope = Stem.Scope.valueOfIgnoreCase(ruleDefinition.getCheck().getCheckStemScope(), true);
      
      boolean folderHasMembership = RuleUtils.folderHasMembership(rulesBean, ruleDefinition.getCheck().getCheckOwnerId(), 
          ruleDefinition.getCheck().getCheckOwnerName(), 
          stemScope, null);
      
      if (folderHasMembership) {
        return false;
      }

      Set<PermissionEntry> permissionEntries = RuleUtils.permissionsForUser(ruleDefinition
          .getAttributeAssignType().getOwnerAttributeDefId(), rulesBean, false);
      
      if (GrouperUtil.length(permissionEntries) == 0) {
        return false;
      }
      
      return true;

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
        Group group = RuleUtils.group(ruleDefinition.getIfCondition().getIfOwnerId(), 
            ruleDefinition.getIfCondition().getIfOwnerName(), ruleDefinition.getAttributeAssignType().getOwnerGroupId(), false, false);
        if (group == null) {
          LOG.error("Group doesnt exist in rule! " + ruleDefinition);
          return false;
        }
        String groupId = group.getId();
        
        Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndFieldAndMemberIdsAndType(
              groupId, Group.getDefaultList(), 
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

      return RuleUtils.validateGroup(ruleDefinition.getIfCondition().getIfOwnerId(), 
          ruleDefinition.getIfCondition().getIfOwnerName(), 
          ruleDefinition.getAttributeAssignType().getOwnerGroupId());

    }
    
  }, 
  
  
  /** if on group which has membership */
  thisGroupHasImmediateEnabledMembership {

    /**
     * 
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {
      return RuleUtils.validateGroup(ruleDefinition.getIfCondition().getIfOwnerId(), 
          ruleDefinition.getIfCondition().getIfOwnerName(), 
          ruleDefinition.getAttributeAssignType().getOwnerGroupId());

    }

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
        
        Group group = RuleUtils.group(ruleDefinition.getIfCondition().getIfOwnerId(), 
            ruleDefinition.getIfCondition().getIfOwnerName(), ruleDefinition.getAttributeAssignType().getOwnerGroupId(), false, true);
        String groupId = group.getId();
        
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
  
  /** if on group which has membership with no end date */
  thisGroupHasImmediateEnabledNoEndDateMembership {

    /**
     * 
     */
    @Override
    public String validate(RuleDefinition ruleDefinition) {
      return RuleUtils.validateGroup(ruleDefinition.getIfCondition().getIfOwnerId(), 
          ruleDefinition.getIfCondition().getIfOwnerName(), 
          ruleDefinition.getAttributeAssignType().getOwnerGroupId());

    }

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
        
        Group group = RuleUtils.group(ruleDefinition.getIfCondition().getIfOwnerId(), 
            ruleDefinition.getIfCondition().getIfOwnerName(), ruleDefinition.getAttributeAssignType().getOwnerGroupId(), false, true);
        String groupId = group.getId();
        
        Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndFieldAndMemberIdsAndType(
              groupId, Group.getDefaultList(), 
              GrouperUtil.toSet(memberId), "immediate", true);

        
        for (Membership membership : memberships) {
          if (membership.getDisabledTime() == null) {
            return true;
          }
        }
        
        return false;
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
          .getAttributeAssignType().getOwnerAttributeDefId(), rulesBean, false);
      
      return GrouperUtil.length(permissionEntries) > 0;
      
    }
  },
  /** if permission def has assignment with no end date */
  thisPermissionDefHasNoEndDateAssignment {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleIfConditionEnum#shouldFire(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public boolean shouldFire(final RuleDefinition ruleDefinition, final RuleEngine ruleEngine,
        final RulesBean rulesBean) {
      
      Set<PermissionEntry> permissionEntries = RuleUtils.permissionsForUser(ruleDefinition
          .getAttributeAssignType().getOwnerAttributeDefId(), rulesBean, true);
      
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
    
    if (!StringUtils.isBlank(ruleDefinition.getIfCondition().getIfConditionEnumArg0())
        || !StringUtils.isBlank(ruleDefinition.getIfCondition().getIfConditionEnumArg1())) {
      return "This if condition enum does not take args: " + this.name() + ", " 
        + ruleDefinition.getIfCondition().getIfConditionEnumArg0()
        + ", " + ruleDefinition.getIfCondition().getIfConditionEnumArg1();
    }
    
    RuleIfCondition ruleIfCondition = ruleDefinition.getIfCondition();
    if (!StringUtils.isBlank(ruleIfCondition.getIfOwnerId()) || !StringUtils.isBlank(ruleIfCondition.getIfOwnerName())) {
      return "This ifConditionEnum " + this.name() + " requires no ifOwnerId or ifOwnerName";
    }
    return null;
  }
  
  /**
   * make sure there are no params
   * @param ruleDefinition
   * @return error message if there are params
   */
  public static String validateNoParams(RuleDefinition ruleDefinition) {
    //if (!StringUtils.isBlank(ruleDefinition.getIfCondition().get))
    //TODO
    return null;
  }
  

  
}
