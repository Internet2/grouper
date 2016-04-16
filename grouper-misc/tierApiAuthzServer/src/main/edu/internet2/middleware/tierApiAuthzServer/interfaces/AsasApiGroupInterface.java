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
/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.interfaces;

import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsSearchParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.groups.AsasApiGroupsSearchResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup;


/**
 * implement this interface to provide logic for the authz standard api server group operations
 * @author mchyzer
 *
 */
public interface AsasApiGroupInterface {

  /**
   * perform a search, e.g. a GET on /groups
   * @param asasApiGroupsSearchParam
   * @return the result
   */
  public AsasApiGroupsSearchResult search(AsasApiEntityLookup authenticatedSubject, 
      AsasApiGroupsSearchParam asasApiGroupsSearchParam);
  
}
