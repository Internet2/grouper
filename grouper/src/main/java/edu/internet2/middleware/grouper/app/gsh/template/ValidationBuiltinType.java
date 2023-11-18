package edu.internet2.middleware.grouper.app.gsh.template;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
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

    @Override
    public String getErrorMessage(String valueFromUser) {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.alpha.message");
      errorMessage = errorMessage.replace("$$valueFromUser$$", valueFromUser);
      return errorMessage;
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

    @Override
    public String getErrorMessage(String valueFromUser) {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.alphaNumeric.message");
      errorMessage = errorMessage.replace("$$valueFromUser$$", valueFromUser);
      return errorMessage;
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

    @Override
    public String getErrorMessage(String valueFromUser) {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.alphaNumericUnderscore.message");
      errorMessage = errorMessage.replace("$$valueFromUser$$", valueFromUser);
      return errorMessage;
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

    @Override
    public String getErrorMessage(String valueFromUser) {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.alphaNumericDash.message");
      errorMessage = errorMessage.replace("$$valueFromUser$$", valueFromUser);
      return errorMessage;
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

    @Override
    public String getErrorMessage(String valueFromUser) {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.alphaNumericUnderscoreDash.message");
      errorMessage = errorMessage.replace("$$valueFromUser$$", valueFromUser);
      return errorMessage;
    }
  },
  noColons {

    @Override
    public boolean doesValuePassValidation(String valueFromUser) {
      if (StringUtils.isNotBlank(valueFromUser)) {
        return !valueFromUser.contains(":");
      }
      
      return true;
    }

    @Override
    public String getErrorMessage(String valueFromUser) {
      String errorMessage = GrouperTextContainer.textOrNull("gshTemplate.error.noColons.message");
      errorMessage = errorMessage.replace("$$valueFromUser$$", valueFromUser);
      return errorMessage;
    }
    
    
  };
  
  
  public abstract boolean doesValuePassValidation(String valueFromUser);
  
  public abstract String getErrorMessage(String valueFromUser);
  
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
