/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;



/**
 * method chaining system config
 */
public class GrouperMessageSystemParam {

  /**
   * 
   */
  public GrouperMessageSystemParam() {
  }

  /**
   * if objects should be auto created if not there, e.g. 
   * queues, topics, privileges
   */
  private boolean autocreateObjects;

  /**
   * if objects should be auto created if not there, e.g. 
   * queues, topics, privileges
   * @param theAutocreate
   * @return this for chaining
   */
  public GrouperMessageSystemParam assignAutocreateObjects(boolean theAutocreate) {
    this.autocreateObjects = theAutocreate;
    return this;
  }
  
  
  /**
   * @return the autocreateObjects
   */
  public boolean isAutocreateObjects() {
    return this.autocreateObjects;
  }

  /**
   * message system name to send message to
   */
  private String messageSystemName;

  /**
   * message system name to send message to
   * @param theMessageSystemName
   * @return this for chaining
   */
  public GrouperMessageSystemParam assignMesssageSystemName(String theMessageSystemName) {
    this.messageSystemName = theMessageSystemName;
    return this;
  }

  /**
   * message system name to send message to
   * @return message system name
   */
  public String getMessageSystemName() {
    return this.messageSystemName;
  }
  
  /**
   * if going over WS, this is the message system name on the WS side
   */
  private String grouperWsMessageSystemName;

  /**
   * message system name to send message to
   * @param theGrouperWsMessageSystemName
   * @return grouper ws message system name
   */
  public GrouperMessageSystemParam assignGrouperWsMessageSystemName(String theGrouperWsMessageSystemName) {
    this.grouperWsMessageSystemName = theGrouperWsMessageSystemName;
    return this;
  }
  
  /**
   * message system name to send message to
   * @return the grouper ws message system name
   */
  public String getGrouperWsMessageSystemName() {
    return this.grouperWsMessageSystemName;
  }
  
}
