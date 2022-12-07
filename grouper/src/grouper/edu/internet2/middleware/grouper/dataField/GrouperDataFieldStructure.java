package edu.internet2.middleware.grouper.dataField;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperDataFieldStructure {

  attribute, rowColumn;
  
  /**
   * 
   * @param type
   * @param exceptionOnNull
   * @return field type
   */
  public static GrouperDataFieldStructure valueOfIgnoreCase(String type, boolean exceptionOnNull) {

    if (StringUtils.equals("row", type)) {
      return rowColumn;
    }
    
    GrouperDataFieldStructure fieldType = GrouperUtil.enumValueOfIgnoreCase(GrouperDataFieldStructure.class, 
        type, exceptionOnNull);
    
    return fieldType;
  }
}
