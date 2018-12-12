/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public enum ConfigItemMetadataType {
  
  /** true/false */
  BOOLEAN, 
  
  /** any string */
  STRING, 

  /** group name in system */
  GROUP, 

  /** folder in system */
  STEM, 
  
  /** name of attribute def in system */
  ATTRIBUTEDEF, 

  /** name of attribute def name in system */
  ATTRIBUTEDEFNAME, 

  /** subject id or identifier in system */
  SUBJECT, 
  
  /** any integer or long datatype */
  INTEGER, 
  
  /** floating point number in system */
  FLOATING, 
  
  /** password, or encrypted, or file name, or script */
  PASSWORD, 
  
  /** fully qualified class in system */
  CLASS;
  
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static ConfigItemMetadataType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(ConfigItemMetadataType.class, 
        string, exceptionOnNull);

  }
}
