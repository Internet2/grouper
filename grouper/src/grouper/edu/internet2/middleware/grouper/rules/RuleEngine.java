package edu.internet2.middleware.grouper.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.RulesBase;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueContainer;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * processes rules and kicks off actions
 * @author mchyzer
 *
 */
public class RuleEngine {

  /** cached rule definitions */
  private static GrouperCache<Boolean, RuleEngine> ruleEngineCache = 
    new GrouperCache<Boolean, RuleEngine>(RuleEngine.class.getName() + ".ruleEngine", 100, false, 5*60, 5*60, false);
  
  /** rule definitions */
  private Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
  
  /**
   * rule definitions
   * @return the ruleDefinitions
   */
  public Set<RuleDefinition> getRuleDefinitions() {
    return this.ruleDefinitions;
  }

  /**
   * get rule definitions from cache based on name or id
   * @param ruleCheck
   * @return the definitions
   */
  public Set<RuleDefinition> ruleCheckIndexDefinitionsByNameOrId(RuleCheck ruleCheck) {
   
    String ownerId = ruleCheck.getCheckOwnerId();
    String ownerName = ruleCheck.getCheckOwnerName();
    
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>();
    
    if (!StringUtils.isBlank(ownerId)) {
      ruleCheck.setCheckOwnerName(null);
      ruleDefinitions.addAll(GrouperUtil.nonNull(this.getRuleCheckIndex().get(ruleCheck)));
      ruleCheck.setCheckOwnerName(ownerName);
    }
    
    if (!StringUtils.isBlank(ownerName)) {
      ruleCheck.setCheckOwnerId(null);
      ruleDefinitions.addAll(GrouperUtil.nonNull(this.getRuleCheckIndex().get(ruleCheck)));
      ruleCheck.setCheckOwnerId(ownerId);
    }
    
    return ruleDefinitions;
  }
  
  /**
   * rule definitions
   * @param ruleDefinitions the ruleDefinitions to set
   */
  public void setRuleDefinitions(Set<RuleDefinition> ruleDefinitions) {
    this.ruleDefinitions = ruleDefinitions;
  }

  /**
   * 
   * @return all the rule definitions, cached
   */
  private static RuleEngine ruleEngine() {
    RuleEngine ruleEngine = ruleEngineCache.get(Boolean.TRUE);
    
    if (ruleEngine == null) {
    
      synchronized (RuleEngine.class) {
        ruleEngine = ruleEngineCache.get(Boolean.TRUE);
        if (ruleEngine == null) {
          Map<AttributeAssign, Set<AttributeAssignValueContainer>> attributeAssignValueContainers 
            = allRulesAttributeAssignValueContainers();
          
          RuleEngine newEngine = new RuleEngine();
          Set<RuleDefinition> newDefinitions = newEngine.getRuleDefinitions();
          
          for (Set<AttributeAssignValueContainer> attributeAssignValueContainersSet : 
              GrouperUtil.nonNull(attributeAssignValueContainers).values()) {
            RuleDefinition ruleDefinition = ruleDefinition(attributeAssignValueContainersSet);
            
            String invalidReason = ruleDefinition.validate();
            if (StringUtils.isBlank(invalidReason)) {
              newDefinitions.add(ruleDefinition);
            } else {
              //throw out invalid rules
              LOG.error("Invalid rule definition: " 
                  + invalidReason + ", ruleDefinition: " + ruleDefinition);
            }
            
          }
          
          newEngine.indexData();
          
          ruleEngineCache.put(Boolean.TRUE, ruleEngine);
        }
      }
      
    }
    
    return ruleEngine;
    
  }

  /** map of checks to sets of relevant rules */
  private Map<RuleCheck, Set<RuleDefinition>> ruleCheckIndex = null;

  
  /**
   * map of checks to sets of relevant rules
   * @return the ruleCheckIndex
   */
  public Map<RuleCheck, Set<RuleDefinition>> getRuleCheckIndex() {
    return this.ruleCheckIndex;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleEngine.class);
  
  /**
   * index the rules so they can be searched quickly
   */
  private void indexData() {
    this.ruleCheckIndex = new HashMap<RuleCheck, Set<RuleDefinition>>();
    
    for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(this.ruleDefinitions)) {
      
      Set<RuleDefinition> checkRuleDefinitions = this.ruleCheckIndex.get(ruleDefinition.getCheck());
      
      //if this list isnt there, then make one
      if (checkRuleDefinitions == null) {
        checkRuleDefinitions = new HashSet<RuleDefinition>();
        this.ruleCheckIndex.put(ruleDefinition.getCheck(), checkRuleDefinitions);
      }
      
      checkRuleDefinitions.add(ruleDefinition);
      
    }
    
  }
  
  /**
   * find rules and fire them
   * @param ruleCheckType
   * @param rulesBean
   */
  public static void fireRule(RuleCheckType ruleCheckType, RulesBean rulesBean) {

    RuleEngine ruleEngine = ruleEngine();
    Set<RuleDefinition> ruleDefinitions = ruleCheckType.ruleDefinitions(ruleEngine, rulesBean);

    for (RuleDefinition ruleDefinition : GrouperUtil.nonNull(ruleDefinitions)) {
      
      if (ruleDefinition.getIfCondition().shouldFire(ruleDefinition, ruleEngine, rulesBean)) {
        
        //we are firing
        ruleDefinition.getThen().fireRule(ruleDefinition, ruleEngine, rulesBean);
        
      }
      
    }
    
  }
  
  /**
   * rule definitions from attribute assigns
   * @param attributeAssignValueContainers
   * @return the definition or null if it doesnt make sense
   */
  private static RuleDefinition ruleDefinition(
      Set<AttributeAssignValueContainer> attributeAssignValueContainers) {
    
    //RuleUtils.RULE_ACT_AS_SUBJECT_ID
    //RuleUtils.RULE_ACT_AS_SUBJECT_IDENTIFIER
    //RuleUtils.RULE_ACT_AS_SUBJECT_SOURCE_ID
    //RuleUtils.RULE_CHECK_TYPE
    //RuleUtils.RULE_CHECK_OWNER_ID
    //RuleUtils.RULE_CHECK_OWNER_NAME
    //RuleUtils.RULE_IF_CONDITION_EL
    //RuleUtils.RULE_IF_CONDITION_ENUM
    //RuleUtils.RULE_THEN_EL
    
    String actAsSubjectId = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleActAsSubjectIdName());
    String actAsSubjectIdentifier = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleActAsSubjectIdentifierName());
    String actAsSubjectSourceId = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleActAsSubjectSourceIdName());
    String checkTypeString = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckTypeName());
    String checkOwnerId = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckOwnerIdName());
    String checkOwnerName = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckOwnerNameName());
    String checkStemScope = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckStemScopeName());
    String ifConditionEl = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleIfConditionElName());
    String ifConditionEnum = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleIfConditionEnumName());
    String thenEl = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleThenElName());
    String thenEnum = AttributeAssignValueContainer
      .attributeValueString(attributeAssignValueContainers, RuleUtils.ruleThenEnumName());
    
    //lets do the subject first
    RuleSubjectActAs ruleSubjectActAs = new RuleSubjectActAs(
        actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier);
    
    RuleCheck ruleCheck = new RuleCheck(checkTypeString, checkOwnerId, 
        checkOwnerName, checkStemScope);
    
    RuleIfCondition ruleIfCondition = new RuleIfCondition(ifConditionEl, ifConditionEnum);
    
    RuleThen ruleThen = new RuleThen(thenEl, thenEnum);
    
    AttributeAssign attributeAssignType = attributeAssignValueContainers
      .iterator().next().getAttributeTypeAssign();
    RuleDefinition ruleDefinition = new RuleDefinition(attributeAssignType, 
        ruleSubjectActAs, ruleCheck, ruleIfCondition, ruleThen);
    
    return ruleDefinition;
  }
  
  /**
   * get all rules from the DB in the form of attribute assignments
   * @return the assigns of all rules
   */
  public static Map<AttributeAssign, Set<AttributeAssignValueContainer>> allRulesAttributeAssignValueContainers() {
    
    AttributeDef ruleTypeDef = RuleUtils.ruleTypeAttributeDef();
    
    Map<AttributeAssign, Set<AttributeAssignValueContainer>> result = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findByAttributeTypeDefNameId(ruleTypeDef.getId());
  
    return result;
    
  }

}
