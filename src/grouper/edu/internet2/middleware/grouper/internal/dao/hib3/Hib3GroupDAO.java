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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
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
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Basic Hibernate <code>Group</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GroupDAO.java,v 1.30 2009-03-15 08:18:10 mchyzer Exp $
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
            groupTypeTuple.setGroupUuid( _g.getUuid() );
            groupTypeTuple.setTypeUuid( _gt.getUuid() );
            hibernateSession.byObject().save(groupTypeTuple);
 
            //get it again in case it was changed in the hook
            Group g2 = null;
            try {
              g2 = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), _g.getUuid(), true);
            } catch (GroupNotFoundException gnfe) {
              throw new RuntimeException("Weird problem getting group: " + _g.getName());
            }

            //note this used to be saveOrUpdate
            hibernateSession.byObject().update( g2 ); // modified group
 
            //let HibernateSession commit or rollback depending on if problem or enclosing transaction
            return groupTypeTuple;
          }
    });
  } 

  /**
   * @param _g 
   * @param mships 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void delete(final Group _g, final Set mships)
    throws  GrouperDAOException {

    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            ByObject byObject = hibernateSession.byObject();
            // delete memberships
            byObject.delete(mships);
            hibernateSession.misc().flush();
            
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
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.GroupDAO#deleteType(edu.internet2.middleware.grouper.Group, edu.internet2.middleware.grouper.GroupType)
   */
  public GroupTypeTuple deleteType(final Group group, final GroupType groupType) 
    throws  GrouperDAOException {
    return (GroupTypeTuple)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            
            GroupTypeTuple groupTypeTuple = Hib3GroupTypeTupleDAO.findByGroupAndType(group, groupType);
            hibernateSession.byObject().delete( groupTypeTuple );
            
            //delete all attributes used by the group of this type
            Set<Field> fields = GrouperUtil.nonNull(groupType.getFields());
            
            Map<String, String> attributes = group.getAttributesDb();

            for (Field field : fields) {
              
              if (attributes.containsKey(field.getName())) {
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
            Set types = group.getTypesDb();
            types.remove( groupType );

            //no need to call group.store, since the below will take care of attribute changes
            //NOTE: used to be saveOrUpdate
            hibernateSession.byObject().update( group ); 
            
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
      .createQuery("select g.id from Group as g where g.uuid = :uuid")
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
   * @param uuid 
   * @return map
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Map<String, String> findAllAttributesByGroup(final String uuid) throws  GrouperDAOException {
    final Map attrs = new HashMap();

    List<Attribute> hib3Attributes = HibernateSession.byHqlStatic()
      .setGrouperTransactionType(GrouperTransactionType.READONLY_OR_USE_EXISTING)
      .createQuery("from Attribute as a where a.groupUuid = :uuid")
      .setCacheable(false).setCacheRegion(KLASS + ".FindAllAttributesByGroup")
      .setString("uuid", uuid).list(Attribute.class);
    
    for (Attribute attribute : hib3Attributes) {
      attrs.put( attribute.getAttrName(), attribute.getValue() );
    }
    return attrs;
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
                  + "or g.uuid in " +
                "(select a.groupUuid from Attribute as a where lower(a.value) like :value) "
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
                + "or g.uuid in " 
                + "(select a.groupUuid from Attribute as a where lower(a.value) like :value)) " 
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
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            Set<Group> groups = hibernateSession.byHql().createQuery(
                "select g from Group as g where lower(g.nameDb) like  :value " +
                  "or lower(g.displayNameDb) like :value " +
                  "or lower(g.extensionDb) like :value " +
                  "or lower(g.displayExtensionDb) like :value  "
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByApproximateName")
              .setString( "value", "%" + name.toLowerCase() + "%" ).listSet(Group.class);

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
   * @see     GroupDAO#findAllByApproximateName(String, String)
   * @since   @HEAD@
   */
  public Set<Group> findAllByApproximateName(final String name, final String scope)
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            Set<Group> groups = hibernateSession.byHql().createQuery(
                "select g from Group as g where (lower(g.nameDb) like  :value " +
                "or lower(g.displayNameDb) like :value " +
                "or lower(g.extensionDb) like :value " +
                "or lower(g.displayExtensionDb) like :value) and g.nameDb like :scope "
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByApproximateName")
              .setString("value", "%" + name.toLowerCase() + "%").setString("scope", scope + "%").listSet(Group.class);

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
      .createQuery("select g from Group as g where g.nameDb = :value")
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
   * <p><b>Implementation Notes.</b</p>
   * <ol>
   * <li>Hibernate caching is enabled.</li>
   * </ol>
   * @see     GroupDAO#findByUuid(String, boolean)
   * @since   @HEAD@
   */
  public Group findByUuid(String uuid, boolean exceptionIfNotFound)
      throws GrouperDAOException, GroupNotFoundException {
    Group dto = HibernateSession.byHqlStatic()
      .createQuery("from Group as g where g.uuid = :uuid")
      .setCacheable(true)
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
   * @param mof
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public void revokePriv(final Group _g, final DefaultMemberOf mof)
    throws  GrouperDAOException {
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            
            ByObject byObject = hibernateSession.byObject();
            byObject.delete(mof.getDeletes());
            hibernateSession.misc().flush();
            
            byObject.saveOrUpdate(mof.getSaves());
            hibernateSession.misc().flush();
            
            byObject.update( _g );
            return null;
          }
    });
  } 

  /**
   * @param _g
   * @param toDelete
   * @throws GrouperDAOException
   * @since   @HEAD@
   */
  public void revokePriv(final Group _g, final Set toDelete)
    throws  GrouperDAOException {
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            ByObject byObject = hibernateSession.byObject();
            byObject.delete(toDelete);
            byObject.update( _g );
            return null;
          }
    });
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


  /**
   * update the attributes for a group
   * @param hibernateSession 
   * @param checkExisting true if an update, false if insert
   * @param group
   */
  public void _updateAttributes(HibernateSession hibernateSession, boolean checkExisting, Group group) {
    ByObject byObject = hibernateSession.byObject();
    // TODO 20070531 split and test
    ByHql byHql = hibernateSession.byHql();
    byHql.createQuery("from Attribute as a where a.groupUuid = :uuid");
    byHql.setCacheable(false);
    byHql.setCacheRegion(KLASS + "._UpdateAttributes");
    byHql.setString("uuid", group.getUuid());
    Map                   attrs = new HashMap(group.getAttributesDb());
    String                k;
    //TODO CH 20080217: replace with query.list() and see if p6spy generates fewer queries
    List<Attribute> attributes = checkExisting ? GrouperUtil.nonNull(byHql.list(Attribute.class)) : new ArrayList<Attribute>();
    for (Attribute attribute : attributes) {
      k = attribute.getAttrName();
      if ( attrs.containsKey(k) ) {
        // attr both in db and in memory.  compare.
        if ( !attribute.getValue().equals( (String) attrs.get(k) ) ) {
          attribute.setValue( (String) attrs.get(k) );
          byObject.update(attribute);
        }
        attrs.remove(k);
      }
      else {
        // attr only in db.
        byObject.delete(attribute);
        attrs.remove(k);
      }
    }
    // now handle entries that were only in memory
    Map.Entry kv;
    Iterator it = attrs.entrySet().iterator();
    while (it.hasNext()) {
      kv = (Map.Entry) it.next();
      Attribute attributeDto = new Attribute(); 
      attributeDto.setFieldId( FieldFinder.findFieldIdForAttribute((String) kv.getKey(), true ));
      attributeDto.setGroupUuid(group.getUuid());
      attributeDto.setValue( (String) kv.getValue() );
      byObject.save(attributeDto);
    }
  } // private void _updateAttributes(hs)

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

} 

