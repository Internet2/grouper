package edu.internet2.middleware.grouper.app.gsh.template;

import java.io.File;

import edu.internet2.middleware.grouper.app.gsh.GrouperGroovyInput;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GshTemplateInputType {
  
  INTEGER {

    @Override
    public String generateGshVariable(GrouperGroovyInput grouperGroovyInput, GshTemplateInputConfig gshTemplateInputConfig, Object valueFromUser) {
      
     String valueToUse = GrouperUtil.isEmpty(valueFromUser) ? gshTemplateInputConfig.getDefaultValue(): GrouperUtil.stringValue(valueFromUser);
     
     Integer valueToUseInteger = GrouperUtil.intObjectValue(valueToUse, true);
     
     grouperGroovyInput.assignInputValueInteger(gshTemplateInputConfig.getName(), valueToUseInteger);
     grouperGroovyInput.assignInputFormElement(gshTemplateInputConfig.getName(), gshTemplateInputConfig.getConfigItemFormElement());

     return "Integer "+gshTemplateInputConfig.getName() + " = grouperGroovyRuntime.retrieveInputValueInteger(\"" + gshTemplateInputConfig.getName() + "\");\n";
    }

    @Override
    public boolean canConvertToCorrectType(Object valueFromUser) {
      
      if (!GrouperUtil.isBlank(valueFromUser)) {
        try {
          GrouperUtil.intValue(valueFromUser);
        } catch(Throwable e) {
          return false;
        }
      }
      
      return true;
    }
    
    @Override
    public Integer convertToType(Object valueFromUser) {
      return GrouperUtil.intObjectValue(valueFromUser, true);
    }
    
  },
  
  STRING {
    
    @Override
    public String generateGshVariable(GrouperGroovyInput grouperGroovyInput, GshTemplateInputConfig gshTemplateInputConfig, Object valueFromUser) {
      
      String valueToUse = GrouperUtil.isEmpty(valueFromUser) ? gshTemplateInputConfig.getDefaultValue(): GrouperUtil.stringValue(valueFromUser);

      grouperGroovyInput.assignInputValueString(gshTemplateInputConfig.getName(), valueToUse);

      grouperGroovyInput.assignInputFormElement(gshTemplateInputConfig.getName(), gshTemplateInputConfig.getConfigItemFormElement());
      
      return "String "+gshTemplateInputConfig.getName() + " = grouperGroovyRuntime.retrieveInputValueString(\"" + gshTemplateInputConfig.getName() + "\");\n";
    }
    
    @Override
    public boolean canConvertToCorrectType(Object valueFromUser) {
      return true;
    }
    
    @Override
    public String convertToType(Object valueFromUser) {
       if (valueFromUser instanceof File) {
         return GrouperUtil.readFileIntoString((File) valueFromUser);
       }
      return GrouperUtil.stringValue(valueFromUser);
    }
    
  }, 
  
 FILE {
    
    @Override
    public String generateGshVariable(GrouperGroovyInput grouperGroovyInput, GshTemplateInputConfig gshTemplateInputConfig, Object valueFromUser) {
      throw new RuntimeException("file type is not supported in V1 GSH templates!");
    }
    
    @Override
    public boolean canConvertToCorrectType(Object valueFromUser) {
      if (valueFromUser == null || valueFromUser instanceof File) {
        return true;
      } 
      return false;
    }
    
    @Override
    public File convertToType(Object valueFromUser) {
      return valueFromUser != null ? ((File)valueFromUser): null;
    }
    
  }, 
  
  BOOLEAN {
    
    @Override
    public String generateGshVariable(GrouperGroovyInput grouperGroovyInput, GshTemplateInputConfig gshTemplateInputConfig, Object valueFromUser) {
     
      
     String valueToUse = GrouperUtil.isEmpty(valueFromUser) ? gshTemplateInputConfig.getDefaultValue(): GrouperUtil.stringValue(valueFromUser);

     Boolean valueToUseBoolean = GrouperUtil.booleanObjectValue(valueToUse);

     grouperGroovyInput.assignInputValueBoolean(gshTemplateInputConfig.getName(), valueToUseBoolean);
     grouperGroovyInput.assignInputFormElement(gshTemplateInputConfig.getName(), gshTemplateInputConfig.getConfigItemFormElement());
     return "Boolean "+gshTemplateInputConfig.getName() + " = grouperGroovyRuntime.retrieveInputValueBoolean(\"" + gshTemplateInputConfig.getName() + "\");\n";
    }
    
    @Override
    public boolean canConvertToCorrectType(Object valueFromUser) {
      
      if (!GrouperUtil.isBlank(valueFromUser)) {
        try {
          GrouperUtil.booleanValue(valueFromUser);
        } catch(Throwable e) {
          return false;
        }
      }
      return true;
    }
    
    @Override
    public Boolean convertToType(Object valueFromUser) {
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
  
  
  public abstract String generateGshVariable(GrouperGroovyInput grouperGroovyInput, GshTemplateInputConfig gshTemplateInputConfig, Object valueFromUser);

  public abstract boolean canConvertToCorrectType(Object valueFromUser);
  
  public abstract Object convertToType(Object valueFromUser);

}

