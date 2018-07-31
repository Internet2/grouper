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
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.CompositeFinder;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperAccessAdapter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.entity.EntityUtils;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByCriteriaStatic;
import edu.internet2.middleware.grouper.hibernate.ByHql;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.dao.QuerySortField;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Basic Hibernate <code>Group</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GroupDAO.java,v 1.51 2009-12-10 08:54:15 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3GroupDAO extends Hib3DAO implements GroupDAO {
  
  /** */
  private static GrouperCache<String, Boolean> existsCache = null;
  
  /**
   * lazy load
   * @return cache
   */
  private static GrouperCache<String, Boolean> getExistsCache() {
    if(existsCache==null) {
      synchronized(Hib3GroupDAO.class) {
        if (existsCache == null) {
          existsCache=new GrouperCache<String, Boolean>("edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GroupDAO.exists",
                1000, false, 30, 120, false);
        }
      }
    }
    return existsCache;
  }


  /** */
  private static final String KLASS = Hib3GroupDAO.class.getName();

  /**
   * put in cache
   * @param uuid
   * @param exists
   */
  public void putInExistsCache(String uuid, boolean exists) {
    getExistsCache().put(uuid, exists);
  }

  /**
   * @param _g 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void delete(final Group _g)
    throws  GrouperDAOException {

    if (GrouperConfig.retrieveConfig().propertyValueBoolean("group.checkForFactorWhenDeletingGroup", true)) {

      Set<Composite> composites = CompositeFinder.findAsFactor(_g);
      
      if (GrouperUtil.length(composites) > 0) {
        StringBuilder ownerList = new StringBuilder();
        boolean firstLine = true;
        for (Composite composite : composites) {
          
          try {
            if (!firstLine) {
              ownerList.append(", ");
            }
            
            Group owner = composite.getOwnerGroup();
  
            ownerList.append(owner.getName());
            
          } catch (GroupNotFoundException gnfe) {
            ownerList.append("not allowed to VIEW group");
          }
          
          firstLine = false;
          
        }
  
        throw new RuntimeException("Cant delete group " + _g.getName() 
            + ", since it is a factor of composite(s): " + ownerList);
      }
    }
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            ByObject byObject = hibernateSession.byObject();         

            // delete role sets (only the ones underneath of this one, 
            // others might cause foreign key problems
            if (TypeOfGroup.role.equals(_g.getTypeOfGroup())) {
              GrouperDAOFactory.getFactory().getRoleSet().deleteByIfHasRole(_g);
            }

            // delete group sets
            GrouperDAOFactory.getFactory().getGroupSet().deleteSelfByOwnerGroup(_g);

            // delete group
            byObject.delete( _g );
            return null;
          }
    });

  } 

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Hib3GroupDAO.class);

  /**
   * @param uuid 
   * @return if exists
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public boolean exists(String uuid)
    throws  GrouperDAOException {
    if ( getExistsCache().containsKey(uuid) ) {
      return getExistsCache().get(uuid).booleanValue();
    }
    
    Object id = HibernateSession.byHqlStatic()
      .createQuery("select theGroup.uuid from Group as theGroup where theGroup.uuid = :uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".Exists")
      .setString("uuid", uuid).uniqueResult(Object.class);
    
    boolean rv  = false;
    if ( id != null ) {
      rv = true;
    }
    getExistsCache().put(uuid, rv);
    return rv;
  } 

  /**
   * @param val 
   * @return  set
   * @throws GrouperDAOException 
   * @throws IllegalStateException 
   * @since   @HEAD@
   */
  public Set<Group> findAllByAnyApproximateAttr(final String val) 
    throws  GrouperDAOException,
            IllegalStateException {
    return findAllByAnyApproximateAttr(val, null);
          }

  /**
   * @param val 
   * @param scope
   * @return set
   * @throws GrouperDAOException
   * @throws IllegalStateException
   * @since   @HEAD@
   */
  public Set<Group> findAllByAnyApproximateAttr(final String val, final String scope)
    throws  GrouperDAOException,
            IllegalStateException {
    return findAllByAnyApproximateAttr(val, scope, false);
  } 

  /**
   * @param val 
   * @param scope
   * @param secureQuery
   * @return set
   * @throws GrouperDAOException
   * @throws IllegalStateException
   * @since   @HEAD@
   */
  public Set<Group> findAllByAnyApproximateAttr(final String val, final String scope, final boolean secureQuery)
    throws  GrouperDAOException,
            IllegalStateException {

    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            ByHqlStatic byHql = HibernateSession.byHqlStatic();
            
            
            StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
          
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();
            
            //see if we are adding more to the query
            boolean changedQuery = false;
            StringBuilder sqlWhereClause = new StringBuilder();
                
            if (secureQuery) {
                changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);
            }
            
            if (changedQuery && hql.toString().contains(" where ")) {
              hql.append(" and ");
            } else {
              hql.append(" where ");
            }
                        
            hql.append(" (lower(theGroup.nameDb) like :value or ");
            hql.append(" lower(theGroup.extensionDb) like :value or ");
            hql.append(" lower(theGroup.displayNameDb) like :value or ");
            hql.append(" lower(theGroup.displayExtensionDb) like :value or ");
            hql.append(" lower(theGroup.descriptionDb) like :value ");

            String legacyAttributeStemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
            String legacyAttributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");

            hql.append(" or exists ");
            hql.append("(select value from AttributeAssignValue value, AttributeAssign assign, AttributeAssign assign2, AttributeDefName name, AttributeDefName name2");
            
            if (secureQuery) {
              grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
                  grouperSession.getSubject(), byHql, 
                  hql, sqlWhereClause, "name.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
              
              grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
                  grouperSession.getSubject(), byHql, 
                  hql, sqlWhereClause, "name2.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
              
              changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                  grouperSession.getSubject(), byHql, 
                  hql, "theGroup.uuid", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);
            }
            
            if (changedQuery && hql.toString().contains(" where ")) {
              hql.append(" and ");
            } else {
              hql.append(" where ");
            }
            
            if (sqlWhereClause.toString().trim().length() > 0) {
              hql.append(sqlWhereClause).append(" and ");
            }
            
            hql.append(" assign.ownerGroupId = theGroup.uuid and assign.id = assign2.ownerAttributeAssignId and assign2.id = value.attributeAssignId and assign2.attributeDefNameId = name2.id and assign.attributeDefNameId = name.id and " +
              "name2.nameDb like :attributeName and lower(value.valueString) like :value)) ");
            byHql.setString("attributeName", legacyAttributeStemName + ":" + legacyAttributePrefix + "%");

            if (!StringUtils.isBlank(scope)) {
              hql.append(" and theGroup.nameDb like :scope");
              byHql.setString("scope", scope + "%");
            }

            byHql.createQuery(hql.toString());
            Set<Group> groups = byHql.setCacheable(false).setCacheRegion(KLASS + ".FindAllByApproximateAttr")
              .setString( "value", "%" + val.toLowerCase() + "%" ).listSet(Group.class);

            return groups;
          }
    });

    return resultGroups;
  } 

  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This method will generate a full table scan of the attributes table.  It will not
   * perform well if there are a large number of groups.</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @see     GroupDAO#findAllByApproximateAttr(String, String)
   * @since   @HEAD@
   */
  public Set<Group> findAllByApproximateAttr(final String attr, final String val) 
    throws  GrouperDAOException,
            IllegalStateException
  {
    return findAllByApproximateAttrHelper(attr, val, null, false);
  }

  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This method will generate a full table scan of the attributes table.  It will not
   * perform well if there are a large number of groups.</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @see     GroupDAO#findAllByApproximateAttr(String, String, String)
   * @since   @HEAD@
   */
  public Set<Group> findAllByApproximateAttr(final String attr, final String val, final String scope)
    throws  GrouperDAOException,
            IllegalStateException {
    return findAllByApproximateAttrHelper(attr, val, scope, false);
  }

  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This method will generate a full table scan of the attributes table.  It will not
   * perform well if there are a large number of groups.</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @param attr attr name
   * @param val value
   * @param scope folder to search in
   * @param secureQuery if restrict to who can view
   * @return  groups
   */
  private Set<Group> findAllByApproximateAttrHelper(final String attr, final String val, final String scope, final boolean secureQuery) {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            ByHqlStatic byHql = HibernateSession.byHqlStatic();
            
            
            StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
            if (!Group._internal_fieldAttribute(attr)) {
              hql.append(", AttributeAssignValue value, AttributeAssign assign, AttributeAssign assign2, AttributeDefName name, AttributeDefName name2 ");
            }
          
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();
            
            //see if we are adding more to the query
            boolean changedQuery = false;
            StringBuilder sqlWhereClause = new StringBuilder();
            
            if (secureQuery) {
              if (Group._internal_fieldAttribute(attr)) {
              changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                  grouperSession.getSubject(), byHql, 
                  hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);
              } else {
                grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, sqlWhereClause, "name.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
                
                grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, sqlWhereClause, "name2.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
                
                changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, "theGroup.uuid", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);
              }
            }
            
            if (changedQuery && hql.toString().contains(" where ")) {
              hql.append(" and ");
            } else {
              hql.append(" where ");
            }
            
            if (sqlWhereClause.toString().trim().length() > 0) {
              hql.append(sqlWhereClause).append(" and ");
            }
                        
            if (Group._internal_fieldAttribute(attr)) {
              hql.append(" lower(theGroup." + attr + "Db) like :value ");
            } else {
              String legacyAttributeStemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
              String legacyAttributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");

              hql.append(" assign.ownerGroupId = theGroup.uuid and assign.id = assign2.ownerAttributeAssignId and assign2.id = value.attributeAssignId and assign2.attributeDefNameId = name2.id and assign.attributeDefNameId = name.id and " +
                "name2.nameDb = :attributeName and lower(value.valueString) like :value ");
              
              String attributePrefix = legacyAttributeStemName + ":" + legacyAttributePrefix;
              if (attr.startsWith(attributePrefix)) {
                byHql.setString("attributeName",  attr);
              } else {
                byHql.setString("attributeName",  attributePrefix + attr);
              }
            }
            if (!StringUtils.isBlank(scope)) {
              hql.append(" and theGroup.nameDb like :scope");
              byHql.setString("scope", scope + "%");
            }

            byHql.createQuery(hql.toString());
            Set<Group> groups = byHql.setCacheable(false).setCacheRegion(KLASS + ".FindAllByApproximateAttr")
              .setString( "value", "%" + val.toLowerCase() + "%" ).listSet(Group.class);
 
            return groups;
            
          }
    });
 
    return resultGroups;
  }


  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This method will generate a full table scan of the groups table.  It will not
   * perform well if there are a large number of groups.</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @see     GroupDAO#findAllByApproximateName(String)
   * @since   @HEAD@
   */
  public Set<Group> findAllByApproximateName(final String name)
      throws GrouperDAOException {
    return findAllByApproximateNameHelper(name, null, true, true, null);
  } 

  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This method will generate a full table scan of the groups table.  It will not
   * perform well if there are a large number of groups.</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @see     GroupDAO#findAllByApproximateName(String, String)
   * @since   @HEAD@
   */
  public Set<Group> findAllByApproximateName(final String name, final String scope)
      throws GrouperDAOException {
    return findAllByApproximateNameHelper(name, scope, true, true, null);
  }
  
  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This method will generate a full table scan of the groups table.  It will not
   * perform well if there are a large number of groups.</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @param name 
   * @param scope 
   * @param queryOptions 
   * @return the groups
   * @throws GrouperDAOException 
   * @see     GroupDAO#findAllByApproximateName(String, String)
   * @since   @HEAD@
   */
  public Set<Group> findAllByApproximateNameSecure(final String name, final String scope, QueryOptions queryOptions)
      throws GrouperDAOException {
    return findAllByApproximateNameSecureHelper(name, scope, true, true, queryOptions, null);
  }

  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This method will generate a full table scan of the groups table.  It will not
   * perform well if there are a large number of groups.</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @param name 
   * @param scope 
   * @param queryOptions 
   * @param typeOfGroups
   * @return the groups
   * @throws GrouperDAOException 
   * @see     GroupDAO#findAllByApproximateName(String, String)
   * @since   @HEAD@
   */
  public Set<Group> findAllByApproximateNameSecure(final String name, final String scope, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups) {
    return findAllByApproximateNameSecureHelper(name, scope, true, true, queryOptions, typeOfGroups);
  }

  /**
   * Find groups using an approximate string for the current name,
   * display name, extension, display extension.
   * @param name
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Group> findAllByApproximateCurrentName(final String name)
      throws GrouperDAOException {
    return findAllByApproximateNameHelper(name, null, true, false, null);
  }

  /**
   * Find groups using an approximate string for the current name,
   * display name, extension, display extension.
   * @param name
   * @param scope
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Group> findAllByApproximateCurrentName(final String name, final String scope)
      throws GrouperDAOException {
    return findAllByApproximateNameHelper(name, scope, true, false, null);
  }
  
  /**
   * Find groups using an approximate string for the alternate name.
   * @param name
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Group> findAllByApproximateAlternateName(final String name)
      throws GrouperDAOException {
    return findAllByApproximateNameHelper(name, null, false, true, null);
  }

  
  /**
   * Find groups using an approximate string for the alternate name.
   * @param name
   * @param scope
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Group> findAllByApproximateAlternateName(final String name,
      final String scope) throws GrouperDAOException {
    return findAllByApproximateNameHelper(name, scope, false, true, null);
  }

  
  /**
   * Helper for find by approximate name queries
   * @param name
   * @param scope
   * @param currentNames
   * @param alternateNames
   * @param queryOptions 
   * @return set
   * @throws GrouperDAOException
   * @throws IllegalStateException
   */
  private Set<Group> findAllByApproximateNameHelper(final String name, final String scope,
      final boolean currentNames, final boolean alternateNames, final QueryOptions queryOptions)
      throws GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            List<Criterion> criterionList = new ArrayList<Criterion>();
            Junction nameFieldsOr = Restrictions.disjunction();
            
            if (currentNames) {
              nameFieldsOr.add(Restrictions.ilike("nameDb", name, MatchMode.ANYWHERE));
              nameFieldsOr.add(Restrictions.ilike("displayNameDb", name, MatchMode.ANYWHERE));
              //these are substrings, why would they be there???
              //nameFieldsOr.add(Restrictions.ilike("extensionDb", name, MatchMode.ANYWHERE));
              //nameFieldsOr.add(Restrictions.ilike("displayExtensionDb", name, MatchMode.ANYWHERE));
            } 

            if (alternateNames) {
              nameFieldsOr.add(Restrictions.ilike("alternateNameDb", name, MatchMode.ANYWHERE));
            }
            
            criterionList.add(nameFieldsOr);
            
            if (scope != null) {
              criterionList.add(Restrictions.like("nameDb", scope, MatchMode.START));
            }
            ByCriteriaStatic byCriteriaStatic = HibernateSession.byCriteriaStatic();
            
            //reset sorting
            if (queryOptions != null) {
              
              massageSortFields(queryOptions.getQuerySort());
              
              byCriteriaStatic.options(queryOptions);
            }
            byCriteriaStatic.setCacheable(false);
            byCriteriaStatic.setAlias("theGroup");
            byCriteriaStatic.setCacheRegion(KLASS + ".FindAllByApproximateName");
            
            Set<Group> groups = byCriteriaStatic.listSet(Group.class,
                HibUtils.listCrit(criterionList));
            
            return groups;
          }
    });
    return resultGroups;
  }

  /**
   * if there are sort fields, go through them, and replace name with nameDb, etc,
   * extension for extensionDb, displayName with displayNameDb, and displayExtension with displayExtensionDb
   * @param querySort
   */
  public static void massageSortFields(QuerySort querySort) {
    massageSortFields(querySort, "theGroup");
  }

  /**
   * if there are sort fields, go through them, and replace name with nameDb, etc,
   * extension for extensionDb, displayName with displayNameDb, and displayExtension with displayExtensionDb
   * @param querySort
   * @param alias is the hql query alias
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
      if (StringUtils.equals("displayExtension", querySortField.getColumn())
          || StringUtils.equals("display_extension", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".displayExtensionDb");
      }
      if (StringUtils.equals("displayName", querySortField.getColumn())
          || StringUtils.equals("display_name", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".displayNameDb");
      }
    }
  }

  /**
   * @param d
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByCreatedAfter(final Date d) 
    throws  GrouperDAOException {
    return this._internal_findAllByDateHelper(d, false, null, true, "createTimeLong");
  }

  /**
   * helper for date queries
   * @param d
   * @param includeScope 
   * @param scope 
   * @param findAllAfter 
   * @param dateField 
   * @return set
   * @throws GrouperDAOException
   */
  private Set<Group> _internal_findAllByDateHelper(final Date d, final boolean includeScope, final String scope, 
      final boolean findAllAfter, final String dateField)  
    throws  GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            List<Criterion> criterionList = new ArrayList<Criterion>();
            
            if (findAllAfter) {
              criterionList.add(Restrictions.gt(dateField, d.getTime()));
            } else {
              criterionList.add(Restrictions.lt(dateField, d.getTime()));
            }
            
            if (includeScope) {
              criterionList.add(Restrictions.ilike("nameDb", scope, MatchMode.START));
            }
            HibernateSession.byCriteriaStatic().setCacheable(false);
            HibernateSession.byCriteriaStatic().setCacheRegion(KLASS + ".FindAllByCreatedAfter");
            
            Set<Group> groups = HibernateSession.byCriteriaStatic().listSet(Group.class, 
                HibUtils.listCrit(criterionList));
            
            return groups;
          }
    });
    return resultGroups;
  }


  /**
   * @param d
   * @param scope
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByCreatedAfter(final Date d, final String scope) {
    return this._internal_findAllByDateHelper(d, true, scope, true, "createTimeLong");
  }


  /**
   * @param d 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Group> findAllByCreatedBefore(final Date d) 
    throws  GrouperDAOException {
    return this._internal_findAllByDateHelper(d, false, null, false, "createTimeLong");
  } 

  /**
   * @param d 
   * @param scope
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Group> findAllByCreatedBefore(final Date d, final String scope)
    throws  GrouperDAOException {
    return this._internal_findAllByDateHelper(d, true, scope, false, "createTimeLong");
  }


  /**
   * @param d
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByModifiedAfter(final Date d) 
    throws  GrouperDAOException {
    
    return this._internal_findAllByDateHelper(d, false, null, true, "modifyTimeLong");
  }

  /**
   * @param d
   * @param scope
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByModifiedAfter(final Date d, final String scope)
    throws  GrouperDAOException {

    return this._internal_findAllByDateHelper(d, true, scope, true, "modifyTimeLong");
  }


  /**
   * @param d
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByModifiedBefore(final Date d) 
    throws  GrouperDAOException {
    return this._internal_findAllByDateHelper(d, false, null, false, "modifyTimeLong");
  } 

  /**
   * @param d
   * @param scope
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByModifiedBefore(final Date d, final String scope)
    throws  GrouperDAOException {
    return this._internal_findAllByDateHelper(d, true, scope, false, "modifyTimeLong");
  }

  /**
   * @param d
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByLastMembershipBefore(final Date d) 
    throws  GrouperDAOException {
    return this._internal_findAllByDateHelper(d, false, null, false, "lastMembershipChangeDb");
  } 

  /**
   * @param d
   * @param scope
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByLastMembershipBefore(final Date d, final String scope)
    throws  GrouperDAOException {
    return this._internal_findAllByDateHelper(d, true, scope, false, "lastMembershipChangeDb");
  }
  
  /**
   * @param d
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByLastMembershipAfter(final Date d) 
    throws  GrouperDAOException {
    
    return this._internal_findAllByDateHelper(d, false, null, true, "lastMembershipChangeDb");
  }

  /**
   * @param d
   * @param scope
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByLastMembershipAfter(final Date d, final String scope)
    throws  GrouperDAOException {

    return this._internal_findAllByDateHelper(d, true, scope, true, "lastMembershipChangeDb");
  }


  /**
   * Note, this doesnt cache
   * @param _gt
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByType(final GroupType _gt) 
    throws  GrouperDAOException {
    
    return findAllByType(_gt, new QueryOptions().secondLevelCache(false));
    
  } 

  /**
   * @param _gt
   * @param scope
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByType(final GroupType _gt, final String scope)
    throws  GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

            Set<Group> groups = hibernateSession.byHql().createQuery(
                "select theGroup from Group as theGroup, AttributeAssign groupTypeAssign " +
                "where groupTypeAssign.attributeDefNameId = :type " +
                "and groupTypeAssign.ownerGroupId = theGroup.uuid " +
                "and theGroup.nameDb like :scope"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByType")
              .setString("type", _gt.getUuid()).setString("scope", scope + "%").listSet(Group.class);

            return groups;
          }
    });
    return resultGroups;
  } 

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findByAttribute(java.lang.String, java.lang.String, boolean, boolean)
   */
  public Group findByAttribute(final String attr, final String val, final boolean exceptionIfNotFound, final boolean secureQuery) 
      throws GrouperDAOException, GroupNotFoundException {
    Group result = (Group)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            ByHqlStatic byHql = HibernateSession.byHqlStatic();
            
            
            StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
            if (!Group._internal_fieldAttribute(attr)) {
              hql.append(", AttributeAssignValue value, AttributeAssign assign, AttributeAssign assign2, AttributeDefName name, AttributeDefName name2 ");
            }
          
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();
            
            //see if we are adding more to the query
            boolean changedQuery = false;
            StringBuilder sqlWhereClause = new StringBuilder();
    
            if (secureQuery) {
              if (Group._internal_fieldAttribute(attr)) {
                changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);
              } else {
                grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, sqlWhereClause, "name.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
                
                grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, sqlWhereClause, "name2.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
                
                changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, "theGroup.uuid", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);
              }
            }
            
            if (changedQuery && hql.toString().contains(" where ")) {
              hql.append(" and ");
            } else {
              hql.append(" where ");
            }
            
            if (sqlWhereClause.toString().trim().length() > 0) {
              hql.append(sqlWhereClause).append(" and ");
            }
    
            if (Group._internal_fieldAttribute(attr)) {
              hql.append(" theGroup." + attr + "Db = :value ");
            } else {
              String legacyAttributeStemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
              String legacyAttributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");

              hql.append(" assign.ownerGroupId = theGroup.uuid and assign.id = assign2.ownerAttributeAssignId and assign2.id = value.attributeAssignId and assign2.attributeDefNameId = name2.id and assign.attributeDefNameId = name.id and " +
                "name2.nameDb = :attributeName and value.valueString like :value ");
              
              String attributePrefix = legacyAttributeStemName + ":" + legacyAttributePrefix;
              if (attr.startsWith(attributePrefix)) {
                byHql.setString("attributeName", attr);
              } else {
                byHql.setString("attributeName", attributePrefix + attr);
              }
            }

            byHql.createQuery(hql.toString());
            Group group = byHql.setCacheable(false).setCacheRegion(KLASS + ".FindByAttribute")
              .setString("value", val).uniqueResult(Group.class);
    
            return group;
            
          }
    });
 
    if (result == null && exceptionIfNotFound) {
       throw new GroupNotFoundException();
     }
    
    return result;
  }

  /**
   * @param attr
   * @param val
   * @param exceptionIfNotFound 
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   * @since   @HEAD@
   */
  public Group findByAttribute(String attr, String val, boolean exceptionIfNotFound)
      throws GrouperDAOException, GroupNotFoundException {
    return findByAttribute(attr, val, exceptionIfNotFound, false);
  }


  /**
   * @param attr
   * @param val
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   * @deprecated use overload
   */
  @Deprecated
  public Group findByAttribute(String attr, String val) 
    throws  GrouperDAOException,
            GroupNotFoundException {
    return findByAttribute(attr, val, true);
  } 

  /**
   * @param name
   * @param exceptionIfNotFound
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   */
  public Group findByName(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, GroupNotFoundException {
    return findByName(name, exceptionIfNotFound, null);
  }

  /**
   * @param name
   * @param exceptionIfNotFound exception if cant find group
   * @param queryOptions if we should use cache or not
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   * @since   @HEAD@
   */
  public Group findByName(final String name, boolean exceptionIfNotFound, QueryOptions queryOptions) 
    throws  GrouperDAOException,
            GroupNotFoundException {
    return findByName(name, exceptionIfNotFound, queryOptions, null);
  }

  /**
   * @param name
   * @param exceptionIfNotFound exception if cant find group
   * @param queryOptions if we should use cache or not
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   * @since   @HEAD@
   */
  public Group findByName(final String name, boolean exceptionIfNotFound, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups) 
    throws  GrouperDAOException,
            GroupNotFoundException {
    
    StringBuilder hql = new StringBuilder("select theGroup from Group as theGroup where (theGroup.nameDb = :value or theGroup.alternateNameDb = :value)");
    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .setCacheable(true).setCacheRegion(KLASS + ".FindByName").options(queryOptions);
        
    byHqlStatic.createQuery(hql.toString());
    
    Group group = byHqlStatic.setString("value", name).uniqueResult(Group.class);

    if (group != null && GrouperUtil.length(typeOfGroups) > 0) {
      // see if the type of group matches
      if (!typeOfGroups.contains(group.getTypeOfGroup())) {
        group = null;
      }
    }
    
    //System.out.println("Group: " + name + ", found? " + (group!=null));
    
    //handle exceptions out of data access method...
    if (group == null && exceptionIfNotFound) {
      throw new GroupNotFoundException("Cannot find group with name: '" + name + "'");
    }
    return group;

  }
  
  public Set<Group> findByApproximateDescriptionSecure(final String description, 
      final QueryOptions queryOptions, final Set<TypeOfGroup> typeOfGroups) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);

    if (changedQuery && hql.toString().contains(" where ")) {
      hql.append(" and ");
    } else {
      hql.append(" where ");
    }
    hql.append(" lower(theGroup.descriptionDb) like :value ");

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
    
    TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, hql, byHqlStatic);
    String hqlString = hql.toString();
    Set<Group> resultGroups = new HashSet<Group>();
    if (!hqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      byHqlStatic.createQuery(hqlString).options(queryOptions);
  
      resultGroups = byHqlStatic.setString("value", "%"+ description.toLowerCase() + "%").listSet(Group.class);
    }
    
    //if the hql didnt filter, this will
    Set<Group> filteredGroups = grouperSession.getAccessResolver()
      .postHqlFilterGroups(resultGroups, grouperSession.getSubject(), AccessPrivilege.VIEW_PRIVILEGES);
    
    return filteredGroups;
    
  }
  
  public Set<Group> findByDescriptionSecure(final String description, final QueryOptions queryOptions,
      final Set<TypeOfGroup> typeOfGroups) {

    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);

    if (changedQuery && hql.toString().contains(" where ")) {
      hql.append(" and ");
    } else {
      hql.append(" where ");
    }
    
    hql.append(" theGroup.descriptionDb = :value ");

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
    
    Set<Group> resultGroups = null;
    TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, hql, byHqlStatic);
    String hqlString = hql.toString();

    if (!hqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      byHqlStatic.createQuery(hqlString)
        .setCacheable(true).setCacheRegion(KLASS + ".FindByNameSecure").options(queryOptions);
  
      resultGroups = byHqlStatic.setString("value", description).listSet(Group.class);
    }
    
    //if the hql didnt filter, this will
    Set<Group> filteredGroups = grouperSession.getAccessResolver()
      .postHqlFilterGroups(resultGroups, grouperSession.getSubject(), AccessPrivilege.VIEW_PRIVILEGES);
    
    return filteredGroups;
  
  }
  
  public Set<Group> findByDisplayNameSecure(final String displayName, final QueryOptions queryOptions, 
      final Set<TypeOfGroup> typeOfGroups) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);

    if (changedQuery && hql.toString().contains(" where ")) {
      hql.append(" and ");
    } else {
      hql.append(" where ");
    }
    
    hql.append(" theGroup.displayNameDb = :value ");

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
    
    Set<Group> resultGroups = null;
    TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, hql, byHqlStatic);
    String hqlString = hql.toString();

    if (!hqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      byHqlStatic.createQuery(hqlString)
        .setCacheable(true).setCacheRegion(KLASS + ".FindByNameSecure").options(queryOptions);
  
      resultGroups = byHqlStatic.setString("value", displayName).listSet(Group.class);
    }
    
    
  //if the hql didnt filter, this will
    Set<Group> filteredGroups = grouperSession.getAccessResolver()
      .postHqlFilterGroups(resultGroups, grouperSession.getSubject(), AccessPrivilege.VIEW_PRIVILEGES);
    
    return filteredGroups;
  }
  
  public Set<Group> findByApproximateDisplayNameSecure(final String displayName, 
      final QueryOptions queryOptions, final Set<TypeOfGroup> typeOfGroups) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);

    if (changedQuery && hql.toString().contains(" where ")) {
      hql.append(" and ");
    } else {
      hql.append(" where ");
    }
    
    hql.append(" lower(theGroup.displayNameDb) like :value ");

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
    
    TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, hql, byHqlStatic);
    String hqlString = hql.toString();
    Set<Group> resultGroups = new HashSet<Group>();
    if (!hqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      byHqlStatic.createQuery(hqlString).options(queryOptions);
  
      resultGroups = byHqlStatic.setString("value", "%"+ displayName.toLowerCase() + "%").listSet(Group.class);
    }
    
    //if the hql didnt filter, this will
    Set<Group> filteredGroups = grouperSession.getAccessResolver()
      .postHqlFilterGroups(resultGroups, grouperSession.getSubject(), AccessPrivilege.VIEW_PRIVILEGES);
    
    return filteredGroups;
    
  }
  
  public Set<Group> findByExtensionSecure(final String extension, final QueryOptions queryOptions, 
      final Set<TypeOfGroup> typeOfGroups) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);

    if (changedQuery && hql.toString().contains(" where ")) {
      hql.append(" and ");
    } else {
      hql.append(" where ");
    }
    
    hql.append(" theGroup.extensionDb = :value ");

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
    
    Set<Group> resultGroups = null;
    TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, hql, byHqlStatic);
    String hqlString = hql.toString();

    if (!hqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      byHqlStatic.createQuery(hqlString)
        .setCacheable(true).setCacheRegion(KLASS + ".FindByNameSecure").options(queryOptions);
  
      resultGroups = byHqlStatic.setString("value", extension).listSet(Group.class);
    }
    
    
  //if the hql didnt filter, this will
    Set<Group> filteredGroups = grouperSession.getAccessResolver()
      .postHqlFilterGroups(resultGroups, grouperSession.getSubject(), AccessPrivilege.VIEW_PRIVILEGES);
    
    return filteredGroups;
  }
  
  public Set<Group> findByApproximateExtensionSecure(final String extension, 
      final QueryOptions queryOptions, final Set<TypeOfGroup> typeOfGroups) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);

    if (changedQuery && hql.toString().contains(" where ")) {
      hql.append(" and ");
    } else {
      hql.append(" where ");
    }
    
    hql.append(" lower(theGroup.extensionDb) like :value ");

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
    
    TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, hql, byHqlStatic);
    String hqlString = hql.toString();
    Set<Group> resultGroups = new HashSet<Group>();
    if (!hqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      byHqlStatic.createQuery(hqlString).options(queryOptions);
  
      resultGroups = byHqlStatic.setString("value", "%"+ extension.toLowerCase() + "%").listSet(Group.class);
    }
    
    //if the hql didnt filter, this will
    Set<Group> filteredGroups = grouperSession.getAccessResolver()
      .postHqlFilterGroups(resultGroups, grouperSession.getSubject(), AccessPrivilege.VIEW_PRIVILEGES);
    
    return filteredGroups;
    
  }
  
  public Set<Group> findByDisplayExtensionSecure(final String displayExtension, final QueryOptions queryOptions, 
      final Set<TypeOfGroup> typeOfGroups) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);

    if (changedQuery && hql.toString().contains(" where ")) {
      hql.append(" and ");
    } else {
      hql.append(" where ");
    }
    
    hql.append(" theGroup.displayExtensionDb = :value ");

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
    
    Set<Group> resultGroups = null;
    TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, hql, byHqlStatic);
    String hqlString = hql.toString();

    if (!hqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      byHqlStatic.createQuery(hqlString)
        .setCacheable(true).setCacheRegion(KLASS + ".FindByNameSecure").options(queryOptions);
  
      resultGroups = byHqlStatic.setString("value", displayExtension).listSet(Group.class);
    }
    
    
  //if the hql didnt filter, this will
    Set<Group> filteredGroups = grouperSession.getAccessResolver()
      .postHqlFilterGroups(resultGroups, grouperSession.getSubject(), AccessPrivilege.VIEW_PRIVILEGES);
    
    return filteredGroups;
  }
  
  public Set<Group> findByApproximateDisplayExtensionSecure(final String displayExtension, 
      final QueryOptions queryOptions, final Set<TypeOfGroup> typeOfGroups) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);

    if (changedQuery && hql.toString().contains(" where ")) {
      hql.append(" and ");
    } else {
      hql.append(" where ");
    }
    
    hql.append(" lower(theGroup.displayExtensionDb) like :value ");

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
    
    TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, hql, byHqlStatic);
    String hqlString = hql.toString();
    Set<Group> resultGroups = new HashSet<Group>();
    if (!hqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      byHqlStatic.createQuery(hqlString).options(queryOptions);
  
      resultGroups = byHqlStatic.setString("value", "%"+ displayExtension.toLowerCase() + "%").listSet(Group.class);
    }
    
    //if the hql didnt filter, this will
    Set<Group> filteredGroups = grouperSession.getAccessResolver()
      .postHqlFilterGroups(resultGroups, grouperSession.getSubject(), AccessPrivilege.VIEW_PRIVILEGES);
    
    return filteredGroups;
    
  }
  
  /**
   * @param name
   * @param exceptionIfNotFound exception if cant find group
   * @param queryOptions if we should use cache or not
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   * @since   @HEAD@
   */
  public Group findByNameSecure(final String name, boolean exceptionIfNotFound, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups) {
    return findByNameSecure(name, exceptionIfNotFound, queryOptions, typeOfGroups, AccessPrivilege.VIEW_PRIVILEGES);
  }
  
  /**
   * @param name
   * @param exceptionIfNotFound exception if cant find group
   * @param queryOptions if we should use cache or not
   * @param typeOfGroups
   * @param inPrivSet
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   * @since   @HEAD@
   */
  public Group findByNameSecure(final String name, boolean exceptionIfNotFound, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups, Set<Privilege> inPrivSet) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        hql, "theGroup.uuid", inPrivSet);

    if (changedQuery && hql.toString().contains(" where ")) {
      hql.append(" and ");
    } else {
      hql.append(" where ");
    }
    
    hql.append(" ( theGroup.nameDb = :value or theGroup.alternateNameDb = :value ) ");

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
    
    Group group = null;
    String hqlString = hql.toString();

    if (!hqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      byHqlStatic.createQuery(hqlString)
        .setCacheable(true).setCacheRegion(KLASS + ".FindByNameSecure").options(queryOptions);
  
      group = byHqlStatic.setString("value", name).uniqueResult(Group.class);
    }
    
    if (group != null && GrouperUtil.length(typeOfGroups) > 0) {
      // see if the type of group matches
      if (!typeOfGroups.contains(group.getTypeOfGroup())) {
        group = null;
      }
    }
    
    //System.out.println("Group: " + name + ", found? " + (group!=null));
    
    //handle exceptions out of data access method...
    if (group == null && exceptionIfNotFound) {
      throw new GroupNotFoundException("Cannot find group with name: '" + name + "'");
    }
    return group;

  }
  
  
  /**
   * Find a group by its current name only.
   * @param name
   * @param exceptionIfNotFound
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   */
  public Group findByCurrentName(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, GroupNotFoundException {
    Group group = HibernateSession.byHqlStatic()
      .createQuery("select theGroup from Group as theGroup where theGroup.nameDb = :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByCurrentName")
      .setString("value", name).uniqueResult(Group.class);

    //handle exceptions out of data access method...
    if (group == null && exceptionIfNotFound) {
      throw new GroupNotFoundException("Cannot find group with name: '" + name + "'");
    }
    return group;
  }
  
  /**
   * Find a group by its alternate name only.
   * @param name
   * @param exceptionIfNotFound
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   */
  public Group findByAlternateName(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, GroupNotFoundException {
    Group group = HibernateSession.byHqlStatic()
      .createQuery("select theGroup from Group as theGroup where theGroup.alternateNameDb = :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByAlternateName")
      .setString("value", name).uniqueResult(Group.class);

    //handle exceptions out of data access method...
    if (group == null && exceptionIfNotFound) {
      throw new GroupNotFoundException("Cannot find group with alternate name: '" + name + "'");
    }
    return group;
  }


  /**
   * @param name
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   * @deprecated use overload
   */
  @Deprecated
  public Group findByName(final String name) 
    throws  GrouperDAOException,
            GroupNotFoundException {
    return findByName(name, true);
  } 

  /**
   * <ol>
   * <li>Hibernate caching is enabled.</li>
   * </ol>
   * @see     GroupDAO#findByUuid(String)
   * @deprecated use overload
   */
  @Deprecated
  public Group findByUuid(String uuid) 
    throws  GrouperDAOException,
            GroupNotFoundException  {
    return findByUuid(uuid, true);
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findByUuid(java.lang.String, boolean)
   */
  public Group findByUuid(String uuid, boolean exceptionIfNotFound)
      throws GrouperDAOException, GroupNotFoundException {
    return findByUuid(uuid, exceptionIfNotFound, null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findByUuid(java.lang.String, boolean, QueryOptions)
   */
  public Group findByUuid(String uuid, boolean exceptionIfNotFound, QueryOptions queryOptions)
      throws GrouperDAOException, GroupNotFoundException {
    return findByUuid(uuid, exceptionIfNotFound, queryOptions, null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findByUuid(java.lang.String, boolean, QueryOptions, Set)
   */
  public Group findByUuid(String uuid, boolean exceptionIfNotFound, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups)
        throws GrouperDAOException, GroupNotFoundException {
    StringBuilder hql = new StringBuilder("from Group as theGroup where theGroup.uuid = :uuid ");
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }

    Group group = byHqlStatic
      .createQuery(hql.toString())
      .setCacheable(true)
      .options(queryOptions)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid).uniqueResult(Group.class);
    
    if (group != null && GrouperUtil.length(typeOfGroups) > 0) {
      // see if the type of group matches
      if (!typeOfGroups.contains(group.getTypeOfGroup())) {
        group = null;
      }
    }
    
    if (group == null && exceptionIfNotFound) {
       throw new GroupNotFoundException("Cant find group by uuid: " + uuid);
    }
    return group;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findByUuid(java.lang.String, boolean, QueryOptions, Set)
   */
  public Group findByUuidSecure(String uuid, boolean exceptionIfNotFound, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups)
      throws GrouperDAOException, GroupNotFoundException {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder("select distinct theGroup from Group as theGroup ");
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        sql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);

    if (changedQuery && sql.toString().contains(" where ")) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
    sql.append(" theGroup.uuid = :uuid ");

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }

    byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(true)
      .options(queryOptions)
      .setCacheRegion(KLASS + ".FindByUuidSecure")
      .setString("uuid", uuid);
    Group group = byHqlStatic.uniqueResult(Group.class);
    
    if (group != null && GrouperUtil.length(typeOfGroups) > 0) {
      // see if the type of group matches
      if (!typeOfGroups.contains(group.getTypeOfGroup())) {
        group = null;
      }
    }
    
    if (group == null && exceptionIfNotFound) {
       throw new GroupNotFoundException("Cant find group by uuid: " + uuid);
    }
    return group;
  }

  /**
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> getAllGroups()
    throws  GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

            Set<Group> groups = hibernateSession.byHql().createQuery(
                "select theGroup from Group as theGroup order by theGroup.displayNameDb")
              .setCacheable(false)
              .setCacheRegion(KLASS + ".GetAllGroups")
              .listSet(Group.class);

            return groups;
          }
    });

    return resultGroups;
  }

  /**
   * @param scope
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> getAllGroups(final String scope)
    throws  GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

            Set<Group> groups = hibernateSession.byHql().createQuery(
                "select theGroup from Group as theGroup where theGroup.nameDb like :scope order by theGroup.displayNameDb")
              .setCacheable(false)
              .setCacheRegion(KLASS + ".GetAllGroups")
              .setString("scope", scope + "%")
              .listSet(Group.class);

            return groups;
          }
    });

    return resultGroups;
  }

  /**
   * @param stem
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> getImmediateChildren(final Stem stem)
    throws  GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

            Set<Group> groups = hibernateSession.byHql().createQuery(
                "select theGroup from Group as theGroup where theGroup.parentUuid = :parent order by theGroup.displayNameDb")
              .setCacheable(false)
              .setCacheRegion(KLASS + ".GetImmediateChildren")
              .setString("parent", stem.getUuid())
              .listSet(Group.class);

            return groups;
          }
    });

    return resultGroups;
  }

  /**
   * @param _g
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public void update(Group _g)
    throws  GrouperDAOException {
    
    HibernateSession.byObjectStatic().update(_g);
    
  } 


  /**
   *
   * @param hibernateSession
   * @throws HibernateException
   */
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    //               but right now that is blowing up due to the session being flushed.
    hibernateSession.byHql().createQuery("delete from Group").executeUpdate();
    getExistsCache().clear();
  } 

  // @since 1.2.1         
//  /**
//   * @param hibernateSession 
//   * @param hib3GroupAttributes 
//   * @return the set of dtos
//   * @throws HibernateException 
//   * 
//   */
//  private Set<Group> _getGroupsFromGroupsAndAttributesQuery(HibernateSession hibernateSession, List<Object[]> hib3GroupAttributes)
//    throws  HibernateException {   
//    Iterator it = hib3GroupAttributes.iterator();
//    Map<String, Group> results = new HashMap<String, Group>();
//        
//    while (it.hasNext()) {
//      Object[] tuple = (Object[])it.next();
//      Group group = (Group)tuple[0];
//      String groupUuid = group.getUuid();
//      Map currAttributes = null;
//      if (results.containsKey(groupUuid)) {
//        group = (Group)results.get(groupUuid);
//        currAttributes = group.getAttributesDb();
//      } else {
//        currAttributes = new HashMap();
//      }
//      Attribute currAttribute = (Attribute)tuple[1];
//      HibUtils.evict(hibernateSession, currAttribute, true);
//      currAttributes.put(currAttribute.getAttrName(), currAttribute.getValue());
//      group.setAttributes(currAttributes);
//      results.put(groupUuid, group);
//    }
//    
//    Set groups = new LinkedHashSet(results.values());
//    HibUtils.evict(hibernateSession, results.values(), true);
//      
//    return groups;
//  } // private Set _getGroupsFromGroupsAndAttributesQuery(qry)
  
  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This looks for groups by exact attribute value</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findAllByAttr(java.lang.String, java.lang.String)
   */
  public Set<Group> findAllByAttr(final String attr, final String val) throws GrouperDAOException,
      IllegalStateException {
    return findAllByAttr(attr, val, null, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findAllByAttr(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public Set<Group> findAllByAttr(final String attr, final String val, final String scope, final boolean secureQuery) throws GrouperDAOException,
    IllegalStateException {
    
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            ByHqlStatic byHql = HibernateSession.byHqlStatic();
            
            
            StringBuilder hql = new StringBuilder("select distinct theGroup from Group as theGroup ");
            if (!Group._internal_fieldAttribute(attr)) {
              hql.append(", AttributeAssignValue value, AttributeAssign assign, AttributeAssign assign2, AttributeDefName name, AttributeDefName name2 ");
            }
          
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();
            
            //see if we are adding more to the query
            boolean changedQuery = false;
            StringBuilder sqlWhereClause = new StringBuilder();
                
            if (secureQuery) {
              if (Group._internal_fieldAttribute(attr)) {
                changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);
              } else {
                grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, sqlWhereClause, "name.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
                
                grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, sqlWhereClause, "name2.attributeDefId", AttributeDefPrivilege.ATTR_READ_PRIVILEGES);
                
                changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                    grouperSession.getSubject(), byHql, 
                    hql, "theGroup.uuid", AccessPrivilege.ATTRIBUTE_READ_PRIVILEGES);
              }
            }
            
            if (changedQuery && hql.toString().contains(" where ")) {
              hql.append(" and ");
            } else {
              hql.append(" where ");
            }
            
            if (sqlWhereClause.toString().trim().length() > 0) {
              hql.append(sqlWhereClause).append(" and ");
            }
                        
            if (Group._internal_fieldAttribute(attr)) {
              hql.append(" theGroup." + attr + "Db = :value ");
            } else {
              String legacyAttributeStemName = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.baseStem");
              String legacyAttributePrefix = GrouperConfig.retrieveConfig().propertyValueStringRequired("legacyAttribute.attribute.prefix");

              hql.append(" assign.ownerGroupId = theGroup.uuid and assign.id = assign2.ownerAttributeAssignId and assign2.id = value.attributeAssignId and assign2.attributeDefNameId = name2.id and assign.attributeDefNameId = name.id and " +
                "name2.nameDb = :attributeName and value.valueString = :value ");
              
              String attributePrefix = legacyAttributeStemName + ":" + legacyAttributePrefix;
              if (attr.startsWith(attributePrefix)) {
                byHql.setString("attributeName", attr);
              } else {
                byHql.setString("attributeName", attributePrefix + attr);
              }
            }
            if (!StringUtils.isBlank(scope)) {
              hql.append(" and theGroup.nameDb like :scope");
              byHql.setString("scope", scope + "%");
            }
            
            byHql.createQuery(hql.toString());
            Set<Group> groups = byHql.setCacheable(false).setCacheRegion(KLASS + ".FindAllByAttr")
              .setString("value", val).listSet(Group.class);
            
            return groups;
            
          }
    });

    return resultGroups;
  }

  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This looks for groups by exact attribute value</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findAllByAttr(java.lang.String, java.lang.String, java.lang.String)
   */
  public Set<Group> findAllByAttr(final String attr, final String val, final String scope) throws GrouperDAOException,
      IllegalStateException {
    return findAllByAttr(attr, val, scope, false);
  }
            
  /**
   * find groups by creator or modifier
   * @param member
   * @return the groups
   */
  public Set<Group> findByCreatorOrModifier(Member member) {
    if (member == null || StringUtils.isBlank(member.getUuid())) {
      throw new RuntimeException("Need to pass in a member");
    }
    Set<Group> groups = HibernateSession.byHqlStatic()
      .createQuery("from Group as theGroup where theGroup.creatorUuid = :uuid1 or theGroup.modifierUuid = :uuid2")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByCreatorOrModifier")
      .setString( "uuid1", member.getUuid() ).setString("uuid2", member.getUuid())
      .listSet(Group.class);
    return groups;

  }
  
  /**
   * @see GroupDAO#getAllGroupsSecure(GrouperSession, Subject, Set, QueryOptions, Set)
   */
  public Set<Group> getAllGroupsSecure(GrouperSession grouperSession, Subject subject,
      Set<Privilege> inPrivSet, QueryOptions queryOptions)
      throws GrouperDAOException {
    return getAllGroupsSecure(grouperSession, subject, inPrivSet, queryOptions, null);
  }



  /**
   * In this case, send in the attribute name to sort by (default is displayName).
   * Make sure the grouperSession can see the groups
   * @param grouperSession 
   * @param subject 
   * @param queryOptions 
   * @param inPrivSet means that each row must have a matching priv in this set to user or GrouperAll.
   * There are some constants in AccessPrivilege of pre-canned sets
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Group> getAllGroupsSecure(GrouperSession grouperSession, Subject subject, 
      Set<Privilege> inPrivSet, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups)
      throws  GrouperDAOException {
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }

    StringBuilder sql = new StringBuilder("select distinct theGroup from Group theGroup ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query
    grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", inPrivSet);

    TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, sql, byHqlStatic);
    
    try {

      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }
      
      String sqlString = sql.toString();
      Set<Group> groups = new HashSet<Group>();

      if (!sqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
        groups = byHqlStatic.createQuery(sqlString)
          .setCacheable(false)
          .setCacheRegion(KLASS + ".GetAllGroupsSecure")
          .options(queryOptions)
          .listSet(Group.class);
      }
            
      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, subject, inPrivSet);

      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getAllGroupsSecure(java.lang.String, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Group> getAllGroupsSecure(String scope, GrouperSession grouperSession,
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions) throws GrouperDAOException {
    return getAllGroupsSecure(scope, grouperSession, subject, inPrivSet, queryOptions, null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getAllGroupsSecure(java.lang.String, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions, Set)
   */
  public Set<Group> getAllGroupsSecure(final String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions,
      Set<TypeOfGroup> typeOfGroups)
    throws  GrouperDAOException {

    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }

    StringBuilder sql = new StringBuilder("select distinct theGroup from Group theGroup ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", inPrivSet);

    if (changedQuery && sql.toString().contains(" where ")) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }

    //this should be lower to make it easier to search for stuff
    sql.append("  lower(theGroup.nameDb) like :scope");
    
    TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, sql, byHqlStatic);
    
    try {

      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }
      
      String sqlString = sql.toString();
      Set<Group> groups = new HashSet<Group>();
      
      if (!sqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
        groups = byHqlStatic.createQuery(sqlString)
          .setString("scope", StringUtils.defaultString(scope).toLowerCase() + "%")
          .setCacheable(false)
          .setCacheRegion(KLASS + ".GetAllGroupsSecureScope")
          .options(queryOptions)
          .listSet(Group.class);
      }

      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, subject, inPrivSet);

      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getImmediateChildrenSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Group> getImmediateChildrenSecure(GrouperSession grouperSession, Stem stem,
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions) throws GrouperDAOException {
    return getImmediateChildrenSecure(grouperSession, stem, subject, inPrivSet, queryOptions, null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getImmediateChildrenSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions, Set)
   */
  public Set<Group> getImmediateChildrenSecure(GrouperSession grouperSession, 
      final Stem stem, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions,
      Set<TypeOfGroup> typeOfGroups)
    throws  GrouperDAOException {

    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }

    StringBuilder sql = new StringBuilder("select distinct theGroup from Group as theGroup ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", inPrivSet);

    if (changedQuery && sql.toString().contains(" where ")) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
    
    sql.append(" theGroup.parentUuid = :parent ");

    TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, sql, byHqlStatic);
    
    try {

      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }

      String sqlString = sql.toString();
      Set<Group> groups = new HashSet<Group>();
      
      if (!sqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
        groups = byHqlStatic.createQuery(sqlString)
          .setString("parent", stem.getUuid())
          .setCacheable(false)
          .setCacheRegion(KLASS + ".getImmediateChildrenSecure")
          .options(queryOptions)
          .listSet(Group.class);
      }

      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, subject, inPrivSet);

      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    }

  }


  /** batch size for memberships (setable for testing) */
  static int batchSize = 50;

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findByUuids(java.util.Collection, boolean)
   */
  public Set<Group> findByUuids(Collection<String> uuids, boolean exceptionOnNotFound)
      throws GroupNotFoundException {
    if (uuids == null) {
      return null;
    }
    Set<Group> groups = new LinkedHashSet<Group>();
    if (GrouperUtil.length(uuids) == 0) {
    return groups;
    }
    //lets page through these
    int pages = GrouperUtil.batchNumberOfBatches(uuids, batchSize);

    List<String> uuidsList = GrouperUtil.listFromCollection(uuids);
    
    for (int i=0; i<pages; i++) {
      List<String> uuidPageList = GrouperUtil.batchList(uuidsList, batchSize, i);

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select theGroup from Group as theGroup "
          + " where theGroup.uuid in (");

      //add all the uuids
      byHqlStatic.setCollectionInClause(query, uuidPageList);
      query.append(")");
      Set<Group> currentList = byHqlStatic.createQuery(query.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByUuids")
        .listSet(Group.class);
      if (exceptionOnNotFound && currentList.size() != uuidPageList.size()) {
        throw new GroupNotFoundException("Didnt find all uuids: " + GrouperUtil.toStringForLog(uuidPageList)
            + " , " + uuidPageList.size() + " != " + currentList.size());
      }
      
      //we want to put these in in order...
      for (String uuid : uuidPageList) {
        groups.add(GrouperUtil.retrieveByProperty(currentList, Group.FIELD_UUID, uuid));
      }
      
    }
    return groups;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getAllGroupsMembershipSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions, boolean)
   */
  public Set<Group> getAllGroupsMembershipSecure(GrouperSession grouperSession, Subject subject, 
      Set<Privilege> inPrivSet, QueryOptions queryOptions, boolean enabledOnly)
      throws  GrouperDAOException {
    return getAllGroupsMembershipSecure(null, grouperSession, subject, inPrivSet, queryOptions, enabledOnly);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getAllGroupsMembershipSecure(java.lang.String, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions, boolean)
   */
  public Set<Group> getAllGroupsMembershipSecure(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> inPrivSet,
      QueryOptions queryOptions, boolean enabledOnly)
      throws GrouperDAOException {
    return getAllGroupsMembershipSecure(scope, grouperSession, subject, inPrivSet, queryOptions, enabledOnly, null, null);
  }


  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getAllGroupsMembershipSecure(java.lang.String, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions, boolean, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope)
   */
  public Set<Group> getAllGroupsMembershipSecure(final String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions, boolean enabledOnly, Stem stem, Scope stemScope)
    throws  GrouperDAOException {
  
    boolean hasScope = StringUtils.isNotBlank(scope);
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }

    String listId = Group.getDefaultList().getUuid();
  
    StringBuilder sql = new StringBuilder("select distinct theGroup from Group theGroup, " +
    		" MembershipEntry listMembership ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", inPrivSet);
  
    if (changedQuery && sql.toString().contains(" where ")) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
    if (hasScope) {
      sql.append(" theGroup.nameDb like :scope and ");
    }
    sql.append(" listMembership.ownerGroupId = theGroup.uuid and listMembership.fieldId = :listId " +
        " and listMembership.memberUuid = :memberId ");
    
    if (enabledOnly) {
      sql.append(" and listMembership.enabledDb = 'T'");
    }
    
    if (stem != null) {
      switch (stemScope) {
        case ONE:
          
          sql.append(" and theGroup.parentUuid = :stemId and ");
          byHqlStatic.setString("stemId", stem.getUuid());
          break;
        case SUB:
          
          sql.append(" and theGroup.nameDb like :stemSub and ");
          byHqlStatic.setString("stemSub", stem.getName() + ":%");
          
          break;
        default:
          throw new RuntimeException("Not expecting scope: " + stemScope);
      }
    }

    Member member = MemberFinder.internal_findBySubject(subject, null, false);
    
    if (member == null) {
      new LinkedHashSet<Group>();
    }
    
    try {
      String sqlString = sql.toString();
      Set<Group> groups = new HashSet<Group>();
      if (!sqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
        byHqlStatic.createQuery(sqlString)
          .setString("listId", listId)
          .setString("memberId", member.getUuid());
        if (hasScope) {
          byHqlStatic.setString("scope", scope + "%");
        }
    
        if (queryOptions != null) {
          massageSortFields(queryOptions.getQuerySort());
        }
  
        groups = byHqlStatic
          .setCacheable(false)
          .setCacheRegion(KLASS + ".GetAllGroupsSecureScope")
          .options(queryOptions)
          .listSet(Group.class);
      }
  
      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, subject, inPrivSet);
  
      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    }
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getImmediateChildrenMembershipSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions, boolean)
   */
  public Set<Group> getImmediateChildrenMembershipSecure(GrouperSession grouperSession, 
      final Stem stem, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions, boolean enabledOnly)
    throws  GrouperDAOException {
  
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }

    String listId = Group.getDefaultList().getUuid();

    StringBuilder sql = new StringBuilder("select distinct theGroup from Group theGroup," +
    		" MembershipEntry listMembership ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", inPrivSet);
  
    if (changedQuery && sql.toString().contains(" where ")) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
    
    sql.append(" theGroup.parentUuid = :parent " +
        " and listMembership.ownerGroupId = theGroup.uuid and listMembership.fieldId = :listId" +
        " and listMembership.memberUuid = :memberId ");
    
    if (enabledOnly) {
      sql.append(" and listMembership.enabledDb = 'T'");
    }
    
    Member member = MemberFinder.internal_findBySubject(subject, null, false);
    
    try {
  
      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }

      String sqlString = sql.toString();
      Set<Group> groups = new HashSet<Group>();
      if (!sqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
        groups = byHqlStatic.createQuery(sqlString)
          .setString("parent", stem.getUuid())
          .setString("listId", listId)
          .setString("memberId", member.getUuid())
          .setCacheable(false)
          .setCacheRegion(KLASS + ".getImmediateChildrenSecure")
          .options(queryOptions)
          .listSet(Group.class);  
      }
      
      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, subject, inPrivSet);
  
      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#updateLastMembershipChange(java.lang.String)
   */
  public void updateLastMembershipChange(String groupId) {
    HibernateSession.bySqlStatic().executeSql(
        "update grouper_groups set last_membership_change = ? where id = ?",
        GrouperUtil.toListObject(System.currentTimeMillis(), groupId), HibUtils.listType(LongType.INSTANCE, StringType.INSTANCE));
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#updateLastImmediateMembershipChange(java.lang.String)
   */
  public void updateLastImmediateMembershipChange(String groupId) {
    HibernateSession.bySqlStatic().executeSql(
        "update grouper_groups set last_imm_membership_change = ? where id = ?",
        GrouperUtil.toListObject(System.currentTimeMillis(), groupId), HibUtils.listType(LongType.INSTANCE, StringType.INSTANCE));
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#updateLastMembershipChangeIncludeAncestorGroups(java.lang.String)
   */
  public void updateLastMembershipChangeIncludeAncestorGroups(String groupId) {
    
    // note that i'm not doing this all in one update statement with a subquery due to
    // a mysql bug:  http://bugs.mysql.com/bug.php?id=8139
    
    List<String> groupIds = GrouperUtil.listFromCollection(GrouperDAOFactory.getFactory().getGroupSet().findAllOwnerGroupsByMemberGroup(groupId));
    if (groupIds.size() == 0) {
      return;
    }
    
    String queryPrefix = "update grouper_groups set last_membership_change = ? where id ";
    Object time = (Object) System.currentTimeMillis();
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupIds, 100);
    for (int i = 0; i < numberOfBatches; i++) {
      List<String> groupIdsInBatch = GrouperUtil.batchList(groupIds, 100, i);
      List<Object> params = new ArrayList<Object>();
      params.add(time);
      params.addAll(groupIdsInBatch);
      
      List<Type> types = new ArrayList<Type>();
      types.add(LongType.INSTANCE);
      for (int j=0;j<GrouperUtil.length(groupIdsInBatch);j++) {
        types.add(StringType.INSTANCE);
      }
      
      String queryInClause = HibUtils.convertToInClauseForSqlStatic(groupIdsInBatch);
      HibernateSession.bySqlStatic().executeSql(queryPrefix + " in (" + queryInClause + ")", params, types); 
    }
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getAllGroupsMembershipSecure(java.lang.String, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.Boolean, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope)
   */
  public Set<Group> getAllGroupsMembershipSecure(Field field, String scope,
      GrouperSession grouperSession, Subject subject, QueryOptions queryOptions,
      Boolean enabled, MembershipType membershipType, Stem stem, Scope stemScope)
      throws GrouperDAOException {
    boolean hasScope = StringUtils.isNotBlank(scope);
    
    if ((stem == null) != (stemScope == null)) {
      throw new RuntimeException("If stem is set, then stem scope must be set.  If stem isnt set, then stem scope must not be set: " + stem + ", " + stemScope);
    }
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }
    List<QuerySortField> querySortFields = queryOptions.getQuerySort().getQuerySortFields();

    //reset from friendly sort fields to non friendly
    for (QuerySortField querySortField : querySortFields) {
      if (StringUtils.equalsIgnoreCase(querySortField.getColumn(), "name")) {
        querySortField.setColumn("theGroup.nameDb");
      } else if (StringUtils.equalsIgnoreCase(querySortField.getColumn(), "displayName")) {
        querySortField.setColumn("theGroup.displayNameDb");
      } else if (StringUtils.equalsIgnoreCase(querySortField.getColumn(), "extension")) {
        querySortField.setColumn("theGroup.extensionDb");
      } else if (StringUtils.equalsIgnoreCase(querySortField.getColumn(), "displayExtension")) {
        querySortField.setColumn("theGroup.displayExtensionDb");
      }
    }
    
    String listId = field.getUuid();
  
    StringBuilder sql = new StringBuilder("select distinct theGroup from Group theGroup, " +
        " MembershipEntry listMembership ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //make sure the session can read the privs
    Set<Privilege> inPrivSet = AccessPrivilege.READ_PRIVILEGES;
    
    //subject to check privileges for
    Subject accessSubject = grouperSession.getSubject();
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(accessSubject, byHqlStatic, 
        sql, "theGroup.uuid", inPrivSet);
  
    if (changedQuery && sql.toString().contains(" where ")) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
    if (hasScope) {
      sql.append(" theGroup.nameDb like :scope and ");
      byHqlStatic.setString("scope", scope + "%");
    }
    if (stem != null) {
      switch (stemScope) {
        case ONE:
          
          sql.append(" theGroup.parentUuid = :stemId and ");
          byHqlStatic.setString("stemId", stem.getUuid());
          break;
        case SUB:
          
          sql.append(" theGroup.nameDb like :stemSub and ");
          byHqlStatic.setString("stemSub", stem.getName() + ":%");
          
          break;
        default:
          throw new RuntimeException("Not expecting scope: " + stemScope);
      }
    }
    
    //immediate or effective, etc
    if (membershipType != null) {
      sql.append(" listMembership.type ").append(membershipType.queryClause()).append(" and ");
    }
    if (enabled != null && enabled) {
      sql.append(" listMembership.enabledDb = 'T' and ");
    }
    if (enabled != null && !enabled) {
      sql.append(" listMembership.enabledDb = 'F' and ");
    }
    
    //this must be last due to and's
    sql.append(" listMembership.ownerGroupId = theGroup.uuid and listMembership.fieldId = :listId " +
      " and listMembership.memberUuid = :memberId ");

    Member member = MemberFinder.internal_findBySubject(subject, null, false);
    
    if (member == null) {
      new LinkedHashSet<Group>();
    }
    
    try {
      byHqlStatic.createQuery(sql.toString())
        .setString("listId", listId)
        .setString("memberId", member.getUuid());
  
      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }

      Set<Group> groups = byHqlStatic
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllGroupsSecureStemScope")
        .options(queryOptions)
        .listSet(Group.class);
  
      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, accessSubject, inPrivSet);
  
      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findByUuidOrName(java.lang.String, java.lang.String, boolean)
   */
  public Group findByUuidOrName(String uuid, String name, boolean exceptionIfNull)
      throws GrouperDAOException, GroupNotFoundException {
    return findByUuidOrName(uuid, name, exceptionIfNull, null);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findByUuidOrName(java.lang.String, java.lang.String, boolean, QueryOptions)
   */
  public Group findByUuidOrName(String uuid, String name, boolean exceptionIfNull, QueryOptions queryOptions)
      throws GrouperDAOException, GroupNotFoundException {
    try {
      Group group = HibernateSession.byHqlStatic()
        .createQuery("from Group as theGroup where theGroup.uuid = :uuid or theGroup.nameDb = :name")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .options(queryOptions)
        .setString("uuid", uuid)
        .setString("name", name)
        .uniqueResult(Group.class);
      if (group == null && exceptionIfNull) {
        throw new GroupNotFoundException("Can't find group by uuid: '" + uuid + "' or name '" + name + "'");
      }
      return group;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find group by uuid: '" 
        + uuid + "' or name '" + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#saveUpdateProperties(edu.internet2.middleware.grouper.Group)
   */
  public void saveUpdateProperties(Group group) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update Group " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "creatorUuid = :theCreatorUuid, " +
        "createTimeLong = :theCreateTimeLong, " +
        "modifierUuid = :theModifierUuid, " +
        "modifyTimeLong = :theModifyTimeLong " +
        "where uuid = :theUuid")
        .setLong("theHibernateVersionNumber", group.getHibernateVersionNumber())
        .setString("theCreatorUuid", group.getCreatorUuid())
        .setLong("theCreateTimeLong", group.getCreateTimeLong())
        .setString("theModifierUuid", group.getModifierUuid())
        .setLong("theModifyTimeLong", group.getModifyTimeLong())
        .setString("theContextId", group.getContextId())
        .setString("theUuid", group.getUuid()).executeUpdate();
  }

  /**
   * @see GroupDAO#findAllByType(GroupType, QueryOptions)
   */
  public Set<Group> findAllByType(final GroupType _gt, final QueryOptions queryOptions)
      throws GrouperDAOException {
    
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

            ByHql byHql = hibernateSession.byHql().createQuery(
                "select theGroup from Group as theGroup, AttributeAssign as groupTypeAssign " +
                "where groupTypeAssign.attributeDefNameId = :type " +
                "and groupTypeAssign.ownerGroupId  = theGroup.uuid"
              );
            
            if (!HibUtils.secondLevelCaching(true, queryOptions)) {
              byHql.setCacheable(false);
            } else {
              byHql.setCacheable(true);
            }

            Set<Group> groups = byHql.setCacheRegion(KLASS + ".FindAllByType")
              .setString( "type", _gt.getUuid() ).listSet(Group.class);

            return groups;
          }
    });
    return resultGroups;

    
  }
  
  /**
   * @see GroupDAO#findGroupsInStemWithoutPrivilege(GrouperSession, String, Scope, Subject, Privilege, QueryOptions, boolean, String)
   */
  public Set<Group> findGroupsInStemWithoutPrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, 
      QueryOptions queryOptions, boolean considerAllSubject, 
      String sqlLikeString) {
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }

    StringBuilder sql = new StringBuilder("select distinct theGroup from Group theGroup ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query, note, this is for the ADMIN list since the user should be able to read privs
    Set<Privilege> adminSet = GrouperUtil.toSet(AccessPrivilege.ADMIN);
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        sql, "theGroup.uuid", adminSet);

    boolean changedQueryNotWithPriv = grouperSession.getAccessResolver().hqlFilterGroupsNotWithPrivWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", privilege, considerAllSubject);

    if ((changedQuery || changedQueryNotWithPriv)  && sql.toString().contains(" where ")) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }

    switch (scope) {
      case ONE:
        
        sql.append(" theGroup.parentUuid = :stemId ");
        byHqlStatic.setString("stemId", stemId);
        
        break;
        
      case SUB:
        
        Stem stem = StemFinder.findByUuid(grouperSession, stemId, true);
        sql.append(" theGroup.nameDb like :stemPattern ");
        byHqlStatic.setString("stemPattern", stem.getName() + ":%");

        break;
        
      default:
        throw new RuntimeException("Need to pass in a scope, or its not implemented: " + scope);
    }

    if (!StringUtils.isBlank(sqlLikeString)) {
      
      sql.append(" and theGroup.nameDb like :sqlLikeString ");
      byHqlStatic.setString("sqlLikeString", sqlLikeString);
    }
    

    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }

    String sqlString = sql.toString();
    Set<Group> groups = new HashSet<Group>();
    
    if (!sqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      groups = byHqlStatic.createQuery(sqlString)
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindGroupsInStemWithoutPrivilege")
        .options(queryOptions)
        .listSet(Group.class);
    }
          
    //if the hql didnt filter, this will
    Set<Group> filteredGroups = grouperSession.getAccessResolver()
      .postHqlFilterGroups(groups, grouperSession.getSubject(), adminSet);

    if (!changedQueryNotWithPriv) {
      
      //didnt do this in the query
      Set<Group> originalList = new LinkedHashSet<Group>(filteredGroups);
      filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(originalList, subject, GrouperUtil.toSet(privilege));
      
      //we want the ones in the original list not in the new list
      if (filteredGroups != null) {
        originalList.removeAll(filteredGroups);
      }
      filteredGroups = originalList;
    }
    
    return filteredGroups;
    
  }

  /**
   * @see GroupDAO#getAllGroupsSplitScopeSecure(String, GrouperSession, Subject, Set, QueryOptions, TypeOfGroup)
   * @Override
   */
  public Set<Group> getAllGroupsSplitScopeSecure(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, TypeOfGroup typeOfGroup) {
    Set<TypeOfGroup> typeOfGroups = typeOfGroup == null ? null : GrouperUtil.toSet(typeOfGroup);
    return findAllGroupsSecureHelper(scope, grouperSession, subject, privileges, 
        queryOptions, true, typeOfGroups, null, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * @see GroupDAO#getAllGroupsSplitScopeSecure(String, GrouperSession, Subject, Set, QueryOptions, Set)
   * @Override
   */
  public Set<Group> getAllGroupsSplitScopeSecure(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups) {
    return findAllGroupsSecureHelper(scope, grouperSession, subject, privileges, 
        queryOptions, true, typeOfGroups, null, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * @param scope 
   * @param grouperSession 
   * @param subject 
   * @param privileges 
   * @param queryOptions 
   * @param splitScope 
   * @param typeOfGroups
   * @param membershipSubject
   * @param parentStemId
   * @param stemScope
   * @param findByUuidOrName
   * @param field
   * @param subjectNotInGroup
   * @param totalGroupIds
   * @param totalGroupNames
   * @param compositeOwner
   * @param idOfAttributeDefName 
   * @param attributeValue 
   * @param attributeValuesOnAssignment
   * @param attributeCheckReadOnAttributeDef
   * @param idOfAttributeDefName2
   * @param attributeValue2
   * @param attributeValuesOnAssignment2 
   * @return groups
   * 
   */
  private Set<Group> findAllGroupsSecureHelper(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, boolean splitScope, Set<TypeOfGroup> typeOfGroups, Subject membershipSubject, 
      Field field, String parentStemId, Scope stemScope, boolean findByUuidOrName, Subject subjectNotInGroup,
      Collection<String> totalGroupIds, Collection<String> totalGroupNames, Boolean compositeOwner,
      final String idOfAttributeDefName, Object attributeValue, Set<Object> attributeValuesOnAssignment,
      Boolean attributeCheckReadOnAttributeDef, final String idOfAttributeDefName2, Object attributeValue2, Set<Object> attributeValuesOnAssignment2) {

    if ((attributeValue != null || GrouperUtil.length(attributeValuesOnAssignment) > 0) && StringUtils.isBlank(idOfAttributeDefName)) {
      throw new RuntimeException("If you are searching by attributeValue then you must specify an attribute definition name");
    }
    
    if (attributeValue != null && GrouperUtil.length(attributeValuesOnAssignment) > 0) {
      throw new RuntimeException("Cant send in attributeValue and attributeValuesOnAssignment"); 
    }

    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }
  
    if (findByUuidOrName && StringUtils.isBlank(scope)) {
      throw new RuntimeException("If you are looking by uuid or name, you need to pass in a scope");
    }

    if (subject == null && GrouperUtil.length(privileges) > 0) {
      subject = GrouperSession.staticGrouperSession().getSubject();
    }
    
    Set<Group> overallResults = new LinkedHashSet<Group>();
    
    int groupBatches = GrouperUtil.batchNumberOfBatches(totalGroupIds, 100);

    List<String> totalGroupIdsList = new ArrayList<String>(GrouperUtil.nonNull(totalGroupIds));
    
    for (int groupIndex = 0; groupIndex < groupBatches; groupIndex++) {
      
      List<String> groupIds = GrouperUtil.batchList(totalGroupIdsList, 100, groupIndex);

      int groupNameBatches = GrouperUtil.batchNumberOfBatches(totalGroupNames, 50);

      List<String> totalGroupNamesList = new ArrayList<String>(GrouperUtil.nonNull(totalGroupNames));
      
      for (int groupNameIndex = 0; groupNameIndex < groupNameBatches; groupNameIndex++) {
        
        List<String> groupNames = GrouperUtil.batchList(totalGroupNamesList, 50, groupNameIndex);

    
        StringBuilder sql = new StringBuilder(
            "select distinct theGroup from Group theGroup ");
      
        if (!StringUtils.isBlank(parentStemId) || stemScope != null) {
          
          if (StringUtils.isBlank(parentStemId) || stemScope == null) {
            throw new RuntimeException("If you are passing in a parentStemId or a stemScope, then you need to pass both of them: " + parentStemId + ", " + stemScope);
          }
          
          if (stemScope == Scope.SUB) {
            sql.append(", StemSet theStemSet ");
          }
        }      
    
        ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      
        //see if we are adding more to the query
        boolean changedQuery = false;
        
        if (GrouperUtil.length(privileges) > 0) {
          changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic,
              sql, "theGroup.uuid", privileges);
        }
  
        StringBuilder whereClause = new StringBuilder();
  
        if (GrouperUtil.length(groupIds) > 0) {
  
          if (whereClause.length() > 0) {
            
            whereClause.append(" and ");
            
          }
  
          whereClause.append(" theGroup.uuid in (");
          whereClause.append(HibUtils.convertToInClause(groupIds, byHqlStatic));
          whereClause.append(") ");
          
        }

        //if seeing if the group is a composite owner or not a composite owner
        if (compositeOwner != null) {
          if (whereClause.length() > 0) {
            
            whereClause.append(" and ");
            
          }
          if (!compositeOwner) {
            whereClause.append(" not ");
          }
          
          whereClause.append(" exists ( select 1 from Composite as theComposite where theComposite.factorOwnerUuid = theGroup.uuid ) ");
          
          
        }
        
        if (!StringUtils.isBlank(idOfAttributeDefName)) {

          if (whereClause.length() > 0) {
            
            whereClause.append(" and ");
            
          }

          //default to true
          attributeCheckReadOnAttributeDef = GrouperUtil.defaultIfNull(attributeCheckReadOnAttributeDef, true);

          //make sure user can READ the attribute
          AttributeDefNameFinder attributeDefNameFinder = new AttributeDefNameFinder().addIdOfAttributeDefName(idOfAttributeDefName);

          if (attributeCheckReadOnAttributeDef) {
            attributeDefNameFinder.addPrivilege(AttributeDefPrivilege.ATTR_READ);
          }

          AttributeDefName attributeDefName = attributeDefNameFinder.findAttributeName();

          //cant read the attribute????
          if (attributeDefName == null) {
            return new HashSet<Group>();
          }

          AttributeDef attributeDef = attributeDefName.getAttributeDef();
          
          if (GrouperUtil.length(attributeValuesOnAssignment) > 0) {

            whereClause.append(" exists ( select aav ");
            
            whereClause.append(" from AttributeAssign aa, AttributeAssign aaOnAssign, AttributeAssignValue aav ");
            
            whereClause.append(" where theGroup.uuid = aa.ownerGroupId ");
            whereClause.append(" and aa.id = aaOnAssign.ownerAttributeAssignId ");
            
            whereClause.append(" and aaOnAssign.attributeDefNameId = :idOfAttributeDefName ");
            byHqlStatic.setString("idOfAttributeDefName", idOfAttributeDefName);
            whereClause.append(" and aa.enabledDb = 'T' ");

            AttributeDefValueType attributeDefValueType = attributeDef.getValueType();

            Hib3AttributeAssignDAO.queryByValuesAddTablesWhereClause(byHqlStatic, null, whereClause, attributeDefValueType, attributeValuesOnAssignment, "aaOnAssign");
            
            whereClause.append(" ) ");
            
            
          } else {
            
            whereClause.append(" exists ( select ");
            
            whereClause.append(attributeValue == null ? "aa" : "aav");
            
            whereClause.append(" from AttributeAssign aa ");

            if (attributeValue != null) {
              whereClause.append(", AttributeAssignValue aav ");
            }
            
            whereClause.append(" where theGroup.uuid = aa.ownerGroupId ");
            whereClause.append(" and aa.attributeDefNameId = :idOfAttributeDefName ");
            byHqlStatic.setString("idOfAttributeDefName", idOfAttributeDefName);
            whereClause.append(" and aa.enabledDb = 'T' ");

            if (attributeValue != null) {

              AttributeDefValueType attributeDefValueType = attributeDef.getValueType();

              Hib3AttributeAssignDAO.queryByValueAddTablesWhereClause(byHqlStatic, null, whereClause, attributeDefValueType, attributeValue);
              
            }
            
            whereClause.append(" ) ");
              
          }
            
        }
        
        if (!StringUtils.isBlank(idOfAttributeDefName2)) {

          if (whereClause.length() > 0) {
            
            whereClause.append(" and ");
            
          }

          //default to true
          attributeCheckReadOnAttributeDef = GrouperUtil.defaultIfNull(attributeCheckReadOnAttributeDef, true);

          //make sure user can READ the attribute
          AttributeDefNameFinder attributeDefNameFinder = new AttributeDefNameFinder().addIdOfAttributeDefName(idOfAttributeDefName2);

          if (attributeCheckReadOnAttributeDef) {
            attributeDefNameFinder.addPrivilege(AttributeDefPrivilege.ATTR_READ);
          }

          AttributeDefName attributeDefName = attributeDefNameFinder.findAttributeName();

          //cant read the attribute????
          if (attributeDefName == null) {
            return new HashSet<Group>();
          }

          AttributeDef attributeDef = attributeDefName.getAttributeDef();
          
          if (GrouperUtil.length(attributeValuesOnAssignment2) > 0) {

            whereClause.append(" exists ( select aav ");
            
            whereClause.append(" from AttributeAssign aa, AttributeAssign aaOnAssign, AttributeAssignValue aav ");
            
            whereClause.append(" where theGroup.uuid = aa.ownerGroupId ");
            whereClause.append(" and aa.id = aaOnAssign.ownerAttributeAssignId ");
            
            whereClause.append(" and aaOnAssign.attributeDefNameId = :idOfAttributeDefName2 ");
            byHqlStatic.setString("idOfAttributeDefName2", idOfAttributeDefName2);
            whereClause.append(" and aa.enabledDb = 'T' ");

            AttributeDefValueType attributeDefValueType = attributeDef.getValueType();

            Hib3AttributeAssignDAO.queryByValuesAddTablesWhereClause(byHqlStatic, null, whereClause, attributeDefValueType, attributeValuesOnAssignment2, "aaOnAssign");
            
            whereClause.append(" ) ");
            
            
          } else {
            
            whereClause.append(" exists ( select ");
            
            whereClause.append(attributeValue2 == null ? "aa" : "aav");
            
            whereClause.append(" from AttributeAssign aa ");

            if (attributeValue2 != null) {
              whereClause.append(", AttributeAssignValue aav ");
            }
            
            whereClause.append(" where theGroup.uuid = aa.ownerGroupId ");
            whereClause.append(" and aa.attributeDefNameId = :idOfAttributeDefName2 ");
            byHqlStatic.setString("idOfAttributeDefName2", idOfAttributeDefName2);
            whereClause.append(" and aa.enabledDb = 'T' ");

            if (attributeValue2 != null) {

              AttributeDefValueType attributeDefValueType = attributeDef.getValueType();

              Hib3AttributeAssignDAO.queryByValueAddTablesWhereClause(byHqlStatic, null, whereClause, attributeDefValueType, attributeValue2);
              
            }
            
            whereClause.append(" ) ");
              
          }

        }

        //groupnames or alternate names
        if (GrouperUtil.length(groupNames) > 0) {
          
          if (whereClause.length() > 0) {
            
            whereClause.append(" and ");
            
          }
  
          whereClause.append(" ( theGroup.nameDb in ( ");
          whereClause.append(HibUtils.convertToInClause(groupNames, byHqlStatic));
          whereClause.append(") or theGroup.alternateNameDb in ( ");
          whereClause.append(HibUtils.convertToInClause(groupNames, byHqlStatic));
          whereClause.append(" ) ");
          
          //if entities, then also allow entity identifier
          if (typeOfGroups != null && typeOfGroups.contains(TypeOfGroup.entity)) {
            
            whereClause.append(" or exists ( select theAttributeAssignValue from AttributeAssign theAttributeAssign, " +
                " AttributeAssignValue theAttributeAssignValue, AttributeDefName theAttributeDefName ");
  
            whereClause.append(" where theGroup.typeOfGroupDb = 'entity' and theGroup.uuid = theAttributeAssign.ownerGroupId ");
            whereClause.append(" and theAttributeAssign.attributeDefNameId = theAttributeDefName.id ");
  
            whereClause.append(" and theAttributeDefName.nameDb = :entitySubjectIdDefName ");
            byHqlStatic.setString("entitySubjectIdDefName", EntityUtils.entitySubjectIdentifierName());
  
            whereClause.append(" and theAttributeAssignValue.attributeAssignId = theAttributeAssign.id ");
  
            whereClause.append(" and theAttributeAssignValue.valueString in ( ");

            whereClause.append(HibUtils.convertToInClause(groupNames, byHqlStatic));
            
            whereClause.append(" )");

            
            whereClause.append(" ) ");
            
          }

          
          whereClause.append(" ) ");
          
        }
        
        if (field != null) {
          
          if (whereClause.length() > 0) {
            
            whereClause.append(" and ");
            
          }
    
          if (membershipSubject == null) {
            throw new RuntimeException("Why is membershipSubject null if passing in a field for memberships???");
          }
          
          Member membershipMember = MemberFinder.findBySubject(grouperSession, membershipSubject, false);
          
          //if a member does not exist, its not in any groups
          if (membershipMember == null) {
            return new HashSet<Group>();
          }
          
          whereClause.append(" exists (select 1 from MembershipEntry fieldMembership where fieldMembership.ownerGroupId = theGroup.uuid " +
          		" and fieldMembership.fieldId = :fieldId " +
          		" and fieldMembership.memberUuid = :fieldMembershipMemberUuid and fieldMembership.enabledDb = 'T' ) ");
          byHqlStatic.setString("fieldId", field.getUuid());
          byHqlStatic.setString("fieldMembershipMemberUuid", membershipMember.getUuid());
                
        }
    
        if (subjectNotInGroup != null) {
    
          if (whereClause.length() > 0) {
            
            whereClause.append(" and ");
            
          }
    
          Member membershipMember = MemberFinder.findBySubject(grouperSession, subjectNotInGroup, false);
          
          //if a member does not exist, its not in any groups
          if (membershipMember != null) {
            whereClause.append(" not exists (select 1 from MembershipEntry fieldMembership where fieldMembership.ownerGroupId = theGroup.uuid " +
                " and fieldMembership.fieldId = :fieldId2 " +
                " and fieldMembership.memberUuid = :fieldMembershipMemberUuid2 and fieldMembership.enabledDb = 'T' ) ");
            byHqlStatic.setString("fieldId2", Group.getDefaultList().getUuid());
            byHqlStatic.setString("fieldMembershipMemberUuid2", membershipMember.getUuid());
          }
    
        }
        
        //see if there is a scope
        if (!StringUtils.isBlank(scope)) {
          scope = scope.toLowerCase();
    
          String[] scopes = splitScope ? GrouperUtil.splitTrim(scope, " ") : new String[]{scope};
    
          if (scopes.length > 1 && findByUuidOrName) {
            throw new RuntimeException("If you are looking by uuid or name, then you can only pass in one scope: " + scope);
          }
    
          if (whereClause.length() > 0) {
            whereClause.append(" and ");
          }
          if (GrouperUtil.length(scopes) == 1) {
            whereClause.append(" ( theGroup.id = :theGroupIdScope or ( ");
            byHqlStatic.setString("theGroupIdScope", scope);
          } else {
            whereClause.append(" ( ( ");
          }
    
          int index = 0;
          for (String theScope : scopes) {
            if (index != 0) {
              whereClause.append(" and ");
            }
            
            if (findByUuidOrName) {
              whereClause.append(" theGroup.nameDb = :scope" + index + " or theGroup.alternateNameDb = :scope" + index 
                  + " or theGroup.displayNameDb = :scope" + index + " ");
              byHqlStatic.setString("scope" + index, theScope);
            } else {
              whereClause.append(" ( lower(theGroup.nameDb) like :scope" + index 
                  + " or lower(theGroup.alternateNameDb) like :scope" + index 
                  + " or lower(theGroup.displayNameDb) like :scope" + index 
                  + " or lower(theGroup.descriptionDb) like :scope" + index + " ) ");
              if (splitScope) {
                theScope = "%" + theScope + "%";
              } else if (!theScope.endsWith("%")) {
                theScope += "%";
              }
              byHqlStatic.setString("scope" + index, theScope.toLowerCase());
    
            }        
            
            index++;
          }
          whereClause.append(" ) ");
          
          //if entities, then also allow entity identifier
          if (typeOfGroups != null && typeOfGroups.contains(TypeOfGroup.entity)) {
            
            whereClause.append(" or exists ( select theAttributeAssignValue from AttributeAssign theAttributeAssign, " +
                " AttributeAssignValue theAttributeAssignValue, AttributeDefName theAttributeDefName ");
  
            whereClause.append(" where theGroup.typeOfGroupDb = 'entity' and theGroup.uuid = theAttributeAssign.ownerGroupId ");
            whereClause.append(" and theAttributeAssign.attributeDefNameId = theAttributeDefName.id ");
  
            whereClause.append(" and theAttributeDefName.nameDb = :entitySubjectIdDefName ");
            byHqlStatic.setString("entitySubjectIdDefName", EntityUtils.entitySubjectIdentifierName());
  
            whereClause.append(" and theAttributeAssignValue.attributeAssignId = theAttributeAssign.id ");
            
            index = 0;
            for (@SuppressWarnings("unused") String theScope : scopes) {
              
              if (findByUuidOrName) {
                whereClause.append(" and theAttributeAssignValue.valueString = :scope" + index + " ");
              } else {
                whereClause.append(" and lower(theAttributeAssignValue.valueString) like :scope" + index);      
              }        
              
              index++;
            }
            
           whereClause.append(" ) ");
            
          }

          
          whereClause.append(" ) ");
        }
        
        if (!StringUtils.isBlank(parentStemId) || stemScope != null) {
          if (whereClause.length() > 0) {
            whereClause.append(" and ");
          }
          switch(stemScope) {
            case ONE:
              
              whereClause.append(" theGroup.parentUuid = :theStemId ");
              byHqlStatic.setString("theStemId", parentStemId);
              break;
            case SUB:
              
              whereClause.append(" theGroup.parentUuid = theStemSet.ifHasStemId " +
                " and theStemSet.thenHasStemId = :theStemId ");
              byHqlStatic.setString("theStemId", parentStemId);
              
              break;
            
          }
        }
        
        if (changedQuery && sql.toString().contains(" where ")) {
          sql.append(" and ");
        } else {
          sql.append(" where ");
        }
        sql.append(whereClause);
    
        TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, sql, byHqlStatic);
    
        if (queryOptions != null) {
          massageSortFields(queryOptions.getQuerySort());
        }
    

        String sqlString = sql.toString();
        
        if (!sqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
          Set<Group> tempGroups = byHqlStatic.createQuery(sqlString)
            .setCacheable(false)
            .setCacheRegion(KLASS + ".GetAllGroupsSecure")
            .options(queryOptions)
            .listSet(Group.class);
          
          overallResults.addAll(GrouperUtil.nonNull(tempGroups));
        }
      }      
    }
    
    //if find by uuid or name, try to narrow down to one...
    if (findByUuidOrName) {
      
      //get the one with uuid
      for (Group group : overallResults) {
        if (StringUtils.equals(scope, group.getId())) {
          return GrouperUtil.toSet(group);
        }
      }
      
      //get the one with name
      for (Group group : overallResults) {
        if (StringUtils.equals(scope, group.getName())) {
          return GrouperUtil.toSet(group);
        }
      }
      
      //get the one with alternate name
      for (Group group : overallResults) {
        if (StringUtils.equals(scope, group.getAlternateName())) {
          return GrouperUtil.toSet(group);
        }
      }
      
    }

    Group.initData(overallResults);
    
    return overallResults;
  
  }

  /**
   * Helper for find by approximate name queries
   * @param name
   * @param scope
   * @param currentNames
   * @param alternateNames
   * @param queryOptions 
   * @param typeOfGroups
   * @return set
   * @throws GrouperDAOException
   * @throws IllegalStateException
   */
  private Set<Group> findAllByApproximateNameSecureHelper(final String name, final String scope,
      final boolean currentNames, final boolean alternateNames, final QueryOptions queryOptions, final Set<TypeOfGroup> typeOfGroups)
      throws GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {
  
          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            StringBuilder hql = new StringBuilder("select distinct theGroup from Group theGroup ");
      
            ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
          
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();
            
            //see if we are adding more to the query
            boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                grouperSession.getSubject(), byHqlStatic, 
                hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);
          
            if (changedQuery && hql.toString().contains(" where ")) {
              hql.append(" and ");
            } else {
              hql.append(" where ");
            }
            String lowerName = StringUtils.defaultString(name).toLowerCase();
            hql.append(" ( ");
            if (currentNames) {
              hql.append(" lower(theGroup.nameDb) like :theName or lower(theGroup.displayNameDb) like :theDisplayName ");
              byHqlStatic.setString("theName", "%" + lowerName + "%");
              byHqlStatic.setString("theDisplayName", "%" + lowerName + "%");
            } 
  
            if (alternateNames) {
              if (currentNames) {
                hql.append(" or ");
              }
              hql.append(" lower(theGroup.alternateNameDb) like :theAlternateName ");
              byHqlStatic.setString("theAlternateName", "%" + lowerName + "%");
            }
            
            //if entities, then also allow entity identifier
            if (typeOfGroups != null && typeOfGroups.contains(TypeOfGroup.entity)) {
              
              hql.append(" or exists ( select theAttributeAssignValue from AttributeAssign theAttributeAssign, " +
                  " AttributeAssignValue theAttributeAssignValue, AttributeDefName theAttributeDefName ");
    
              hql.append(" where theGroup.typeOfGroupDb = 'entity' and theGroup.uuid = theAttributeAssign.ownerGroupId ");
              hql.append(" and theAttributeAssign.attributeDefNameId = theAttributeDefName.id ");
    
              hql.append(" and theAttributeDefName.nameDb = :entitySubjectIdDefName ");
              byHqlStatic.setString("entitySubjectIdDefName", EntityUtils.entitySubjectIdentifierName());
    
              hql.append(" and theAttributeAssignValue.attributeAssignId = theAttributeAssign.id ");
    
              hql.append(" and theAttributeAssignValue.valueString like :theName ) ");
              
            }
            
            hql.append(" ) ");
            
            if (scope != null) {
              hql.append(" and theGroup.nameDb like :theStemScope ");
              byHqlStatic.setString("theStemScope", scope + "%");
            }

            //add in the typeOfGroups part
            TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, hql, byHqlStatic);
            
            byHqlStatic.setCacheable(false);
            byHqlStatic.setCacheRegion(KLASS + ".FindAllByApproximateNameSecure");

            //reset sorting
            if (queryOptions != null) {
              
              massageSortFields(queryOptions.getQuerySort());
              
              byHqlStatic.options(queryOptions);
            }
            
            byHqlStatic.createQuery(hql.toString());
            Set<Group> groups = byHqlStatic.listSet(Group.class);
            
            return groups;
          }
    });
    
    Group.initData(resultGroups);

    return resultGroups;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findAllByApproximateAttrSecure(java.lang.String, java.lang.String, java.lang.String)
   */
  public Set<Group> findAllByApproximateAttrSecure(String attr, String val, String scope)
      throws GrouperDAOException, IllegalStateException {
    return findAllByApproximateAttrHelper(attr, val, scope, true);
  }

  /**
   * @see GroupDAO#findByNamesSecure(Collection, QueryOptions)
   */
  public Set<Group> findByNamesSecure(Collection<String> names, QueryOptions queryOptions) {
    return findByNamesSecure(names, queryOptions, null);
  }

  /**
   * @see GroupDAO#findByNamesSecure(Collection, QueryOptions)
   */
  public Set<Group> findByNamesSecure(Collection<String> names, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(names, 90);
    
    Set<Group> groups = new HashSet<Group>();
    
    List<String> namesList = GrouperUtil.listFromCollection(names);
    
    for (int i=0;i<numberOfBatches;i++) {
      
      List<String> namesBatch = GrouperUtil.batchList(namesList, 90, i);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

      StringBuilder sql = new StringBuilder("select distinct theGroup from Group as theGroup ");
      
      //see if we are adding more to the query
      boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
          sql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);

      if (changedQuery && sql.toString().contains(" where ")) {
        sql.append(" and ");
      } else {
        sql.append(" where ");
      }
      sql.append(" ( theGroup.nameDb in ( ");
      
      sql.append(HibUtils.convertToInClause(namesBatch, byHqlStatic)).append(" ) ");

      sql.append(" or theGroup.alternateNameDb in ( ");

      sql.append(HibUtils.convertToInClause(namesBatch, byHqlStatic)).append(" ) ");

      //if entities, then also allow entity identifier
      if (typeOfGroups != null && typeOfGroups.contains(TypeOfGroup.entity)) {
        
        sql.append(" or exists ( select theAttributeAssignValue from AttributeAssign theAttributeAssign, " +
            " AttributeAssignValue theAttributeAssignValue, AttributeDefName theAttributeDefName ");

        sql.append(" where theGroup.typeOfGroupDb = 'entity' and theGroup.uuid = theAttributeAssign.ownerGroupId ");
        sql.append(" and theAttributeAssign.attributeDefNameId = theAttributeDefName.id ");

        sql.append(" and theAttributeDefName.nameDb = :entitySubjectIdDefName ");
        byHqlStatic.setString("entitySubjectIdDefName", EntityUtils.entitySubjectIdentifierName());

        sql.append(" and theAttributeAssignValue.attributeAssignId = theAttributeAssign.id ");

        sql.append(" and theAttributeAssignValue.valueString in ( ");
        sql.append(HibUtils.convertToInClause(namesBatch, byHqlStatic)).append(" ) )");

      }
      sql.append(" ) ");
      TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, sql, byHqlStatic);



      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }

      byHqlStatic
        .createQuery(sql.toString())
        .setCacheable(true)
        .options(queryOptions)
        .setCacheRegion(KLASS + ".FindByNamesSecure");
      
      Set<Group> groupsBatch = byHqlStatic.listSet(Group.class);
      
      groups.addAll(GrouperUtil.nonNull(groupsBatch));
      
    }
    
    return groups;

  }
  

  /**
   * @see GroupDAO#findByUuidsSecure(Collection, QueryOptions)
   */
  public Set<Group> findByUuidsSecure(Collection<String> uuids, QueryOptions queryOptions) {
    return findByUuidsSecure(uuids, queryOptions, null);
  }

  /**
   * @see GroupDAO#findByUuidsSecure(Collection, QueryOptions, Set)
   */
  public Set<Group> findByUuidsSecure(Collection<String> uuids, QueryOptions queryOptions, Set<TypeOfGroup> typeOfGroups) {
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(uuids, 180);
    
    Set<Group> groups = new HashSet<Group>();
    
    List<String> uuidsList = GrouperUtil.listFromCollection(uuids);
    
    for (int i=0;i<numberOfBatches;i++) {
      
      List<String> uuidsBatch = GrouperUtil.batchList(uuidsList, 180, i);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

      StringBuilder sql = new StringBuilder("select distinct theGroup from Group as theGroup ");
      
      //see if we are adding more to the query
      boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
          sql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);

      if (changedQuery && sql.toString().contains(" where ")) {
        sql.append(" and ");
      } else {
        sql.append(" where ");
      }
      sql.append(" theGroup.uuid in ( ");
      
      sql.append(HibUtils.convertToInClause(uuidsBatch, byHqlStatic)).append(" ) ");
      
      TypeOfGroup.appendHqlQuery("theGroup", typeOfGroups, sql, byHqlStatic);

      if (queryOptions != null) {
        massageSortFields(queryOptions.getQuerySort());
      }

      byHqlStatic
        .createQuery(sql.toString())
        .setCacheable(true)
        .options(queryOptions)
        .setCacheRegion(KLASS + ".FindByUuidsSecure");
      
      Set<Group> groupsBatch = byHqlStatic.listSet(Group.class);
      
      groups.addAll(GrouperUtil.nonNull(groupsBatch));
      
    }
    
    return groups;
  }

  /**
   * not a secure method, find by id index
   */
  @Override
  public Group findByIdIndex(Long idIndex, boolean exceptionIfNotFound)
      throws GroupNotFoundException {
    
    StringBuilder hql = new StringBuilder("select theGroup from Group as theGroup where (theGroup.idIndex = :theIdIndex)");
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .setCacheable(true).setCacheRegion(KLASS + ".FindByIdIndex");
    
    byHqlStatic.createQuery(hql.toString());
    
    Group group = byHqlStatic.setLong("theIdIndex", idIndex).uniqueResult(Group.class);

    //handle exceptions out of data access method...
    if (group == null && exceptionIfNotFound) {
      throw new GroupNotFoundException("Cannot find group with idIndex: '" + idIndex + "'");
    }
    return group;
    
  }

  /**
   * @see GroupDAO#findByIdIndexSecure(Long, boolean, QueryOptions)
   */
  @Override
  public Group findByIdIndexSecure(Long idIndex, boolean exceptionIfNotFound,
      QueryOptions queryOptions) throws GroupNotFoundException {

    StringBuilder hql = new StringBuilder("select theGroup from Group as theGroup");

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    ByHqlStatic byHql = HibernateSession.byHqlStatic();

    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
          grouperSession.getSubject(), byHql, 
          hql, "theGroup.uuid", AccessPrivilege.VIEW_PRIVILEGES);
    
    if (changedQuery && hql.toString().contains(" where ")) {
      hql.append(" and ");
    } else {
      hql.append(" where ");
    }


    hql.append(" (theGroup.idIndex = :theIdIndex)" );
    byHql.setCacheable(true).setCacheRegion(KLASS + ".FindByIdIndexSecure");
    
    Group group = byHql.createQuery(hql.toString()).setLong("theIdIndex", idIndex)
        .options(queryOptions)
        .uniqueResult(Group.class);

    if (group != null) {
      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(GrouperUtil.toSet(group), grouperSession.getSubject(), AccessPrivilege.VIEW_PRIVILEGES);
      if (GrouperUtil.length(filteredGroups) == 0) {
        group = null;
      }
    }
    
    //handle exceptions out of data access method...
    if (group == null && exceptionIfNotFound) {
      throw new GroupNotFoundException("Cannot find group with idIndex: '" + idIndex + "'");
    }
    return group;
    
    
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getAllGroupsSecure(java.lang.String, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.util.Set, boolean, edu.internet2.middleware.subject.Subject, edu.internet2.middleware.grouper.Field)
   */
  @Override
  public Set<Group> getAllGroupsSecure(String scope, GrouperSession grouperSession,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions,
      Set<TypeOfGroup> typeOfGroups, boolean splitScope, Subject membershipSubject, Field field) {
    return findAllGroupsSecureHelper(scope, grouperSession, subject, privileges, queryOptions, splitScope, 
        typeOfGroups, membershipSubject, field, null, null, false, null, null, null, null, null, null, null, null, null, null, null);
  }

  /**
   * @see GroupDAO#getAllGroupsSecure(String, GrouperSession, Subject, Set, QueryOptions, Set, boolean, Subject, Field, String, Scope, boolean, Subject, Collection, Collection, Boolean)
   */
  @Override
  public Set<Group> getAllGroupsSecure(String scope, GrouperSession grouperSession,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions,
      Set<TypeOfGroup> typeOfGroups, boolean splitScope, Subject membershipSubject,
      Field field, String parentStemId, Scope stemScope,  
      boolean findByUuidOrName, Subject subjectNotInGroup, Collection<String> groupIds, Collection<String> groupNames, 
      Boolean compositeOwner) {
    return findAllGroupsSecureHelper(scope, grouperSession, subject, privileges, queryOptions, 
        splitScope, typeOfGroups, membershipSubject, field, parentStemId, stemScope,
        findByUuidOrName, subjectNotInGroup, groupIds, groupNames, compositeOwner, null, null, null, null, null, null, null);
  }

  /**
   * @see GroupDAO#getAllGroupsSecure(String, GrouperSession, Subject, Set, QueryOptions, Set, boolean, Subject, Field, String, Scope, boolean, Subject, Collection, Collection, Boolean, String, Object)
   */
  @Override
  public Set<Group> getAllGroupsSecure(String scope, GrouperSession grouperSession,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions,
      Set<TypeOfGroup> typeOfGroups, boolean splitScope, Subject membershipSubject,
      Field field, String parentStemId, Scope stemScope,  
      boolean findByUuidOrName, Subject subjectNotInGroup, Collection<String> groupIds, Collection<String> groupNames, 
      Boolean compositeOwner, String idOfAttributeDefName, Object attributeValue) {
    return findAllGroupsSecureHelper(scope, grouperSession, subject, privileges, queryOptions, 
        splitScope, typeOfGroups, membershipSubject, field, parentStemId, stemScope,
        findByUuidOrName, subjectNotInGroup, groupIds, groupNames, compositeOwner, idOfAttributeDefName, attributeValue, null, null, null, null, null);
  }

  /**
   * @see GroupDAO#getAllGroupsSecure(String, GrouperSession, Subject, Set, QueryOptions, Set, boolean, Subject, Field, String, Scope, boolean, Subject, Collection, Collection, Boolean, String, Object, Set, Boolean)
   */
  @Override
  public Set<Group> getAllGroupsSecure(String scope, GrouperSession grouperSession,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions,
      Set<TypeOfGroup> typeOfGroup, boolean splitScope, Subject membershipSubject,
      Field field, String parentStemId, Scope stemScope, boolean findByUuidOrName,
      Subject subjectNotInGroup, Collection<String> groupIds,
      Collection<String> groupNames, Boolean compositeOwner, String idOfAttributeDefName,
      Object attributeValue, Set<Object> attributeValuesOnAssignment, Boolean attributeCheckReadOnAttributeDef) {
    return findAllGroupsSecureHelper(scope, grouperSession, subject, privileges, queryOptions, 
        splitScope, typeOfGroup, membershipSubject, field, parentStemId, stemScope,
        findByUuidOrName, subjectNotInGroup, groupIds, groupNames, compositeOwner, idOfAttributeDefName, 
        attributeValue, attributeValuesOnAssignment, attributeCheckReadOnAttributeDef, null, null, null);
  }

  /**
   * @see GroupDAO#findGroupsInStemWithPrivilege(GrouperSession, String, Scope, Subject, Privilege, QueryOptions, boolean, String)
   */
  public Set<Group> findGroupsInStemWithPrivilege(GrouperSession grouperSession,
      String stemId, Scope scope, Subject subject, Privilege privilege, 
      QueryOptions queryOptions, boolean considerAllSubject, 
      String sqlLikeString) {
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }
  
    StringBuilder sql = new StringBuilder("select distinct theGroup from Group theGroup ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query, note, this is for the ADMIN list since the user should be able to read privs
    Set<Privilege> adminSet = GrouperUtil.toSet(AccessPrivilege.ADMIN);
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        sql, "theGroup.uuid", adminSet);
  
    boolean changedQueryNotWithPriv = grouperSession.getAccessResolver().hqlFilterGroupsWithPrivWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", privilege, considerAllSubject);
  
    if ((changedQuery || changedQueryNotWithPriv) && sql.toString().contains(" where ")) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
  
    switch (scope) {
      case ONE:
        
        sql.append(" theGroup.parentUuid = :stemId ");
        byHqlStatic.setString("stemId", stemId);
        
        break;
        
      case SUB:
        
        Stem stem = StemFinder.findByUuid(grouperSession, stemId, true);
        sql.append(" theGroup.nameDb like :stemPattern ");
        byHqlStatic.setString("stemPattern", stem.getName() + ":%");
  
        break;
        
      default:
        throw new RuntimeException("Need to pass in a scope, or its not implemented: " + scope);
    }
  
    if (!StringUtils.isBlank(sqlLikeString)) {
      
      sql.append(" and theGroup.nameDb like :sqlLikeString ");
      byHqlStatic.setString("sqlLikeString", sqlLikeString);
    }
    
  
    if (queryOptions != null) {
      massageSortFields(queryOptions.getQuerySort());
    }
  
    String sqlString = sql.toString();
    Set<Group> groups = new HashSet<Group>();
    
    if (!sqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      groups = byHqlStatic.createQuery(sqlString)
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindGroupsInStemWithoutPrivilege")
        .options(queryOptions)
        .listSet(Group.class);
    }
          
    //if the hql didnt filter, this will
    Set<Group> filteredGroups = grouperSession.getAccessResolver()
      .postHqlFilterGroups(groups, grouperSession.getSubject(), adminSet);
  
    if (!changedQueryNotWithPriv) {
      
      //didnt do this in the query
      Set<Group> originalList = new LinkedHashSet<Group>(filteredGroups);
      filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(originalList, subject, GrouperUtil.toSet(privilege));
      
      //we want the ones in the original list not in the new list
      if (filteredGroups != null) {
        originalList.removeAll(filteredGroups);
      }
      filteredGroups = originalList;
    }
    
    return filteredGroups;
    
  }

  /**
   * @see GroupDAO#getAllGroupsSecure(String, GrouperSession, Subject, Set, QueryOptions, Set, boolean, Subject, Field, String, Scope, boolean, Subject, Collection, Collection, Boolean, String, Object, Set, Boolean, String, Object)
   */
  @Override
  public Set<Group> getAllGroupsSecure(String scope, GrouperSession grouperSession,
      Subject subject, Set<Privilege> privileges, QueryOptions queryOptions,
      Set<TypeOfGroup> typeOfGroup, boolean splitScope, Subject membershipSubject,
      Field field, String parentStemId, Scope stemScope, boolean findByUuidOrName,
      Subject subjectNotInGroup, Collection<String> groupIds,
      Collection<String> groupNames, Boolean compositeOwner, String idOfAttributeDefName,
      Object attributeValue, Set<Object> attributeValuesOnAssignment, Boolean attributeCheckReadOnAttributeDef,
      String idOfAttributeDefName2,
      Object attributeValue2, Set<Object> attributeValuesOnAssignment2) {
    return findAllGroupsSecureHelper(scope, grouperSession, subject, privileges, queryOptions, 
        splitScope, typeOfGroup, membershipSubject, field, parentStemId, stemScope,
        findByUuidOrName, subjectNotInGroup, groupIds, groupNames, compositeOwner, idOfAttributeDefName, 
        attributeValue, attributeValuesOnAssignment, attributeCheckReadOnAttributeDef, idOfAttributeDefName2,attributeValue2, attributeValuesOnAssignment2);
  }
} 

