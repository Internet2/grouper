/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.rest;

import java.util.Map;

import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasGroupSearchContainer;
import edu.internet2.middleware.authzStandardApiServer.interfaces.AsasApiGroupInterface;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroup;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroupsSearchParam;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiGroupsSearchResult;
import edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups.AsasApiQueryParams;
import edu.internet2.middleware.authzStandardApiServer.interfaces.entity.AsasApiEntityLookup;
import edu.internet2.middleware.authzStandardApiServer.j2ee.AsasHttpServletRequest;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerConfig;
import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;


/**
 * logic for rest calls
 * @author mchyzer
 *
 */
public class AsasRestLogic {

  /**
   * groups list or search
   * @param params
   * @return the result container
   */
  public static AsasGroupSearchContainer getGroups(Map<String, String> params) {
    
    //lets get the groups interface
    AsasApiGroupInterface asasApiGroupInterface = StandardApiServerUtils.interfaceGroupInstance();
    AsasApiEntityLookup authenticatedSubject = AsasApiEntityLookup.retrieveLoggedInUser();
    
    AsasApiGroupsSearchParam asasApiGroupsSearchParam = new AsasApiGroupsSearchParam();
    
    AsasApiQueryParams asasApiQueryParams = AsasApiQueryParams.convertFromQueryString();
    
    Boolean pagingEnabled = AsasHttpServletRequest.retrieve().getParameterBoolean("pagingEnabled");
    boolean pagingDisabled = pagingEnabled != null && !pagingEnabled;
    
    //lets setup the default
    if (!pagingDisabled && asasApiQueryParams.getLimit() == null) {
      //# if there is no limit set for a group search, this is the limit that will be applied, -1 to not have a limit
      //authzStandardApiServer.groupsSearch.defaultLimit = 100
      int defaultLimit = StandardApiServerConfig.retrieveConfig().propertyValueInt("authzStandardApiServer.groupsSearch.defaultLimit", 100);
      if (defaultLimit != -1) {
        asasApiQueryParams.setLimit(Long.valueOf(defaultLimit));
        asasApiQueryParams.setOffset(0L);
      }
    }
    
    //# max number of groups returned from the search.  -1 to not have a limit
    //authzStandardApiServer.groupsSearch.maxLimit = 1000
    int maxLimit = StandardApiServerConfig.retrieveConfig().propertyValueInt("authzStandardApiServer.groupsSearch.maxLimit", 1000);

    //lets setup the max limit
    if (maxLimit != -1) {
      if (pagingDisabled || (asasApiQueryParams.getLimit() != null && asasApiQueryParams.getLimit() > maxLimit)) {
        
        asasApiQueryParams.setLimit(Long.valueOf(maxLimit));
        asasApiQueryParams.setOffset(0L);
      }
    }

    if (StandardApiServerUtils.isBlank(asasApiQueryParams.getSortField())) {
      String defaultSortField = StandardApiServerConfig
          .retrieveConfig().propertyValueString("authzStandardApiServer.groupsSearch.defaultSortField", "name");
      
      asasApiQueryParams.setSortField(defaultSortField);
    }
    
    asasApiGroupsSearchParam.setQueryParams(asasApiQueryParams);
    
    AsasApiGroupsSearchResult asasApiGroupsSearchResult = asasApiGroupInterface.search(authenticatedSubject, asasApiGroupsSearchParam);
     
    AsasGroupSearchContainer asasGroupSearchContainer = new AsasGroupSearchContainer();

    asasGroupSearchContainer.setGroups(AsasApiGroup.convertToList(asasApiGroupsSearchResult.getGroups()));
    
    //merge in the meta
    AsasApiQueryParams.convertTo(asasApiGroupsSearchResult.getQueryParams(), asasGroupSearchContainer.getMeta());
    
    return asasGroupSearchContainer;
  }
  
}
