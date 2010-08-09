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

  /**
   * 
   */
  public RuleCheck() {
    super();
  }

  /**
   * 
   * @param type
   * @param ownerId
   * @param ownerName
   */
  public RuleCheck(String type, String ownerId, String ownerName) {
    super();
    this.type = type;
    this.ownerId = ownerId;
    this.ownerName = ownerName;
  }

  /** type of check */
  private String type;
  
  /** group/stem/etc which fires the rule */
  private String ownerId;
  
  /** group/stem/etc which fires the rule */
  private String ownerName;
  
  
  /**
   * group/stem/etc which fires the rule
   * @return group/stem/etc which fires the rule
   */
  public String getOwnerName() {
    return this.ownerName;
  }

  /**
   * group/stem/etc which fires the rule
   * @param ownerName1
   */
  public void setOwnerName(String ownerName1) {
    this.ownerName = ownerName1;
  }

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
    //RuleCheckType.valueOfIgnoreCase(type1, false);
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
  public String getOwnerId() {
    return this.ownerId;
  }

  /**
   * group which fires the rule
   * @param group1
   */
  public void setOwnerId(String group1) {
    this.ownerId = group1;
  }
  
}
