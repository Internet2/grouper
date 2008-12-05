/**
 * 
 */
package edu.internet2.middleware.grouperClient.ws.beans;


/**
 * <pre>
 * Class to lookup a stem via web service
 * 
 * </pre>
 * @author mchyzer
 */
public class WsStemLookup {

  /**
   * uuid of the stem to find
   */
  private String uuid;

  /** name of the stem to find (includes stems, e.g. stem1:stem2:stemName */
  private String stemName;

  /**
   * uuid of the stem to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the stem to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
   * @return the theName
   */
  public String getStemName() {
    return this.stemName;
  }

  /**
   * name of the stem to find (includes stems, e.g. stem1:stem2:stemName
   * @param theName the theName to set
   */
  public void setStemName(String theName) {
    this.stemName = theName;
  }

  /**
   * 
   */
  public WsStemLookup() {
    //blank
  }

  /**
   * construct with fields
   * @param theStemName
   * @param stemUuid
   */
  public WsStemLookup(String theStemName, String stemUuid) {
    this.stemName = theStemName;
    this.uuid = stemUuid;
  }
}
