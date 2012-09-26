package edu.internet2.middleware.grouper.tableIndex;

import edu.internet2.middleware.grouper.stem.StemHierarchyType;
import edu.internet2.middleware.grouper.util.GrouperUtil;


public enum TableIndexType {
  
  /** index assigned to a group */
  group,
  
  /** index assigned to a stem */
  stem,
  
  /** index assigned to an attribute def */
  attributeDef,
  
  /** index assigned to an attribute name */
  attributeName;

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static TableIndexType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(TableIndexType.class, 
        string, exceptionOnNull);

  }

}
