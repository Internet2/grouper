/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClientExt.xmpp;

import org.jivesoftware.smack.packet.Message;


/**
 * implement to handle an xmpp message
 */
public interface GrouperClientXmppMessageHandler {

  /**
   * handle an xmpp message
   * @param message
   */
  public void handleMessage(Message message);
  
}
