package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public enum GrouperProvisioningObjectMetadataItemValueType {

  STRING {

    @Override
    public Object convert(Object value) {
      if (value == null || value instanceof String) {
        return value;
      }
      return GrouperUtil.stringValue(value);
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      return true;
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
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      
      if (StringUtils.isNotBlank(valueFromUser)) {
        try {
          GrouperUtil.intValue(valueFromUser);
        } catch(Throwable e) {
          return false;
        }
      }
      
      return true;
    }
  },
  
  FLOAT {

    @Override
    public Object convert(Object value) {
      return GrouperUtil.floatObjectValue(value, true);      
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      
      if (StringUtils.isNotBlank(valueFromUser)) {
        try {
          GrouperUtil.floatValue(valueFromUser);
        } catch(Throwable e) {
          return false;
        }
      }
      
      return true;
    }
   
    
  },
  
  BOOLEAN {

    @Override
    public Object convert(Object value) {
      return GrouperUtil.booleanObjectValue(value);
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      
      if (StringUtils.isNotBlank(valueFromUser)) {
        try {
          GrouperUtil.booleanValue(valueFromUser);
        } catch(Throwable e) {
          return false;
        }
      }
      return true;
    }
    
  },
  
  TIMESTAMP {

    @Override
    public Object convert(Object value) {
      return GrouperUtil.toTimestamp(value);
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      
      if (StringUtils.isNotBlank(valueFromUser)) {
        try {
          GrouperUtil.stringToTimestamp(valueFromUser);
        } catch(Throwable e) {
          return false;
        }
      }
      
      return true;
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
  
  public abstract boolean canConvertToCorrectType(String valueFromUser);

}

