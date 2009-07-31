/*
 * @author mchyzer
 * $Id: SimpleMembershipList.java,v 1.1 2009-07-31 14:27:27 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.json;


/**
 * membership list bean for screen
 */
public class SimpleMembershipList {
  
  /**
   * members in result
   */
  private GuiMember[] members;
  
  /**
   * paging data
   */
  private GuiPaging paging;

  /**
   * members in result
   * @return members
   */
  public GuiMember[] getMembers() {
    return this.members;
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
   * @return paging
   */
  public GuiPaging getPaging() {
    return this.paging;
  }

  /**
   * paging in result
   * @param paging1
   */
  public void setPaging(GuiPaging paging1) {
    this.paging = paging1;
  }
  
  
}
