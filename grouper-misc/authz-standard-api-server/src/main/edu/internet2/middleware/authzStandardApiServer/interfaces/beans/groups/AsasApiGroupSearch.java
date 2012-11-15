/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.interfaces.beans.groups;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasGroup;
import edu.internet2.middleware.authzStandardApiServer.corebeans.AsasGroupSearch;


/**
 * Multiple groupList
 * @author mchyzer
 *
 */
public class AsasApiGroupSearch {

  /**
   * paging object, if no paging, this is null
   */
  private AsasApiPaging paging;
  
  /**
   * paging object, if no paging, this is null
   * @return the paging
   */
  public AsasApiPaging getPaging() {
    return this.paging;
  }
  
  /**
   * @param paging1 the paging to set
   */
  public void setPaging(AsasApiPaging paging1) {
    this.paging = paging1;
  }


  /**
   * convert the api beans to the transport beans
   * @param asasApiGroupSearch
   * @return the api bean
   */
  public static AsasGroupSearch convertTo(AsasApiGroupSearch asasApiGroupSearch) {
    if (asasApiGroupSearch == null) {
      return null;
    }
    AsasGroupSearch asasGroupSearch = new AsasGroupSearch();
    if (asasApiGroupSearch.groupList != null) {
      List<AsasGroup> asasGroupList = new ArrayList<AsasGroup>();
      asasGroupSearch.setGroupList(asasGroupList);
      
      for (AsasApiGroup asasApiGroup : asasApiGroupSearch.groupList) {
        asasGroupList.add(AsasApiGroup.convertTo(asasApiGroup));
      }
    }
    asasGroupSearch.setPaging(AsasApiPaging.convertTo(asasApiGroupSearch.paging));
    return asasGroupSearch;
  }
  
  /**
   * list of groupList
   */
  private List<AsasApiGroup> groupList = null;

  
  /**
   * @return the groupList
   */
  public List<AsasApiGroup> getGroupList() {
    return this.groupList;
  }

  
  /**
   * @param groups1 the groupList to set
   */
  public void setGroupList(List<AsasApiGroup> groups1) {
    this.groupList = groups1;
  }
  
  
  
}
