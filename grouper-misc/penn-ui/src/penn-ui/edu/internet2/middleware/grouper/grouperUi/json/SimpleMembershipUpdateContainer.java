/*
 * @author mchyzer
 * $Id: SimpleMembershipUpdateContainer.java,v 1.1 2009-08-05 00:57:19 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;



/**
 * bean for simple membership update
 */
public class SimpleMembershipUpdateContainer {

  /**
   * store to request scope (called from retrieveFromRequest)
   */
  private void storeToRequest() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    httpServletRequest.setAttribute("simpleMembershipUpdateContainer", this);
  }

  /**
   * retrieveFromRequest, can be null
   * @return the app state in request scope
   */
  public static SimpleMembershipUpdateContainer retrieveFromRequest() {
    HttpServletRequest httpServletRequest = GrouperUiJ2ee.retrieveHttpServletRequest();
    SimpleMembershipUpdateContainer simpleMembershipUpdateContainer = (SimpleMembershipUpdateContainer)httpServletRequest
      .getAttribute("simpleMembershipUpdateContainer");
    if (simpleMembershipUpdateContainer == null) {
      simpleMembershipUpdateContainer = new SimpleMembershipUpdateContainer();
      simpleMembershipUpdateContainer.storeToRequest();
    }
    return simpleMembershipUpdateContainer;
  }

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
  private GuiGroup group;

  /**
   * members in result
   */
  private GuiMember[] members;

  /**
   * paging data
   */
  private GuiPaging paging;
  
  /**
   * 
   * @return the group
   */
  public GuiGroup getGroup() {
    return this.group;
  }

  /**
   * group object
   * @param group1
   */
  public void setGroup(GuiGroup group1) {
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

  /**
   * members in result
   * @return members
   */
  public GuiMember[] getMembers() {
    return this.members;
  }

  /**
   * paging in result
   * @return paging
   */
  public GuiPaging getPaging() {
    return this.paging;
  }

  /**
   * members in result
   * @param members1
   */
  public void setMembers(GuiMember[] members1) {
    this.members = members1;
  }

  /**
   * paging in result
   * @param paging1
   */
  public void setPaging(GuiPaging paging1) {
    this.paging = paging1;
  }
  
}
