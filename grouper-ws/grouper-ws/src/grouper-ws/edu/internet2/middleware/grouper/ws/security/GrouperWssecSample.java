/*
 * @author mchyzer $Id: GrouperWssecSample.java,v 1.1 2008-04-01 08:38:34 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.security;

import java.io.IOException;

import org.apache.ws.security.WSPasswordCallback;

/**
 * sample implementation of wssec
 */
public class GrouperWssecSample implements GrouperWssecAuthentication {

  /**
   * @see edu.internet2.middleware.grouper.ws.security.GrouperWssecAuthentication#authenticate(org.apache.ws.security.WSPasswordCallback)
   */
  public boolean authenticate(WSPasswordCallback wsPasswordCallback) throws IOException {
    System.out.println("identifier: " + wsPasswordCallback.getIdentifer() + ", usage: "
        + wsPasswordCallback.getUsage());

    if (wsPasswordCallback.getUsage() == WSPasswordCallback.USERNAME_TOKEN) {
      // for passwords sent in digest mode we need to provide the password,
      // because the original one can't be un-digested from the message

      // we can throw either of the two Exception types if authentication fails
      if (!"GrouperSystem".equals(wsPasswordCallback.getIdentifer())) {
        throw new IOException("unknown user: " + wsPasswordCallback.getIdentifer());
      }

      // this will throw an exception if the passwords don't match
      wsPasswordCallback.setPassword("mypass");
      return true;
    }

    if (wsPasswordCallback.getUsage() == WSPasswordCallback.USERNAME_TOKEN_UNKNOWN) {
      // for passwords sent in cleartext mode we can compare passwords directly

      if (!"GrouperSystem".equals(wsPasswordCallback.getIdentifer())) {
        throw new IOException("unknown user: " + wsPasswordCallback.getIdentifer());
      }

      // we can throw either of the two Exception types if authentication fails
      if (!"mypass".equals(wsPasswordCallback.getPassword())) {
        throw new IOException("password incorrect for user: "
            + wsPasswordCallback.getIdentifer());
      }
      return true;
    }
    //this is not ok
    return false;
  }

}
