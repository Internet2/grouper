/**
 * Copyright 2018 Internet2
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
 */

package edu.internet2.middleware.grouper.service;

import java.util.Set;

import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;


/**
 * utility methods for services
 */
public class ServiceUtils {

  /**
   * @param idOfAttributeDef
   * @param queryOptions 
   * @return the stems
   */
  public static Set<Stem> retrieveStemsForService(String idOfAttributeDef, QueryOptions queryOptions) {
    return new StemFinder().assignIdOfAttributeDefName(idOfAttributeDef).assignAttributeCheckReadOnAttributeDef(false)
        .assignQueryOptions(queryOptions).findStems();
  }

}
