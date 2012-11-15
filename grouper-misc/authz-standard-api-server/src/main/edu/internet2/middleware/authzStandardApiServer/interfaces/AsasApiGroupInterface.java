/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.interfaces;

import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroupsSearchParam;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroupsSearchResult;
import edu.internet2.middleware.authzStandardApiServer.interfaces.entity.AsasApiEntityLookup;


/**
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
