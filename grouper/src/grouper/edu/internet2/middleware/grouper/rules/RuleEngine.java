package edu.internet2.middleware.grouper.rules;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValueContainer;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * processes rules and kicks off actions
 * @author mchyzer
 *
 */
public class RuleEngine {

  /** cached rule definitions */
  private static GrouperCache<Boolean, Set<RuleDefinition>> ruleDefinitionsCache = 
    new GrouperCache<Boolean, Set<RuleDefinition>>(RuleEngine.class.getName() + ".ruleEngine", 100, false, 5*60, 5*60, false);
  
  /**
   * 
   * @return all the rule definitions, cached
   */
  private static Set<RuleDefinition> allRuleDefinitions() {
    Set<RuleDefinition> ruleDefinitions = ruleDefinitionsCache.get(Boolean.TRUE);
    
    if (ruleDefinitions == null) {
    
      synchronized (RuleEngine.class) {
        ruleDefinitions = ruleDefinitionsCache.get(Boolean.TRUE);
        if (ruleDefinitions == null) {
          Map<AttributeAssign, Set<AttributeAssignValueContainer>> attributeAssignValueContainers = allRulesAttributeAssignValueContainers();
          
          Set<RuleDefinition> newDefinitions = new HashSet<RuleDefinition>();
          
          for (Set<AttributeAssignValueContainer> attributeAssignValueContainersSet : GrouperUtil.nonNull(attributeAssignValueContainers).values()) {
            RuleDefinition ruleDefinition = ruleDefinition(attributeAssignValueContainersSet);
            newDefinitions.add(ruleDefinition);
          }
          
          ruleDefinitions = newDefinitions;
          ruleDefinitionsCache.put(Boolean.TRUE, ruleDefinitions);
        }
      }
      
    }
    
    return ruleDefinitions;
    
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
    
    String actAsSubjectId = AttributeAssignValueContainer.attributeValueString(attributeAssignValueContainers, RuleUtils.ruleActAsSubjectIdName());
    String actAsSubjectIdentifier = AttributeAssignValueContainer.attributeValueString(attributeAssignValueContainers, RuleUtils.ruleActAsSubjectIdentifierName());
    String actAsSubjectSourceId = AttributeAssignValueContainer.attributeValueString(attributeAssignValueContainers, RuleUtils.ruleActAsSubjectSourceIdName());
    String checkTypeString = AttributeAssignValueContainer.attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckTypeName());
    String checkOwnerId = AttributeAssignValueContainer.attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckOwnerIdName());
    String checkOwnerName = AttributeAssignValueContainer.attributeValueString(attributeAssignValueContainers, RuleUtils.ruleCheckOwnerNameName());
    String ifConditionEl = AttributeAssignValueContainer.attributeValueString(attributeAssignValueContainers, RuleUtils.ruleIfConditionElName());
    String ifConditionEnum = AttributeAssignValueContainer.attributeValueString(attributeAssignValueContainers, RuleUtils.ruleIfConditionEnumName());
    String thenEl = AttributeAssignValueContainer.attributeValueString(attributeAssignValueContainers, RuleUtils.ruleThenElName());
    
    RuleDefinition ruleDefinition = new RuleDefinition();
    
    //lets do the subject first
    RuleSubjectActAs ruleSubjectActAs = new RuleSubjectActAs(actAsSubjectId, actAsSubjectSourceId, actAsSubjectIdentifier);
    
    ruleDefinition.setActAs(ruleSubjectActAs);
    
    RuleCheck ruleCheck = new RuleCheck(checkTypeString, checkOwnerId, checkOwnerName);
    
    ruleDefinition.setCheck(ruleCheck);
    
    RuleIfCondition ruleIfCondition = new RuleIfCondition(ifConditionEl, ifConditionEnum);
    
//    ruleDefinition.set
    
    return null;
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
