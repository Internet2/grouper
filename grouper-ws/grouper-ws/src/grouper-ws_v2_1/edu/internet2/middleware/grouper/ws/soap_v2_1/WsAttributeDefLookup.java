/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap_v2_1;


/**
 * <pre>
 * Class to lookup an attribute def via web service
 * 
 * developers make sure each setter calls this.clearAttributeDef();
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefLookup {

  /**
   * uuid of the attributeDef to find
   */
  private String uuid;

  /** name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef */
  private String name;

  /**
   * uuid of the attributeDef to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the attributeDef to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef
   * @return the theName
   */
  public String getName() {
    return this.name;
  }

  /**
   * name of the attributeDef to find (includes stems, e.g. stem1:stem2:attributeDef
   * @param theName the theName to set
   */
  public void setName(String theName) {
    this.name = theName;
  }

  /**
   * 
   */
  public WsAttributeDefLookup() {
    //blank
  }

}
