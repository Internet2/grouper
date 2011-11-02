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

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByHql;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.dao.QuerySortField;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
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
      existsCache=new GrouperCache<String, Boolean>("edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GroupDAO.exists",
            1000, false, 30, 120, false); 
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
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#addType(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.GroupType)
   */
  public GroupTypeTuple addType(final Group _g, final GroupType _gt) 
    throws  GrouperDAOException {
    
    return (GroupTypeTuple)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            GroupTypeTuple groupTypeTuple = new GroupTypeTuple();
            groupTypeTuple.assignGroupUuid( _g.getUuid(), _g );
            groupTypeTuple.setTypeUuid( _gt.getUuid() );
            groupTypeTuple.setId(GrouperUuid.getUuid());
            hibernateSession.byObject().save(groupTypeTuple);
            
            //MCH dont save again due to optimistic locking
            //get it again in case it was changed in the hook
//            Group g2 = null;
//            try {
//              g2 = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), _g.getUuid(), true);
//            } catch (GroupNotFoundException gnfe) {
//              throw new RuntimeException("Weird problem getting group: " + _g.getName());
//            }
//
//            //note this used to be saveOrUpdate
//            hibernateSession.byObject().update( g2 ); // modified group
 
            //let HibernateSession commit or rollback depending on if problem or enclosing transaction
            return groupTypeTuple;
          }
    });
  } 

  /**
   * @param _g 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void delete(final Group _g)
    throws  GrouperDAOException {

    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            ByObject byObject = hibernateSession.byObject();
            
            // delete attributes
            ByHql byHql = hibernateSession.byHql();
            byHql.createQuery("delete from Attribute where group_id = :group");
            byHql.setString("group", _g.getUuid() );
            byHql.executeUpdate();
            // delete type tuples
            byHql = hibernateSession.byHql();
            byHql.createQuery("delete from GroupTypeTuple where group_uuid = :group");
            byHql.setString("group", _g.getUuid() );
            byHql.executeUpdate();
            
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
  private static final Log LOG = GrouperUtil.getLog(Hib3GroupDAO.class);

  /**
   * @param group 
   * @param groupType 
   * @return tuple
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public GroupTypeTuple deleteType(final Group group, final GroupType groupType) 
    throws  GrouperDAOException {
    return (GroupTypeTuple)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            
            //delete all attributes used by the group of this type
            Set<Field> fields = GrouperUtil.nonNull(groupType.getFields());
            
            //remove the attributes first
            for (Field field : fields) {
              
              //get attributes each time, in case something else removed
              if (group.getAttributesMap(false).containsKey(field.getName())) {
                LOG.debug("deleting attribute: " + field.getName() + " from group: " + group.getName()
                     + " since the type was removed");
                try {
                  group.deleteAttribute(field.getName());
                } catch (Exception e) {
                  throw new RuntimeException("Exception removing field: " + field.getName() 
                      + ", from group: " + group.getName(),e);
                }
              }
            }
            //take this out for the hook's benefit, then remove the object
            Set types = group.getTypesDb();
            types.remove( groupType );
            GroupTypeTuple groupTypeTuple = Hib3GroupTypeTupleDAO.findByGroupAndType(group, groupType);
            hibernateSession.byObject().delete( groupTypeTuple );

            return groupTypeTuple;
          }
    });
  } 

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
      .createQuery("select g.uuid from Group as g where g.uuid = :uuid")
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

    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            
            String valLowerForQuery = "%" + val.toLowerCase() + "%";
            Set<Group> groups = hibernateSession.byHql().createQuery(
                "select g from Group as g where "
                  + "lower(g.nameDb) like :value "
                  + "or lower(g.extensionDb) like :value "
                  + "or lower(g.displayNameDb) like :value "
                  + "or lower(g.extensionDb) like :value "
                  + "or lower(g.displayExtensionDb) like :value "
                  + "or lower(g.descriptionDb) like :value "
                  + "or exists " +
                "(select a from Attribute as a where lower(a.value) like :value and a.groupUuid = g.uuid) "
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByAnyApproximateAttr")
              .setString( "value", valLowerForQuery ).listSet(Group.class);

            return groups;
          }
    });

    return resultGroups;
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

    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            
            Set<Group> groups = hibernateSession.byHql().createQuery(
                "select g from Group as g where "
                + "(lower(g.nameDb) like :value "
                + "or lower(g.extensionDb) like :value "
                + "or lower(g.displayNameDb) like :value "
                + "or lower(g.extensionDb) like :value "
                + "or lower(g.displayExtensionDb) like :value "
                + "or lower(g.descriptionDb) like :value "
                + "or exists " 
                + "(select a from Attribute as a where lower(a.value) like :value and a.groupUuid = g.uuid) ) " 
                + "and g.nameDb like :scope"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByAnyApproximateAttr")
              .setString("value", "%" + val.toLowerCase() + "%").setString("scope", scope + "%").listSet(Group.class);

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
          
            if (!changedQuery) {
              hql.append(" where ");
            } else {
              hql.append(" and ");
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
              hql.append(" theGroup.alternateNameDb like :theAlternateName ");
              byHqlStatic.setString("theAlternateName", "%" + lowerName + "%");
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
            
            
            StringBuilder hql = new StringBuilder("select distinct g from Group as g ");
            if (!Group._internal_fieldAttribute(attr)) {
              hql.append(", Field field, Attribute as a ");
            }
          
            GrouperSession grouperSession = GrouperSession.staticGrouperSession();
            
            //see if we are adding more to the query
            boolean changedQuery = false;
            
            if (secureQuery) {
              changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
                  grouperSession.getSubject(), byHql, 
                  hql, "g.uuid", AccessPrivilege.VIEW_PRIVILEGES);
            }
            
            if (!changedQuery) {
              hql.append(" where ");
            } else {
              hql.append(" and ");
            }
            
            if (Group._internal_fieldAttribute(attr)) {
              hql.append(" lower(g." + attr + "Db) like :value ");
            } else {
              hql.append(" a.groupUuid = g.uuid " +
                "and field.name = :field and lower(a.value) like :value " +
                "and field.uuid = a.fieldId and field.typeString = 'attribute' ");
              byHql.setString("field", attr);
            }
            if (!StringUtils.isBlank(scope)) {
              hql.append(" and g.nameDb like :scope");
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
   * @see     GroupDAO#findAllByApproximateName(String)
   * @since   @HEAD@
   */
  public Set<Group> findAllByApproximateName(final String name)
      throws GrouperDAOException {
    return findAllByApproximateNameHelper(name, null, true, true);
  } 

  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This method will generate a full table scan of the attributes table.  It will not
   * perform well if there are a large number of groups.</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @see     GroupDAO#findAllByApproximateName(String, String)
   * @since   @HEAD@
   */
  public Set<Group> findAllByApproximateName(final String name, final String scope)
      throws GrouperDAOException {
    return findAllByApproximateNameHelper(name, scope, true, true);
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
    return findAllByApproximateNameHelper(name, null, true, false);
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
    return findAllByApproximateNameHelper(name, scope, true, false);
  }
  
  /**
   * Find groups using an approximate string for the alternate name.
   * @param name
   * @return set
   * @throws GrouperDAOException
   */
  public Set<Group> findAllByApproximateAlternateName(final String name)
      throws GrouperDAOException {
    return findAllByApproximateNameHelper(name, null, false, true);
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
    return findAllByApproximateNameHelper(name, scope, false, true);
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
//        querySortField.setColumn("extensionDb");
      }
      if (StringUtils.equals("name", querySortField.getColumn())) {
//        querySortField.setColumn("nameDb");
      }
      if (StringUtils.equals("displayExtension", querySortField.getColumn())) {
        querySortField.setColumn("display_extension");
      }
      if (StringUtils.equals("displayName", querySortField.getColumn())) {
        querySortField.setColumn("display_name");
      }
    }

  }


  /**
   * Helper for find by approximate name queries
   * @param name
   * @param scope
   * @param currentNames
   * @param alternateNames
   * @return set
   * @throws GrouperDAOException
   * @throws IllegalStateException
   */
  private Set<Group> findAllByApproximateNameHelper(final String name, final String scope,
      final boolean currentNames, final boolean alternateNames)
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
              nameFieldsOr.add(Restrictions.ilike("extensionDb", name, MatchMode.ANYWHERE));
              nameFieldsOr.add(Restrictions.ilike("displayExtensionDb", name, MatchMode.ANYWHERE));
            } 

            if (alternateNames) {
              nameFieldsOr.add(Restrictions.ilike("alternateNameDb", name, MatchMode.ANYWHERE));
            }
            
            criterionList.add(nameFieldsOr);
            
            if (scope != null) {
              criterionList.add(Restrictions.like("nameDb", scope, MatchMode.START));
            }
            HibernateSession.byCriteriaStatic().setCacheable(false);
            HibernateSession.byCriteriaStatic().setCacheRegion(KLASS + ".FindAllByApproximateName");
            
            Set<Group> groups = HibernateSession.byCriteriaStatic().listSet(Group.class, 
                HibUtils.listCrit(criterionList));
            
            return groups;
          }
    });
    return resultGroups;
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
                "select g from Group as g, GroupTypeTuple as gtt " +
                "where gtt.typeUuid = :type " +
                "and gtt.groupUuid = g.uuid " +
                "and g.nameDb like :scope"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByType")
              .setString("type", _gt.getUuid()).setString("scope", scope + "%").listSet(Group.class);

            return groups;
          }
    });
    return resultGroups;
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
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    String attributeHql = null;
    
    if (Group._internal_fieldAttribute(attr)) {
      attributeHql = "select g from Group as g where g." + attr + "Db = :value";
      byHqlStatic.createQuery(attributeHql);
    } else {
      attributeHql = "select g from Group as g, Field field, " +
        "Attribute as a where a.groupUuid = g.uuid " +
        "and field.name = :field and a.value like :value " +
        "and field.uuid = a.fieldId and field.typeString = 'attribute'";
      byHqlStatic.createQuery(attributeHql).setString("field", attr);
    }
    

    Group group = 
      byHqlStatic.setCacheable(false)
      .setCacheRegion(KLASS + ".FindByAttribute")
      .setString("value", val).uniqueResult(Group.class);
    
     if (group == null && exceptionIfNotFound) {
       throw new GroupNotFoundException();
     }
     return group;
    
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
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
    .createQuery("select g from Group as g where g.nameDb = :value or g.alternateNameDb = :value")
    .setCacheable(true).setCacheRegion(KLASS + ".FindByName").options(queryOptions);

    Group group = byHqlStatic.setString("value", name).uniqueResult(Group.class);

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
      .createQuery("select g from Group as g where g.nameDb = :value")
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
      .createQuery("select g from Group as g where g.alternateNameDb = :value")
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
    Group group = HibernateSession.byHqlStatic()
      .createQuery("from Group as g where g.uuid = :uuid")
      .setCacheable(true)
      .options(queryOptions)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid).uniqueResult(Group.class);
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
                "select g from Group as g")
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
                "select g from Group as g where g.nameDb like :scope")
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
                "select g from Group as g where g.parentUuid = :parent")
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
    // TODO 20070307 ideally i would just put hooks for associated tables into "onDelete()" 
    //               but right now that is blowing up due to the session being flushed.
    hibernateSession.byHql().createQuery("delete from GroupTypeTuple").executeUpdate();
    hibernateSession.byHql().createQuery("delete from Attribute").executeUpdate(); 
    hibernateSession.byHql().createQuery("delete from Group").executeUpdate();
    getExistsCache().clear();
  } 


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#_findAllTypesByGroup(java.lang.String)
   */
  public Set<GroupType> _findAllTypesByGroup(final String uuid)
    throws  GrouperDAOException {
    Set<GroupType> types = new LinkedHashSet<GroupType>();
    List<GroupTypeTuple> groupTypeTuples = 
      HibernateSession.byHqlStatic()
        .createQuery("from GroupTypeTuple as gtt where gtt.groupUuid = :group")
        .setCacheable(false).setString("group", uuid).list(GroupTypeTuple.class);
    
    GroupTypeDAO dao = GrouperDAOFactory.getFactory().getGroupType(); 
    try {
      for (GroupTypeTuple  gtt : groupTypeTuples) {
        types.add( dao.findByUuid( gtt.getTypeUuid(), true ) );
      }
    } catch (SchemaException eS) {
      throw new GrouperDAOException( "Problem with finding by uuid: " + uuid + ", " + eS.getMessage(), eS );
    }
    return types;
  } // private static Set _findAllTypesByGroup(uuid)


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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

            String attributeHql = null;
            ByHql byHql = hibernateSession.byHql();
            if (Group._internal_fieldAttribute(attr)) {
              attributeHql = "select g from Group as g where g." + attr + "Db = :value";
              byHql.createQuery(attributeHql);
            } else {
              attributeHql = "select g from Group as g, Field field, " +
                "Attribute as a where a.groupUuid = g.uuid " +
                "and field.name = :field and a.value = :value " +
                "and field.uuid = a.fieldId and field.typeString = 'attribute'";
              byHql.createQuery(attributeHql).setString("field", attr);
            }
            
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

            String attributeHql = null;
            ByHql byHql = hibernateSession.byHql();
            if (Group._internal_fieldAttribute(attr)) {
              attributeHql = "select g from Group as g where g." + attr + "Db = :value " +
                "and g.nameDb like :scope";
              byHql.createQuery(attributeHql);
            } else {
              attributeHql = "select g from Group as g, Field field, " +
                "Attribute as a where a.groupUuid = g.uuid " +
                "and field.name = :field and a.value = :value " +
                "and field.uuid = a.fieldId and field.typeString = 'attribute' " +
                "and g.nameDb like :scope";
              byHql.createQuery(attributeHql).setString("field", attr);
            }
            
            Set<Group> groups = byHql.setCacheable(false).setCacheRegion(KLASS + ".FindAllByAttr")
              .setString("value", val).setString("scope", scope + "%").listSet(Group.class);
            
            return groups;
            
          }
    });

    return resultGroups;
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
      .createQuery("from Group as g where g.creatorUuid = :uuid1 or g.modifierUuid = :uuid2")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByCreatorOrModifier")
      .setString( "uuid1", member.getUuid() ).setString("uuid2", member.getUuid())
      .listSet(Group.class);
    return groups;

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
      Set<Privilege> inPrivSet, QueryOptions queryOptions)
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

    try {

      Set<Group> groups = byHqlStatic.createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllGroupsSecure")
        .options(queryOptions)
        .listSet(Group.class);
            
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
  public Set<Group> getAllGroupsSecure(final String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
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

    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }

    sql.append("  theGroup.nameDb like :scope");
    
    try {

      Set<Group> groups = byHqlStatic.createQuery(sql.toString())
        .setString("scope", scope + "%")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllGroupsSecureScope")
        .options(queryOptions)
        .listSet(Group.class);

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
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getImmediateChildrenSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Group> getImmediateChildrenSecure(GrouperSession grouperSession, 
      final Stem stem, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
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

    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
    
    sql.append(" theGroup.parentUuid = :parent ");
    
    try {

      Set<Group> groups = byHqlStatic.createQuery(sql.toString())
        .setString("parent", stem.getUuid())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".getImmediateChildrenSecure")
        .options(queryOptions)
        .listSet(Group.class);

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

    for (int i=0; i<pages; i++) {
      List<String> uuidPageList = GrouperUtil.batchList(uuids, batchSize, i);

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select g from Group as g "
          + " where g.uuid in (");

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
  
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
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
      byHqlStatic.createQuery(sql.toString())
        .setString("listId", listId)
        .setString("memberId", member.getUuid());
      if (hasScope) {
        byHqlStatic.setString("scope", scope + "%");
      }
  
      Set<Group> groups = byHqlStatic
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllGroupsSecureScope")
        .options(queryOptions)
        .listSet(Group.class);
  
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
  
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
    
    sql.append(" theGroup.parentUuid = :parent " +
        " and listMembership.ownerGroupId = theGroup.uuid and listMembership.fieldId = :listId" +
        " and listMembership.memberUuid = :memberId ");
    
    if (enabledOnly) {
      sql.append(" and listMembership.enabledDb = 'T'");
    }
    
    Member member = MemberFinder.internal_findBySubject(subject, null, false);
    
    try {
  
      Set<Group> groups = byHqlStatic.createQuery(sql.toString())
        .setString("parent", stem.getUuid())
        .setString("listId", listId)
        .setString("memberId", member.getUuid())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".getImmediateChildrenSecure")
        .options(queryOptions)
        .listSet(Group.class);  
      
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
        GrouperUtil.toList((Object) System.currentTimeMillis(), groupId));
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#updateLastImmediateMembershipChange(java.lang.String)
   */
  public void updateLastImmediateMembershipChange(String groupId) {
    HibernateSession.bySqlStatic().executeSql(
        "update grouper_groups set last_imm_membership_change = ? where id = ?",
        GrouperUtil.toList((Object) System.currentTimeMillis(), groupId));
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#updateLastMembershipChangeIncludeAncestorGroups(java.lang.String)
   */
  public void updateLastMembershipChangeIncludeAncestorGroups(String groupId) {
    
    // note that i'm not doing this all in one update statement with a subquery due to
    // a mysql bug:  http://bugs.mysql.com/bug.php?id=8139
    
    Set<String> groupIds = GrouperDAOFactory.getFactory().getGroupSet().findAllOwnerGroupsByMemberGroup(groupId);
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
      
      String queryInClause = HibUtils.convertToInClauseForSqlStatic(groupIdsInBatch);
      HibernateSession.bySqlStatic().executeSql(queryPrefix + " in (" + queryInClause + ")", params); 
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
  
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
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
    try {
      Group group = HibernateSession.byHqlStatic()
        .createQuery("from Group as theGroup where theGroup.uuid = :uuid or theGroup.nameDb = :name")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
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
                "select g from Group as g, GroupTypeTuple as gtt " +
                "where gtt.typeUuid = :type " +
                "and gtt.groupUuid  = g.uuid"
              );
            
            if (queryOptions != null && queryOptions.getSecondLevelCache() != null && !queryOptions.getSecondLevelCache()) {
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
    grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(grouperSession.getSubject(), byHqlStatic, 
        sql, "theGroup.uuid", adminSet);

    boolean changedQueryNotWithPriv = grouperSession.getAccessResolver().hqlFilterGroupsNotWithPrivWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", privilege, considerAllSubject);

    if (!StringUtils.isBlank(sqlLikeString)) {
      sql.append(" and theGroup.nameDb like :sqlLikeString ");
      byHqlStatic.setString("sqlLikeString", sqlLikeString);
    }
    
    switch (scope) {
      case ONE:
        
        sql.append(" and theGroup.parentUuid = :stemId ");
        byHqlStatic.setString("stemId", stemId);
        
        break;
        
      case SUB:
        
        Stem stem = StemFinder.findByUuid(grouperSession, stemId, true);
        sql.append(" and theGroup.nameDb like :stemPattern ");
        byHqlStatic.setString("stemPattern", stem.getName() + ":%");

        break;
        
      default:
        throw new RuntimeException("Need to pass in a scope, or its not implemented: " + scope);
    }
    
    Set<Group> groups = byHqlStatic.createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindGroupsInStemWithoutPrivilege")
      .options(queryOptions)
      .listSet(Group.class);
          
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
   * @see GroupDAO#
   * @Override
   */
  public Set<Group> getAllGroupsSplitScopeSecure(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, TypeOfGroup typeOfGroup) {
    return findAllGroupsSecureHelper(scope, grouperSession, subject, privileges, queryOptions, true, typeOfGroup);
  }

  /**
   * 
   * @param scope 
   * @param grouperSession 
   * @param subject 
   * @param privileges 
   * @param queryOptions 
   * @param splitScope 
   * @param typeOfGroup
   * @return groups
   * 
   */
  private Set<Group> findAllGroupsSecureHelper(String scope,
      GrouperSession grouperSession, Subject subject, Set<Privilege> privileges,
      QueryOptions queryOptions, boolean splitScope, TypeOfGroup typeOfGroup) {
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }
  
    StringBuilder sql = new StringBuilder(
        "select distinct theGroup from Group theGroup ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic,
        sql, "theGroup.uuid", privileges);

    StringBuilder whereClause = new StringBuilder();
    
    if (typeOfGroup != null) {
      if (whereClause.length() > 0) {
        whereClause.append(" and ");
      }
      whereClause.append(" theGroup.typeOfGroupDb = :theTypeOfGroup ");
      byHqlStatic.setString("theTypeOfGroup", typeOfGroup.name());

    }
    
    //see if there is a scope
    if (!StringUtils.isBlank(scope)) {
      scope = scope.toLowerCase();

      String[] scopes = splitScope ? GrouperUtil.splitTrim(scope, " ") : new String[]{scope};

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
        whereClause.append(" ( lower(theGroup.nameDb) like :scope" + index 
            + " or lower(theGroup.displayNameDb) like :scope" + index 
            + " or lower(theGroup.descriptionDb) like :scope" + index + " ) ");
        if (splitScope) {
          theScope = "%" + theScope + "%";
        } else if (!theScope.endsWith("%")) {
          theScope += "%";
        }
        byHqlStatic.setString("scope" + index, theScope);
        index++;
      }
      whereClause.append(" ) ) ");
    }
    if (changedQuery) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
    sql.append(whereClause);

    Set<Group> groups = byHqlStatic.createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".GetAllGroupsSecure")
      .options(queryOptions)
      .listSet(Group.class);
    
    return groups;
  
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#findAllByApproximateAttrSecure(java.lang.String, java.lang.String, java.lang.String)
   */
  public Set<Group> findAllByApproximateAttrSecure(String attr, String val, String scope)
      throws GrouperDAOException, IllegalStateException {
    return findAllByApproximateAttrHelper(attr, val, scope, true);
  }

  

} 

