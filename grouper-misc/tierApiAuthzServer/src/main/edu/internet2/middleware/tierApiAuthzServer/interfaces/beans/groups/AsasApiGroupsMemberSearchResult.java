/*******************************************************************************
 * Copyright 2016 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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

  /**
   * 
   */
  private AsasApiGroup asasApiGroup;
  
  
  
  
  /**
   * @return the asasApiGroup
   */
  public AsasApiGroup getAsasApiGroup() {
    return this.asasApiGroup;
  }

  
  /**
   * @param asasApiGroup the asasApiGroup to set
   */
  public void setAsasApiGroup(AsasApiGroup asasApiGroup) {
    this.asasApiGroup = asasApiGroup;
  }

  /**
   * 
   */
  private AsasApiUser asasApiUser;
  
  /**
   * @return the asasApiUser
   */
  public AsasApiUser getAsasApiUser() {
    return this.asasApiUser;
  }

  
  /**
   * @param asasApiUser1 the asasApiUser to set
   */
  public void setAsasApiUser(AsasApiUser asasApiUser1) {
    this.asasApiUser = asasApiUser1;
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
    asasGroupMemberContainer.getMeta().setTierSuccess(true);
    if (asasApiGroupMemberSearch.getAsasApiUser() != null) {
      AsasApiUser asasApiUser = asasApiGroupMemberSearch.getAsasApiUser();
      asasGroupMemberContainer.setId(asasApiUser.getId());
      asasGroupMemberContainer.setTierNetId(asasApiUser.getTierNetId());
      if (asasApiUser.getName() != null) {
        AsasApiName asasApiName = asasApiUser.getName();
        AsasName asasName = AsasApiName.convertToAsasName(asasApiName);
        asasGroupMemberContainer.setName(asasName);
      }
      asasGroupMemberContainer.setUserName(asasGroupMemberContainer.getUserName());
      asasGroupMemberContainer.getMeta().setTierHttpStatusCode(200);
      asasGroupMemberContainer.getMeta().setTierResultCode("SUCCESS_IS_MEMBER");
    } else if (asasApiGroupMemberSearch.getAsasApiGroup() != null) {
      AsasApiGroup asasApiGroup = asasApiGroupMemberSearch.getAsasApiGroup();
      asasGroupMemberContainer.setId(asasApiGroup.getId());
      asasGroupMemberContainer.setDisplayName(asasApiGroup.getDisplayName());
      asasGroupMemberContainer.setTierSystemName(asasApiGroup.getName());
      asasGroupMemberContainer.getMeta().setTierHttpStatusCode(200);
      asasGroupMemberContainer.getMeta().setTierResultCode("SUCCESS_IS_MEMBER");
    } else {
      asasGroupMemberContainer.getMeta().setTierHttpStatusCode(404);
      asasGroupMemberContainer.getMeta().setTierResultCode("SUCCESS_IS_NOT_MEMBER");
    }
    return asasGroupMemberContainer;
  }
  
}
