/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_0;


/**
 * <pre>
 * Class to lookup an attribute def name via web service
 * 
 * developers make sure each setter calls this.clearAttributeDefName();
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefNameLookup {

  /**
   * uuid of the attributeDefName to find
   */
  private String uuid;

  /** name of the attributeDefName to find (includes stems, e.g. stem1:stem2:attributeDefNameName */
  private String name;

  /**
   * uuid of the attributeDefName to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the attributeDefName to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * name of the attributeDefName to find (includes stems, e.g. stem1:stem2:attributeDefNameName
   * @return the theName
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of the attributeDefName to find (includes stems, e.g. stem1:stem2:attributeDefNameName
   * @param theName the theName to set
   */
  public void setName(String theName) {
    this.name = theName;
  }

  /**
   * 
   */
  public WsAttributeDefNameLookup() {
    //blank
  }

}
