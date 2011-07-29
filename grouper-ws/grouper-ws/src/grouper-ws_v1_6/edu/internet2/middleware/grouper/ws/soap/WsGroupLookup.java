/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import org.apache.commons.lang.StringUtils;

/**
 * <pre>
 * Class to lookup a group via web service
 * 
 * developers make sure each setter calls this.clearGroup();
 * </pre>
 * @author mchyzer
 */
public class WsGroupLookup {

  /**
   * see if this group lookup has data
   * @return true if it has data
   */
  public boolean hasData() {
    return !StringUtils.isBlank(this.groupName) || !StringUtils.isBlank(this.uuid);
  }
  
  /**
   * uuid of the group to find
   */
  private String uuid;

  /** name of the group to find (includes stems, e.g. stem1:stem2:groupName */
  private String groupName;

  /**
   * uuid of the group to find
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * uuid of the group to find
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * name of the group to find (includes stems, e.g. stem1:stem2:groupName
   * @return the theName
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * name of the group to find (includes stems, e.g. stem1:stem2:groupName
   * @param theName the theName to set
   */
  public void setGroupName(String theName) {
    this.groupName = theName;
  }

  /**
   * 
   */
  public WsGroupLookup() {
    //blank
  }

}
