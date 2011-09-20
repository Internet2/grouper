/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import edu.internet2.middleware.grouper.ws.soap_v2_1.WsAttributeAssign;
import edu.internet2.middleware.grouper.ws.soap_v2_1.WsAttributeAssignValueResult;


/**
 * holds an attribute assign result.  Also holds value results (if value operations were performed).
 * note if attribute assignments have values and the attribute is removed, the values will not be in 
 * this result
 */
public class WsAssignAttributeResult {

  /** set of results of this attribute assign value */
  private WsAttributeAssignValueResult[] wsAttributeAssignValueResults;

  
  
  /**
   * set of results of this attribute assign value
   * @return the array of result object
   */
  public WsAttributeAssignValueResult[] getWsAttributeAssignValueResults() {
    return this.wsAttributeAssignValueResults;
  }

  /**
   * set of results of this attribute assign value
   * @param wsAttributeAssignValueResults1
   */
  public void setWsAttributeAssignValueResults(
      WsAttributeAssignValueResult[] wsAttributeAssignValueResults1) {
    this.wsAttributeAssignValueResults = wsAttributeAssignValueResults1;
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

  /** if the values were changed, T|F */
  private String valuesChanged;

  /** if this assignment was deleted, T|F */
  private String deleted;

  /**
   * if the values were changed, T|F
   * @return if the values were changed, T|F
   */
  public String getValuesChanged() {
    return this.valuesChanged;
  }

  /**
   * if the values were changed, T|F
   * @param valuesChanged1
   */
  public void setValuesChanged(String valuesChanged1) {
    this.valuesChanged = valuesChanged1;
  }

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
