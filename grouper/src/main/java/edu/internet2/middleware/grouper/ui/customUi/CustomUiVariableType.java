/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ui.customUi;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * type of variable to use
 */
public enum CustomUiVariableType {

  /**
   * string
   */
  STRING {

    @Override
    public Object convertTo(Object input) {
      if (input == null) {
        return null;
      }
      if (input instanceof String) {
        return (String) input;
      }
      return GrouperUtil.stringValue(input);
    }

    @Override
    public Class<?> sqlResultClass() {
      return String.class;
    }

    @Override
    public Object sqlConvertResult(Object sqlResult) {
      return sqlResult;
    }

    @Override
    public String screenValue(Object value, Map<String, Object> variableMap) {
      if (value == null || "".equals(value)) {
        return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiTypeBlank']}", null, null, null, null, variableMap);
      }
      return GrouperUtil.stringValue(value);
    }
  },
  
  /**
   * boolean
   */
  BOOLEAN {

    @Override
    public Object convertTo(Object input) {
      if (input == null || input instanceof Boolean) {
        return input;
      }
      return GrouperUtil.booleanObjectValue(input);
    }

    @Override
    public Class<?> sqlResultClass() {
      return Long.class;
    }

    @Override
    public Object sqlConvertResult(Object sqlResult) {
      if (!(sqlResult instanceof Long)) {
        throw new RuntimeException("A boolean SQL query should return 0 for false and more than 0 for true!");
      }
      Long sqlResultLong = (Long)sqlResult;
      return sqlResultLong != 0L;
    }

    @Override
    public String screenValue(Object value, Map<String, Object> variableMap) {
      if (value == null || "".equals(value)) {
        return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiTypeBlank']}", null, null, null, null, variableMap);
      }
      boolean booleanValue = GrouperUtil.booleanValue(value);
      if (booleanValue) {
        return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiTypeTrue']}", null, null, null, null, variableMap);
      }
      return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiTypeFalse']}", null, null, null, null, variableMap);
    }
  },
  
  /**
   * java long integer
   */
  INTEGER {

    @Override
    public Object convertTo(Object input) {
      return GrouperUtil.longObjectValue(input, true);
    }

    @Override
    public Class<?> sqlResultClass() {
      return Long.class;
    }

    @Override
    public Object sqlConvertResult(Object sqlResult) {
      return sqlResult;
    }
    
    @Override
    public String screenValue(Object value, Map<String, Object> variableMap) {
      if (value == null || "".equals(value)) {
        return CustomUiUtil.substituteExpressionLanguage("${textContainer.text['guiCustomUiTypeBlank']}", null, null, null, null, variableMap);
      }
      return GrouperUtil.stringValue(value);
    }

  };
  
  /**
   * 
   * @param input
   * @return the converted value
   */
  public abstract Object convertTo(Object input);
  
  /**
   * 
   * @return the sql class related
   */
  public abstract Class<?> sqlResultClass();
  
  /**
   * convert sql result to type
   * @param sqlResult
   * @return the object
   */
  public abstract Object sqlConvertResult(Object sqlResult);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNull will not allow null or blank entries
   * @return the enum or null or exception if not found
   */
  public static CustomUiVariableType valueOfIgnoreCase(String string, boolean exceptionOnNull) {
    return GrouperUtil.enumValueOfIgnoreCase(CustomUiVariableType.class, 
        string, exceptionOnNull);
  
  }

  /**
   * 
   * @param value
   * @param variableMap 
   * @return the screen value
   */
  public abstract String screenValue(Object value, Map<String, Object> variableMap);
}
