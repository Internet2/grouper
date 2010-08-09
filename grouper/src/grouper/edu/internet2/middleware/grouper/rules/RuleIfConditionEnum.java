/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * built in if condition
 * @author mchyzer
 *
 */
public enum RuleIfConditionEnum {

  /** */
  test;
  
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
