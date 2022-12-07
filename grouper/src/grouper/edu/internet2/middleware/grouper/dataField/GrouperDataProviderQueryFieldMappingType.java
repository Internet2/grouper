package edu.internet2.middleware.grouper.dataField;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperDataProviderQueryFieldMappingType {

  attribute;
  
  /**
   * 
   * @param type
   * @param exceptionOnNull
   * @return field type
   */
  public static GrouperDataProviderQueryFieldMappingType valueOfIgnoreCase(String type, boolean exceptionOnNull) {

    GrouperDataProviderQueryFieldMappingType fieldType = GrouperUtil.enumValueOfIgnoreCase(GrouperDataProviderQueryFieldMappingType.class, 
        type, exceptionOnNull);
    
    return fieldType;
  }

}
