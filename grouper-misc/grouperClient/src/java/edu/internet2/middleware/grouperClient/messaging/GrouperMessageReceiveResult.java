/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.messaging;

import java.util.Collection;


/**
 * result bean to receive messages
 */
public class GrouperMessageReceiveResult {

  /**
   * 
   */
  public GrouperMessageReceiveResult() {
  }

  /**
   * grouper messages
   */
  private Collection<GrouperMessage> grouperMessages;

  
  /**
   * @return the grouperMessages
   */
  public Collection<GrouperMessage> getGrouperMessages() {
    return this.grouperMessages;
  }

  
  /**
   * @param grouperMessages1 the grouperMessages to set
   */
  public void setGrouperMessages(Collection<GrouperMessage> grouperMessages1) {
    this.grouperMessages = grouperMessages1;
  }
  
  
}
