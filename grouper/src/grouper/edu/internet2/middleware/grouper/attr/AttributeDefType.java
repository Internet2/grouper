/**
 * 
 */
package edu.internet2.middleware.grouper.attr;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author mchyzer
 *
 */
public enum AttributeDefType {
    
  /** if this is an attribute */
  attr, 
  
  /** group up things into one application */
  service, 
  
  /** if this is a type */
  type, 
  
  /** if this is a limit of an attribute */
  limit, 
  
  /** if this is a permission */
  perm;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static AttributeDefType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    //v2.2+ convert domain to service
    if (StringUtils.equalsIgnoreCase("domain", string)) {
      string = service.name();
    }
    return GrouperUtil.enumValueOfIgnoreCase(AttributeDefType.class, 
        string, exceptionOnNull);

  }
  
  /**
   * return the set of attributeDefTypes, never null
   * @param attributeDefTypeStrings
   * @return the set of attribute def types
   */
  public static Set<AttributeDefType> toSet(String[] attributeDefTypeStrings) {
    Set<AttributeDefType> result = new HashSet<AttributeDefType>();
    if (GrouperUtil.length(attributeDefTypeStrings) == 0) {
      return result;
    }
    for (String attributeDefTypeString : attributeDefTypeStrings) {
      AttributeDefType attributeDefType = valueOfIgnoreCase(attributeDefTypeString, false);
      if (attributeDefType != null) {
        result.add(attributeDefType);
      }
    }
    return result;
  }
  
}
