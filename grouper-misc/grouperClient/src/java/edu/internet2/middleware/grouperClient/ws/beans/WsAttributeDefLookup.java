/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * <pre>
 * Class to lookup an attribute def via web service
 * 
 * </pre>
 * @author mchyzer
 */
public class WsAttributeDefLookup {

  /**
   * 
   */
  public WsAttributeDefLookup() {
    //empty
  }
  
  /**
   * @param uuid1
   * @param name1
   */
  public WsAttributeDefLookup(String name1, String uuid1) {
    super();
    this.uuid = uuid1;
    this.name = name1;
  }

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

}
