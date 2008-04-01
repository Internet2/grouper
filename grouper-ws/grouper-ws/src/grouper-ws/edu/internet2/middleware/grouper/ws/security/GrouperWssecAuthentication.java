/*
 * @author mchyzer
 * $Id: GrouperWssecAuthentication.java,v 1.1 2008-04-01 08:38:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.security;

import java.io.IOException;

import org.apache.ws.security.WSPasswordCallback;


/**
 * Implement this for rampart security.  See GrouperWssecSample
 * for an example
 */
public interface GrouperWssecAuthentication {

  /**
   * <pre>
   * authenticate the user, and find the subject and return.
   * See GrouperWssecSample for an example
   * </pre>
   * @param wsPasswordCallback
   * @return true if that callback type is supported, false if not
   * @throws IOException if there is a problem or if user is not authenticated correctly
   */
  public boolean authenticate(WSPasswordCallback wsPasswordCallback) throws IOException;
  

}
