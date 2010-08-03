/**
 * 
 */
package edu.internet2.middleware.grouper.rules;

import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 * Define a rule, convert to a JSON string for attribute
 * @author mchyzer
 *
 */
public class RuleDefinition {

  /**
   * get all rules from the DB in the form of attribute assignments
   * @return the assigns of all rules
   */
  public static Set<AttributeAssign> allRules() {
    
    String ruleAttributeName = GrouperCheckConfig.attributeRuleStemName() + ":rule";
    
    AttributeDefName ruleName = AttributeDefNameFinder.findByName(ruleAttributeName, true);
    
    Set<AttributeAssign> attributeAssigns = GrouperDAOFactory.getFactory()
      .getAttributeAssign().findByAttributeDefNameId(ruleName.getId());
    
    return attributeAssigns;
    
  }
  
  //actAs: user (blank means me, though bad idea)
  //check:
  //  - type: flattenedMembershipChange
  //  - groups: X,Z
  //if: member is not a member of Group checkedGroup (immediate or flattened)
  //then: remove member from group A

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
