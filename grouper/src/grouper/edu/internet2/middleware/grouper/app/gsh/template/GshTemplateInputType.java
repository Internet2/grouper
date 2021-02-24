package edu.internet2.middleware.grouper.app.gsh.template;

import org.apache.commons.text.StringEscapeUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;

public enum GshTemplateInputType {
  
  INTEGER {

    @Override
    public String generateGshVariable(GshTemplateInputConfig gshTemplateInputConfig, String valueFromUser) {
      
     String valueToUse = GrouperUtil.defaultIfEmpty(valueFromUser, gshTemplateInputConfig.getDefaultValue());

     Integer valueToUseInteger = GrouperUtil.intObjectValue(valueToUse, true);
     
     return "Integer "+gshTemplateInputConfig.getName() + " = " + valueToUseInteger + ";\n";
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
  
  STRING {
    
    @Override
    public String generateGshVariable(GshTemplateInputConfig gshTemplateInputConfig, String valueFromUser) {
      
      String valueToUse = GrouperUtil.defaultIfEmpty(valueFromUser, gshTemplateInputConfig.getDefaultValue());
      
      return "String "+gshTemplateInputConfig.getName() + " = " 
        + (valueFromUser == null ? "null" : ("\"" + StringEscapeUtils.escapeJava(valueToUse) + "\"")) + ";\n";
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      return true;
    }
    
  }, 
  
  BOOLEAN {
    
    @Override
    public String generateGshVariable(GshTemplateInputConfig gshTemplateInputConfig, String valueFromUser) {
      
     String valueToUse = GrouperUtil.defaultIfEmpty(valueFromUser, gshTemplateInputConfig.getDefaultValue());

     Boolean valueToUseBoolean = GrouperUtil.booleanObjectValue(valueToUse);
     
     String value = valueToUseBoolean == null ? "null" : (valueToUseBoolean ? "true" : "false");
     
     return "Boolean "+gshTemplateInputConfig.getName() + " = " + value + ";\n";
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
    
  };
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   */
  public static GshTemplateInputType valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GshTemplateInputType.class, string, exceptionOnNotFound);
  }
  
  
  public abstract String generateGshVariable(GshTemplateInputConfig gshTemplateInputConfig, String valueFromUser);

  public abstract boolean canConvertToCorrectType(String valueFromUser);

}

