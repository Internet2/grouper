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

import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderDeleteParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderDeleteResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderSaveParam;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.beans.folders.AsasApiFolderSaveResult;
import edu.internet2.middleware.tierApiAuthzServer.interfaces.entity.AsasApiEntityLookup;


/**
 * implement this interface to provide logic for the authz standard api server folder operations
 * @author mchyzer
 *
 */
public interface AsasApiFolderInterface {

  /**
   * save a folder, e.g. a POST (insert) or PUT (update) on /folders/name:some:name
   * @param asasApiFolderSaveParam
   * @return the result
   */
  public AsasApiFolderSaveResult save(AsasApiEntityLookup authenticatedSubject, 
      AsasApiFolderSaveParam asasApiFolderSaveParam);

  /**
   * delete a folder, e.g. a DELETE on /folders/name:some:name
   * @param asasApiFolderDeleteParam
   * @return the result
   */
  public AsasApiFolderDeleteResult delete(AsasApiEntityLookup authenticatedSubject, 
      AsasApiFolderDeleteParam asasApiFolderDeleteParam);
  
}
