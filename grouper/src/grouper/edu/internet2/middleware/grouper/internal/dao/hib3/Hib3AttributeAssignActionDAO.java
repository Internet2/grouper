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
package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeAssignActionNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 * Data Access Object for attribute assign action
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignActionDAO.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
public class Hib3AttributeAssignActionDAO extends Hib3DAO implements AttributeAssignActionDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeAssignActionDAO.class.getName();

  /**
   * reset the attribute defs
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeAssignAction").executeUpdate();
  }

  /**
   * config key for caching
   */
  private static final String GROUPER_FLASHCACHE_ATTRIBUTE_ASSIGN_ACTION = "grouper.flashcache.attribute.assign.action.in.finder";
  
  /**
   * cache stuff in attributeAssigns by uuid, <uuid> or attributeDefId, <attributeDefId>
   */
  private static GrouperCache<MultiKey, Set<AttributeAssignAction>> attributeAssignActionFlashCache = new GrouperCache(
      "edu.internet2.middleware.grouper.internal.dao.hib3.Hib3AttributeAssignActionDAO.attributeAssignActionFlashCache");

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3AttributeAssignActionDAO.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#findById(java.lang.String, boolean)
   */
  public AttributeAssignAction findById(String id, boolean exceptionIfNotFound) {
    
    Set<AttributeAssignAction> attributeAssignActions = attributeAssignActionFlashCacheRetrieveById(id, null);
    if (GrouperUtil.length(attributeAssignActions) > 0) {
      return attributeAssignActions.iterator().next();
    }      
    
    AttributeAssignAction attributeAssignAction = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssignAction where id = :theId")
      .setString("theId", id).uniqueResult(AttributeAssignAction.class);
    if (attributeAssignAction == null && exceptionIfNotFound) {
      throw new AttributeAssignActionNotFoundException("Cant find attribute assign action by id: " + id);
    }

    if (attributeAssignAction != null) {
      attributeAssignActionFlashCacheAddIfSupposedTo(attributeAssignAction);
    }
    
    return attributeAssignAction;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction)
   */
  public void saveOrUpdate(AttributeAssignAction attributeAssignAction) {

    MultiKey multiKey = attributeAssignActionFlashCacheMultikeyId(attributeAssignAction.getId());
    attributeAssignActionFlashCache.remove(multiKey);
    
    multiKey = attributeAssignActionFlashCacheMultikeyAttributeDefId(attributeAssignAction.getAttributeDefId());
    attributeAssignActionFlashCache.remove(multiKey);
    
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignAction);
    
    attributeAssignActionFlashCacheAddIfSupposedTo(attributeAssignAction);

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#delete(edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction)
   */
  public void delete(AttributeAssignAction attributeAssignAction) {
    
    MultiKey multiKey = attributeAssignActionFlashCacheMultikeyId(attributeAssignAction.getId());
    attributeAssignActionFlashCache.remove(multiKey);
    
    multiKey = attributeAssignActionFlashCacheMultikeyAttributeDefId(attributeAssignAction.getAttributeDefId());
    attributeAssignActionFlashCache.remove(multiKey);
    
    HibernateSession.byObjectStatic().delete(attributeAssignAction);    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#findByAttributeDefId(java.lang.String)
   */
  public Set<AttributeAssignAction> findByAttributeDefId(String attributeDefId) {
    
    Set<AttributeAssignAction> attributeAssignActions = attributeAssignActionFlashCacheRetrieveByAttributeDefId(attributeDefId, null);
    if (attributeAssignActions != null) {
      return attributeAssignActions;
    }      
    
    attributeAssignActions = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssignAction where attributeDefId = :theAttributeDefId order by nameDb")
      .setString("theAttributeDefId", attributeDefId)
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByAttributeDefId")
      .listSet(AttributeAssignAction.class);

    if (GrouperUtil.length(attributeAssignActions) > 0) {
      attributeAssignActionFlashCacheAddIfSupposedToByAttributeDef(attributeDefId, attributeAssignActions);
    }

    return attributeAssignActions;
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#findByUuidOrKey(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public AttributeAssignAction findByUuidOrKey(String id, String attributeDefId,
      String name, boolean exceptionIfNull) {
    
    if (!StringUtils.isBlank(id)) {
      Set<AttributeAssignAction> attributeAssignActions = attributeAssignActionFlashCacheRetrieveById(id, null);
      if (GrouperUtil.length(attributeAssignActions) > 0) {
        return attributeAssignActions.iterator().next();
      }
    }
    
    if (!StringUtils.isBlank(attributeDefId) && !StringUtils.isBlank(name)) {
      Set<AttributeAssignAction> attributeAssignActions = attributeAssignActionFlashCacheRetrieveById(attributeDefId, null);
      
      for (AttributeAssignAction attributeAssignAction : GrouperUtil.nonNull(attributeAssignActions)) {
        if (StringUtils.equals(name, attributeAssignAction.getName())) {
          return attributeAssignAction;
        }
      }
    }
    
    try {
      AttributeAssignAction attributeAssignAction = HibernateSession.byHqlStatic()
        .createQuery("from AttributeAssignAction as theAttributeAssignAction " +
        		"where theAttributeAssignAction.id = :theId " +
        		"or (theAttributeAssignAction.attributeDefId = :theAttributeDefId " +
        		"and theAttributeAssignAction.nameDb = :theNameDb)")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .setString("theId", id)
        .setString("theAttributeDefId", attributeDefId)
        .setString("theNameDb", name)
        .uniqueResult(AttributeAssignAction.class);
      if (attributeAssignAction == null && exceptionIfNull) {
        throw new RuntimeException("Can't find attributeAssignAction by id: '" 
            + id + "' or attributeDefId: " + attributeDefId + ", name: '" + name + "'");
      }
      if (attributeAssignAction != null) {
        attributeAssignActionFlashCacheAddIfSupposedTo(attributeAssignAction);
      }

      return attributeAssignAction;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find attributeAssignAction by id: '" 
            + id + "' or attributeDefId: " + attributeDefId 
            + ", name: '" + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#saveUpdateProperties(edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction)
   */
  public void saveUpdateProperties(AttributeAssignAction attributeAssignAction) {
    
    MultiKey multiKey = attributeAssignActionFlashCacheMultikeyId(attributeAssignAction.getId());
    attributeAssignActionFlashCache.remove(multiKey);
    
    multiKey = attributeAssignActionFlashCacheMultikeyAttributeDefId(attributeAssignAction.getAttributeDefId());
    attributeAssignActionFlashCache.remove(multiKey);

    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AttributeAssignAction " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "createdOnDb = :theCreatedOnDb, " +
        "lastUpdatedDb = :theLastUpdatedDb " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attributeAssignAction.getHibernateVersionNumber())
        .setLong("theCreatedOnDb", attributeAssignAction.getCreatedOnDb())
        .setLong("theLastUpdatedDb", attributeAssignAction.getLastUpdatedDb())
        .setString("theContextId", attributeAssignAction.getContextId())
        .setString("theId", attributeAssignAction.getId()).executeUpdate();
    
    attributeAssignActionFlashCacheAddIfSupposedTo(attributeAssignAction);
    
  }

  /**
   * get a group fom flash cache
   * @param id
   * @param queryOptions
   * @return the assignments
   */
  private static Set<AttributeAssignAction> attributeAssignActionFlashCacheRetrieveById(Object id, QueryOptions queryOptions) {
    if (attributeAssignActionFlashCacheableById(id, queryOptions)) {
      MultiKey attributeAssignActionFlashKey = attributeAssignActionFlashCacheMultikeyId(id);
      //see if its already in the cache
      Set<AttributeAssignAction> attributeAssignActions = attributeAssignActionFlashCache.get(attributeAssignActionFlashKey);
      if (attributeAssignActions != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from attributeAssignAction flash cache by id: " + id);
        }
        return attributeAssignActions;
      }
    }
    return null;
  }

  /**
   * see if this is cacheable
   * @param id
   * @param queryOptions 
   * @return if cacheable
   */
  private static boolean attributeAssignActionFlashCacheableById(Object id, QueryOptions queryOptions) {
  
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_ATTRIBUTE_ASSIGN_ACTION, true)) {
      return false;
    }
  
    if (id == null || ((id instanceof String) && StringUtils.isBlank((String)id))) {
      return false;
    }
  
    if (queryOptions != null 
        && queryOptions.getSecondLevelCache() != null && !queryOptions.getSecondLevelCache()) {
      return false;
    }
    
    return true;
  }

  /**
   * see if this is cacheable
   * @param id
   * @param queryOptions 
   * @return if cacheable
   */
  private static boolean attributeAssignActionFlashCacheableByAttributeDefId(Object id, QueryOptions queryOptions) {
  
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_ATTRIBUTE_ASSIGN_ACTION, true)) {
      return false;
    }
  
    if (id == null || ((id instanceof String) && StringUtils.isBlank((String)id))) {
      return false;
    }
  
    if (queryOptions != null 
        && queryOptions.getSecondLevelCache() != null && !queryOptions.getSecondLevelCache()) {
      return false;
    }
    
    return true;
  }

  /**
   * add group to cache if not null
   * @param attributeAssignAction
   */
  private static void attributeAssignActionFlashCacheAddIfSupposedTo(AttributeAssignAction attributeAssignAction) {
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_ATTRIBUTE_ASSIGN_ACTION, true)) {
      return;
    }
    if (attributeAssignAction != null) {
      attributeAssignActionFlashCache.put(attributeAssignActionFlashCacheMultikeyId(attributeAssignAction.getId()), GrouperUtil.toSet(attributeAssignAction));
    }
  }

  /**
   * add group to cache if not null
   * @param attributeDefId 
   * @param attributeAssignActions 
   */
  private static void attributeAssignActionFlashCacheAddIfSupposedToByAttributeDef(String attributeDefId, Set<AttributeAssignAction> attributeAssignActions) {
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_ATTRIBUTE_ASSIGN_ACTION, true)) {
      return;
    }
    
    for (AttributeAssignAction attributeAssignAction : attributeAssignActions ) {
      attributeAssignActionFlashCache.put(attributeAssignActionFlashCacheMultikeyId(attributeAssignAction.getId()), GrouperUtil.toSet(attributeAssignAction));
    }
    attributeAssignActionFlashCache.put(attributeAssignActionFlashCacheMultikeyAttributeDefId(attributeDefId), 
        attributeAssignActions);

  }

  /**
   * multikey
   * @param id
   * @return if cacheable
   */
  private static MultiKey attributeAssignActionFlashCacheMultikeyId(Object id) {
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_ATTRIBUTE_ASSIGN_ACTION, true)) {
      return null;
    }
  
    return new MultiKey("id", id);
  }

  /**
   * multikey
   * @param attribuetDefId
   * @return if cacheable
   */
  private static MultiKey attributeAssignActionFlashCacheMultikeyAttributeDefId(Object attribuetDefId) {
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_ATTRIBUTE_ASSIGN_ACTION, true)) {
      return null;
    }
  
    return new MultiKey("attribuetDefId", attribuetDefId);
  }

  /**
   * get a group fom flash cache
   * @param attributeDefId
   * @param queryOptions
   * @return the assignments
   */
  private static Set<AttributeAssignAction> attributeAssignActionFlashCacheRetrieveByAttributeDefId(Object attributeDefId, QueryOptions queryOptions) {
    if (attributeAssignActionFlashCacheableByAttributeDefId(attributeDefId, queryOptions)) {
      MultiKey attributeAssignActionFlashKey = attributeAssignActionFlashCacheMultikeyAttributeDefId(attributeDefId);
      //see if its already in the cache
      Set<AttributeAssignAction> attributeAssignActions = attributeAssignActionFlashCache.get(attributeAssignActionFlashKey);
      if (attributeAssignActions != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from attributeAssignAction flash cache by attribute def id: " + attributeDefId);
        }
        return attributeAssignActions;
      }
    }
    return null;
  }

} 

