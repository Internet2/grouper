package edu.internet2.middleware.grouper.rules;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.rules.beans.RulesBean;

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
    if (!StringUtils.isBlank(this.ifConditionEl) &&  !StringUtils.isBlank(this.ifConditionEl)) {
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
   * @return true if this check passes
   */
  public boolean shouldFire(RuleDefinition ruleDefinition, 
      RuleEngine ruleEngine, RulesBean rulesBean) {

    RuleIfConditionEnum ruleIfConditionEnum = this.ifConditionEnum();
    if (ruleIfConditionEnum != null) {
      return ruleIfConditionEnum.shouldFire(ruleDefinition, 
      ruleEngine, rulesBean);
    } else if (!StringUtils.isBlank(this.ifConditionEl)) {
      throw new RuntimeException("Not implemented");
    }
    
    throw new RuntimeException("Shouldnt get here");
  }

}
