package edu.internet2.middleware.grouper.rules;

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

  
}
