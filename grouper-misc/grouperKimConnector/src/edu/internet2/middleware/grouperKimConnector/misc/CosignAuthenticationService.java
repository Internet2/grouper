/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.misc;

import javax.servlet.http.HttpServletRequest;

import org.kuali.rice.kim.service.AuthenticationService;


/**
 * This authenticator will work with cosign or other authentication services
 * that put the netId in the request.attribute(REMOTE_USER)
 */
public class CosignAuthenticationService implements AuthenticationService {

  /**
   * @see org.kuali.rice.kim.service.AuthenticationService#getPrincipalName(javax.servlet.http.HttpServletRequest)
   */
  public String getPrincipalName(HttpServletRequest request) {
    String netId = (String)request.getAttribute("REMOTE_USER");
    System.out.println("netId: " + netId);
    return netId;
  }

}
