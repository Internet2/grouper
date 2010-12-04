/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.client;

import edu.internet2.middleware.grouperClient.GrouperClient;


/**
 * extend this class to customize how authentication works
 */
public abstract class ClientAuthenticationType {

  /**
   * 
   * @param grouperClient
   */
  public abstract void authenticate(GrouperClient grouperClient);

}
