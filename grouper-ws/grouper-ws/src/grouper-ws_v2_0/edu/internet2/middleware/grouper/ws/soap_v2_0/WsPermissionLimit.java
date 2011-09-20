/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;

import edu.internet2.middleware.grouper.ws.soap_v2_1.WsAttributeAssignValue;


/**
 * represents a limit on a permission.  Note this is an attribute assignment
 * you can link to the attribute assignment by the attribute assign id
 */
public class WsPermissionLimit {

  /** value(s) in this assignment if any */
  private WsAttributeAssignValue[] wsAttributeAssignValues;
  
  /** if this is an attribute assign attribute, this is the foreign key */
  private String attributeAssignId;
  
  /** attribute name id in this assignment */
  private String attributeDefNameId;
  
  /** attribute name in this assignment */
  private String attributeDefNameName;

  /**
   * value(s) in this assignment if any
   * @return values
   */
  public WsAttributeAssignValue[] getWsAttributeAssignValues() {
    return this.wsAttributeAssignValues;
  }

  /**
   * value(s) in this assignment if any
   * @param wsAttributeAssignValues1
   */
  public void setWsAttributeAssignValues(WsAttributeAssignValue[] wsAttributeAssignValues1) {
    this.wsAttributeAssignValues = wsAttributeAssignValues1;
  }

  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @return attribute assign id
   */
  public String getAttributeAssignId() {
    return this.attributeAssignId;
  }

  /**
   * attribute name id in this assignment
   * @return attribute name id in this assignment
   */
  public String getAttributeDefNameId() {
    return this.attributeDefNameId;
  }

  /**
   * attribute name in this assignment
   * @return attribute name in this assignment
   */
  public String getAttributeDefNameName() {
    return this.attributeDefNameName;
  }

  /**
   * if this is an attribute assign attribute, this is the foreign key
   * @param ownerAttributeAssignId1
   */
  public void setAttributeAssignId(String ownerAttributeAssignId1) {
    this.attributeAssignId = ownerAttributeAssignId1;
  }

  /**
   * attribute name id in this assignment
   * @param attributeDefNameId1
   */
  public void setAttributeDefNameId(String attributeDefNameId1) {
    this.attributeDefNameId = attributeDefNameId1;
  }

  /**
   * attribute name in this assignment
   * @param attributeDefNameName1
   */
  public void setAttributeDefNameName(String attributeDefNameName1) {
    this.attributeDefNameName = attributeDefNameName1;
  }
}
