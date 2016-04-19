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
   * membershipType
   */
  private AsasApiMembershipTypeParam membershipType;

  /**
   * @return the membershipType
   */
  public AsasApiMembershipTypeParam getMembershipType() {
    return this.membershipType;
  }
  
  /**
   * @param membershipType1 the membershipType to set
   */
  public void setMembershipType(AsasApiMembershipTypeParam membershipType1) {
    this.membershipType = membershipType1;
  }

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
