/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author mchyzer
 *
 */
public enum AttributeDefType {
  
  /** whole number type, can be used for date/timestamp or other things */
  INT, 
  
  /** text */
  STRING, 
  
  /** floating point number */
  DOUBLE, 
  
  /** no value type, the attribute itself is all that is needed */
  MARKER; 
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeDefType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeDefType.class, 
        string, exceptionOnNull);

  }
  
}
