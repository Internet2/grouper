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
package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.exception.AttributeDefNameTooManyResults;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
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
import edu.internet2.middleware.grouper.service.ServiceRole;
import edu.internet2.middleware.grouper.util.GrouperUtil;
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
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByIdSecure(java.lang.String, boolean)
   */
  public AttributeDefName findByIdSecure(String id, boolean exceptionIfNotFound) {
    AttributeDefName attributeDefName = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDefName where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDefName.class);
    
    attributeDefName = filterSecurity(attributeDefName);
    
    if (attributeDefName == null && exceptionIfNotFound) {
      throw new AttributeDefNameNotFoundException("Cant find (or not allowed to find) attribute def name by id: " + id);
    }
    return attributeDefName;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  public void saveOrUpdate(AttributeDefName attributeDefName) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDefName);
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
    
    AttributeDefName attributeDefName = HibernateSession.byHqlStatic()
      .createQuery("select a from AttributeDefName as a where a.nameDb = :value")
      .options(queryOptions)
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByName")
      .setString("value", name).uniqueResult(AttributeDefName.class);
  
    attributeDefName = filterSecurity(attributeDefName);
  
    //handle exceptions out of data access method...
    if (attributeDefName == null && exceptionIfNotFound) {
      throw new AttributeDefNameNotFoundException("Cannot find (or not allowed to find) attribute def name with name: '" + name + "'");
    }
    return attributeDefName;
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
        .createQuery("from AttributeDefName where stemId = :id")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByStem")
        .setString("id", id)
        .listSet(AttributeDefName.class);
    
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
    try {
      AttributeDefName attributeDefName = HibernateSession.byHqlStatic()
        .createQuery("from AttributeDefName as theAttributeDefName where theAttributeDefName.id = :theId or theAttributeDefName.nameDb = :theName")
        .setCacheable(true)
        .options(queryOptions)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .setString("theId", id)
        .setString("theName", name)
        .uniqueResult(AttributeDefName.class);
      if (attributeDefName == null && exceptionIfNotFound) {
        throw new GroupNotFoundException("Can't find attributeDefName by id: '" + id + "' or name '" + name + "'");
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
        sqlTables, sqlWhereClause, "attributeDefName.attributeDefId", AttributeDefPrivilege.READ_PRIVILEGES);

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
   * @param filterByServicesCanView if true then only return services the user can view based on memberships which have the 
   * service, or folders which have the service, or attribute definitions which have the service,
   * or the view privilege set
   * @param serviceRole if filtering by services the user has in a certain role
   * @return  attribute def names
   * 
   */
  private Set<AttributeDefName> findAllAttributeNamesSecureHelper(String scope,
      GrouperSession grouperSession, String attributeDefId, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, boolean splitScope, AttributeAssignType attributeAssignType,
      AttributeDefType attributeDefType, ServiceRole serviceRole) {
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theAttributeDefName.displayNameDb");
    }
  
    Member member = subject == null ? null : MemberFinder.findBySubject(grouperSession, subject, true);
    
    StringBuilder sql = new StringBuilder(
        "select distinct theAttributeDefName from AttributeDefName theAttributeDefName, AttributeDef theAttributeDef ");

    if (serviceRole != null) {
      sql.append(", ServiceRoleView theServiceRoleView ");
    }
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    StringBuilder whereClause = new StringBuilder(" theAttributeDefName.attributeDefId = theAttributeDef.id ");

    if (serviceRole != null) {
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
        whereClause.append(" ( lower(theAttributeDefName.nameDb) like :scope" + index 
            + " or lower(theAttributeDefName.displayNameDb) like :scope" + index 
            + " or lower(theAttributeDefName.description) like :scope" + index + " ) ");
        if (!theScope.endsWith("%")) {
          theScope += "%";
        }
        if (splitScope) {
          if (!theScope.startsWith("%")) {
            theScope = "%" + theScope;
          }
        }
        byHqlStatic.setString("scope" + index, theScope);
        index++;
      }
      whereClause.append(" ) ) ");
    }
  
    boolean changedQuery = false;
    
    if (serviceRole != null) {
      
      if (member == null) {
        throw new RuntimeException("If filtering by serviceRole: " + serviceRole + ", then pass in a subject who can see the services");
      }
      
      //must be a service type
      whereClause.append(" and theAttributeDefName.id = theServiceRoleView.serviceNameId ");

      changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
          grouperSession.getSubject(), byHqlStatic, 
          sql, "theServiceRoleView.groupId", AccessPrivilege.READ_PRIVILEGES);
      
      //fields for the service role
      HibUtils.convertFieldsToSqlInString(serviceRole.fieldsForGroupQuery(), byHqlStatic, whereClause, "theServiceRoleView.fieldId");

      whereClause.append(" and theServiceRoleView.memberId = :groupMemberId ");
      byHqlStatic.setString("groupMemberId", member.getUuid());

      //no need to do security for services, right????
      
    } else {

      //see if we are adding more to the query
      grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(subject, byHqlStatic,
          sql, whereClause, "theAttributeDefName.attributeDefId", privileges);
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
    
  
    return attributeDefNames;
  
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
        null);
  }

  /**
   * not a secure method, find by id index
   */
  @Override
  public AttributeDefName findByIdIndex(Long idIndex, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws AttributeDefNameNotFoundException {
    
    StringBuilder hql = new StringBuilder("select theAttributeDefName from AttributeDefName as theAttributeDefName where (theAttributeDefName.idIndex = :theIdIndex)");
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .setCacheable(true).setCacheRegion(KLASS + ".FindByIdIndex");
    
    byHqlStatic.createQuery(hql.toString());
    
    AttributeDefName attributeDefName = byHqlStatic.setLong("theIdIndex", idIndex).uniqueResult(AttributeDefName.class);

    //handle exceptions out of data access method...
    if (attributeDefName == null && exceptionIfNotFound) {
      throw new AttributeDefNameNotFoundException("Cannot find AttributeDefName with idIndex: '" + idIndex + "'");
    }
    return attributeDefName;
    
  }


  /**
   * secure method, find by id index
   */
  @Override
  public AttributeDefName findByIdIndexSecure(Long idIndex, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws AttributeDefNameNotFoundException {
    
    AttributeDefName attributeDefName = findByIdIndex(idIndex, exceptionIfNotFound, queryOptions);
    
    attributeDefName = filterSecurity(attributeDefName);

    //handle exceptions out of data access method...
    if (attributeDefName == null && exceptionIfNotFound) {
      throw new AttributeDefNameNotFoundException("Cannot find AttributeDefName with idIndex: '" + idIndex + "'");
    }
    return attributeDefName;
    
  }

  /**
   * @see AttributeDefNameDAO#findAllAttributeNamesSecure(String, boolean, GrouperSession, String, Subject, Set, QueryOptions, AttributeAssignType, AttributeDefType, ServiceRole)
   */
  @Override
  public Set<AttributeDefName> findAllAttributeNamesSecure(String scope,
      boolean splitScope, GrouperSession grouperSession, String attributeDefId,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions,
      AttributeAssignType attributeAssignType, AttributeDefType attributeDefType,
      ServiceRole serviceRole) {
    return findAllAttributeNamesSecureHelper(scope, grouperSession, attributeDefId, subject, 
        privileges, queryOptions, true, attributeAssignType, attributeDefType, serviceRole);
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
        privileges, queryOptions, true, attributeAssignType, attributeDefType, null);
  }

} 

