/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * <pre>
 * Class to lookup an attribute assignment via web service
 * 
 * </pre>
 * @author mchyzer
 */
public class WsAttributeAssignLookup {

  /**
   * uuid of the attributeAssign to find
   */
  private String uuid;

  /**
   * uuid of the attributeAssign to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the attributeAssign to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

}
