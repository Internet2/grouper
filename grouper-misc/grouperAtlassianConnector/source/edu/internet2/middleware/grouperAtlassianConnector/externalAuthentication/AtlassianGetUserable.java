package edu.internet2.middleware.grouperAtlassianConnector.externalAuthentication;

import java.security.Principal;

/**
 * get userable
 * @author mchyzer
 */
public interface AtlassianGetUserable {

  /**
   * get a principal
   * @param username
   * @return the principal
   */
  Principal getUser(String username);

}
