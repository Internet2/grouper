package edu.internet2.middleware.grouper.app.gsh.template;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GshTemplateInputType {
  
  INTEGER {

    @Override
    public String generateGshVariable(GshTemplateRuntime gshTemplateRuntime, GshTemplateInputConfig gshTemplateInputConfig, String valueFromUser) {
      
     String valueToUse = GrouperUtil.defaultIfEmpty(valueFromUser, gshTemplateInputConfig.getDefaultValue());

     Integer valueToUseInteger = GrouperUtil.intObjectValue(valueToUse, true);
     
     gshTemplateRuntime.assignInputValueInteger(gshTemplateInputConfig.getName(), valueToUseInteger);

     return "Integer "+gshTemplateInputConfig.getName() + " = gsh_builtin_gshTemplateRuntime.retrieveInputValueInteger(\"" + gshTemplateInputConfig.getName() + "\");\n";
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
    
    @Override
    public Integer converToType(String valueFromUser) {
      return GrouperUtil.intObjectValue(valueFromUser, true);
    }
    
  },
  
  STRING {
    
    @Override
    public String generateGshVariable(GshTemplateRuntime gshTemplateRuntime, GshTemplateInputConfig gshTemplateInputConfig, String valueFromUser) {
      
      String valueToUse = GrouperUtil.defaultIfEmpty(valueFromUser, gshTemplateInputConfig.getDefaultValue());

      gshTemplateRuntime.assignInputValueString(gshTemplateInputConfig.getName(), valueToUse);

      return "String "+gshTemplateInputConfig.getName() + " = gsh_builtin_gshTemplateRuntime.retrieveInputValueString(\"" + gshTemplateInputConfig.getName() + "\");\n";
    }
    
    @Override
    public boolean canConvertToCorrectType(String valueFromUser) {
      return true;
    }
    
    @Override
    public String converToType(String valueFromUser) {
      return valueFromUser;
    }
    
  }, 
  
  BOOLEAN {
    
    @Override
    public String generateGshVariable(GshTemplateRuntime gshTemplateRuntime, GshTemplateInputConfig gshTemplateInputConfig, String valueFromUser) {
      
     String valueToUse = GrouperUtil.defaultIfEmpty(valueFromUser, gshTemplateInputConfig.getDefaultValue());

     Boolean valueToUseBoolean = GrouperUtil.booleanObjectValue(valueToUse);

     gshTemplateRuntime.assignInputValueBoolean(gshTemplateInputConfig.getName(), valueToUseBoolean);
     return "Boolean "+gshTemplateInputConfig.getName() + " = gsh_builtin_gshTemplateRuntime.retrieveInputValueBoolean(\"" + gshTemplateInputConfig.getName() + "\");\n";
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
    
    @Override
    public Boolean converToType(String valueFromUser) {
      return GrouperUtil.booleanObjectValue(valueFromUser);
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
  
  
  public abstract String generateGshVariable(GshTemplateRuntime gshTemplateRuntime, GshTemplateInputConfig gshTemplateInputConfig, String valueFromUser);

  public abstract boolean canConvertToCorrectType(String valueFromUser);
  
  public abstract Object converToType(String valueFromUser);

}

