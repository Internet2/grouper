/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.interfaces;

import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsMemberSearchParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsMemberSearchResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup;


/**
 * implement this interface to provide logic for the authz standard api server groupsMember operations
 * @author mchyzer
 *
 */
public interface AsasApiGroupsMemberInterface {

  /**
   * perform a search, e.g. a GET on /Groups/id:something/Members/id:something
   * @param asasApiGroupsMemberSearchParam
   * @return the result
   */
  public AsasApiGroupsMemberSearchResult search(AsasApiEntityLookup authenticatedSubject, 
      AsasApiGroupsMemberSearchParam asasApiGroupsMemberSearchParam);
  
}
