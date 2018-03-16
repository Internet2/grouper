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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.dao.QuerySortField;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;

/**
 * Data Access Object for attribute def
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefDAO.java,v 1.6 2009-10-26 02:26:07 mchyzer Exp $
 */
public class Hib3AttributeDefDAO extends Hib3DAO implements AttributeDefDAO {
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3AttributeDefDAO.class);

  /**
   * 
   */
  private static final String KLASS = Hib3AttributeDefDAO.class.getName();

  /**
   * reset the attribute defs
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeDef").executeUpdate();
  }

  /**
   * names and ids of attribute defs which should cache as root
   */
  private static Set<String> attributeDefCacheAsRootIdsAndNames = new HashSet<String>();
  /**
   * cache stuff in attributeDef by subjectSourceId, subjectId, name, uuid, idIndex
   */
  private static GrouperCache<MultiKey, AttributeDef> attributeDefFlashCache = new GrouperCache(
      "edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder.attributeDefFinderFlashCache");
  /**
   * cache stuff in attribute defs by name, uuid, idIndex
   */
  private static GrouperCache<Object, AttributeDef> attributeDefRootCache = new GrouperCache(
      "edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder.attributeDefFinderCache");
  /**
   * 
   */
  private static final String GROUPER_CACHE_FIND_ATTRIBUTE_DEF_ROOT_BY_CACHE = "grouperCache.find.attributeDefRootByCache";
  /**
   * 
   */
  private static final String GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_CACHE = "grouperFlashCache.find.attributeDefCache";

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByIdSecure(java.lang.String, boolean)
   */
  public AttributeDef findByIdSecure(String id, boolean exceptionIfNotFound) {
    return findByIdSecure(id, exceptionIfNotFound, null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByIdSecure(String, boolean, QueryOptions)
   */
  public AttributeDef findByIdSecure(String id, boolean exceptionIfNotFound, QueryOptions queryOptions) {
    
    
    AttributeDef attributeDef = attributeDefCacheAsRootRetrieve(id, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }      
    
    attributeDef = attributeDefFlashCacheRetrieve(id, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }      

    attributeDef = findById(id, exceptionIfNotFound, queryOptions);

    //make sure grouper session can view the attribute def
    attributeDef = filterSecurity(attributeDef);
    
    if (attributeDef != null) {
      attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
      attributeDefFlashCacheAddIfSupposedTo(attributeDef);
      
      return attributeDef;
    }
    LOG.info("AttributeDef not found: " + id);
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new AttributeDefNotFoundException("AttributeDef not found: " + id);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findById(java.lang.String, boolean)
   */
  public AttributeDef findById(String id, boolean exceptionIfNotFound) {
    return findById(id, exceptionIfNotFound, null);
  }
  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findById(java.lang.String, boolean, QueryOptions)
   */
  public AttributeDef findById(String id, boolean exceptionIfNotFound, QueryOptions queryOptions) {

    AttributeDef attributeDef = attributeDefCacheAsRootRetrieve(id, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }
    attributeDef = attributeDefFlashCacheAsRootRetrieve(id, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }
    if (queryOptions != null) {
      // why would the caller be sorting here?
      massageSortFields(queryOptions.getQuerySort());
    }
    attributeDef = HibernateSession.byHqlStatic()
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindById")
      .createQuery(
        "from AttributeDef as theAttributeDef where theAttributeDef.id = :theId")
      .setString("theId", id)
      .options(queryOptions).uniqueResult(AttributeDef.class);

    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cant find AttributeDef by id: " + id);
    }
    attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
    attributeDefFlashCacheAddIfSupposedToAsRoot(attributeDef);
    return attributeDef;
  }

  /**
   * make sure grouper session can view the attribute def
   * @param attributeDefs
   * @return the set of attribute defs
   */
  static Set<AttributeDef> filterSecurity(Set<AttributeDef> attributeDefs) {
    Set<AttributeDef> result = new LinkedHashSet<AttributeDef>();
    if (attributeDefs != null) {
      for (AttributeDef attributeDef : attributeDefs) {
        attributeDef = filterSecurity(attributeDef);
        if (attributeDef != null) {
          result.add(attributeDef);
        }
      }
    }
    return result;
  }
  
  /**
   * make sure grouper session can view the attribute def
   * @param attributeDef
   * @return the attributeDef or null
   */
  static AttributeDef filterSecurity(AttributeDef attributeDef) {
    if (attributeDef == null) {
      return null;
    }
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    if ( PrivilegeHelper.canAttrView( grouperSession.internal_getRootSession(), attributeDef, grouperSession.getSubject() ) ) {
      return attributeDef;
    }
    return null;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  public void saveOrUpdate(AttributeDef attributeDef) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDef);
    
    attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
    attributeDefFlashCache.clear();

  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByNameSecure(java.lang.String, boolean)
   */
  public AttributeDef findByNameSecure(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, AttributeDefNotFoundException {
    return findByNameSecure(name, exceptionIfNotFound, null);
  }
 
  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByNameSecure(java.lang.String, boolean)
   */
  public AttributeDef findByNameSecure(String name, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws GrouperDAOException, AttributeDefNotFoundException {

    AttributeDef attributeDef = attributeDefCacheAsRootRetrieve(name, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }      
    
    attributeDef = attributeDefFlashCacheRetrieve(name, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }      

    if (queryOptions != null) {
      // why would the caller be sorting here?
      massageSortFields(queryOptions.getQuerySort());
    }
    attributeDef = HibernateSession.byHqlStatic()
      .createQuery("select theAttributeDef from AttributeDef as theAttributeDef where theAttributeDef.nameDb = :value")
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByName")
      .options(queryOptions)
      .setString("value", name).uniqueResult(AttributeDef.class);

    //make sure grouper session can view the attribute def
    attributeDef = filterSecurity(attributeDef);
    
    //handle exceptions out of data access method...
    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cannot find (or not allowed to find) attribute def with name: '" + name + "'");
    }
    
    attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
    attributeDefFlashCacheAddIfSupposedTo(attributeDef);
    
    return attributeDef;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByAttributeDefNameIdSecure(java.lang.String, boolean)
   */
  public AttributeDef findByAttributeDefNameIdSecure(String attributeDefNameId,
      boolean exceptionIfNotFound) {

    AttributeDefName attributeDefName = AttributeDefNameFinder.findById(attributeDefNameId, exceptionIfNotFound);
    
    if (attributeDefName != null) {
      return findById(attributeDefName.getAttributeDefId(), true);
    }
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByStem(java.lang.String)
   */
  public Set<AttributeDef> findByStem(String id) {
    Set<AttributeDef> attributeDefs = HibernateSession.byHqlStatic()
        .createQuery("from AttributeDef where stemId = :id order by nameDb")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByStem")
        .setString("id", id)
        .listSet(AttributeDef.class);
    
    for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefs)) {
      attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
      attributeDefFlashCacheAddIfSupposedToAsRoot(attributeDef);
    }

    return attributeDefs;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByUuidOrName(java.lang.String, java.lang.String, boolean)
   */
  public AttributeDef findByUuidOrName(String id, String name,
      boolean exceptionIfNotFound) {
    return findByUuidOrName(id, name, exceptionIfNotFound, null);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByUuidOrName(java.lang.String, java.lang.String, boolean)
   */
  public AttributeDef findByUuidOrName(String id, String name,
      boolean exceptionIfNotFound, QueryOptions queryOptions) {
    if (queryOptions != null) {
      // why would the caller be sorting here?
      massageSortFields(queryOptions.getQuerySort());
    }
    try {
      AttributeDef attributeDef = HibernateSession.byHqlStatic()
        .createQuery("from AttributeDef as theAttributeDef where theAttributeDef.id = :theId or theAttributeDef.nameDb = :theName")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .setString("theId", id)
        .options(queryOptions)
        .setString("theName", name)
        .uniqueResult(AttributeDef.class);
      if (attributeDef == null && exceptionIfNotFound) {
        throw new AttributeDefNotFoundException("Can't find attributeDef by id: '" + id + "' or name '" + name + "'");
      }
      attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
      attributeDefFlashCacheAddIfSupposedToAsRoot(attributeDef);

      return attributeDef;
    }
    catch (GrouperDAOException e) {
      String error = "Problem finding attributeDef by id: '" 
        + id + "' or name '" + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#saveUpdateProperties(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  public void saveUpdateProperties(AttributeDef attributeDef) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AttributeDef " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "creatorId = :theCreatorId, " +
        "createdOnDb = :theCreatedOnDb " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attributeDef.getHibernateVersionNumber())
        .setString("theCreatorId", attributeDef.getCreatorId())
        .setLong("theCreatedOnDb", attributeDef.getCreatedOnDb())
        .setString("theContextId", attributeDef.getContextId())
        .setString("theId", attributeDef.getId()).executeUpdate();
    
    attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
    attributeDefFlashCache.clear();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#delete(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  public void delete(final AttributeDef attributeDef) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        List<Membership> memberships = GrouperDAOFactory.getFactory().getMembership().findAllImmediateByAttrDefOwnerAsList(attributeDef.getId(), false);
        
        
        hibernateHandlerBean.getHibernateSession().byObject().setEntityName("ImmediateMembershipEntry").delete(memberships);
        
        Set<AttributeAssignAction> attributeAssignActions = GrouperDAOFactory.getFactory().getAttributeAssignAction().findByAttributeDefId(attributeDef.getId());
        
        for (AttributeAssignAction attributeAssignAction : attributeAssignActions) {
          attributeAssignAction.delete();
        }
        
        GrouperDAOFactory.getFactory().getGroupSet().deleteSelfByOwnerAttrDef(attributeDef.getId());

        hibernateHandlerBean.getHibernateSession().byObject().delete(attributeDef);
        
        attributeDefFlashCache.clear();
        attributeDefRootCache.clear();
        
        return null;
      }
    });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#getAllAttributeDefsSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<AttributeDef> getAllAttributeDefsSecure(GrouperSession grouperSession,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions) {
    return getAllAttributeDefsSecure(null, grouperSession, subject, privileges, queryOptions);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#getAllAttributeDefsSecure(java.lang.String, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<AttributeDef> getAllAttributeDefsSecure(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions) {
    return getAllAttributeDefsSecureHelper(scope, grouperSession, subject, privileges, 
        queryOptions, false, null, null, null, null, false, null);
  }

  /**
   * @param scope 
   * @param grouperSession 
   * @param subject 
   * @param privileges 
   * @param queryOptions 
   * @param splitScope 
   * @param attributeAssignType
   * @param attributeDefType
   * @param parentStemId
   * @param stemScope
   * @param findByUuidOrName
   * @param totalAttributeDefIds
   * @return  attribute defs
   * 
   */
  private Set<AttributeDef> getAllAttributeDefsSecureHelper(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, boolean splitScope, AttributeAssignType attributeAssignType,
      AttributeDefType attributeDefType, String parentStemId, Scope stemScope, 
      boolean findByUuidOrName, Collection<String> totalAttributeDefIds) {
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    massageSortFields(queryOptions.getQuerySort());
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theAttributeDef.nameDb");
    }
    Set<AttributeDef> overallResults = new LinkedHashSet<AttributeDef>();

    int attributeDefBatches = GrouperUtil.batchNumberOfBatches(totalAttributeDefIds, 100);

    List<String> totalAttributeDefIdsList = new ArrayList<String>(GrouperUtil.nonNull(totalAttributeDefIds));
    
    for (int attributeDefIndex = 0; attributeDefIndex < attributeDefBatches; attributeDefIndex++) {
      
      List<String> attributeDefIds = GrouperUtil.batchList(totalAttributeDefIdsList, 100, attributeDefIndex);

      StringBuilder sql = new StringBuilder("select distinct theAttributeDef from AttributeDef theAttributeDef ");
  
      if (!StringUtils.isBlank(parentStemId) || stemScope != null) {
        
        if (StringUtils.isBlank(parentStemId) || stemScope == null) {
          throw new RuntimeException("If you are passing in a parentStemId or a stemScope, then you need to pass both of them: " + parentStemId + ", " + stemScope);
        }
        
        if (stemScope == Scope.SUB) {
          sql.append(", StemSet theStemSet ");
        }
      }      
  
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
      StringBuilder whereClause = new StringBuilder();
      
      if (attributeDefType != null) {
        if (whereClause.length() > 0) {
          whereClause.append(" and ");
        }
        whereClause.append(" theAttributeDef.attributeDefTypeDb = :theAttributeDefType ");
        byHqlStatic.setString("theAttributeDefType", attributeDefType.name());
      }

      if (GrouperUtil.length(attributeDefIds) > 0) {

        if (whereClause.length() > 0) {
          
          whereClause.append(" and ");
          
        }

        whereClause.append(" theAttributeDef.id in (");
        whereClause.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
        whereClause.append(") ");
        
      }
      
      if (attributeAssignType != null) {
        if (whereClause.length() > 0) {
          whereClause.append(" and ");
        }
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
        
        String[] scopes = splitScope ? GrouperUtil.splitTrim(scope, " ") : new String[]{scope};
        int index = 0;
        boolean firstScope = true;
        for (String theScope : scopes) {
          if (whereClause.length() > 0) {
            whereClause.append(" and ");
          } 
          if (firstScope) {
            whereClause.append(" (( ");
          }
          firstScope = false;
  
          if (findByUuidOrName) {
            whereClause.append(" theAttributeDef.nameDb = :scope" + index + " ");
            byHqlStatic.setString("scope" + index, theScope);
          } else {
            whereClause.append(" ( lower(theAttributeDef.nameDb) like :scope" + index 
                + " or lower(theAttributeDef.description) like :scope" + index + " ) ");
            if (splitScope) {
              theScope = "%" + theScope + "%";
            } else if (!theScope.endsWith("%")) {
              theScope += "%";
            }
            byHqlStatic.setString("scope" + index, theScope.toLowerCase());
  
          }        
          
          index++;
        }
  
        whereClause.append(" ) or ( theAttributeDef.id = :attributeId  )) ");
        byHqlStatic.setString("attributeId", scope);
      }
  
      if (!StringUtils.isBlank(parentStemId) || stemScope != null) {
        switch(stemScope) {
          case ONE:
            
            if (whereClause.length() > 0) {
              whereClause.append(" and ");
            } 
            whereClause.append(" theAttributeDef.stemId = :theStemId ");
            byHqlStatic.setString("theStemId", parentStemId);
            break;
          case SUB:
            
            if (whereClause.length() > 0) {
              whereClause.append(" and ");
            } 
            whereClause.append(" theAttributeDef.stemId = theStemSet.ifHasStemId " +
                " and theStemSet.thenHasStemId = :theStemId ");
            byHqlStatic.setString("theStemId", parentStemId);
            
            break;
          
        }
      }
  
      
      //see if we are adding more to the query
      if (GrouperUtil.length(privileges) > 0) {
        grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(subject, byHqlStatic,
            sql, whereClause, "theAttributeDef.id", privileges);
      }
      
      if (whereClause.length() > 0) {
        sql.append(" where ").append(whereClause);
      }    
      
      Set<AttributeDef> attributeDefs = byHqlStatic.createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllAttributeDefsSecure")
        .options(queryOptions)
        .listSet(AttributeDef.class);
      
      //if the hql didnt filter, this will
      Set<AttributeDef> tempResult = GrouperUtil.length(privileges) == 0 ? attributeDefs 
          : grouperSession.getAttributeDefResolver()
              .postHqlFilterAttrDefs(attributeDefs, subject, privileges);
  
      overallResults.addAll(GrouperUtil.nonNull(tempResult));
    }

    for (AttributeDef attributeDef : GrouperUtil.nonNull(overallResults)) {
      attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
      attributeDefFlashCacheAddIfSupposedTo(attributeDef);
    }

    //if find by uuid or name, try to narrow down to one...
    if (findByUuidOrName) {
      
      //get the one with uuid
      for (AttributeDef attributeDef : overallResults) {
        if (StringUtils.equals(scope, attributeDef.getId())) {
          return GrouperUtil.toSet(attributeDef);
        }
      }

      //get the one with name
      for (AttributeDef attributeDef : overallResults) {
        if (StringUtils.equals(scope, attributeDef.getName())) {
          return GrouperUtil.toSet(attributeDef);
        }
      }
    }
    
    return overallResults;

  }
  
  /**
   * @see AttributeDefDAO#findAttributeDefsInStemWithoutPrivilege(GrouperSession, String, Scope, Subject, Privilege, QueryOptions, boolean, String)
   */
  public Set<AttributeDef> findAttributeDefsInStemWithoutPrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, QueryOptions queryOptions, boolean considerAllSubject, 
      String sqlLikeString) {
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    massageSortFields(queryOptions.getQuerySort());
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theAttributeDef.nameDb");
    }

    StringBuilder sqlTables = new StringBuilder("select distinct theAttributeDef from AttributeDef theAttributeDef ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sqlWhereClause = new StringBuilder();

    switch (scope) {
      case ONE:
        
        sqlWhereClause.append(" theAttributeDef.stemId = :stemId ");
        byHqlStatic.setString("stemId", stemId);
        
        break;
        
      case SUB:
        
        Stem stem = StemFinder.findByUuid(grouperSession, stemId, true);
        sqlWhereClause.append(" theAttributeDef.nameDb like :stemPattern ");
        byHqlStatic.setString("stemPattern", stem.getName() + ":%");

        break;
        
      default:
        throw new RuntimeException("Need to pass in a scope, or its not implemented: " + scope);
    }
    

    
    //see if we are adding more to the query, note, this is for the ADMIN list since the user should be able to read privs
    Set<Privilege> adminSet = GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN);

    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        sqlTables, sqlWhereClause, "theAttributeDef.id", adminSet);

    if (!StringUtils.isBlank(sqlLikeString)) {
      if (sqlWhereClause.length() > 0) {
        sqlWhereClause.append(" and ");
      }
      sqlWhereClause.append(" theAttributeDef.nameDb like :sqlLikeString ");
      byHqlStatic.setString("sqlLikeString", sqlLikeString);
    }
    

    StringBuilder sql = sqlTables.append(" where ").append(sqlWhereClause);

    boolean changedQueryNotWithPriv = grouperSession.getAttributeDefResolver().hqlFilterAttributeDefsNotWithPrivWhereClause(subject, byHqlStatic, 
        sql, "theAttributeDef.id", privilege, considerAllSubject);

    Set<AttributeDef> attributeDefs = byHqlStatic.createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAttributeDefsInStemWithoutPrivilege")
      .options(queryOptions)
      .listSet(AttributeDef.class);
          
    for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefs)) {
      attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
      attributeDefFlashCacheAddIfSupposedToAsRoot(attributeDef);
    }

    //if the hql didnt filter, this will
    Set<AttributeDef> filteredAttributeDefs = grouperSession.getAttributeDefResolver()
      .postHqlFilterAttrDefs(attributeDefs, grouperSession.getSubject(), adminSet);

    if (!changedQueryNotWithPriv) {
      
      //didnt do this in the query
      Set<AttributeDef> originalList = new LinkedHashSet<AttributeDef>(filteredAttributeDefs);
      filteredAttributeDefs = grouperSession.getAttributeDefResolver()
        .postHqlFilterAttrDefs(originalList, subject, GrouperUtil.toSet(privilege));
      
      //we want the ones in the original list not in the new list
      if (filteredAttributeDefs != null) {
        originalList.removeAll(filteredAttributeDefs);
      }
      filteredAttributeDefs = originalList;
    }
    
    return filteredAttributeDefs;
    
  }

  /**
   * @see AttributeDefDAO#getAllAttributeDefsSplitScopeSecure(String, GrouperSession, Subject, Set, QueryOptions, AttributeAssignType, AttributeDefType)
   * @Override
   */
  public Set<AttributeDef> getAllAttributeDefsSplitScopeSecure(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, AttributeAssignType attributeAssignType,
      AttributeDefType attributeDefType) {
    return getAllAttributeDefsSecureHelper(scope, grouperSession, subject, 
        privileges, queryOptions, true, attributeAssignType, attributeDefType, null, null, false, null);
  }

  /**
   * if there are sort fields, go through them, and replace name with nameDb, etc,
   * extension for extensionDb
   * @param querySort
   */
  public static void massageSortFields(QuerySort querySort) {
    massageSortFields(querySort, "theAttributeDef");
  }

  /**
   * if there are sort fields, go through them, and replace name with nameDb, etc,
   * extension for extensionDb
   * @param querySort
   * @param alias
   */
  public static void massageSortFields(QuerySort querySort, String alias) {
    if (querySort == null) {
      return;
    }
    for (QuerySortField querySortField : GrouperUtil.nonNull(querySort.getQuerySortFields())) {
      if (StringUtils.equals("extension", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".extensionDb");
      }
      if (StringUtils.equals("name", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".nameDb");
      }
    }
  }

  /**
   * not a secure method, find by id index
   */
  @Override
  public AttributeDef findByIdIndex(Long idIndex, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws AttributeDefNotFoundException {
    
    AttributeDef attributeDef = attributeDefCacheAsRootRetrieve(idIndex, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }
    attributeDef = attributeDefFlashCacheAsRootRetrieve(idIndex, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }
    if (queryOptions != null) {
      // why would the caller be sorting here?
      massageSortFields(queryOptions.getQuerySort());
    }
    StringBuilder hql = new StringBuilder("select theAttributeDef from AttributeDef as theAttributeDef where (theAttributeDef.idIndex = :theIdIndex)");
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic().options(queryOptions)
      .setCacheable(true).setCacheRegion(KLASS + ".FindByIdIndex");
    
    byHqlStatic.createQuery(hql.toString());
    
    attributeDef = byHqlStatic.setLong("theIdIndex", idIndex).uniqueResult(AttributeDef.class);

    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cant find AttributeDef by id: " + idIndex);
    }
    attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
    attributeDefFlashCacheAddIfSupposedToAsRoot(attributeDef);
    return attributeDef;
    
  }


  /**
   * secure method, find by id index
   */
  @Override
  public AttributeDef findByIdIndexSecure(Long idIndex, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws AttributeDefNotFoundException {
    
    AttributeDef attributeDef = attributeDefCacheAsRootRetrieve(idIndex, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }      
    
    attributeDef = attributeDefFlashCacheRetrieve(idIndex, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }      

    attributeDef = findByIdIndex(idIndex, exceptionIfNotFound, queryOptions);

    //make sure grouper session can view the attribute def
    attributeDef = filterSecurity(attributeDef);
    
    if (attributeDef != null) {
      attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
      attributeDefFlashCacheAddIfSupposedTo(attributeDef);
      
      return attributeDef;
    }
    LOG.info("AttributeDef not found: " + idIndex);
    if (!exceptionIfNotFound) {
      return null;
    }
    throw new AttributeDefNotFoundException("AttributeDef not found: " + idIndex);
    
  }

  /**
   * @see AttributeDefDAO#findByIdsSecure(Collection, QueryOptions)
   */
  @Override
  public Set<AttributeDef> findByIdsSecure(Collection<String> ids,
      QueryOptions queryOptions) {
    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(ids, 180);
    
    Set<AttributeDef> attributeDefs = new HashSet<AttributeDef>();
    
    List<String> idsList = GrouperUtil.listFromCollection(ids);
    
    for (int i=0;i<numberOfBatches;i++) {
      
      List<String> uuidsBatch = GrouperUtil.batchList(idsList, 180, i);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

      StringBuilder sql = new StringBuilder("select distinct theAttributeDef from AttributeDef as theAttributeDef ");
      
      StringBuilder whereClause = new StringBuilder();
      
      //see if we are adding more to the query
      grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
          grouperSession.getSubject(), byHqlStatic, 
          sql, whereClause, "theAttributeDef.id", AttributeDefPrivilege.ATTR_VIEW_PRIVILEGES);

      sql.append(" where ").append(whereClause);
      
      if (whereClause.length() > 0) {
        sql.append(" and ");
      }
      
      sql.append(" theAttributeDef.id in ( ");
      
      sql.append(HibUtils.convertToInClause(uuidsBatch, byHqlStatic)).append(" ) ");
      
      byHqlStatic
        .createQuery(sql.toString())
        .setCacheable(true)
        .options(queryOptions)
        .setCacheRegion(KLASS + ".FindByUuidsSecure");
      
      Set<AttributeDef> attributeDefsBatch = byHqlStatic.listSet(AttributeDef.class);
      
      attributeDefs.addAll(GrouperUtil.nonNull(attributeDefsBatch));
      
    }
    
    return attributeDefs;
  }

  /**
   * @see AttributeDefDAO#findAllAttributeDefsSecure(String, boolean, Subject, Set, QueryOptions, String, Scope, boolean, Collection)
   */
  @Override
  public Set<AttributeDef> findAllAttributeDefsSecure(String scope, boolean splitScope,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions,
      String parentStemId, Scope stemScope, boolean findByUuidOrName, Collection<String> totalAttributeDefIds) {
    
    return getAllAttributeDefsSecureHelper(scope, GrouperSession.staticGrouperSession(), 
        subject, privileges, queryOptions, splitScope, null, null, parentStemId, stemScope, findByUuidOrName, totalAttributeDefIds);
    
  }

  /**
   * @see AttributeDefDAO#findAttributeDefsInStemWithPrivilege(GrouperSession, String, Scope, Subject, Privilege, QueryOptions, boolean, String)
   */
  public Set<AttributeDef> findAttributeDefsInStemWithPrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, QueryOptions queryOptions, boolean considerAllSubject, 
      String sqlLikeString) {
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    massageSortFields(queryOptions.getQuerySort());
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theAttributeDef.nameDb");
    }
  
    StringBuilder sqlTables = new StringBuilder("select distinct theAttributeDef from AttributeDef theAttributeDef ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    StringBuilder sqlWhereClause = new StringBuilder();
  
    switch (scope) {
      case ONE:
        
        sqlWhereClause.append(" theAttributeDef.stemId = :stemId ");
        byHqlStatic.setString("stemId", stemId);
        
        break;
        
      case SUB:
        
        Stem stem = StemFinder.findByUuid(grouperSession, stemId, true);
        sqlWhereClause.append(" theAttributeDef.nameDb like :stemPattern ");
        byHqlStatic.setString("stemPattern", stem.getName() + ":%");
  
        break;
        
      default:
        throw new RuntimeException("Need to pass in a scope, or its not implemented: " + scope);
    }
    
  
    
    //see if we are adding more to the query, note, this is for the ADMIN list since the user should be able to read privs
    Set<Privilege> adminSet = GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN);
  
    grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        sqlTables, sqlWhereClause, "theAttributeDef.id", adminSet);
  
    if (!StringUtils.isBlank(sqlLikeString)) {
      if (sqlWhereClause.length() > 0) {
        sqlWhereClause.append(" and ");
      }
      sqlWhereClause.append(" theAttributeDef.nameDb like :sqlLikeString ");
      byHqlStatic.setString("sqlLikeString", sqlLikeString);
    }
    
  
    StringBuilder sql = sqlTables.append(" where ").append(sqlWhereClause);
  
    boolean changedQueryNotWithPriv = grouperSession.getAttributeDefResolver().hqlFilterAttributeDefsWithPrivWhereClause(subject, byHqlStatic, 
        sql, "theAttributeDef.id", privilege, considerAllSubject);
  
    Set<AttributeDef> attributeDefs = byHqlStatic.createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAttributeDefsInStemWithPrivilege")
      .options(queryOptions)
      .listSet(AttributeDef.class);
          
    for (AttributeDef attributeDef : GrouperUtil.nonNull(attributeDefs)) {
      attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
      attributeDefFlashCacheAddIfSupposedToAsRoot(attributeDef);
    }

    //if the hql didnt filter, this will
    Set<AttributeDef> filteredAttributeDefs = grouperSession.getAttributeDefResolver()
      .postHqlFilterAttrDefs(attributeDefs, grouperSession.getSubject(), adminSet);
  
    if (!changedQueryNotWithPriv) {
      
      //didnt do this in the query
      Set<AttributeDef> originalList = new LinkedHashSet<AttributeDef>(filteredAttributeDefs);
      filteredAttributeDefs = grouperSession.getAttributeDefResolver()
        .postHqlFilterAttrDefs(originalList, subject, GrouperUtil.toSet(privilege));
      
      //we want the ones in the original list not in the new list
      if (filteredAttributeDefs != null) {
        originalList.removeAll(filteredAttributeDefs);
      }
      filteredAttributeDefs = originalList;
    }
    
    return filteredAttributeDefs;
    
  }

  /**
   * see if this is cacheable
   * @param id
   * @param queryOptions 
   * @param checkGrouperSession 
   * @return if cacheable
   */
  private static boolean attributeDefCacheableAsRoot(Object id, QueryOptions queryOptions, boolean checkGrouperSession) {
  
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_CACHE_FIND_ATTRIBUTE_DEF_ROOT_BY_CACHE, true)) {
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
  
    if (queryOptions != null 
        && queryOptions.getSecondLevelCache() != null && !queryOptions.getSecondLevelCache()) {
      return false;
    }
    
    if (!attributeDefCacheAsRootIdsAndNames.contains(id)) {
      return false;
    }
    
    return true;
  }

  /**
   * add attributeDef to cache if not null
   * @param attributeDef
   */
  private static void attributeDefCacheAsRootAddIfSupposedTo(AttributeDef attributeDef) {
    if (attributeDef != null && GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_CACHE_FIND_ATTRIBUTE_DEF_ROOT_BY_CACHE, true)
        && !StringUtils.isBlank(attributeDef.getUuid()) && !StringUtils.isBlank(attributeDef.getName()) 
        && attributeDefCacheAsRootIdsAndNames.contains(attributeDef.getId())
        && attributeDef.getIdIndex() != null) {
      attributeDefCacheAsRootIdsAndNamesAdd(attributeDef);
    }
    
  }

  /**
   * mark an attribute def as supposed to be cached, and put it in the cache
   * @param attributeDef
   */
  public static void attributeDefCacheAsRootIdsAndNamesAdd(AttributeDef attributeDef) {
    if (attributeDef == null) {
      return;
    }
    attributeDefCacheAsRootIdsAndNames.add(attributeDef.getId());
    attributeDefCacheAsRootIdsAndNames.add(attributeDef.getName());
  
    attributeDefRootCache.put(attributeDef.getId(), attributeDef);
    attributeDefRootCache.put(attributeDef.getName(), attributeDef);
    attributeDefRootCache.put(attributeDef.getIdIndex(), attributeDef);
  }

  /**
   * 
   * @param attributeDefIdOrName or id index
   * @return if this is cached
   */
  public static boolean attributeDefCacheAsRootIdsAndNamesContains(Object attributeDefIdOrName) {
    return attributeDefCacheAsRootIdsAndNames.contains(attributeDefIdOrName);
  }

  /**
   * get a attributeDef from root cache
   * @param id
   * @param queryOptions
   * @return the attributeDef or null
   */
  private static AttributeDef attributeDefCacheAsRootRetrieve(Object id, QueryOptions queryOptions) {
    
    if (attributeDefCacheableAsRoot(id, queryOptions, true)) {
      
      //see if its already in the cache
      AttributeDef attributeDef = attributeDefRootCache.get(id);
      if (attributeDef != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from attributeDef root cache by name: " + attributeDef.getName());
        }
        return attributeDef;
      }
    }
    return null;
  }

  /**
   * get a attributeDef from flash root cache
   * @param id
   * @param queryOptions
   * @return the attributeDef or null
   */
  private static AttributeDef attributeDefFlashCacheAsRootRetrieve(Object id, QueryOptions queryOptions) {

    if (attributeDefFlashCacheable(id, queryOptions)) {
      MultiKey flashCacheMultiKey = attributeDefFlashCacheMultikeyAsRoot(id);
      AttributeDef attributeDef = attributeDefFlashCache.get(flashCacheMultiKey);
      if (attributeDef != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from attributeDef flash root cache by id: " + id);
        }
        return attributeDef;
      }
    }
    return null;
  }

  /**
   * remove this from all caches
   * @param attributeDef
   */
  public static void attributeDefCacheRemove(AttributeDef attributeDef) {
    attributeDefRootCache.remove(attributeDef.getUuid());
    attributeDefRootCache.remove(attributeDef.getName());
    attributeDefRootCache.remove(attributeDef.getIdIndex());
  
    attributeDefFlashCache.clear();
  }

  /**
   * see if this is cacheable
   * @param id
   * @param queryOptions 
   * @return if cacheable
   */
  private static boolean attributeDefFlashCacheable(Object id, QueryOptions queryOptions) {
  
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_CACHE, true)) {
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
   * add attributeDef to cache if not null
   * @param attributeDef
   */
  private static void attributeDefFlashCacheAddIfSupposedTo(AttributeDef attributeDef) {
    if (attributeDef == null || !GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_CACHE, true)) {
      return;
    }
    
    for (Object id : new Object[]{attributeDef.getUuid(), attributeDef.getName(), attributeDef.getIdIndex()}) {
      MultiKey multiKey = attributeDefFlashCacheMultikey(id);
      if (multiKey == null) {
        continue;
      }
      attributeDefFlashCache.put(multiKey, attributeDef);
    }
    
  }

  /**
   * add attributeDef to cache if not null
   * @param attributeDef
   */
  private static void attributeDefFlashCacheAddIfSupposedToAsRoot(AttributeDef attributeDef) {
    if (attributeDef == null || !GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_CACHE, true)) {
      return;
    }
    
    for (Object id : new Object[]{attributeDef.getUuid(), attributeDef.getName(), attributeDef.getIdIndex()}) {
      MultiKey multiKey = attributeDefFlashCacheMultikeyAsRoot(id);
      attributeDefFlashCache.put(multiKey, attributeDef);
    }
    
  }

  /**
   * multikey
   * @param id
   * @return if cacheable
   */
  private static MultiKey attributeDefFlashCacheMultikeyAsRoot(Object id) {
    
    if (id == null || !GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_CACHE, true)) {
      return null;
    }
    Subject rootSubject = SubjectFinder.findRootSubject();
    return new MultiKey(rootSubject.getSourceId(), rootSubject.getId(), id);
  }

  /**
   * multikey
   * @param id
   * @return if cacheable
   */
  private static MultiKey attributeDefFlashCacheMultikey(Object id) {
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_FIND_ATTRIBUTE_DEF_CACHE, true)) {
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
   * get a attributeDef from flash cache
   * @param id
   * @param queryOptions
   * @return the group or null
   */
  private static AttributeDef attributeDefFlashCacheRetrieve(Object id, QueryOptions queryOptions) {
    if (attributeDefFlashCacheable(id, queryOptions)) {
      MultiKey groupFlashMultikey = attributeDefFlashCacheMultikey(id);
      //see if its already in the cache
      AttributeDef attributeDef = attributeDefFlashCache.get(groupFlashMultikey);
      if (attributeDef != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from attribute def flash cache by id: " + attributeDef.getName());
        }
        return attributeDef;
      }
    }
    return null;
  }

  /**
   * get a attributeDef from flash cache
   * @param id
   * @param queryOptions
   * @return the group or null
   */
  private static AttributeDef attributeDefFlashCacheRetrieveAsRoot(Object id, QueryOptions queryOptions) {
    if (attributeDefFlashCacheable(id, queryOptions)) {
      MultiKey groupFlashMultikey = attributeDefFlashCacheMultikeyAsRoot(id);
      //see if its already in the cache
      AttributeDef attributeDef = attributeDefFlashCache.get(groupFlashMultikey);
      if (attributeDef != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from attribute def flash cache by id: " + attributeDef.getName());
        }
        return attributeDef;
      }
    }
    return null;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByName(java.lang.String, boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public AttributeDef findByName(String name, boolean exceptionIfNotFound,
      QueryOptions queryOptions) throws GrouperDAOException,
      AttributeDefNotFoundException {
    
    AttributeDef attributeDef = attributeDefCacheAsRootRetrieve(name, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }      
    
    attributeDef = attributeDefFlashCacheRetrieveAsRoot(name, queryOptions);
    if (attributeDef != null) {
      return attributeDef;
    }      

    if (queryOptions != null) {
      // why would the caller be sorting here?
      massageSortFields(queryOptions.getQuerySort());
    }
    attributeDef = HibernateSession.byHqlStatic()
      .createQuery("select theAttributeDef from AttributeDef as theAttributeDef where theAttributeDef.nameDb = :value")
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByName")
      .options(queryOptions)
      .setString("value", name).uniqueResult(AttributeDef.class);

    //handle exceptions out of data access method...
    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cannot find (or not allowed to find) attribute def with name: '" + name + "'");
    }
    
    attributeDefCacheAsRootAddIfSupposedTo(attributeDef);
    attributeDefFlashCacheAddIfSupposedToAsRoot(attributeDef);
    
    return attributeDef;
  }
  


} 

