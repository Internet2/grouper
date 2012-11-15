package edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups;


/**
 * search result bean
 * @author mchyzer
 *
 */
public class AsasApiGroupsSearchResult {

  /**
   * paging
   */
  private AsasApiPaging paging;
  
  /**
   * paging
   * @return the paging
   */
  public AsasApiPaging getPaging() {
    return this.paging;
  }
  
  /**
   * paging
   * @param asasApiPaging1 the paging to set
   */
  public void setPaging(AsasApiPaging asasApiPaging1) {
    this.paging = asasApiPaging1;
  }


  /**
   * groups result
   */
  private AsasApiGroupSearch groupSearch;
  
  /**
   * groups result
   * @return the groupSearch
   */
  public AsasApiGroupSearch getGroupSearch() {
    return this.groupSearch;
  }

  /**
   * groups result
   * @param asasApiGroups1 the groupSearch to set
   */
  public void setGroupSearch(AsasApiGroupSearch asasApiGroups1) {
    this.groupSearch = asasApiGroups1;
  }

}
