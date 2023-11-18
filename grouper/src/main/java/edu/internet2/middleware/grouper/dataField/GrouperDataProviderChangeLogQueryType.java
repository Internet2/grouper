package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperDataProviderChangeLogQueryType {

  sql;
  
  
  /**
   * 
   * @param type
   * @param exceptionOnNull
   * @return field type
   */
  public static GrouperDataProviderChangeLogQueryType valueOfIgnoreCase(String type, boolean exceptionOnNull) {

    
    GrouperDataProviderChangeLogQueryType fieldType = GrouperUtil.enumValueOfIgnoreCase(GrouperDataProviderChangeLogQueryType.class, 
        type, exceptionOnNull);
    
    return fieldType;
  }
}
