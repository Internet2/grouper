/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.entity;

import java.util.List;

import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * logic to help with entity subjects
 * @author mchyzer
 *
 */
public class EntitySubject {

  /**
   * cache from entity id (group uuid), to the entity attribute value, or null if caching that there is none
   */
  private static GrouperCache<String, String> grouperCache = null;
  
  /**
   * lazy load the group cache
   * @return the cache
   */
  private static GrouperCache<String, String> grouperCache() {
    if (grouperCache == null) {
      grouperCache = new GrouperCache(EntitySubject.class.getName() + ".EntityAttributeIdCache");
    }
    return grouperCache;
  }
  
  
  /**
   * get the id from a cache, or look it up and add to cache
   * @param id
   * @return the id
   */
  public static String entityIdAttributeValue(String id) {
    if (grouperCache().containsKey(id)) {
      String entityId = grouperCache.get(id);
      //maybe it was removed while getting it... hmmm
      if (grouperCache().containsKey(id)) {
        return entityId;
      }
    }
    
    List<Object[]> results = GrouperDAOFactory.getFactory().getEntity().findEntitiesByGroupIds(GrouperUtil.toSet(id));
    
    String entityId = null;
    if (results.size() > 0) {
      entityId = ((AttributeAssignValue)results.get(0)[1]).valueString();
    }
    grouperCache().put(id, entityId);
    return entityId;
    
    
    
  }
  
  /**
   * if we are looking up the entity ids ahead of time, then put in cache so they dont have to be looked up again
   * @param id
   * @param entityAttributeId
   */
  public static void assignEntityIdInCache(String id, String entityAttributeId) {
    grouperCache().put(id, entityAttributeId);
  }
  
}
