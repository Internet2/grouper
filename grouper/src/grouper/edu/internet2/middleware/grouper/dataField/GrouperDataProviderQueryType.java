package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperDataProviderQueryType {

  sql,
  
  ldap;  
  
  /**
   * 
   * @param type
   * @param exceptionOnNull
   * @return field type
   */
  public static GrouperDataProviderQueryType valueOfIgnoreCase(String type, boolean exceptionOnNull) {

    
    GrouperDataProviderQueryType fieldType = GrouperUtil.enumValueOfIgnoreCase(GrouperDataProviderQueryType.class, 
        type, exceptionOnNull);
    
    return fieldType;
  }
}
