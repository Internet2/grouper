package edu.internet2.middleware.grouper.app.provisioning;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperProvisioningObjectMetadataItemValueType {

  STRING {

    @Override
    public Object convert(Object value) {
      if (value == null || value instanceof String) {
        return value;
      }
      return GrouperUtil.stringValue(value);
    }
    
  }, 
  
  INTEGER {

    @Override
    public Object convert(Object value) {
      if (value == null || value instanceof Integer) {
        return value;
      }
      return GrouperUtil.intValue(value);
    }
  },
  
  FLOAT {

    @Override
    public Object convert(Object value) {
      return GrouperUtil.floatObjectValue(value, true);      
    }
   
    
  },
  
  BOOLEAN {

    @Override
    public Object convert(Object value) {
      return GrouperUtil.booleanObjectValue(value);
    }
    
  },
  
  TIMESTAMP {

    @Override
    public Object convert(Object value) {
      return GrouperUtil.toTimestamp(value);
    }
    
  };
  
  /**
   * convert to type should be
   * @param value
   * @return
   */
  public abstract Object convert(Object value);
  
  /**
   * 
   * @param type
   * @param exceptionOnNotFound
   * @return field type
   */
  public static GrouperProvisioningObjectMetadataItemValueType valueOfIgnoreCase(String type, boolean exceptionOnNotFound) {

    return GrouperUtil.enumValueOfIgnoreCase(GrouperProvisioningObjectMetadataItemValueType.class, type, exceptionOnNotFound);
    
  }

}

