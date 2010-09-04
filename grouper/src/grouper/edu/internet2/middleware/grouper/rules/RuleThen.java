package edu.internet2.middleware.grouper.rules;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * rule then part
 * @author mchyzer
 *
 */
public class RuleThen {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(RuleThen.class);


  /**
   * 
   */
  public RuleThen() {
    
  }

  /**
   * 
   * @param thenEl1
   * @param thenEnum1
   * @param _thenEnumArg0 
   * @param _thenEnumArg1 
   */
  public RuleThen(String thenEl1, String thenEnum1, String _thenEnumArg0, String _thenEnumArg1) {
    super();
    this.thenEl = thenEl1;
    this.thenEnum = thenEnum1;
    this.thenEnumArg0 = _thenEnumArg0;
    this.thenEnumArg1 = _thenEnumArg1;
  }


  /** if it is an el, put that here */
  private String thenEl;
  
  /** arg0 to the then clause */
  private String thenEnumArg0;
  
  /** arg1 to the then clause */
  private String thenEnumArg1;
  
  /**
   * arg0 to the then clause
   * @return arg0
   */
  public String getThenEnumArg0() {
    return this.thenEnumArg0;
  }

  /**
   * arg0 to the then clause
   * @param theThenEnumArg0
   */
  public void setThenEnumArg0(String theThenEnumArg0) {
    this.thenEnumArg0 = theThenEnumArg0;
  }

  /**
   * 
   * @return arg1
   */
  public String getThenEnumArg1() {
    return this.thenEnumArg1;
  }

  /**
   * then enum arg1
   * @param theThenEnumArg1
   */
  public void setThenEnumArg1(String theThenEnumArg1) {
    this.thenEnumArg1 = theThenEnumArg1;
  }


  /** if it is an enum, put that here */
  private String thenEnum;

  /**
   * if it is an el, put that here
   * @return el
   */
  public String getThenEl() {
    return this.thenEl;
  }

  
  /**
   * if it is an el, put that here
   * @param thenEl1
   */
  public void setThenEl(String thenEl1) {
    this.thenEl = thenEl1;
  }

  /**
   * if it is an enum, put that here
   * @return enum
   */
  public String getThenEnum() {
    return this.thenEnum;
  }

  /**
   * if it is an enum, put that here
   * @param thenEnum1
   */
  public void setThenEnum(String thenEnum1) {
    this.thenEnum = thenEnum1;
  }

  /**
   * convert the type to an enum
   * @return rule check type
   */
  public RuleThenEnum thenEnum() {
    return RuleThenEnum.valueOfIgnoreCase(this.thenEnum, false);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    toStringHelper(result);
    return result.toString();
  }
  
  /**
   * 
   * @param result
   */
  void toStringHelper(StringBuilder result) {
    if (!StringUtils.isBlank(this.thenEl)) {
      result.append("thenEl: ").append(this.thenEl).append(", ");
    }
    if (!StringUtils.isBlank(this.thenEnum)) {
      result.append("thenEnum: ").append(this.thenEnum).append(", ");
    }
    if (!StringUtils.isBlank(this.thenEnumArg0)) {
      result.append("thenEnumArg0: ").append(this.thenEnumArg0).append(", ");
    }
    if (!StringUtils.isBlank(this.thenEnumArg1)) {
      result.append("thenEnumArg1: ").append(this.thenEnumArg1).append(", ");
    }
  }

  /**
   * validate this 
   * @param ruleDefinition
   * @return error or null if ok
   */
  public String validate(RuleDefinition ruleDefinition) {
    if (StringUtils.isBlank(this.thenEl) ==  StringUtils.isBlank(this.thenEnum)) {
      return "Enter one and only one of thenEl and thenEnum!";
    }
    if (!StringUtils.isBlank(this.thenEnum)) {
      RuleThenEnum ruleThenEnum = null;
      try {
        ruleThenEnum = RuleThenEnum.valueOfIgnoreCase(this.thenEnum, true);
      } catch (Exception e) {
        return e.getMessage();
      }
      String errorMessage = ruleThenEnum.validate(ruleDefinition);
      if (!StringUtils.isBlank(errorMessage)) {
        return errorMessage;
      }
    } else {
      if (!StringUtils.isBlank(this.thenEnumArg0) || !StringUtils.isBlank(this.thenEnumArg1)) {
        return "Cant enter arg0 or arg1 for a then clause if there is no then enum";
      }
    }
    return null;
  }

  /**
   * fire this rule
   * @param ruleDefinition
   * @param ruleEngine
   * @param rulesBean
   * @param logDataForThisDefinition if logger if logging, else not
   */
  public void fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine, 
      RulesBean rulesBean, StringBuilder logDataForThisDefinition) {
    RuleThenEnum ruleThenEnum = this.thenEnum();
    if (ruleThenEnum != null) {
      Object result = ruleThenEnum.fireRule(ruleDefinition, ruleEngine, rulesBean, logDataForThisDefinition);
      if (logDataForThisDefinition != null) {
        logDataForThisDefinition.append(", enumResult: ").append(result);
      }
    } else if (!StringUtils.isBlank(this.thenEl)) {
      Map<String, Object> variableMap =  new HashMap<String, Object>();

      
      Subject actAsSubject = ruleDefinition.getActAs().subject(true);
      boolean hasAccessToEl = RuleEngine.hasAccessToElApi(actAsSubject);

      ruleDefinition.addElVariables(variableMap, rulesBean, hasAccessToEl);
      
      if (logDataForThisDefinition != null) {
        logDataForThisDefinition.append(", EL variables: ");
        for (String varName : variableMap.keySet()) {
          logDataForThisDefinition.append(varName);
          Object value = variableMap.get(varName);
          if (value instanceof String) {
            logDataForThisDefinition.append("(").append(value).append(")");
          }
          logDataForThisDefinition.append(",");
        }
      }
      
      String result = GrouperUtil.substituteExpressionLanguage(this.thenEl, variableMap);
      
      if (logDataForThisDefinition != null) {
        logDataForThisDefinition.append(", elResult: ").append(result);
      }
      
    } else {
      //should have an enum or EL
      throw new RuntimeException("Shouldnt get here, why is there no enum or el for the then clause???");
    }
  }
}
