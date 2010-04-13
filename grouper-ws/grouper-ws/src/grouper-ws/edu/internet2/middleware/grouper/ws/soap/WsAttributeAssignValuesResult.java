/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.soap;


/**
 * holds an attribute assign result.  Also holds value results (if value operations were performed).
 * note if attribute assignments have values and the attribute is removed, the values will not be in 
 * this result
 */
public class WsAttributeAssignValuesResult {

  /** if this assignment was changed, T|F */
  private String changed;

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

  /** underlying attribute assign value result */
  private WsAttributeAssignValueResult[] wsAttributeAssignValueResults;

  /**
   * underlying attribute assign value result
   * @return underlying attribute assign value result
   */
  public WsAttributeAssignValueResult[] getWsAttributeAssignValueResults() {
    return this.wsAttributeAssignValueResults;
  }

  /**
   * underlying attribute assign value result
   * @param wsAttributeAssignValueResult1
   */
  public void setWsAttributeAssignValueResults(
      WsAttributeAssignValueResult[] wsAttributeAssignValueResult1) {
    this.wsAttributeAssignValueResults = wsAttributeAssignValueResult1;
  }
  
}
