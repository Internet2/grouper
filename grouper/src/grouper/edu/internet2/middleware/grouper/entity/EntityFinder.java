/**
 * Copyright 2014 Internet2
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
/**
 * 
 */
package edu.internet2.middleware.grouper.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * class to find entities.
 * TODO add in finder by attribute subject id
 * TODO add former names
 * @author mchyzer
 *
 */
public class EntityFinder {

  /**
   * find entities by term, any substring which will be split by spaces
   */
  private String terms;
  
  /**
   * add a search term, any substring which will be split by spaces
   * @param theTerms
   * @return this for chaining
   */
  public EntityFinder assignTerms(String theTerms) {
    this.terms = theTerms;
    return this;
  }
  
  /**
   * names to find
   */
  private List<String> names;
  
  /**
   * uuids to find
   */
  private List<String> ids;
  
  /**
   * folder names to look in, parent, not ancestor
   */
  private List<String> parentFolderNames;
  
  /**
   * folder ids to look in.  note this is only the parent folder, not ancestor
   */
  private List<String> parentFolderIds;
  
  /**
   * add an entity id to search for
   * @param theId
   * @return this for chaining
   */
  public EntityFinder addId(String theId) {
    if (this.ids == null) {
      this.ids = new ArrayList<String>();
    }
    this.ids.add(theId);
    return this;
  }

  /**
   * add an entity name to search for
   * @param theName
   * @return this for chaining
   */
  public EntityFinder addName(String theName) {
    if (this.names == null) {
      this.names = new ArrayList<String>();
    }
    this.names.add(theName);
    return this;
  }
  
  /**
   * add a parent folder id to search for.  note, this is the immediate parent folder id, not ancestor
   * @param parentFolderId
   * @return this for chaining
   */
  public EntityFinder addParentFolderId(String parentFolderId) {
    if (this.parentFolderIds == null) {
      this.parentFolderIds = new ArrayList<String>();
    }
    this.parentFolderIds.add(parentFolderId);
    return this;
  }
  
  /**
   * add a parent folder name to search in, note this is the immediate parent folder name, not an ancentor
   * @param parentFolderName
   * @return this for chaining
   */
  public EntityFinder addParentFolderName(String parentFolderName) {
    if (this.parentFolderNames == null) {
      this.parentFolderNames = new ArrayList<String>();
    }
    this.parentFolderNames.add(parentFolderName);
    return this;
  }

  /**
   * find an entity
   * @param exceptionIfNotFound true if exception should be thrown if entity not found
   * @return the entity or null or exception
   */
  public Entity findEntity(boolean exceptionIfNotFound) {
  
    Set<Entity> entities = findEntities();
    
    //this should find one if it is there...
    Entity entity = null;
    
    if (GrouperUtil.length(entities) > 1) {
      throw new RuntimeException("Why is there more than one entity found? " + this);
    }
    
    if (GrouperUtil.length(entities) == 1) {
      entity = entities.iterator().next();
    }
    
    if (entity == null && exceptionIfNotFound) {
      throw new RuntimeException("could not find entity: " 
          + this);
    }
    return entity;
    
  }

  /**
   * names of an ancestor folders to search in
   */
  private List<String> ancestorFolderNames;
  
  /**
   * ids of an ancestor folders to search in
   */
  private List<String> ancestorFolderIds;

  /**
   * add an ancestor folder name to search in
   * @param theFolderName
   * @return the folder name
   */
  public EntityFinder addAncestorFolderName(String theFolderName) {
    if (this.ancestorFolderNames == null) {
      this.ancestorFolderNames = new ArrayList<String>();
    }
    this.ancestorFolderNames.add(theFolderName);
    return this;
  }

  /**
   * add an ancestor folder id to search in
   * @param theFolderId
   * @return this for chaining
   */
  public EntityFinder addAncestorFolderId(String theFolderId) {
    if (this.ancestorFolderIds == null) {
      this.ancestorFolderIds = new ArrayList<String>();
    }
    this.ancestorFolderIds = new ArrayList<String>();
    return this;
  }
  
  /**
   * find a list of entities
   * @return the set of entities never null
   */
  public Set<Entity> findEntities() {
    
    
    Set<Entity> entities = GrouperDAOFactory.getFactory().getEntity().findEntitiesSecure(
        GrouperSession.staticGrouperSession(),this.ancestorFolderIds, this.ancestorFolderNames, 
        this.ids, this.names, this.parentFolderIds, this.parentFolderNames, this.terms, 
        AccessPrivilege.VIEW_ENTITY_PRIVILEGES, this.queryOptions);
    
    return entities;
    
  }

  /**
   * find a list of entities, and the String subjectIdentifier
   * @return the list of entities never null, if there is a subjectIdentifier for the entity, that will be next in the array, else null
   */
  public List<Object[]> findEntitiesAndSubjectIdentifier() {
    
    Set<Entity> entities = this.findEntities();
    
    List<Object[]> results = new ArrayList<Object[]>();
    
    if (GrouperUtil.length(entities) == 0) {
      return results;
    }
    
    Set<String> entityIds = new LinkedHashSet<String>();
    
    for (Entity entity : entities) {
      entityIds.add(entity.getId());
    }
    
    List<Object[]> entityAndAttributeValues = GrouperDAOFactory.getFactory().getEntity().findEntitiesByGroupIds(entityIds);
    
    //add to map from group id to entityId
    Map<String, String> attributeValueLookupMap = new HashMap<String, String>();
    for (Object[] row : entityAndAttributeValues) {
      attributeValueLookupMap.put(((Group)row[0]).getId(), ((AttributeAssignValue)row[1]).valueString());
    }

    for (Entity entity : entities) {
      Object[] result = new Object[]{entity, attributeValueLookupMap.get(entity.getId())};
      results.add(result);
    }
    
    return results;
    
  }

  /**
   * query options for sorting and paging
   */
  private QueryOptions queryOptions;
  
  /**
   * assign query options for sorting and paging
   * @param queryOptions1
   * @return this for paging
   */
  public EntityFinder assignQueryOptions(QueryOptions queryOptions1) {
    this.queryOptions = queryOptions1;
    return this;
  }
  
  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    if (GrouperUtil.length(this.ids) > 0) {
      result.append("ids: ").append(GrouperUtil.toStringForLog(this.ids, 100));
    }
    if (GrouperUtil.length(this.names) > 0) {
      result.append("names: ").append(GrouperUtil.toStringForLog(this.names, 100));
    }
    if (!StringUtils.isBlank(this.terms)) {
      result.append("terms: ").append(GrouperUtil.toStringForLog(this.terms, 100));
    }
    if (GrouperUtil.length(this.parentFolderIds) > 0) {
      result.append("parentFolderIds: ").append(GrouperUtil.toStringForLog(this.parentFolderIds, 100));
    }
    if (GrouperUtil.length(this.parentFolderNames) > 0) {
      result.append("parentFolderNames: ").append(GrouperUtil.toStringForLog(this.parentFolderNames, 100));
    }
    if (GrouperUtil.length(this.ancestorFolderIds) > 0) {
      result.append("ancestorFolderIds: ").append(GrouperUtil.toStringForLog(this.ancestorFolderIds, 100));
    }
    if (GrouperUtil.length(this.ancestorFolderNames) > 0) {
      result.append("ancestorFolderNames: ").append(GrouperUtil.toStringForLog(this.ancestorFolderNames, 100));
    }
    if (this.queryOptions != null) {
      result.append("queryOptions: ").append(this.queryOptions);
    }
    return result.toString();
  }

  
}
