package edu.internet2.middleware.grouper.rules;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * rule then part
 * @author mchyzer
 *
 */
public class RuleThen {

  /**
   * 
   */
  public RuleThen() {
    
  }

  /**
   * 
   * @param thenEl
   * @param thenEnum
   */
  public RuleThen(String thenEl, String thenEnum) {
    super();
    this.thenEl = thenEl;
    this.thenEnum = thenEnum;
  }


  /** if it is an el, put that here */
  private String thenEl;
  
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
  }

  /**
   * validate this 
   * @return error or null if ok
   */
  public String validate() {
    if (StringUtils.isBlank(this.thenEl) ==  StringUtils.isBlank(this.thenEnum)) {
      return "Enter one and only one of thenEl and thenEnum!";
    }
    if (!StringUtils.isBlank(this.thenEnum)) {
      try {
        RuleThenEnum.valueOfIgnoreCase(this.thenEnum, true);
      } catch (Exception e) {
        return e.getMessage();
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
