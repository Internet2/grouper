/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.rules;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class RuleFinder {

  /**
   * 
   */
  public RuleFinder() {
  }

  /**
   * find group inherit rules by stem name
   * @param stem
   * @return the rules
   */
  public static Set<RuleDefinition> findGroupPrivilegeInheritRules(Stem stem) {
    
    RuleEngine ruleEngine = RuleEngine.ruleEngine();
    
    //handle root stem... hmmm
    //we need to simulate a child object here
    String groupName = stem.isRootStem() ? "qwertyuiopasdfghjkl:b" : (stem.getName() + ":b");
    
    RuleCheck ruleCheck = new RuleCheck(RuleCheckType.groupCreate.name(), null, groupName, null, null, null);
    Set<RuleDefinition> ruleDefinitions = new HashSet<RuleDefinition>(GrouperUtil.nonNull(ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck)));
    
    Iterator<RuleDefinition> iterator = ruleDefinitions.iterator();
    
    while (iterator.hasNext()) {
      
      RuleDefinition ruleDefinition = iterator.next();
      RuleThen ruleThen = ruleDefinition.getThen();
      RuleThenEnum ruleThenEnum = ruleThen.thenEnum();
      if (ruleThenEnum != RuleThenEnum.assignGroupPrivilegeToGroupId) {
        iterator.remove();
      }
    }
    
    return ruleDefinitions;

  }
  
  /**
   * find folder inherit rules by stem name
   * @param stem
   * @return the rules
   */
  public static Set<RuleDefinition> findFolderPrivilegeInheritRules(Stem stem) {
    
    RuleEngine ruleEngine = RuleEngine.ruleEngine();

    //handle root stem... hmmm
    //we need to simulate a child object here
    String stemName = stem.isRootStem() ? "qwertyuiopasdfghjkl:b" : (stem.getName() + ":b");

    RuleCheck ruleCheck = new RuleCheck(RuleCheckType.stemCreate.name(), null, stemName, null, null, null);
    Set<RuleDefinition> ruleDefinitions = ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck);

    Iterator<RuleDefinition> iterator = ruleDefinitions.iterator();
    
    while (iterator.hasNext()) {
      
      RuleDefinition ruleDefinition = iterator.next();
      RuleThen ruleThen = ruleDefinition.getThen();
      RuleThenEnum ruleThenEnum = ruleThen.thenEnum();
      if (ruleThenEnum != RuleThenEnum.assignStemPrivilegeToStemId) {
        iterator.remove();
      }
    }
    
    return ruleDefinitions;

  }
  
  
  /**
   * find attribute def inherit rules by stem name
   * @param stem
   * @return the rules
   */
  public static Set<RuleDefinition> findAttributeDefPrivilegeInheritRules(Stem stem) {
    
    RuleEngine ruleEngine = RuleEngine.ruleEngine();

    //handle root stem... hmmm
    //we need to simulate a child object here
    String attributeDefName = stem.isRootStem() ? "qwertyuiopasdfghjkl:b" : (stem.getName() + ":b");

    RuleCheck ruleCheck = new RuleCheck(RuleCheckType.attributeDefCreate.name(), null, attributeDefName, null, null, null);
    Set<RuleDefinition> ruleDefinitions = ruleEngine.ruleCheckIndexDefinitionsByNameOrIdInFolder(ruleCheck);
    
    Iterator<RuleDefinition> iterator = ruleDefinitions.iterator();
    
    while (iterator.hasNext()) {
      
      RuleDefinition ruleDefinition = iterator.next();
      RuleThen ruleThen = ruleDefinition.getThen();
      RuleThenEnum ruleThenEnum = ruleThen.thenEnum();
      if (ruleThenEnum != RuleThenEnum.assignAttributeDefPrivilegeToAttributeDefId) {
        iterator.remove();
      }
    }
    
    return ruleDefinitions;

  }
  
  
  
}
