package edu.internet2.middleware.tierInstrumentationCollector.db;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * type of attribute
 * @author mchyzer
 */
public enum TierInstrumentationCollectorAttributeType {

  /** the string */
  string_type, 

  /** integer */
  integer_type, 

  /** floating */
  floating_type, 

  /** the boolean */
  boolean_type, 

  /** the timestamp */
  timestamp_type;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static TierInstrumentationCollectorAttributeType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperClientUtils.enumValueOfIgnoreCase(TierInstrumentationCollectorAttributeType.class, 
        string, exceptionOnNull);

  }
  
  
}
