package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups;

import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.AsasApiQueryParams;


/**
 * request for group search.
 * take into account the paging/sorting if specified in the query params
 * 
 * @author mchyzer
 *
 */
public class AsasApiGroupsSearchParam {

  /**
   * queryParams object for which objects to request
   */
  private AsasApiQueryParams queryParams;

  
  /**
   * queryParams object for which objects to request
   * @return the queryParams
   */
  public AsasApiQueryParams getQueryParams() {
    return this.queryParams;
  }

  
  /**
   * queryParams object for which objects to request
   * @param queryParams1 the queryParams to set
   */
  public void setQueryParams(AsasApiQueryParams queryParams1) {
    this.queryParams = queryParams1;
  }
  
}
