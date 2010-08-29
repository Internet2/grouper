package edu.internet2.middleware.grouper.rules;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.rules.beans.RulesBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

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
   * @param ifConditionEl1
   * @param ifConditionEnum1
   * @param theIfOwnerId 
   * @param theIfOwnerName 
   */
  public RuleIfCondition(String ifConditionEl1, String ifConditionEnum1,
      String theIfOwnerId, String theIfOwnerName) {
    super();
    this.ifConditionEl = ifConditionEl1;
    this.ifConditionEnum = ifConditionEnum1;
    this.ifOwnerId = theIfOwnerId;
    this.ifOwnerName = theIfOwnerName;
  }


  /** if it is an el, put that here */
  private String ifConditionEl;
  
  /** if it is an enum, put that here */
  private String ifConditionEnum;

  /** if the enum needs an owner, this is the name */
  private String ifOwnerName;
  
  /** if the enum needs an owner, this is the id */
  private String ifOwnerId;
  
  /**
   * if the enum needs an owner, this is the name
   * @return name
   */
  public String getIfOwnerName() {
    return this.ifOwnerName;
  }

  
  /**
   * if the enum needs an owner, this is the name
   * @param ifOwnerName1
   */
  public void setIfOwnerName(String ifOwnerName1) {
    this.ifOwnerName = ifOwnerName1;
  }

  /**
   * if the enum needs an owner, this is the id
   * @return id
   */
  public String getIfOwnerId() {
    return this.ifOwnerId;
  }

  /**
   * if the enum needs an owner, this is the id
   * @param ifOwnerId1
   */
  public void setIfOwnerId(String ifOwnerId1) {
    this.ifOwnerId = ifOwnerId1;
  }

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
    if (!StringUtils.isBlank(this.ifOwnerId)) {
      result.append("ifOwnerId: ").append(this.ifOwnerId).append(", ");
    }
    if (!StringUtils.isBlank(this.ifOwnerName)) {
      result.append("ifOwnerName: ").append(this.ifOwnerName).append(", ");
    }
  }

  /**
   * validate this 
   * @param ruleDefinition 
   * @return error or null if ok
   */
  public String validate(RuleDefinition ruleDefinition) {
    //can be blank
    if (!StringUtils.isBlank(this.ifConditionEnum) &&  !StringUtils.isBlank(this.ifConditionEl)) {
      return "Do not enter both of ifConditionEl and ifConditionEnum!";
    }
    if (!StringUtils.isBlank(this.ifOwnerId) &&  !StringUtils.isBlank(this.ifOwnerName)) {
      return "Do not enter both of ifOwnerId and ifOwnerName!";
    }
    if (!StringUtils.isBlank(this.ifConditionEnum)) {
      RuleIfConditionEnum ruleIfConditionEnum = null;
      try {
        ruleIfConditionEnum = RuleIfConditionEnum.valueOfIgnoreCase(this.ifConditionEnum, true);
      } catch (Exception e) {
        return e.getMessage();
      }
      String enumValidation = ruleIfConditionEnum.validate(ruleDefinition);
      if (!StringUtils.isBlank(enumValidation)) {
        return enumValidation;
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
      
      Subject actAsSubject = ruleDefinition.getActAs().subject(true);
      boolean hasAccessToElApi = RuleEngine.hasAccessToElApi(actAsSubject);
      
      Map<String, Object> variableMap =  new HashMap<String, Object>();
      
      ruleDefinition.addElVariables(variableMap, rulesBean, hasAccessToElApi);
      
      if (!StringUtils.isBlank(this.ifOwnerId)) {
        variableMap.put("ifOwnerId", this.ifOwnerId);
      }
      if (!StringUtils.isBlank(this.ifOwnerName)) {
        variableMap.put("ifOwnerName", this.ifOwnerName);
      }
      
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

  /**
   * see if the owner is a group (note, owner requiredness not checked)
   * @return the error message
   */
  public String validateOwnerGroup() {
    return RuleUtils.validateGroup(this.ifOwnerId, this.ifOwnerName);
  }
  
  /**
   * see if the owner is a stem (note, owner requiredness not checked)
   * @return the error message
   */
  public String validateOwnerStem() {
    return RuleUtils.validateStem(this.ifOwnerId, this.ifOwnerName);
  }
  

}
