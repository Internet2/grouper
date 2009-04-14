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
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
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
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Basic Hibernate <code>Group</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GroupDAO.java,v 1.39 2009-04-14 07:41:24 mchyzer Exp $
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
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            GroupTypeTuple groupTypeTuple = new GroupTypeTuple();
            groupTypeTuple.assignGroupUuid( _g.getUuid(), _g );
            groupTypeTuple.setTypeUuid( _gt.getUuid() );
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            
            String attributeHql = null;
            
            ByHqlStatic byHql = HibernateSession.byHqlStatic();
            
            if (Group._internal_fieldAttribute(attr)) {
              attributeHql = "select g from Group as g where lower(g." + attr + "Db) like :value";
              byHql.createQuery(attributeHql);
            } else {
              attributeHql = "select g from Group as g, Field field, " +
                "Attribute as a where a.groupUuid = g.uuid " +
                "and field.name = :field and lower(a.value) like :value " +
                "and field.uuid = a.fieldId and field.typeString = 'attribute'";
              byHql.createQuery(attributeHql).setString("field", attr);
            }

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
   * @see     GroupDAO#findAllByApproximateAttr(String, String, String)
   * @since   @HEAD@
   */
  public Set<Group> findAllByApproximateAttr(final String attr, final String val, final String scope)
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            String attributeHql = null;
            
            ByHqlStatic byHql = HibernateSession.byHqlStatic();
            
            if (Group._internal_fieldAttribute(attr)) {
              attributeHql = "select g from Group as g where lower(g." + attr + "Db) like :value " +
              		"and g.nameDb like :scope";
              byHql.createQuery(attributeHql);
            } else {
              attributeHql = "select g from Group as g, Field field, " +
                "Attribute as a where a.groupUuid = g.uuid " +
                "and field.name = :field and lower(a.value) like :value " +
                "and field.uuid = a.fieldId and field.typeString = 'attribute' " +
                "and g.nameDb like :scope";
              byHql.createQuery(attributeHql).setString("field", attr);
            }

            Set<Group> groups = byHql.setCacheable(false).setCacheRegion(KLASS + ".FindAllByApproximateAttr")
              .setString("scope", scope + "%")
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
   * @param _gt
   * @return set
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public Set<Group> findAllByType(final GroupType _gt) 
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
                "and gtt.groupUuid  = g.uuid"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByType")
              .setString( "type", _gt.getUuid() ).listSet(Group.class);

            return groups;
          }
    });
    return resultGroups;
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
    Group group = HibernateSession.byHqlStatic()
      .createQuery("select g from Group as g where g.nameDb = :value or g.alternateNameDb = :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByName")
      .setString("value", name).uniqueResult(Group.class);

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
    Group dto = HibernateSession.byHqlStatic()
      .createQuery("from Group as g where g.uuid = :uuid")
      .setCacheable(true)
      .options(queryOptions)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid).uniqueResult(Group.class);
    if (dto == null) {
       throw new GroupNotFoundException();
    }
    return dto;
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
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getAllGroupsMembershipSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Group> getAllGroupsMembershipSecure(GrouperSession grouperSession, Subject subject, 
      Set<Privilege> inPrivSet, QueryOptions queryOptions)
      throws  GrouperDAOException {
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }
    String listId = Group.getDefaultList().getUuid();

    StringBuilder sql = new StringBuilder("select distinct theGroup from Group theGroup, Membership listMembership ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", inPrivSet);
  
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
    
    sql.append("  listMembership.ownerGroupId = theAttribute.groupUuid and listMembership.fieldId = :listId" +
        " and listMembership.memberUuid = :memberId ");
    
    Member member = MemberFinder.internal_findBySubject(subject, null, false);
    
    try {
  
      Set<Group> groups = byHqlStatic.createQuery(sql.toString())
        .setString("listId", listId)
        .setString("memberId", member.getUuid())
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
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getAllGroupsMembershipSecure(java.lang.String, edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Group> getAllGroupsMembershipSecure(final String scope, GrouperSession grouperSession, 
      Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException {
  
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }

    String listId = Group.getDefaultList().getUuid();
  
    StringBuilder sql = new StringBuilder("select distinct theGroup from Group theGroup, " +
    		" Membership listMembership ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theGroup.uuid", inPrivSet);
  
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
  
    sql.append( " theGroup.nameDb like :scope" +
        " and listMembership.ownerGroupId = nameAttribute.groupUuid and listMembership.fieldId = :listId" +
        " and listMembership.memberUuid = :memberId ");
    
    Member member = MemberFinder.internal_findBySubject(subject, null, false);
    
    if (member == null) {
      new LinkedHashSet<Group>();
    }
    
    try {
  
      Set<Group> groups = byHqlStatic.createQuery(sql.toString())
        .setString("listId", listId)
        .setString("memberId", member.getUuid())
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
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#getImmediateChildrenMembershipSecure(edu.internet2.middleware.grouper.GrouperSession, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.subject.Subject, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Group> getImmediateChildrenMembershipSecure(GrouperSession grouperSession, 
      final Stem stem, Subject subject, Set<Privilege> inPrivSet, QueryOptions queryOptions)
    throws  GrouperDAOException {
  
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("theGroup.displayNameDb");
    }

    String listId = Group.getDefaultList().getUuid();

    StringBuilder sql = new StringBuilder("select distinct theGroup from Group theGroup," +
    		" Membership listMembership ");
  
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
        " and listMembership.ownerGroupId = theAttribute.groupUuid and listMembership.fieldId = :listId" +
        " and listMembership.memberUuid = :memberId ");

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

} 

