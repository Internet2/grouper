/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * built in if condition
 * @author mchyzer
 *
 */
public enum RuleThenEnum {

  /** */
  test {

    /**
     * 
     * @see edu.internet2.middleware.grouper.rules.RuleThenEnum#fireRule(edu.internet2.middleware.grouper.rules.RuleDefinition, edu.internet2.middleware.grouper.rules.RuleEngine, edu.internet2.middleware.grouper.rules.beans.RulesBean)
     */
    @Override
    public void fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine,
        RulesBean rulesBean) {
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
   */
  public abstract void fireRule(RuleDefinition ruleDefinition, 
      RuleEngine ruleEngine, RulesBean rulesBean);
  
}
