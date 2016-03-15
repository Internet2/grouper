/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups;

import edu.internet2.middleware.tierApiAuthzServer.exceptions.AsasRestInvalidRequest;
import edu.internet2.middleware.tierApiAuthzServer.rest.AsasRestPost;
import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;


/**
 *
 */
public enum AsasApiMembershipTypeParam {

  /**
   * all memberships
   */
  all,
  
  /**
   * only direct memberships (note could be indirect too)
   */
  direct,
  
  /**
   * only indirect memberships (note could be direct too)
   */
  indirect;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws AsasRestInvalidRequest if there is a problem
   */
  public static AsasApiMembershipTypeParam valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws AsasRestInvalidRequest {
    try {
      return StandardApiServerUtils.enumValueOfIgnoreCase(AsasApiMembershipTypeParam.class, 
          string, exceptionOnNotFound);
    } catch (AsasRestInvalidRequest asasRestInvalidRequest) {
      asasRestInvalidRequest.setHttpResponseCode("400");
      asasRestInvalidRequest.setTierResultCode("ERROR_INVALID_PARAM");
      throw asasRestInvalidRequest;
    }
  }

  
}
