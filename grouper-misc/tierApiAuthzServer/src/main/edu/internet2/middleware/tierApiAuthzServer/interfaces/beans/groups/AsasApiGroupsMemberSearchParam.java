package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups;

import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.AsasApiQueryParams;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup;


/**
 * request for group search.
 * take into account the paging/sorting if specified in the query params
 * 
 * @author mchyzer
 *
 */
public class AsasApiGroupsMemberSearchParam {

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

  private AsasApiGroupLookup asasApiGroupLookup = new AsasApiGroupLookup();
  
  private AsasApiEntityLookup asasApiEntityLookup = new AsasApiEntityLookup();


  
  /**
   * @return the asasApiGroupLookup
   */
  public AsasApiGroupLookup getAsasApiGroupLookup() {
    return this.asasApiGroupLookup;
  }


  
  /**
   * @param asasApiGroupLookup the asasApiGroupLookup to set
   */
  public void setAsasApiGroupLookup(AsasApiGroupLookup asasApiGroupLookup) {
    this.asasApiGroupLookup = asasApiGroupLookup;
  }


  
  /**
   * @return the asasApiEntityLookup
   */
  public AsasApiEntityLookup getAsasApiEntityLookup() {
    return this.asasApiEntityLookup;
  }


  
  /**
   * @param asasApiEntityLookup the asasApiEntityLookup to set
   */
  public void setAsasApiEntityLookup(AsasApiEntityLookup asasApiEntityLookup) {
    this.asasApiEntityLookup = asasApiEntityLookup;
  }
  
  
  
}
