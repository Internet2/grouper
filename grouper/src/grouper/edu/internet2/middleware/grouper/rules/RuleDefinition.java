/**
 * 
 */
package edu.internet2.middleware.grouper.rules;


import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;


/**
 * Define a rule, convert to a JSON string for attribute
 * @author mchyzer
 *
 */
public class RuleDefinition {

  /** who this rule acts as */
  private RuleSubjectActAs actAs;

  /** when this rules is triggered */
  private RuleCheck check;

  /** only fire if this condition occurs */
  private String ifCondition;
  
  /** do this when the rule fires */
  private String then;

  /**
   * who this rule acts as
   * @return who this rule acts as
   */
  public RuleSubjectActAs getActAs() {
    return this.actAs;
  }

  /**
   * who this rule acts as
   * @param actAs1
   */
  public void setActAs(RuleSubjectActAs actAs1) {
    this.actAs = actAs1;
  }

  /**
   * when this rules is triggered
   * @return the check
   */
  public RuleCheck getCheck() {
    return check;
  }

  /**
   * when this rules is triggered
   * @param check1
   */
  public void setCheck(RuleCheck check1) {
    this.check = check1;
  }

  /**
   * only fire if this condition occurs
   * @return the if condition
   */
  public String getIfCondition() {
    return ifCondition;
  }

  /**
   * only fire if this condition occurs
   * @param ifCondition1
   */
  public void setIfCondition(String ifCondition1) {
    this.ifCondition = ifCondition1;
  }

  /**
   * do this when the rule fires
   * @return the then part
   */
  public String getThen() {
    return then;
  }

  /**
   * do this when the rule fires
   * @param then1
   */
  public void setThen(String then1) {
    this.then = then1;
  }
  
}
