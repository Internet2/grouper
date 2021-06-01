package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public enum GshTemplateInputValidationType {

  builtin {

    @Override
    public boolean doesValuePassValidation(GshTemplateInputConfig gshTemplateInputConfig,  String valueFromUser, List<GshTemplateInput> templateInputs) {
      return gshTemplateInputConfig.getValidationBuiltinType().doesValuePassValidation(valueFromUser);
    }
    
    
  },
  
  regex {

    @Override
    public boolean doesValuePassValidation(GshTemplateInputConfig gshTemplateInputConfig,  String valueFromUser, List<GshTemplateInput> templateInputs) {
      
        Pattern pattern = Pattern.compile(gshTemplateInputConfig.getValidationRegex());
        Matcher matcher = pattern.matcher(valueFromUser == null ? "": valueFromUser);
        return matcher.matches();
    }
    
    
  }, 
  
  jexl {

    @Override
    public boolean doesValuePassValidation(GshTemplateInputConfig gshTemplateInputConfig,  String valueFromUser, List<GshTemplateInput> templateInputs) {
      
      Map<String, Object> variableMap = new HashMap<String, Object>();
      
      variableMap.put("grouperUtil", new GrouperUtil());
      variableMap.put("value", valueFromUser == null ? "": valueFromUser);
      
      for (GshTemplateInput gshTemplateInput: templateInputs) {
        variableMap.put(gshTemplateInput.getName(), gshTemplateInput.getValueString());
      }
      
      try {        
        
        Object substituteExpressionLanguageScript = GrouperUtil.substituteExpressionLanguageScript(gshTemplateInputConfig.getValidationJexl(), variableMap, true, false, true);
        return GrouperUtil.booleanObjectValue(substituteExpressionLanguageScript);
      } catch(Exception e) {
        return false;
      }
      
    }
  },

  none {

    @Override
    public boolean doesValuePassValidation(GshTemplateInputConfig gshTemplateInputConfig,  String valueFromUser, List<GshTemplateInput> templateInputs) {
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
  public static GshTemplateInputValidationType valueOfIgnoreCase(String string, boolean exceptionOnNotFound) {
    return GrouperUtil.enumValueOfIgnoreCase(GshTemplateInputValidationType.class, string, exceptionOnNotFound);
  }
  
  public abstract boolean doesValuePassValidation(GshTemplateInputConfig gshTemplateInputConfig,  String valueFromUser, List<GshTemplateInput> templateInputs);
}
