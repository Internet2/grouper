/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author mchyzer
 *
 */
public enum AttributeDefValueType {
  
  /** whole number type, can be used for date/timestamp or other things */
  integer,
  
  /** text */
  string,
  
  /** floating point number */
  floating,
  
  /** no value type, the attribute itself is all that is needed */
  marker,
  
  /** this is a reference to a subject in the grouper_members table */
  memberId;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeDefValueType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(AttributeDefValueType.class, 
        string, exceptionOnNull);

  }
  
}
