package edu.internet2.middleware.grouper.rules;

/**
 * e.g.
 * check:
 *  - type: flattenedMembershipChange
 *  - groups: X,Z
 *  
 * the type of check and any params
 * @author mchyzer
 *
 */
public class RuleCheck {

  
  /** type of check */
  private String type;
  
  /** group which fires the rule */
  private String group;
  
  /** stem ancestor to check */
  private String stemAncestor;

  /**
   * type of rule check
   * @return the type
   */
  public String getType() {
    return this.type;
  }

  /**
   * type of rule check
   * @param type1
   */
  public void setType(String type1) {
    RuleCheckType.valueOfIgnoreCase(type1, false);
    this.type = type1;
  }

  /**
   * convert the type to an enum
   * @return rule check type
   */
  public RuleCheckType typeEnum() {
    return RuleCheckType.valueOfIgnoreCase(this.type, false);
  }
  
  /**
   * group which fires the rule
   * @return the group
   */
  public String getGroup() {
    return this.group;
  }

  /**
   * group which fires the rule
   * @param group1
   */
  public void setGroup(String group1) {
    this.group = group1;
  }

  /**
   * stem ancestor that fires the rule
   * @return the stem ancestor
   */
  public String getStemAncestor() {
    return this.stemAncestor;
  }

  /**
   * stem ancestor that fires the rule
   * @param stemAncestor1
   */
  public void setStemAncestor(String stemAncestor1) {
    this.stemAncestor = stemAncestor1;
  }
  
}
