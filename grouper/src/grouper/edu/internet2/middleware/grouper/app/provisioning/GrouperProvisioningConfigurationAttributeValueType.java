package edu.internet2.middleware.grouper.app.provisioning;

import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GrouperProvisioningConfigurationAttributeValueType {

  STRING {

    @Override
    public Object convert(Object value) {
      if (value == null || value instanceof String) {
        return value;
      }
      return GrouperUtil.stringValue(value);
    }
    @Override
    public boolean correctTypeNonSet(Object value) {
      return value == null || value instanceof String;
    }
  },
  
  INT {

    @Override
    public Object convert(Object value) {
      if (value == null || value instanceof Integer) {
        return value;
      }
      return GrouperUtil.intValue(value);
    }
    @Override
    public boolean correctTypeNonSet(Object value) {
      return value == null || value instanceof Integer;
    }
  },
  
  LONG {

    @Override
    public Object convert(Object value) {
      if (value == null || value instanceof Long) {
        return value;
      }
      return GrouperUtil.longValue(value);
    }

    @Override
    public boolean correctTypeNonSet(Object value) {
      return value == null || value instanceof Long;
    }
  };
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static GrouperProvisioningConfigurationAttributeValueType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(GrouperProvisioningConfigurationAttributeValueType.class, string, exceptionOnNull);
  }

  /**
   * if the type is correct and assume not a set
   * @param value
   * @return
   */
  public abstract boolean correctTypeNonSet(Object value);

  /**
   * convert to type should be
   * @param value
   * @return
   */
  public abstract Object convert(Object value);

  /**
   * see if all members of set are correct value
   * @param set
   * @return
   */
  public boolean correctTypeForSet(Set set) {
    for (Object value : GrouperUtil.nonNull(set)) {
      if (!this.correctTypeNonSet(value)) {
        return false;
      }
    }
    return true;
  }

}
