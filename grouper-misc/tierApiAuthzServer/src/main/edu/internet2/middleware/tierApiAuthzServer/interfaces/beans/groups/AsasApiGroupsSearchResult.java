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

import java.util.List;

import edu.internet2.middleware.tierApiAuthzServer.corebeans.AsasGroupSearchContainer;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.AsasApiQueryParams;


/**
 * search result bean
 * @author mchyzer
 *
 */
public class AsasApiGroupsSearchResult {

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
   * list of groups
   */
  private List<AsasApiGroup> groups = null;
  
  /**
   * @return the groups
   */
  public List<AsasApiGroup> getGroups() {
    return this.groups;
  }

  /**
   * @param groups1 the groups to set
   */
  public void setGroups(List<AsasApiGroup> groups1) {
    this.groups = groups1;
  }

  /**
   * convert the api beans to the transport beans
   * @param asasApiGroupSearch
   * @return the api bean
   */
  public static AsasGroupSearchContainer convertTo(AsasApiGroupsSearchResult asasApiGroupSearch) {
    if (asasApiGroupSearch == null) {
      return null;
    }
    AsasGroupSearchContainer asasGroupSearchContainer = new AsasGroupSearchContainer();
    asasGroupSearchContainer.setGroups(AsasApiGroup.convertToArray(asasApiGroupSearch.getGroups()));
    AsasApiQueryParams.convertTo(asasApiGroupSearch.queryParams, asasGroupSearchContainer.getMeta());
    return asasGroupSearchContainer;
  }

}
