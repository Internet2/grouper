/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.interfaces;

import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsSearchParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsSearchResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup;


/**
 * implement this interface to provide logic for the authz standard api server group operations
 * @author mchyzer
 *
 */
public interface AsasApiGroupInterface {

  /**
   * perform a search, e.g. a GET on /groups
   * @param asasApiGroupsSearchParam
   * @return the result
   */
  public AsasApiGroupsSearchResult search(AsasApiEntityLookup authenticatedSubject, 
      AsasApiGroupsSearchParam asasApiGroupsSearchParam);
  
}
