/**
 * 
 */
package edu.internet2.middleware.grouperStandardApi.interfaces;

import edu.internet2.middleware.authzStandardApiServer.interfaces.AsasApiGroupInterface;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroupsSearchParam;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroupsSearchResult;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiPaging;
import edu.internet2.middleware.authzStandardApiServer.interfaces.entity.AsasApiEntityLookup;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;


/**
 * Implement the group interface
 * @author mchyzer
 *
 */
public class GaasGroupInterfaceImpl implements AsasApiGroupInterface {

  /**
   * @see edu.internet2.middleware.authzStandardApiServer.interfaces.AsasApiGroupInterface#search(edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroupsSearchParam)
   */
  @Override
  public AsasApiGroupsSearchResult search(AsasApiEntityLookup authenticatedSubject,
      AsasApiGroupsSearchParam asasApiGroupsSearchParam) {
    
    if (asasApiGroupsSearchParam == null) {
      throw new NullPointerException();
    }
    
    if (authenticatedSubject == null) {
      throw new NullPointerException();
    }
    

    
    
    //start a session
    
    
    AsasApiPaging paging = asasApiGroupsSearchParam.getPaging();
    
    if (paging == null) {
      
    }
    
  }

}
