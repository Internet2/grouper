/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateInit.java,v 1.1 2009-07-31 14:27:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import edu.internet2.middleware.grouper.ws.soap.WsGroup;


/**
 *
 */
public class SimpleMembershipUpdateInit {

  /** if can read group */
  private boolean canReadGroup;
  
  /** if can update group */
  private boolean canUpdateGroup;

  /** if can find group */
  private boolean canFindGroup;

  /** if this is a composite group */
  private boolean isCompositeGroup;
  
  /**
   * group object
   */
  private WsGroup group;
  
  /**
   * 
   * @return the group
   */
  public WsGroup getGroup() {
    return this.group;
  }

  /**
   * group object
   * @param group1
   */
  public void setGroup(WsGroup group1) {
    this.group = group1;
  }

  /**
   * if this is a composite group
   * @return true if composite group
   */
  public boolean isCompositeGroup() {
    return this.isCompositeGroup;
  }

  /**
   * if this is a composite group
   * @param isCompositeGroup1
   */
  public void setCompositeGroup(boolean isCompositeGroup1) {
    this.isCompositeGroup = isCompositeGroup1;
  }

  /**
   * 
   * @return if can read group
   */
  public boolean isCanReadGroup() {
    return this.canReadGroup;
  }

  /**
   * if can read group
   * @param canReadGroup1
   */
  public void setCanReadGroup(boolean canReadGroup1) {
    this.canReadGroup = canReadGroup1;
  }

  /**
   * if can update group
   * @return if can update group
   */
  public boolean isCanUpdateGroup() {
    return this.canUpdateGroup;
  }

  /**
   * if can update group
   * @param canUpdateGroup1
   */
  public void setCanUpdateGroup(boolean canUpdateGroup1) {
    this.canUpdateGroup = canUpdateGroup1;
  }

  /**
   * if can find group
   * @return if can find group
   */
  public boolean isCanFindGroup() {
    return this.canFindGroup;
  }

  /**
   * if can find group
   * @param canFindGroup1
   */
  public void setCanFindGroup(boolean canFindGroup1) {
    this.canFindGroup = canFindGroup1;
  }
  
}
