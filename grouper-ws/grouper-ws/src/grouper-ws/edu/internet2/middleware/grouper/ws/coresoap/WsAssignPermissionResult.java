/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.coresoap;


/**
 * holds a permission assign result.  
 */
public class WsAssignPermissionResult  {

  /**
   * 
   */
  public WsAssignPermissionResult() {
    //empty
  }

  /**
   * convert
   * @param wsAssignAttributeResult
   */
  public WsAssignPermissionResult(WsAssignAttributeResult wsAssignAttributeResult) {
    this.changed = wsAssignAttributeResult.getChanged();
    this.deleted = wsAssignAttributeResult.getDeleted();
    this.wsAttributeAssigns = wsAssignAttributeResult.getWsAttributeAssigns();
  }

  /** assignment(s) involved */
  private WsAttributeAssign[] wsAttributeAssigns;

  /**
   * assignment involved
   * @return assignment involved
   */
  public WsAttributeAssign[] getWsAttributeAssigns() {
    return this.wsAttributeAssigns;
  }

  /**
   * assignment involved
   * @param wsAttributeAssigns1
   */
  public void setWsAttributeAssigns(WsAttributeAssign[] wsAttributeAssigns1) {
    this.wsAttributeAssigns = wsAttributeAssigns1;
  }

  /** if this assignment was changed, T|F */
  private String changed;
  /** if this assignment was deleted, T|F */
  private String deleted;

  /**
   * if this assignment was changed, T|F
   * @return if changed
   */
  public String getChanged() {
    return this.changed;
  }

  /**
   * if this assignment was changed, T|F
   * @param changed1
   */
  public void setChanged(String changed1) {
    this.changed = changed1;
  }

  /**
   * if this assignment was deleted, T|F
   * @return if this assignment was deleted, T|F
   */
  public String getDeleted() {
    return this.deleted;
  }

  /**
   * if this assignment was deleted, T|F
   * @param deleted1
   */
  public void setDeleted(String deleted1) {
    this.deleted = deleted1;
  }
  
  
  
}
