package edu.internet2.middleware.grouper.rules;

import org.apache.commons.digester.RulesBase;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.rules.beans.RulesBean;

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
   */
  public void fireRule(RuleDefinition ruleDefinition, RuleEngine ruleEngine, RulesBean rulesBean) {
    RuleThenEnum ruleThenEnum = this.thenEnum();
    if (ruleThenEnum != null) {
      ruleThenEnum.fireRule(ruleDefinition, ruleEngine, rulesBean);
    } else if (!StringUtils.isBlank(this.thenEl)) {
      throw new RuntimeException("Not implemented");
    }
    throw new RuntimeException("Shouldnt get here");
  }
}
