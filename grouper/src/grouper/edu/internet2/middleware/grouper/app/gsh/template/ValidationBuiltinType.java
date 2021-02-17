package edu.internet2.middleware.grouper.app.gsh.template;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum ValidationBuiltinType {

  
  alpha {

    @Override
    public boolean doesValuePassValidation(String valueFromUser) {
      if (StringUtils.isNotBlank(valueFromUser)) {
        return StringUtils.isAlpha(valueFromUser);  
      }
      
      return true;
    }
  },
  
  alphaNumeric {

    @Override
    public boolean doesValuePassValidation(String valueFromUser) {
      if (StringUtils.isNotBlank(valueFromUser)) {
        return StringUtils.isAlphanumeric(valueFromUser);  
      }
      
      return true;
    }
  },
  
  alphaNumericUnderscore {

    @Override
    public boolean doesValuePassValidation(String valueFromUser) {
      if (StringUtils.isNotBlank(valueFromUser)) {
        return valueFromUser.matches("^[a-zA-Z0-9_]+$"); 
      }
      
      return true;
    }
  },
  alphaNumericDash {

    @Override
    public boolean doesValuePassValidation(String valueFromUser) {
      
      if (StringUtils.isNotBlank(valueFromUser)) {
        return valueFromUser.matches("^[a-zA-Z0-9-]+$"); 
      }
      
      return true;
    }
  },
  alphaNumericUnderscoreDash {

    @Override
    public boolean doesValuePassValidation(String valueFromUser) {
      if (StringUtils.isNotBlank(valueFromUser)) {
        return valueFromUser.matches("^[a-zA-Z0-9-_]+$"); 
      }
      
      return true;
    }
  };
  
  
  public abstract boolean doesValuePassValidation(String valueFromUser);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   */
  public static ValidationBuiltinType valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(ValidationBuiltinType.class, string, exceptionOnNotFound);
  }
}
