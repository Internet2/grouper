package edu.internet2.middleware.grouper.rules;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * rule if condition
 * @author mchyzer
 *
 */
public class RuleIfCondition {

  /**
   * 
   */
  public RuleIfCondition() {
    
  }

  /**
   * 
   * @param ifConditionEl
   * @param ifConditionEnum
   */
  public RuleIfCondition(String ifConditionEl, String ifConditionEnum) {
    super();
    this.ifConditionEl = ifConditionEl;
    this.ifConditionEnum = ifConditionEnum;
  }


  /** if it is an el, put that here */
  private String ifConditionEl;
  
  /** if it is an enum, put that here */
  private String ifConditionEnum;

  /**
   * if it is an el, put that here
   * @return el
   */
  public String getIfConditionEl() {
    return this.ifConditionEl;
  }

  
  /**
   * if it is an el, put that here
   * @param ifConditionEl1
   */
  public void setIfConditionEl(String ifConditionEl1) {
    this.ifConditionEl = ifConditionEl1;
  }

  /**
   * if it is an enum, put that here
   * @return enum
   */
  public String getIfConditionEnum() {
    return this.ifConditionEnum;
  }

  /**
   * if it is an enum, put that here
   * @param ifConditionEnum1
   */
  public void setIfConditionEnum(String ifConditionEnum1) {
    this.ifConditionEnum = ifConditionEnum1;
  }

  /**
   * convert the type to an enum
   * @return rule check type
   */
  public RuleIfConditionEnum ifConditionEnum() {
    return RuleIfConditionEnum.valueOfIgnoreCase(this.ifConditionEnum, false);
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
    if (!StringUtils.isBlank(this.ifConditionEl)) {
      result.append("ifConditionEl: ").append(this.ifConditionEl).append(", ");
    }
    if (!StringUtils.isBlank(this.ifConditionEnum)) {
      result.append("ifConditionEnum: ").append(this.ifConditionEnum).append(", ");
    }
  }

  /**
   * validate this 
   * @return error or null if ok
   */
  public String validate() {
    //can be blank
    if (!StringUtils.isBlank(this.ifConditionEnum) &&  !StringUtils.isBlank(this.ifConditionEl)) {
      return "Do not enter both of ifConditionEl and ifConditionEnum!";
    }
    if (!StringUtils.isBlank(this.ifConditionEnum)) {
      try {
        RuleIfConditionEnum.valueOfIgnoreCase(this.ifConditionEnum, true);
      } catch (Exception e) {
        return e.getMessage();
      }
    }
    return null;
  }

  /**
   * if this check passes
   * @param ruleDefinition
   * @param ruleEngine
   * @param rulesBean
   * @param logDataForThisDefinition 
   * @return true if this check passes
   */
  public boolean shouldFire(RuleDefinition ruleDefinition, 
      RuleEngine ruleEngine, RulesBean rulesBean, StringBuilder logDataForThisDefinition) {

    RuleIfConditionEnum ruleIfConditionEnum = this.ifConditionEnum();
    if (ruleIfConditionEnum != null) {
      return ruleIfConditionEnum.shouldFire(ruleDefinition, 
          ruleEngine, rulesBean);
    } else if (!StringUtils.isBlank(this.ifConditionEl)) {
      Map<String, Object> variableMap =  new HashMap<String, Object>();
      variableMap.put("ruleUtils", new RuleUtils());
      
      ruleDefinition.addElVariables(variableMap, rulesBean);
      
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
      
      String result = GrouperUtil.substituteExpressionLanguage(this.ifConditionEl, variableMap);
      
      if (logDataForThisDefinition != null) {
        logDataForThisDefinition.append(", elResult: ").append(result);
      }
      
      return GrouperUtil.booleanObjectValue(result);
    }
    
    throw new RuntimeException("Shouldnt get here");
  }

}
