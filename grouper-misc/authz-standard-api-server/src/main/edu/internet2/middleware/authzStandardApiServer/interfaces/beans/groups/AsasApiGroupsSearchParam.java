package edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups;


/**
 * request for search
 * @author mchyzer
 *
 */
public class AsasApiGroupsSearchParam {

  /**
   * paging object for which objects to request
   */
  private AsasApiPaging paging;

  
  /**
   * paging object for which objects to request
   * @return the paging
   */
  public AsasApiPaging getPaging() {
    return this.paging;
  }

  
  /**
   * paging object for which objects to request
   * @param paging1 the paging to set
   */
  public void setPaging(AsasApiPaging paging1) {
    this.paging = paging1;
  }
  
}
