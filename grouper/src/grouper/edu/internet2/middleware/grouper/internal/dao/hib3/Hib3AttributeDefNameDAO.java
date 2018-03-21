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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.exception.AttributeDefNameTooManyResults;
import edu.internet2.middleware.grouper.exception.AttributeNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.dao.QuerySortField;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.service.ServiceRole;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;

/**
 * Data Access Object for attribute def name
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefNameDAO.java,v 1.6 2009-11-17 02:52:29 mchyzer Exp $
 */
public class Hib3AttributeDefNameDAO extends Hib3DAO implements AttributeDefNameDAO {
  
  /**
   * 
   */
  private static final String KLASS = Hib3AttributeDefNameDAO.class.getName();

  /**
   * reset the attribute def names
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeDefName").executeUpdate();
  }

  /**
   * names and ids of attribute defs which should cache as root
   */
  private static Set<String> attributeDefNameCacheAsRootIdsAndNames = new HashSet<String>();
  /**
   * cache stuff in attributeDef by subjectSourceId, subjectId, name, uuid, idIndex
   */
  private static GrouperCache<MultiKey, AttributeDefName> attributeDefNameFlashCache = new GrouperCache(
      "edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.attributeDefNameFinderFlashCache");
  /**
   * cache stuff in attribute def names by name, uuid, idIndex
   */
  private static GrouperCache<Object, AttributeDefName> attributeDefNameRootCache = new GrouperCache(
      "edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder.attributeDefFinderCache");
  /**
   * 
   */
  private static final String GROUPER_CACHE_FIND_ATTRIBUTE_DEF_NAME_ROOT_BY_CACHE = "grouperCache.find.attributeDefNameRootByCache";
  /**
   * 
   */
  private static final String GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_NAME_CACHE = "grouperFlashCache.find.attributeDefNameCache";
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3AttributeDefNameDAO.class);

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByIdSecure(java.lang.String, boolean)
   */
  public AttributeDefName findByIdSecure(String id, boolean exceptionIfNotFound) {
    return findByIdSecure(id, exceptionIfNotFound, null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByIdSecure(java.lang.String, boolean, QueryOptions)
   */
  public AttributeDefName findByIdSecure(String id, boolean exceptionIfNotFound, QueryOptions queryOptions) {
    
    AttributeDefName attributeDefName = attributeDefNameCacheAsRootRetrieve(id, queryOptions);
    if (attributeDefName != null) {
      return attributeDefName;
    }      
    
    attributeDefName = attributeDefNameFlashCacheRetrieve(id, queryOptions);
    if (attributeDefName != null) {
      return attributeDefName;
    }      

    attributeDefName = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDefName where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDefName.class);
    
    attributeDefNameCacheAsRootAddIfSupposedTo(attributeDefName);
    attributeDefNameFlashCacheAddIfSupposedToAsRoot(attributeDefName);

    attributeDefName = filterSecurity(attributeDefName);
      
    if (attributeDefName != null) {
      attributeDefNameFlashCacheAddIfSupposedTo(attributeDefName);
      
      return attributeDefName;
    }
    LOG.info("AttributeDefName not found: " + id);
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new AttributeDefNameNotFoundException("Cannot find (or not allowed to find) attribute def name with id: '" + id + "'");

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findById(java.lang.String, boolean)
   */
  public AttributeDefName findById(String id, boolean exceptionIfNotFound) {
    
    AttributeDefName attributeDefName = attributeDefNameCacheAsRootRetrieve(id, null);
    if (attributeDefName != null) {
      return attributeDefName;
    }      
    
    attributeDefName = attributeDefNameFlashCacheAsRootRetrieve(id, null);
    if (attributeDefName != null) {
      return attributeDefName;
    }      

    attributeDefName = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDefName where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDefName.class);
    
    attributeDefNameCacheAsRootAddIfSupposedTo(attributeDefName);
    attributeDefNameFlashCacheAddIfSupposedToAsRoot(attributeDefName);

    if (attributeDefName != null) {
      return attributeDefName;
    }
    LOG.info("AttributeDefName not found: " + id);
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new AttributeDefNameNotFoundException("Cannot find (or not allowed to find) attribute def name with id: '" + id + "'");

    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  public void saveOrUpdate(AttributeDefName attributeDefName) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDefName);
    attributeDefNameFlashCache.clear();
    attributeDefNameRootCache.clear();
  }
  /**
   * make sure grouper session can view the attribute def Name
   * @param attributeDefNames
   * @return the set of attribute def Names
   */
  static Set<AttributeDefName> filterSecurity(Set<AttributeDefName> attributeDefNames) {
    Set<AttributeDefName> result = new LinkedHashSet<AttributeDefName>();
    if (attributeDefNames != null) {
      for (AttributeDefName attributeDefName : attributeDefNames) {
        attributeDefName = filterSecurity(attributeDefName);
        if (attributeDefName != null) {
          result.add(attributeDefName);
        }
      }
    }
    return result;
  }
  
  /**
   * make sure grouper session can view the attribute def Name
   * @param attributeDefName
   * @return the attributeDefName or null
   */
  static AttributeDefName filterSecurity(AttributeDefName attributeDefName) {
    if (attributeDefName == null) {
      return null;
    }
    
    AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefName.getAttributeDefId(), false);
    return attributeDef == null ? null : attributeDefName;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByNameSecure(java.lang.String, boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public AttributeDefName findByNameSecure(String name, boolean exceptionIfNotFound,
      QueryOptions queryOptions) throws GrouperDAOException,
      AttributeDefNameNotFoundException {
    
    AttributeDefName attributeDefName = attributeDefNameCacheAsRootRetrieve(name, queryOptions);
    if (attributeDefName != null) {
      return attributeDefName;
    }      
    
    attributeDefName = attributeDefNameFlashCacheRetrieve(name, queryOptions);
    if (attributeDefName != null) {
      return attributeDefName;
    }      

    attributeDefName = findByName(name, exceptionIfNotFound, queryOptions);
    
    attributeDefNameCacheAsRootAddIfSupposedTo(attributeDefName);
    attributeDefNameFlashCacheAddIfSupposedToAsRoot(attributeDefName);

    attributeDefName = filterSecurity(attributeDefName);
      
    if (attributeDefName != null) {
      attributeDefNameFlashCacheAddIfSupposedTo(attributeDefName);
      
      return attributeDefName;
    }
    LOG.info("AttributeDefName not found: " + name);
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new AttributeDefNameNotFoundException("Cannot find (or not allowed to find) attribute def name with name: '" + name + "'");

  }


  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByNameSecure(java.lang.String, boolean)
   */
  public AttributeDefName findByNameSecure(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, AttributeDefNameNotFoundException {
    return findByNameSecure(name, exceptionIfNotFound, null);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#delete(AttributeDefName)
   */
  public void delete(final AttributeDefName attributeDefName) {

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            //set parent to null so mysql doest get mad
            //http://bugs.mysql.com/bug.php?id=15746
            // delete group sets
            GrouperDAOFactory.getFactory().getAttributeDefNameSet().deleteByIfHasAttributeDefName(attributeDefName);
            hibernateHandlerBean.getHibernateSession().byObject().delete(attributeDefName);
            return null;

          }
      
    });

  }
  

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByStem(java.lang.String)
   */
  public Set<AttributeDefName> findByStem(String id) {
    Set<AttributeDefName> attributeDefNames = HibernateSession.byHqlStatic()
        .createQuery("from AttributeDefName where stemId = :id order by nameDb")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByStem")
        .setString("id", id)
        .listSet(AttributeDefName.class);
    
    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
      attributeDefNameCacheAsRootAddIfSupposedTo(attributeDefName);
      attributeDefNameFlashCacheAddIfSupposedToAsRoot(attributeDefName);
    }
    
    return attributeDefNames;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByUuidOrName(java.lang.String, java.lang.String, boolean)
   */
  public AttributeDefName findByUuidOrName(String id, String name,
      boolean exceptionIfNotFound) {
    return findByUuidOrName(id, name, exceptionIfNotFound, null);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByUuidOrName(java.lang.String, java.lang.String, boolean, QueryOptions)
   */
  public AttributeDefName findByUuidOrName(String id, String name,
      boolean exceptionIfNotFound, QueryOptions queryOptions) {

    AttributeDefName attributeDefName = null;
    
    if (!StringUtils.isBlank(id)) {
      attributeDefName = attributeDefNameCacheAsRootRetrieve(id, queryOptions);
      if (attributeDefName != null) {
        return attributeDefName;
      }      
      
      attributeDefName = attributeDefNameFlashCacheAsRootRetrieve(id, queryOptions);
      if (attributeDefName != null) {
        return attributeDefName;
      }      
    }

    if (!StringUtils.isBlank(name)) {
      attributeDefName = attributeDefNameCacheAsRootRetrieve(name, queryOptions);
      if (attributeDefName != null) {
        return attributeDefName;
      }      
      
      attributeDefName = attributeDefNameFlashCacheAsRootRetrieve(name, queryOptions);
      if (attributeDefName != null) {
        return attributeDefName;
      }      
    }

    attributeDefName = HibernateSession.byHqlStatic()
        .createQuery("from AttributeDefName as theAttributeDefName where theAttributeDefName.id = :theId or theAttributeDefName.nameDb = :theName")
        .setCacheable(true)
        .options(queryOptions)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .setString("theId", id)
        .setString("theName", name)
        .uniqueResult(AttributeDefName.class);

    attributeDefNameCacheAsRootAddIfSupposedTo(attributeDefName);
    attributeDefNameFlashCacheAddIfSupposedToAsRoot(attributeDefName);

    try {
      if (attributeDefName == null && exceptionIfNotFound) {
        throw new AttributeDefNameNotFoundException("Can't find attributeDefName by id: '" + id + "' or name '" + name + "'");
      }
      return attributeDefName;
    }
    catch (GrouperDAOException e) {
      String error = "Problem finding attributeDefName by id: '" 
        + id + "' or name '" + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#saveUpdateProperties(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  public void saveUpdateProperties(AttributeDefName attributeDefName) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AttributeDefName " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "lastUpdatedDb = :theLastUpdatedDb, " +
        "createdOnDb = :theCreatedOnDb " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attributeDefName.getHibernateVersionNumber())
        .setLong("theCreatedOnDb", attributeDefName.getCreatedOnDb())
        .setLong("theLastUpdatedDb", attributeDefName.getLastUpdatedDb())
        .setString("theContextId", attributeDefName.getContextId())
        .setString("theId", attributeDefName.getId()).executeUpdate();
    attributeDefNameFlashCache.clear();
    attributeDefNameRootCache.clear();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByAttributeDef(java.lang.String)
   */
  public Set<AttributeDefName> findByAttributeDef(String id) {
    Set<AttributeDefName> attributeDefNames = HibernateSession.byHqlStatic()
      .createQuery("from AttributeDefName where attributeDefId = :id order by name")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByAttributeDef")
      .setString("id", id)
      .listSet(AttributeDefName.class);
  
    return attributeDefNames;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findAllSecure(java.lang.String, java.util.Set, QueryOptions)
   */
  public Set<AttributeDefName> findAllSecure(String searchField,
      Set<String> searchInAttributeDefIds, QueryOptions queryOptions) {

    {
      String searchFieldNoPercents = StringUtils.replace(StringUtils.defaultString(searchField), "%", "");
      
      if (StringUtils.isBlank(searchFieldNoPercents) || searchFieldNoPercents.length() < 2) {
        throw new RuntimeException("Need to pass in a searchField of at least 2 chars");
      }
    }
    
    String searchFieldLower = StringUtils.defaultString(searchField).toLowerCase();

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();
    
    StringBuilder sqlTables = new StringBuilder("from AttributeDefName as attributeDefName ");
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
    StringBuilder sqlWhereClause =  new StringBuilder(" (lower(attributeDefName.extensionDb) like :searchField " +
      "or lower(attributeDefName.displayExtensionDb) like :searchField " +
      "or lower(attributeDefName.description) like :searchField) ");

    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
        grouperSessionSubject, byHqlStatic, 
        sqlTables, sqlWhereClause, "attributeDefName.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);

    StringBuilder sql;
    sql = sqlTables.append(" where ").append(sqlWhereClause);
    
    if (GrouperUtil.length(searchInAttributeDefIds) > 0) {
      sql.append(" and attributeDefName.attributeDefId in (");
      sql.append(HibUtils.convertToInClause(searchInAttributeDefIds, byHqlStatic));
      sql.append(") ");
    }
    
    Set<AttributeDefName> attributeDefNames = byHqlStatic
      .createQuery(sql.toString()).options(queryOptions)
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindAll")
      .setString("searchField", searchFieldLower)
      .listSet(AttributeDefName.class);

    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
      attributeDefNameCacheAsRootAddIfSupposedTo(attributeDefName);
      attributeDefNameFlashCacheAddIfSupposedTo(attributeDefName);
      attributeDefNameFlashCacheAddIfSupposedToAsRoot(attributeDefName);
    }

    int maxSize = GrouperConfig.retrieveConfig().propertyValueInt("findAllAttributeDefNames.maxResultSize", 30000);
    if (maxSize > -1) {
      if (maxSize < attributeDefNames.size()) {
        throw new AttributeDefNameTooManyResults("Too many results: " 
            + attributeDefNames.size() + ", '" + searchField + "'");
      }
    }
    
    return attributeDefNames;

  }

  /**
   * @see AttributeDefNameDAO#findByAttributeDefLike(String, String)
   */
  public Set<AttributeDefName> findByAttributeDefLike(String attributeDefId,
      String likeString) {
    
    //if all
    if (StringUtils.equals(likeString, "%")) {
      return findByAttributeDef(attributeDefId);
    }

    //if some
    Set<AttributeDefName> attributeDefNames = HibernateSession.byHqlStatic()
      .createQuery("from AttributeDefName where attributeDefId = :id and name like :likeString")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByAttributeDefLike")
      .setString("id", attributeDefId)
      .setString("likeString", likeString)
      .listSet(AttributeDefName.class);
  
    return attributeDefNames;
  }

  /**
   * if there are sort fields, go through them, and replace name with nameDb, etc,
   * extension for extensionDb, displayName with displayNameDb, and displayExtension with displayExtensionDb
   * @param querySort
   */
  private static void massageSortFields(QuerySort querySort) {
    if (querySort == null) {
      return;
    }
    for (QuerySortField querySortField : GrouperUtil.nonNull(querySort.getQuerySortFields())) {
      if (StringUtils.equals("extension", querySortField.getColumn())) {
        querySortField.setColumn("theAttributeDefName.extensionDb");
      }
      if (StringUtils.equals("name", querySortField.getColumn())) {
        querySortField.setColumn("theAttributeDefName.nameDb");
      }
      if (StringUtils.equals("displayExtension", querySortField.getColumn())
          || StringUtils.equals("display_extension", querySortField.getColumn())) {
        querySortField.setColumn("theAttributeDefName.displayExtensionDb");
      }
      if (StringUtils.equals("displayName", querySortField.getColumn())
          || StringUtils.equals("display_name", querySortField.getColumn())) {
        querySortField.setColumn("theAttributeDefName.displayNameDb");
      }
      if (StringUtils.equals("description", querySortField.getColumn())) {
        querySortField.setColumn("theAttributeDefName.description");
      }
    }

  }
  
  /**
   * 
   * @param scope 
   * @param grouperSession 
   * @param attributeDefId 
   * @param subject 
   * @param privileges if privileges is null and filterByServicesCanView is true then set the privileges to the view set 
   * @param queryOptions 
   * @param splitScope 
   * @param attributeAssignType
   * @param attributeDefType 
   * @param serviceRole if filtering by services the user has in a certain role
   * @param anyServiceRole true if services should be returned where the user has any role
   * @param parentStemId stem id of the parent of ancestor of this object
   * @param stemScope is SUB or ONE
   * @param findByUuidOrName 
   * @param idsOfAttributeDefNames 
   * @return  attribute def names
   * 
   */
  private Set<AttributeDefName> findAllAttributeNamesSecureHelper(String scope,
      GrouperSession grouperSession, String attributeDefId, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, boolean splitScope, AttributeAssignType attributeAssignType,
      AttributeDefType attributeDefType, ServiceRole serviceRole, boolean anyServiceRole, String parentStemId,
      Scope stemScope, boolean findByUuidOrName, Set<String> idsOfAttributeDefNames) {
    Set<AttributeDefName> attributeDefNames = findAllAttributeNamesSecureHelper2(scope,
        grouperSession, attributeDefId, subject, privileges,
        queryOptions, splitScope, attributeAssignType,
        attributeDefType, serviceRole, anyServiceRole, parentStemId,
        stemScope, findByUuidOrName, idsOfAttributeDefNames);
    
    //get all the attribute defs so that it is more efficient and same security settings
    Set<String> attributeDefIds = new LinkedHashSet<String>();
    
    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
      attributeDefIds.add(attributeDefName.getAttributeDefId());
    }
    
    //note, security has already been checked
    Set<AttributeDef> attributeDefs = new AttributeDefFinder().assignAttributeDefIds(attributeDefIds).findAttributes();
    
    Map<String, AttributeDef> mapAttributeDefIdToAttributeDef = new HashMap<String, AttributeDef>();
    
    for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefs)) {
      mapAttributeDefIdToAttributeDef.put(attributeDef.getId(), attributeDef);
    }

    for (AttributeDefName attributeDefName : attributeDefNames) {
      AttributeDef attributeDef = mapAttributeDefIdToAttributeDef.get(attributeDefName.getAttributeDefId());
      attributeDefName.internalSetAttributeDef(attributeDef);
    }

    return attributeDefNames;
  }

  /**
   * 
   * @param scope 
   * @param grouperSession 
   * @param attributeDefId 
   * @param subject 
   * @param privileges if privileges is null and filterByServicesCanView is true then set the privileges to the view set 
   * @param queryOptions 
   * @param splitScope 
   * @param attributeAssignType
   * @param attributeDefType 
   * @param serviceRole if filtering by services the user has in a certain role
   * @param anyServiceRole true if services should be returned where the user has any role
   * @param parentStemId stem id of the parent of ancestor of this object
   * @param stemScope is SUB or ONE
   * @param findByUuidOrName 
   * @param idsOfAttributeDefNames 
   * @return  attribute def names
   * 
   */
  private Set<AttributeDefName> findAllAttributeNamesSecureHelper2(String scope,
      GrouperSession grouperSession, String attributeDefId, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, boolean splitScope, AttributeAssignType attributeAssignType,
      AttributeDefType attributeDefType, ServiceRole serviceRole, boolean anyServiceRole, String parentStemId,
      Scope stemScope, boolean findByUuidOrName, Set<String> idsOfAttributeDefNames) {

    //lets page through these
    int pages = GrouperUtil.batchNumberOfBatches(idsOfAttributeDefNames, 100);

    List<String> idsOfAttributeDefNamesList = GrouperUtil.listFromCollection(idsOfAttributeDefNames);
    
    Set<AttributeDefName> overallResult = new LinkedHashSet<AttributeDefName>();
    
    for (int i=0; i<pages; i++) {
      List<String> idsOfAttributeDefNamesListBatch = GrouperUtil.batchList(idsOfAttributeDefNamesList, 100, i);

      if (queryOptions == null) {
        queryOptions = new QueryOptions();
      }
      if (queryOptions.getQuerySort() == null) {
        queryOptions.sortAsc("theAttributeDefName.displayNameDb");
      }
    
      Member member = subject == null ? null : MemberFinder.findBySubject(grouperSession, subject, true);
  
      StringBuilder sql = new StringBuilder(
          "select distinct theAttributeDefName from AttributeDefName theAttributeDefName, AttributeDef theAttributeDef ");
  
      if (serviceRole != null || anyServiceRole) {
        
        if (attributeDefType != null && attributeDefType != AttributeDefType.service) {
          throw new RuntimeException("You cant look for services and not have AttributeDefType of service: " + attributeDefType);
        }
        //CH not sure this is needed
        //attributeDefType = AttributeDefType.service;
        sql.append(", ServiceRoleView theServiceRoleView ");
      }
  
      if (!StringUtils.isBlank(parentStemId) || stemScope != null) {
        
        if (StringUtils.isBlank(parentStemId) || stemScope == null) {
          throw new RuntimeException("If you are passing in a parentStemId or a stemScope, then you need to pass both of them: " + parentStemId + ", " + stemScope);
        }
        
        if (stemScope == Scope.SUB) {
          sql.append(", StemSet theStemSet ");
        }
      }      
  
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
      StringBuilder whereClause = new StringBuilder(" theAttributeDefName.attributeDefId = theAttributeDef.id ");
  
      if (serviceRole != null || anyServiceRole) {
        if (attributeDefType == null) {
          attributeDefType = AttributeDefType.service;
        }
        if (attributeDefType != AttributeDefType.service) {
          throw new RuntimeException("Why are you filtering by serviceRole: " + serviceRole + " but you have the " +
          		"attributeDefType not equal to AttributeDefType.service????  (or can be null): " + attributeDefType);
        }
        if (attributeAssignType == null) {
          attributeAssignType = AttributeAssignType.stem;
        }
        if (attributeAssignType != AttributeAssignType.stem) {
          throw new RuntimeException("Why are you filtering by serviceRole: " + serviceRole + " but you have the " +
              "attributeAssignType not equal to AttributeAssignType.stem????  (or can be null): " + attributeAssignType);
        }
        
      }
      
      if (attributeDefType != null) {
        whereClause.append(" and ");
        whereClause.append(" theAttributeDef.attributeDefTypeDb = :theAttributeDefType ");
        byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
      }
  
      if (!StringUtils.isBlank(parentStemId) || stemScope != null) {
        switch(stemScope) {
          case ONE:
            
            whereClause.append(" and theAttributeDefName.stemId = :theStemId ");
            byHqlStatic.setString("theStemId", parentStemId);
            break;
          case SUB:
            
            whereClause.append(" and theAttributeDefName.stemId = theStemSet.ifHasStemId " +
            		" and theStemSet.thenHasStemId = :theStemId ");
            byHqlStatic.setString("theStemId", parentStemId);
            
            break;
          
        }
      }
  
      if (GrouperUtil.length(idsOfAttributeDefNamesListBatch) > 0) {
        
        whereClause.append(" and ");
        whereClause.append(" theAttributeDefName.id in (");

        //add all the uuids
        byHqlStatic.setCollectionInClause(whereClause, idsOfAttributeDefNamesListBatch);
        whereClause.append(")");
        
      }
      
      if (!StringUtils.isBlank(attributeDefId)) {
        
        whereClause.append(" and ");
        whereClause.append(" theAttributeDefName.attributeDefId = :theAttributeDefId ");
        byHqlStatic.setString("theAttributeDefId", attributeDefId);
        
      }
  
      if (attributeAssignType != null) {
        whereClause.append(" and ");
        switch (attributeAssignType) {
          case any_mem:
            whereClause.append(" theAttributeDef.assignToEffMembershipDb = 'T' ");
            break;
          case any_mem_asgn:
            whereClause.append(" theAttributeDef.assignToEffMembershipAssnDb = 'T' ");
            break;
          case attr_def:
            whereClause.append(" theAttributeDef.assignToAttributeDefDb = 'T' ");
            break;
          case attr_def_asgn:
            whereClause.append(" theAttributeDef.assignToAttributeDefAssnDb = 'T' ");
            break;
          case group:
            whereClause.append(" theAttributeDef.assignToGroupDb = 'T' ");
            break;
          case group_asgn:
            whereClause.append(" theAttributeDef.assignToGroupAssnDb = 'T' ");
            break;
          case imm_mem:
            whereClause.append(" theAttributeDef.assignToImmMembershipDb = 'T' ");
            break;
          case imm_mem_asgn:
            whereClause.append(" theAttributeDef.assignToImmMembershipAssnDb = 'T' ");
            break;
          case member:
            whereClause.append(" theAttributeDef.assignToMemberDb = 'T' ");
            break;
          case mem_asgn:
            whereClause.append(" theAttributeDef.assignToMemberAssnDb = 'T' ");
            break;
          case stem:
            whereClause.append(" theAttributeDef.assignToStemDb = 'T' ");
            break;
          case stem_asgn:
            whereClause.append(" theAttributeDef.assignToStemAssnDb = 'T' ");
            break;
          default:
            throw new RuntimeException("Not expecting attribute assign type: " + attributeAssignType);
        }
      }
      
  
      //see if there is a scope
      if (!StringUtils.isBlank(scope)) {
        scope = scope.toLowerCase();
  
        String[] scopes = splitScope ? GrouperUtil.splitTrim(scope, " ") : new String[]{scope};
  
        whereClause.append(" and ");
  
        if (GrouperUtil.length(scopes) == 1) {
          whereClause.append(" ( theAttributeDefName.id = :theAttributeDefNameIdScope or ( ");
          byHqlStatic.setString("theAttributeDefNameIdScope", scope);
        } else {
          whereClause.append(" ( ( ");
        }
  
        int index = 0;
        for (String theScope : scopes) {
          if (index != 0) {
            whereClause.append(" and ");
          }
  
          if (findByUuidOrName) {
            whereClause.append(" ( theAttributeDefName.nameDb = :scope" + index + " or theAttributeDefName.alternateNameDb = :scope" + index 
                + " or theAttributeDefName.displayNameDb = :scope" + index + " ) ");
            byHqlStatic.setString("scope" + index, theScope);
          } else {
            whereClause.append(" ( lower(theAttributeDefName.nameDb) like :scope" + index 
                + " or lower(theAttributeDefName.displayNameDb) like :scope" + index 
                + " or lower(theAttributeDefName.description) like :scope" + index + " ) ");
            if (splitScope) {
              theScope = "%" + theScope + "%";
            } else if (!theScope.endsWith("%")) {
              theScope += "%";
            }
            byHqlStatic.setString("scope" + index, theScope.toLowerCase());
  
          }        
  
          index++;
        }
        whereClause.append(" ) ) ");
      }
    
      boolean changedQuery = false;
      
      if (serviceRole != null || anyServiceRole) {
        
        if (serviceRole != null && anyServiceRole) {
          throw new RuntimeException("Cant set serviceRole and anyServiceRole, they are mutually exclusive");
        }
        
        if (member == null) {
          throw new RuntimeException("If filtering by serviceRole: " + serviceRole + ", then pass in a subject who can see the services");
        }
        
        //must be a service type
        whereClause.append(" and theAttributeDefName.id = theServiceRoleView.serviceNameId ");
  
        changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
            grouperSession.getSubject(), byHqlStatic, 
            sql, "theServiceRoleView.groupId", AccessPrivilege.READ_PRIVILEGES);
        
        //fields for the service role
        HibUtils.convertFieldsToSqlInString(
            anyServiceRole ? ServiceRole.allFieldsForGroupQuery() : serviceRole.fieldsForGroupQuery(), 
            byHqlStatic, whereClause, "theServiceRoleView.fieldId");
  
        whereClause.append(" and theServiceRoleView.memberId = :groupMemberId ");
        byHqlStatic.setString("groupMemberId", member.getUuid());
  
        //no need to do security for services, right????
        
      } else {
  
        //see if we are adding more to the query
        if (GrouperUtil.length(privileges) > 0) {
          
          if (subject == null) {
            subject = grouperSession.getSubject();
          }
          
          grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(subject, byHqlStatic,
              sql, whereClause, "theAttributeDefName.attributeDefId", privileges);
        }
      }
      
      if (changedQuery) {
        sql.append(" and ");      
      } else {
        sql.append(" where ");
      }
      
      sql.append(whereClause);
      
      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }
  
      Set<AttributeDefName> attributeDefNames = byHqlStatic.createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllAttributeDefNamesSecure")
        .options(queryOptions)
        .listSet(AttributeDefName.class);
      
      //if find by uuid or name, try to narrow down to one...
      if (findByUuidOrName) {
        
        //get the one with uuid
        for (AttributeDefName attributeDefName : attributeDefNames) {
          if (StringUtils.equals(scope, attributeDefName.getId())) {
            return GrouperUtil.toSet(attributeDefName);
          }
        }
        
        //get the one with name
        for (AttributeDefName attributeDefName : attributeDefNames) {
          if (StringUtils.equals(scope, attributeDefName.getName())) {
            return GrouperUtil.toSet(attributeDefName);
          }
        }
      }
      overallResult.addAll(GrouperUtil.nonNull(attributeDefNames));
    }
    return overallResult;
  
  }
  
  /**
   * @see AttributeDefNameDAO#findAllAttributeNamesSecure(String, boolean, GrouperSession, String, Subject, Set, QueryOptions, AttributeAssignType, AttributeDefType)
   */
  @Override
  public Set<AttributeDefName> findAllAttributeNamesSecure(String scope,
      boolean splitScope, GrouperSession grouperSession, String attributeDefId,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions,
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType) {
    return findAllAttributeNamesSecureHelper(scope, grouperSession, attributeDefId, 
        subject, privileges, queryOptions, splitScope, attributeAssignType, attributeDefType,
        null, false, null, null, false, null);
  }

  /**
   * not a secure method, find by id index
   */
  @Override
  public AttributeDefName findByIdIndex(Long idIndex, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws AttributeDefNameNotFoundException {
    
    AttributeDefName attributeDefName = attributeDefNameCacheAsRootRetrieve(idIndex, queryOptions);
    if (attributeDefName != null) {
      return attributeDefName;
    }      
    
    attributeDefName = attributeDefNameFlashCacheAsRootRetrieve(idIndex, queryOptions);
    if (attributeDefName != null) {
      return attributeDefName;
    }      

    StringBuilder hql = new StringBuilder("select theAttributeDefName from AttributeDefName as theAttributeDefName where (theAttributeDefName.idIndex = :theIdIndex)");
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .setCacheable(true).setCacheRegion(KLASS + ".FindByIdIndex");
    
    byHqlStatic.createQuery(hql.toString());
    
    attributeDefName = byHqlStatic.setLong("theIdIndex", idIndex).uniqueResult(AttributeDefName.class);
    
    attributeDefNameCacheAsRootAddIfSupposedTo(attributeDefName);
    attributeDefNameFlashCacheAddIfSupposedToAsRoot(attributeDefName);

    if (attributeDefName != null) {
      
      return attributeDefName;
    }
    LOG.info("AttributeDefName not found: " + idIndex);
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new AttributeDefNameNotFoundException("Cannot find (or not allowed to find) attribute def name with idIndex: '" + idIndex + "'");

  }


  /**
   * secure method, find by id index
   */
  @Override
  public AttributeDefName findByIdIndexSecure(Long idIndex, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws AttributeDefNameNotFoundException {
    
    AttributeDefName attributeDefName = attributeDefNameCacheAsRootRetrieve(idIndex, queryOptions);
    if (attributeDefName != null) {
      return attributeDefName;
    }      
    
    attributeDefName = attributeDefNameFlashCacheRetrieve(idIndex, queryOptions);
    if (attributeDefName != null) {
      return attributeDefName;
    }      

    attributeDefName = findByIdIndex(idIndex, exceptionIfNotFound, queryOptions);

    attributeDefNameCacheAsRootAddIfSupposedTo(attributeDefName);
    attributeDefNameFlashCacheAddIfSupposedToAsRoot(attributeDefName);

    attributeDefName = filterSecurity(attributeDefName);
    
      
    if (attributeDefName != null) {
      attributeDefNameFlashCacheAddIfSupposedTo(attributeDefName);
      
      return attributeDefName;
    }
    LOG.info("AttributeDefName not found: " + idIndex);
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new AttributeDefNameNotFoundException("Cannot find (or not allowed to find) attribute def name with idIndex: '" + idIndex + "'");
    
  }

  /**
   * @see AttributeDefNameDAO#findAllAttributeNamesSecure(String, boolean, GrouperSession, String, Subject, Set, QueryOptions, AttributeAssignType, AttributeDefType, ServiceRole, boolean)
   */
  @Override
  public Set<AttributeDefName> findAllAttributeNamesSecure(String scope,
      boolean splitScope, GrouperSession grouperSession, String attributeDefId,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions,
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType,
      ServiceRole serviceRole, boolean anyServiceRole) {
    return findAllAttributeNamesSecureHelper(scope, grouperSession, attributeDefId, subject, 
        privileges, queryOptions, true, attributeAssignType, attributeDefType, serviceRole, anyServiceRole, null, null, false, null);
  }

  /**
   * @see AttributeDefNameDAO#findAllAttributeNamesSplitScopeSecure(String, GrouperSession, String, Subject, Set, QueryOptions, AttributeAssignType, AttributeDefType)
   */
  @Override
  public Set<AttributeDefName> findAllAttributeNamesSplitScopeSecure(String scope,
      GrouperSession grouperSession, String attributeDefId, Subject subject,
      Set<Privilege> privileges, QueryOptions queryOptions,
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType) {
    return findAllAttributeNamesSecureHelper(scope, grouperSession, attributeDefId, subject, 
        privileges, queryOptions, true, attributeAssignType, attributeDefType, null, false, null, null, false, null);
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findLegacyAttributeByName(java.lang.String, boolean)
   */
  public AttributeDefName findLegacyAttributeByName(String name, boolean exceptionIfNull) {
    if (StringUtils.isBlank(name)) {
      throw new RuntimeException("name cant be blank");
    }
                  
    String sql = "select name, def " +
        "from AttributeDefName as name, " +
        "AttributeDef as def " +
        "where name.attributeDefId = def.id and " +
        "name.nameDb = :name";
    
    String stemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
    String attributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");

    String nameOfAttributeDef = stemName + ":" + attributePrefix + name;
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(nameOfAttributeDef, false);
    
    // why would this not be found?
    if (attributeDefName == null) {
      Object[] row = HibernateSession.byHqlStatic().createQuery(sql)
        .setString("name", nameOfAttributeDef)
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindLegacyAttributeByName")
        .uniqueResult(Object[].class);
          
      if (row == null) {
        if (exceptionIfNull) {
          throw new AttributeNotFoundException("Unable to find legacy attribute: " + name);
        }
        
        return null;
      }
      
      attributeDefName = (AttributeDefName)row[0];
      AttributeDef attributeDef = (AttributeDef)row[1];
        
      attributeDefName.internalSetAttributeDef(attributeDef);
    } else {
      AttributeDef attributeDef = AttributeDefFinder.findByIdAsRoot(attributeDefName.getAttributeDefId(), true);
      attributeDefName.internalSetAttributeDef(attributeDef);
    }

    //types should be cached for 5 minutes
    Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesAdd(attributeDefName.getAttributeDef());
    
    return attributeDefName;
  }

  /**
   * @see AttributeDefNameDAO#findByIdsSecure(Collection, QueryOptions)
   */
  @Override
  public Set<AttributeDefName> findByIdsSecure(Collection<String> ids,
      QueryOptions queryOptions) {
    
    Set<AttributeDefName> attributeDefNames = new HashSet<AttributeDefName>();
    
    if (GrouperUtil.length(ids) == 0) {
      return attributeDefNames;
    }
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(ids, 180);
    
    List<String> idsList = GrouperUtil.listFromCollection(ids);
    
    for (int i=0;i<numberOfBatches;i++) {
      
      List<String> uuidsBatch = GrouperUtil.batchList(idsList, 180, i);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

      StringBuilder sql = new StringBuilder("select distinct theAttributeDefName from AttributeDef as theAttributeDef, " +
      		"AttributeDefName as theAttributeDefName ");
      
      StringBuilder whereClause = new StringBuilder();
      
      //see if we are adding more to the query
      grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
          grouperSession.getSubject(), byHqlStatic, 
          sql, whereClause, "theAttributeDef.id", AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES);

      sql.append(" where ").append(whereClause);
      
      if (whereClause.length() > 0) {
        sql.append(" and ");
      }
      
      sql.append(" theAttributeDef.id = theAttributeDefName.attributeDefId and ");
      
      sql.append(" theAttributeDefName.id in ( ");
      
      sql.append(HibUtils.convertToInClause(uuidsBatch, byHqlStatic)).append(" ) ");
      
      byHqlStatic
        .createQuery(sql.toString())
        .setCacheable(true)
        .options(queryOptions)
        .setCacheRegion(KLASS + ".FindByUuidsSecure");
      
      Set<AttributeDefName> attributeDefNamesBatch = byHqlStatic.listSet(AttributeDefName.class);
      
      attributeDefNames.addAll(GrouperUtil.nonNull(attributeDefNamesBatch));
      
    }
    for (AttributeDefName attributeDefName : GrouperUtil.nonNull(attributeDefNames)) {
      
      attributeDefNameCacheAsRootAddIfSupposedTo(attributeDefName);
      attributeDefNameFlashCacheAddIfSupposedToAsRoot(attributeDefName);
      attributeDefNameFlashCacheAddIfSupposedTo(attributeDefName);
      
    }
    return attributeDefNames;
  }

  /**
   * @see AttributeDefNameDAO#findAllAttributeNamesSecure(String, boolean, GrouperSession, String, Subject, Set, QueryOptions, AttributeAssignType, AttributeDefType, ServiceRole, boolean, String, Scope, boolean)
   */
  @Override
  public Set<AttributeDefName> findAllAttributeNamesSecure(String scope,
      boolean splitScope, GrouperSession grouperSession, String attributeDefId,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions,
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType,
      ServiceRole serviceRole, boolean anyServiceRole, String parentStemId,
      Scope stemScope, boolean findByUuidOrName) {
    return findAllAttributeNamesSecureHelper(scope, grouperSession, attributeDefId, 
        subject, privileges, queryOptions, splitScope, attributeAssignType, attributeDefType,
        serviceRole, anyServiceRole, parentStemId, stemScope, findByUuidOrName, null);
  
  }

  /**
   * @see AttributeDefNameDAO#findAllAttributeNamesSecure(String, boolean, GrouperSession, String, Subject, Set, QueryOptions, AttributeAssignType, AttributeDefType, ServiceRole, boolean, String, Scope, boolean, Set)
   */
  @Override
  public Set<AttributeDefName> findAllAttributeNamesSecure(String scope,
      boolean splitScope, GrouperSession grouperSession, String attributeDefId,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions,
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType,
      ServiceRole serviceRole, boolean anyServiceRole, String parentStemId,
      Scope stemScope, boolean findByUuidOrName, Set<String> idsOfAttributeDefNames) {
    return findAllAttributeNamesSecureHelper(scope, grouperSession, attributeDefId, 
        subject, privileges, queryOptions, splitScope, attributeAssignType, attributeDefType,
        serviceRole, anyServiceRole, parentStemId, stemScope, findByUuidOrName, idsOfAttributeDefNames);
  
  }

  /**
   * add attributeDef to cache if not null
   * @param attributeDefName
   */
  private static void attributeDefNameFlashCacheAddIfSupposedToAsRoot(AttributeDefName attributeDefName) {
    if (attributeDefName == null || !GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_NAME_CACHE, true)) {
      return;
    }
    
    for (Object id : new Object[]{attributeDefName.getUuid(), attributeDefName.getName(), attributeDefName.getIdIndex()}) {
      MultiKey multiKey = attributeDefNameFlashCacheMultikeyAsRoot(id);
      attributeDefNameFlashCache.put(multiKey, attributeDefName);
    }
  }

  /**
   * get a attributeDefName from flash root cache
   * @param id
   * @param queryOptions
   * @return the attributeDef or null
   */
  private static AttributeDefName attributeDefNameFlashCacheAsRootRetrieve(Object id, QueryOptions queryOptions) {
  
    if (attributeDefNameFlashCacheable(id, queryOptions)) {
      MultiKey flashCacheMultiKey = attributeDefNameFlashCacheMultikeyAsRoot(id);
      AttributeDefName attributeDefName = attributeDefNameFlashCache.get(flashCacheMultiKey);
      if (attributeDefName != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from attributeDefName flash root cache by id: " + id);
        }
        return attributeDefName;
      }
    }
    return null;
  }

  /**
   * multikey
   * @param id
   * @return if cacheable
   */
  private static MultiKey attributeDefNameFlashCacheMultikeyAsRoot(Object id) {
    
    if (id == null || !GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_NAME_CACHE, true)) {
      return null;
    }
    Subject rootSubject = SubjectFinder.findRootSubject();
    return new MultiKey(rootSubject.getSourceId(), rootSubject.getId(), id);
  }

  /**
   * see if this is cacheable
   * @param id
   * @param queryOptions 
   * @param checkGrouperSession 
   * @return if cacheable
   */
  private static boolean attributeDefNameCacheableAsRoot(Object id, QueryOptions queryOptions, boolean checkGrouperSession) {
  
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_CACHE_FIND_ATTRIBUTE_DEF_NAME_ROOT_BY_CACHE, true)) {
      return false;
    }
  
    if (id == null || ((id instanceof String) && StringUtils.isBlank((String)id))) {
      return false;
    }
  
    if (checkGrouperSession) {
      
      GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
      
      if (grouperSession == null) {
        return false;
      }
      
      Subject grouperSessionSubject = grouperSession.getSubject();
  
      if (grouperSessionSubject == null || !PrivilegeHelper.isWheelOrRoot(grouperSessionSubject)) {
        return false; 
      }
    }
  
    if (!HibUtils.secondLevelCaching(true, queryOptions)) {
      return false;
    }
    
    if (!attributeDefNameCacheAsRootIdsAndNames.contains(id)) {
      return false;
    }
    
    return true;
  }

  /**
   * add attributeDefName to cache if not null
   * @param attributeDefName
   */
  private static void attributeDefNameCacheAsRootAddIfSupposedTo(AttributeDefName attributeDefName) {
    if (attributeDefName != null && GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_CACHE_FIND_ATTRIBUTE_DEF_NAME_ROOT_BY_CACHE, true)
        && !StringUtils.isBlank(attributeDefName.getUuid()) && !StringUtils.isBlank(attributeDefName.getName()) 
        && (attributeDefNameCacheAsRootIdsAndNames.contains(attributeDefName.getId()) 
            || Hib3AttributeDefDAO.attributeDefCacheAsRootIdsAndNamesContains(attributeDefName.getAttributeDefId()))
        && attributeDefName.getIdIndex() != null) {
      attributeDefNameCacheAsRootIdsAndNamesAdd(attributeDefName);
    }
  }

  /**
   * 
   * @param attributeDefName
   */
  public static void attributeDefNameCacheAsRootIdsAndNamesAdd(AttributeDefName attributeDefName) {
    if (attributeDefName == null) {
      return;
    }
    //say this is cached since might be attributedef.id
    attributeDefNameCacheAsRootIdsAndNames.add(attributeDefName.getId());
    attributeDefNameCacheAsRootIdsAndNames.add(attributeDefName.getName());
    
    attributeDefNameRootCache.put(attributeDefName.getId(), attributeDefName);
    attributeDefNameRootCache.put(attributeDefName.getName(), attributeDefName);
    attributeDefNameRootCache.put(attributeDefName.getIdIndex(), attributeDefName);
  
  }

  /**
   * 
   * @param uuidOrNameOfAttributeDefName or id index
   * @return if should be cached
   */
  public static boolean attributeDefNameCacheAsRootIdsAndNamesContains(Object uuidOrNameOfAttributeDefName) {
    return attributeDefNameCacheAsRootIdsAndNames.contains(uuidOrNameOfAttributeDefName);
  }

  /**
   * get a attributeDefName from root cache
   * @param id
   * @param queryOptions
   * @return the group or null
   */
  private static AttributeDefName attributeDefNameCacheAsRootRetrieve(Object id, QueryOptions queryOptions) {
    
    if (attributeDefNameCacheableAsRoot(id, queryOptions, true)) {
      
      //see if its already in the cache
      AttributeDefName attributeDefName = attributeDefNameRootCache.get(id);
      if (attributeDefName != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from attributeDefName root cache by name: " + attributeDefName.getName());
        }
        return attributeDefName;
      }
    }
    return null;
  }

  /**
   * remove this from all caches
   * @param attributeDefName
   */
  public static void attributeDefNameCacheRemove(AttributeDefName attributeDefName) {
    attributeDefNameRootCache.remove(attributeDefName.getUuid());
    attributeDefNameRootCache.remove(attributeDefName.getName());
    attributeDefNameRootCache.remove(attributeDefName.getIdIndex());
  
    attributeDefNameFlashCache.clear();
  }

  /**
   * see if this is cacheable
   * @param id
   * @param queryOptions 
   * @return if cacheable
   */
  private static boolean attributeDefNameFlashCacheable(Object id, QueryOptions queryOptions) {
  
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_NAME_CACHE, true)) {
      return false;
    }
  
    if (id == null || ((id instanceof String) && StringUtils.isBlank((String)id))) {
      return false;
    }
  
    if (!HibUtils.secondLevelCaching(true, queryOptions)) {
      return false;
    }
    
    return true;
  }

  /**
   * add attributeDef to cache if not null
   * @param attributeDefName
   */
  private static void attributeDefNameFlashCacheAddIfSupposedTo(AttributeDefName attributeDefName) {
    if (attributeDefName == null || !GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_NAME_CACHE, true)) {
      return;
    }
    
    for (Object id : new Object[]{attributeDefName.getUuid(), attributeDefName.getName(), attributeDefName.getIdIndex()}) {
      MultiKey multiKey = attributeDefNameFlashCacheMultikey(id);
      if (multiKey == null) {
        continue;
      }
      attributeDefNameFlashCache.put(multiKey, attributeDefName);
    }
    
  }

  /**
   * multikey
   * @param id
   * @return if cacheable
   */
  private static MultiKey attributeDefNameFlashCacheMultikey(Object id) {
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_NAME_CACHE, true)) {
      return null;
    }
  
    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    
    Subject grouperSessionSubject = null;
    
    if (grouperSession == null) {
      grouperSessionSubject = SubjectFinder.findRootSubject();
    } else {
      grouperSessionSubject = grouperSession.getSubject();
    }
        
    return new MultiKey(grouperSessionSubject.getSourceId(), grouperSessionSubject.getId(), id);
  }

  /**
   * get a attributeDefName from flash cache
   * @param id
   * @param queryOptions
   * @return the group or null
   */
  private static AttributeDefName attributeDefNameFlashCacheRetrieve(Object id, QueryOptions queryOptions) {
    if (attributeDefNameFlashCacheable(id, queryOptions)) {
      MultiKey groupFlashMultikey = attributeDefNameFlashCacheMultikey(id);
      //see if its already in the cache
      AttributeDefName attributeDefName = attributeDefNameFlashCache.get(groupFlashMultikey);
      if (attributeDefName != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from attribute def name flash cache by id: " + attributeDefName.getName());
        }
        return attributeDefName;
      }
    }
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByName(java.lang.String, boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public AttributeDefName findByName(String name, boolean exceptionIfNotFound,
      QueryOptions queryOptions) throws GrouperDAOException,
      AttributeDefNameNotFoundException {
    AttributeDefName attributeDefName = attributeDefNameCacheAsRootRetrieve(name, queryOptions);
    if (attributeDefName != null) {
      return attributeDefName;
    }      
    
    attributeDefName = attributeDefNameFlashCacheAsRootRetrieve(name, queryOptions);
    if (attributeDefName != null) {
      return attributeDefName;
    }      

    attributeDefName = HibernateSession.byHqlStatic()
        .createQuery("select a from AttributeDefName as a where a.nameDb = :value")
        .options(queryOptions)
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByName")
        .setString("value", name).uniqueResult(AttributeDefName.class);
    
    attributeDefNameCacheAsRootAddIfSupposedTo(attributeDefName);
    attributeDefNameFlashCacheAddIfSupposedToAsRoot(attributeDefName);

    if (attributeDefName != null) {
      
      return attributeDefName;
    }
    LOG.info("AttributeDefName not found: " + name);
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new AttributeDefNameNotFoundException("Cannot find (or not allowed to find) attribute def name with name: '" + name + "'");
  }

} 

