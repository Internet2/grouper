package edu.internet2.middleware.grouper.rules;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.rules.beans.RulesMembershipBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * type of checking for rules
 * @author mchyzer
 *
 */
public enum RuleCheckType {

  /** if there is a membership remove flattened */
  flattenedMembershipRemove {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembershipRemove(this, ruleEngine, rulesBean);
    }

    /**
     * 
     */
    @Override
    public void addElVariables(Map<String, Object> variableMap, RulesBean rulesBean) {

      RulesMembershipBean rulesMembershipBean = (RulesMembershipBean)rulesBean;
      if (rulesMembershipBean != null) {
        Group group = rulesMembershipBean.getGroup();
        variableMap.put("groupId", group.getId());
        variableMap.put("groupName", group.getName());
      }
      if (!StringUtils.isBlank(rulesMembershipBean.getMemberId())) {
        variableMap.put("memberId", rulesMembershipBean.getMemberId());
      }
      Membership membership = rulesMembershipBean.getMembership();
      if (membership != null) {
        variableMap.put("membershipId", membership.getUuid());
      }
      Subject subject = rulesMembershipBean.getSubject();
      if (subject != null) {
        variableMap.put("subjectId", subject.getId());
        variableMap.put("sourceId", subject.getSourceId());
      }
    }

    
  },
  
  /** if there is a membership remove in transaction of remove */
  membershipRemove {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembershipRemove(this, ruleEngine, rulesBean);
      
    }

    /**
     * 
     */
    @Override
    public void addElVariables(Map<String, Object> variableMap, RulesBean rulesBean) {
      flattenedMembershipRemove.addElVariables(variableMap, rulesBean);
    }
  },
  
  /** if there is a membership remove in transaction of remove of a gropu in a stem */
  membershipRemoveInFolder {

    /**
     * validate this check type
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleCheck ruleCheck) {
      return validate(ruleCheck, true);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      
      return ruleDefinitionsMembershipRemoveInFolder(this, ruleEngine, rulesBean);
      
    }

    /**
     * 
     */
    @Override
    public void addElVariables(Map<String, Object> variableMap, RulesBean rulesBean) {
      flattenedMembershipRemove.addElVariables(variableMap, rulesBean);
    }
  },
  
  /** if a group is created */
  groupCreate {
    
    /**
     * validate this check type
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleCheck ruleCheck) {
      return validate(ruleCheck, true);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      throw new RuntimeException("Not implemented");
    }

    @Override
    public void addElVariables(Map<String, Object> variableMap, RulesBean rulesBean) {
      throw new RuntimeException("Not implemented");
    }
   
  },
  
  /** if a stem is created */
  stemCreate {
    /**
     * validate this check type
     * @param ruleCheck 
     * @return the error or null if valid
     */
    public String validate(RuleCheck ruleCheck) {
      return validate(ruleCheck, true);
    }

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleCheckType#ruleDefinitions(edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean) {
      throw new RuntimeException("Not implemented");
    }

    /**
     * 
     */
    @Override
    public void addElVariables(Map<String, Object> variableMap, RulesBean rulesBean) {
      throw new RuntimeException("Not implemented");
    }
   
  };
  
  /**
   * validate this check type
   * @param ruleCheck 
   * @return the error or null if valid
   */
  public String validate(RuleCheck ruleCheck) {
    return validate(ruleCheck, false);
  }

  /**
   * get the check object from the rules bean
   * @param ruleEngine
   * @param rulesBean
   * @return the rules
   */
  public abstract Set<RuleDefinition> ruleDefinitions(RuleEngine ruleEngine, RulesBean rulesBean);

  /**
   * add EL variables to the substitute map
   * @param variableMap
   * @param rulesBean 
   */
  public abstract void addElVariables(Map<String, Object> variableMap, RulesBean rulesBean);

  
  /**
   * validate this check type
   * @param ruleCheck 
   * @param requireStemScope true to require, false to require blank
   * @return the error or null if valid
   */
  public String validate(RuleCheck ruleCheck, boolean requireStemScope) {
    if (StringUtils.isBlank(ruleCheck.getCheckOwnerId()) == StringUtils.isBlank(ruleCheck.getCheckOwnerName())) {
      return "Enter one and only one of checkOwnerId and checkOwnerName!";
    }
    if (requireStemScope) {
      if (StringUtils.isBlank(ruleCheck.getCheckStemScope())) {
        return "Enter the checkStemScope of ALL or SUB for the folder based rule!";
      }
      try {
        Stem.Scope.valueOfIgnoreCase(ruleCheck.getCheckStemScope(), true);
      } catch (Exception e) {
        return e.getMessage();
      }
    } else {
      if (!StringUtils.isBlank(ruleCheck.getCheckStemScope())) {
        return "Cant put checkStemScope in this ruleCheckType, not allowed";
      }
    }
    return null;
  }
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static RuleCheckType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(RuleCheckType.class, 
        string, exceptionOnNull);

  }
  
  /**
   * for a membership remove, get the rules
   * @param ruleCheckType 
   * @param ruleEngine 
   * @param rulesBean 
   * @return rule definitions
   */
  private static Set<RuleDefinition> ruleDefinitionsMembershipRemove(RuleCheckType ruleCheckType, RuleEngine ruleEngine, RulesBean rulesBean) {
    
    RulesMembershipBean rulesMembershipBean = (RulesMembershipBean)rulesBean;
    
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    //by id
    RuleCheck ruleCheck = new RuleCheck(ruleCheckType.name(), 
        rulesMembershipBean.getGroup().getId(), rulesMembershipBean.getGroup().getName(), null);

    ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrId(ruleCheck)));
    
    return ruleDefinitions;
  }

  /**
   * for a membership remove, get the rules
   * @param ruleCheckType 
   * @param ruleEngine 
   * @param rulesBean 
   * @return rule definitions
   */
  private static Set<RuleDefinition> ruleDefinitionsMembershipRemoveInFolder(RuleCheckType ruleCheckType, RuleEngine ruleEngine, RulesBean rulesBean) {
    
    RulesMembershipBean rulesMembershipBean = (RulesMembershipBean)rulesBean;
    
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    //by id
    RuleCheck ruleCheck = new RuleCheck(ruleCheckType.name(), 
        rulesMembershipBean.getGroup().getId(), rulesMembershipBean.getGroup().getName(), null);

    ruleDefinitions.addAll(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck)));
    
    return ruleDefinitions;
  }


}
