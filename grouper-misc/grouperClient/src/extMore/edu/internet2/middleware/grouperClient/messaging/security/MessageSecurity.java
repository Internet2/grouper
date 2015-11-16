/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging.security;

import java.util.Collection;

import net.sf.json.JSONObject;


/**
 * Implement this interface to provide security on messages since in Grouper
 */
public interface MessageSecurity {

  /**
   * encrypt (or not) or sign or whatever
   * @param sendFrom
   * @param sendTo
   * @param messageContainer
   * @return the json object with the container, and an unencrypted payload
   */
  public JSONObject processForSend(String sendFrom, String sendTo, JSONObject messageContainer);
  
  /**
   * encrypt (or not) or sign or whatever
   * @param sendFrom
   * @param sendTos
   * @param messageContainer
   * @return the json object to send
   */
  public JSONObject processForSend(String sendFrom, Collection<String> sendTos, JSONObject messageContainer);
  
  /**
   * encrypt (or not) or sign or whatever
   * @param sendFrom
   * @param sendTo
   * @param messageContainer
   * @return the json object to send
   */
  public JSONObject processForReceive(String sendFrom, String sendTo, JSONObject messageContainer);
  
  
  
}
