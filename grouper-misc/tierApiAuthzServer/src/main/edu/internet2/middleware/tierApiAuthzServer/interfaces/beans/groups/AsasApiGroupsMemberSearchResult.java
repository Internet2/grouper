package edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups;

import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasGroupsMemberSearchContainer;
import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasName;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.AsasApiQueryParams;


/**
 * search result bean
 * @author mchyzer
 *
 */
public class AsasApiGroupsMemberSearchResult {

  /**
   * queryParams
   */
  private AsasApiQueryParams queryParams;
  
  /**
   * queryParams
   * @return the queryParams
   */
  public AsasApiQueryParams getQueryParams() {
    return this.queryParams;
  }
  
  /**
   * queryParams
   * @param asasApiPaging1 the qsetQueryParamss to set
   */
  public void setQueryParams(AsasApiQueryParams asasApiPaging1) {
    this.queryParams = asasApiPaging1;
  }

  private AsasApiUser asasApiUser;
  
  /**
   * @return the asasApiUser
   */
  public AsasApiUser getAsasApiUser() {
    return this.asasApiUser;
  }

  
  /**
   * @param asasApiUser the asasApiUser to set
   */
  public void setAsasApiUser(AsasApiUser asasApiUser) {
    this.asasApiUser = asasApiUser;
  }

  /**
   * convert the api beans to the transport beans
   * @param asasApiGroupMemberSearch
   * @return the api bean
   */
  public static AsasGroupsMemberSearchContainer convertTo(AsasApiGroupsMemberSearchResult asasApiGroupMemberSearch) {
    if (asasApiGroupMemberSearch == null) {
      return null;
    }
    AsasGroupsMemberSearchContainer asasGroupMemberContainer = new AsasGroupsMemberSearchContainer();
    asasGroupMemberContainer.getMeta().setSuccess(true);
    if (asasApiGroupMemberSearch.getAsasApiUser() != null) {
      asasGroupMemberContainer.setId(asasApiGroupMemberSearch.getAsasApiUser().getId());
      asasGroupMemberContainer.setTierNetId(asasApiGroupMemberSearch.getAsasApiUser().getTierNetId());
      if (asasApiGroupMemberSearch.getAsasApiUser().getName() != null) {
        AsasApiName asasApiName = asasApiGroupMemberSearch.getAsasApiUser().getName();
        AsasName asasName = AsasApiName.convertToAsasName(asasApiName);
        asasGroupMemberContainer.setName(asasName);
      }
      asasGroupMemberContainer.setUserName(asasGroupMemberContainer.getUserName());
      asasGroupMemberContainer.getMeta().setHttpStatusCode(200);
      asasGroupMemberContainer.getMeta().setResultCode("SUCCESS_IS_MEMBER");
    } else {
      asasGroupMemberContainer.getMeta().setHttpStatusCode(404);
      asasGroupMemberContainer.getMeta().setResultCode("SUCCESS_IS_NOT_MEMBER");
    }
    return asasGroupMemberContainer;
  }

}
