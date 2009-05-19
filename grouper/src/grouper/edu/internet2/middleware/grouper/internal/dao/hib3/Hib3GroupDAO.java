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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.ByHql;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Basic Hibernate <code>Group</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GroupDAO.java,v 1.24.2.3 2009-05-19 11:49:51 mchyzer Exp $
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
   * @param _g 
   * @param _gt 
   * @throws GrouperDAOException 
   */
  public void addType(final Group _g, final GroupType _gt) 
    throws  GrouperDAOException {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            hibernateSession.byObject().save(  // new group-type tuple
                new GroupTypeTuple()
                  .setGroupUuid( _g.getUuid() )
                  .setTypeUuid( _gt.getUuid() )
              );
 
            //get it again in case it was changed in the hook
            Group g2 = null;
            try {
              g2 = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), _g.getUuid());
            } catch (GroupNotFoundException gnfe) {
              throw new RuntimeException("Weird problem getting group: " + _g.getName());
            }

            //note this used to be saveOrUpdate
            hibernateSession.byObject().update( g2 ); // modified group
 
            //let HibernateSession commit or rollback depending on if problem or enclosing transaction
            return null;
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

    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
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
   * @param group 
   * @param groupType 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void deleteType(final Group group, final GroupType groupType) 
    throws  GrouperDAOException {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            
            hibernateSession.byObject().delete( Hib3GroupTypeTupleDAO.findByGroupAndType(group, groupType) );
            
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

            group.internal_setModified();

            //no need to call group.store, since the below will take care of attribute changes
            //NOTE: used to be saveOrUpdate
            hibernateSession.byObject().update( group ); 
            
            return null;
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

    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            
            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a where a.groupUuid in " +
                "(select a2.groupUuid from Attribute as a2 where lower(a2.value) like :value) " +
                "and a.groupUuid = g.uuid"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByAnyApproximateAttr")
              .setString( "value", "%" + val.toLowerCase() + "%" ).list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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

    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            
            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a, Field as field, " +
                "Attribute as a2, Attribute as a3 where a.groupUuid = g.uuid and field.uuid = a3.fieldId " +
                "and a.groupUuid = a2.groupUuid and a.groupUuid = a3.groupUuid " +
                "and lower(a2.value) like :value and field.name='name' and field.typeString='attribute' and a3.value like :scope"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByAnyApproximateAttr")
              .setString("value", "%" + val.toLowerCase() + "%").setString("scope", scope + "%").list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a, Field as field, " +
                "Attribute as a2 where a.groupUuid = g.uuid " +
                "and a.groupUuid = a2.groupUuid and field.uuid = a2.fieldId and field.name = :field and lower(a2.value) like :value"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByApproximateAttr")
              .setString("field", attr)
              .setString( "value", "%" + val.toLowerCase() + "%" ).list(Object[].class);
  
            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a, Field as field, Field as field2, " +
                "Attribute as a2, Attribute as a3 where a.groupUuid = g.uuid and field2.uuid = a3.fieldId " +
                "and a.groupUuid = a2.groupUuid and a.groupUuid = a3.groupUuid and field.uuid = a2.fieldId and field.name = :field " +
                "and lower(a2.value) like :value and field2.name='name' and field2.typeString='attribute' and a3.value like :scope"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByApproximateAttr")
              .setString("field", attr)
              .setString("value", "%" + val.toLowerCase() + "%")
              .setString("scope", scope + "%")
              .list(Object[].class);
  
            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a where a.groupUuid in " +
                "(select a2.groupUuid from Attribute as a2, Field as field where " +
                  "   ((field.name = 'name'              and lower(a2.value) like :value) " +
                  "or (field.name = 'displayName'       and lower(a2.value) like :value) " +
                  "or (field.name = 'extension'         and lower(a2.value) like :value) " +
                  "or (field.name = 'displayExtension'  and lower(a2.value) like :value))  " +
                  "and field.uuid = a2.fieldId and field.typeString = 'attribute' " +
                ") " + "and a.groupUuid = g.uuid"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByApproximateName")
              .setString( "value", "%" + name.toLowerCase() + "%" ).list(Object[].class);

            Set  groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a where a.groupUuid in " +
                "(select a2.groupUuid from Attribute as a2, Attribute as a3, Field as field, Field as field2 where " +
                  "   ((field.name = 'name'              and lower(a2.value) like :value) " +
                  "or (field.name = 'displayName'       and lower(a2.value) like :value) " +
                  "or (field.name = 'extension'         and lower(a2.value) like :value) " +
                  "or (field.name = 'displayExtension'  and lower(a2.value) like :value))  " +
                  "and field.uuid = a2.fieldId and field.typeString = 'attribute' " +
                  "and field2.uuid = a3.fieldId and a2.groupUuid = a3.groupUuid " +
                  "and field2.name = 'name' and field2.typeString = 'attribute' and a3.value like :scope " +
                ") " + "and a.groupUuid = g.uuid"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByApproximateName")
              .setString("value", "%" + name.toLowerCase() + "%").setString("scope", scope + "%").list(Object[].class);

            Set  groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a where g.createTimeLong > :time " +
                "and a.groupUuid = g.uuid"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByCreatedAfter")
              .setLong( "time", d.getTime() ).list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
  public Set<Group> findAllByCreatedAfter(final Date d, final String scope)
    throws  GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a, Attribute as a2, Field as field where g.createTimeLong > :time " +
                "and a.groupUuid = g.uuid and a.groupUuid = a2.groupUuid and field.uuid = a2.fieldId " +
                "and field.name='name' and field.typeString='attribute' and a2.value like :scope"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByCreatedAfter")
              .setLong("time", d.getTime()).setString("scope", scope + "%").list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
  public Set<Group> findAllByCreatedBefore(final Date d) 
    throws  GrouperDAOException {
    Set resultGroups = (Set) HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

      public Object callback(HibernateSession hibernateSession) {
        
        List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
            "select g, a from Group as g, Attribute as a where g.createTimeLong < :time " +
            "and a.groupUuid = g.uuid"
          ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByCreatedBefore")
          .setLong( "time", d.getTime() ).list(Object[].class);

        Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
        return groups ;
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
  public Set<Group> findAllByCreatedBefore(final Date d, final String scope)
    throws  GrouperDAOException {
    Set resultGroups = (Set) HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

      public Object callback(HibernateSession hibernateSession) {
        
        List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
            "select g, a from Group as g, Attribute as a, Attribute as a2, Field as field where g.createTimeLong < :time " +
            "and a.groupUuid = g.uuid and a.groupUuid = a2.groupUuid and field.uuid = a2.fieldId " +
            "and field.name='name' and field.typeString='attribute' and a2.value like :scope"
          ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByCreatedBefore")
          .setLong("time", d.getTime()).setString("scope", scope + "%").list(Object[].class);

        Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
        return groups ;
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
  public Set<Group> findAllByModifiedAfter(final Date d) 
    throws  GrouperDAOException {
    
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

      public Object callback(HibernateSession hibernateSession) {

        List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
            "select g, a from Group as g, Attribute as a where g.modifyTimeLong > :time " +
            "and a.groupUuid = g.uuid"
          ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByModifiedAfter")
          .setLong( "time", d.getTime() ).list(Object[].class);

        Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
  public Set<Group> findAllByModifiedAfter(final Date d, final String scope)
    throws  GrouperDAOException {

    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

      public Object callback(HibernateSession hibernateSession) {

        List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
            "select g, a from Group as g, Attribute as a, Attribute as a2, Field as field where g.modifyTimeLong > :time " +
            "and a.groupUuid = g.uuid and a.groupUuid = a2.groupUuid and field.uuid = a2.fieldId " +
            "and field.name='name' and field.typeString='attribute' and a2.value like :scope"
          ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByModifiedAfter")
          .setLong("time", d.getTime()).setString("scope", scope + "%").list(Object[].class);

        Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
  public Set<Group> findAllByModifiedBefore(final Date d) 
    throws  GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a where g.modifyTimeLong < :time " +
                "and a.groupUuid = g.uuid"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByModifiedBefore")
              .setLong( "time", d.getTime() ).list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
  public Set<Group> findAllByModifiedBefore(final Date d, final String scope)
    throws  GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a, Attribute as a2, Field as field where g.modifyTimeLong < :time " +
                "and a.groupUuid = g.uuid and a.groupUuid = a2.groupUuid and field.uuid = a2.fieldId " +
                "and field.name='name' and field.typeString='attribute' and a2.value like :scope"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByModifiedBefore")
              .setLong("time", d.getTime()).setString("scope", scope + "%").list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
            return groups;
          }
    });
    return resultGroups;
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
        GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a, GroupTypeTuple as gtt " +
                "where gtt.typeUuid = :type " +
                "and a.groupUuid = gtt.groupUuid and a.groupUuid = g.uuid"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByType")
              .setString( "type", _gt.getUuid() ).list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
        GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a, Attribute as a2, Field as field, GroupTypeTuple as gtt " +
                "where gtt.typeUuid = :type " +
                "and a.groupUuid = gtt.groupUuid and a.groupUuid = g.uuid " +
                "and field.uuid = a2.fieldId and a.groupUuid = a2.groupUuid " +
                "and field.name = 'name' and field.typeString = 'attribute' and a2.value like :scope"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByType")
              .setString("type", _gt.getUuid()).setString("scope", scope + "%").list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
  public Group findByAttribute(String attr, String val) 
    throws  GrouperDAOException,
            GroupNotFoundException {
    Attribute a = HibernateSession.byHqlStatic()
      .createQuery("select a from Attribute as a, Field field where field.typeString = 'attribute'" +
      		" and field.name = :field and a.value like :value and a.fieldId = field.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByAttribute")
      .setString("field", attr).setString("value", val).uniqueResult(Attribute.class);
    
     if (a == null) {
       throw new GroupNotFoundException();
     }
     return this.findByUuid( a.getGroupUuid() );
  } 

  /**
   * @param name
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   * @since   @HEAD@
   */
  public Group findByName(final String name) 
    throws  GrouperDAOException,
            GroupNotFoundException {
    return findByName(name, true);
  }

  /**
   * @param name
   * @param useCache if we should use cache or not
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   * @since   @HEAD@
   */
  public Group findByName(final String name, boolean useCache) 
    throws  GrouperDAOException,
            GroupNotFoundException {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .createQuery("select g from Attribute as a, Group as g, Field as field where a.groupUuid = g.uuid " +
      "and field.name = 'name' and a.value = :value and field.typeString = 'attribute' and a.fieldId = field.uuid")
      .setCacheable(useCache);
    
    if (useCache) {
      byHqlStatic.setCacheRegion(KLASS + ".FindByName");
    }
    
    Group group = byHqlStatic.setString("value", name).uniqueResult(Group.class);

    //handle exceptions out of data access method...
    if (group == null) {
      throw new GroupNotFoundException("Cannot find group with name: '" + name + "'");
    }
    return group;
  } 

  /**
   * <p><b>Implementation Notes.</b</p>
   * <ol>
   * <li>Hibernate caching is enabled.</li>
   * </ol>
   * @see     GroupDAO#findByUuid(String)
   * @since   @HEAD@
   */
  public Group findByUuid(String uuid) 
    throws  GrouperDAOException,
            GroupNotFoundException  {
    Group dto = HibernateSession.byHqlStatic()
      .createQuery("from Group as g where g.uuid = :uuid")
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid).uniqueResult(Group.class);
    if (dto == null) {
       throw new GroupNotFoundException("Cant find group by id: " + uuid);
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a where a.groupUuid = g.uuid")
              .setCacheable(false)
              .setCacheRegion(KLASS + ".GetAllGroups")
              .list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a, Attribute as a2, Field field where a.groupUuid = g.uuid " +
                "and a.groupUuid = a2.groupUuid and field.uuid = a2.fieldId and field.name='name' " +
                "and field.typeString='attribute' and a2.value like :scope")
              .setCacheable(false)
              .setCacheRegion(KLASS + ".GetAllGroups")
              .setString("scope", scope + "%")
              .list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a where a.groupUuid = g.uuid and g.parentUuid = :parent")
              .setCacheable(false)
              .setCacheRegion(KLASS + ".GetImmediateChildren")
              .setString("parent", stem.getUuid())
              .list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            
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
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
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
        types.add( dao.findByUuid( gtt.getTypeUuid() ) );
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
      attributeDto.setFieldId( FieldFinder.findFieldIdForAttribute((String) kv.getKey() ));
      attributeDto.setGroupUuid(group.getUuid());
      attributeDto.setValue( (String) kv.getValue() );
      byObject.save(attributeDto);
    }
  }

  /**
   * @param hib3GroupAttributes 
   * @return the set of dtos
   * @throws HibernateException 
   * 
   */
  private Set<Group> _getGroupsFromGroupsAndAttributesQuery(List<Object[]> hib3GroupAttributes)
    throws  HibernateException {   
    Iterator it = hib3GroupAttributes.iterator();
    Map<String, Group> results = new HashMap<String, Group>();
        
    while (it.hasNext()) {
      Object[] tuple = (Object[])it.next();
      Group group = (Group)tuple[0];
      String groupUuid = group.getUuid();
      Map currAttributes = null;
      if (results.containsKey(groupUuid)) {
        group = (Group)results.get(groupUuid);
        currAttributes = group.getAttributesDb();
      } else {
        currAttributes = new HashMap();
      }
      Attribute currAttribute = (Attribute)tuple[1];
      currAttributes.put(currAttribute.getAttrName(), currAttribute.getValue());
      group.setAttributes(currAttributes);
      results.put(groupUuid, group);
    }
    
    Set groups = new LinkedHashSet(results.values());
      
    return groups;
  } // private Set _getGroupsFromGroupsAndAttributesQuery(qry)
  
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a, Field field, " +
                "Attribute as a2 where a.groupUuid = g.uuid " +
                "and a.groupUuid = a2.groupUuid and field.name = :field and a2.value = :value " +
                "and field.uuid = a2.fieldId and field.typeString = 'attribute'"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByAttr")
              .setString("field", attr).setString( "value", val ).list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {

            List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                "select g, a from Group as g, Attribute as a, Field field, Field field2, " +
                "Attribute as a2, Attribute as a3 where a.groupUuid = g.uuid " +
                "and a.groupUuid = a2.groupUuid and field.name = :field and a2.value = :value " +
                "and field.uuid = a2.fieldId and field.typeString = 'attribute' " +
                "and a.groupUuid = a3.groupUuid and field2.uuid = a3.fieldId and field2.name='name' " +
                "and field2.typeString='attribute' and a3.value like :scope"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByAttr")
              .setString("field", attr).setString("value", val).setString("scope", scope + "%").list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(groupAttributes);
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
      queryOptions.sortAsc("displayName");
    }
    //TODO update for 1.5
    String fieldId = FieldFinder.findFieldIdForAttribute(
        queryOptions.getQuerySort().getQuerySortFields().get(0).getColumn());

    StringBuilder sql = new StringBuilder("select distinct theAttribute from Attribute theAttribute ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theAttribute.groupUuid", inPrivSet);

    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
    
    sql.append("  theAttribute.fieldId = :theFieldId ");
    
    QuerySort querySort = queryOptions.getQuerySort();
    try {
      //swap this out for value
      queryOptions.sort(new QuerySort("theAttribute.value", querySort.getQuerySortFields().get(0).isAscending()));

      List<Attribute> attributes = byHqlStatic.createQuery(sql.toString())
        .setString("theFieldId", fieldId)
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllGroupsSecure")
        .options(queryOptions)
        .list(Attribute.class);
      
      //get a list of unique uuids
      Collection<String> groupIds = new LinkedHashSet<String>(
          GrouperUtil.propertyList(attributes, Attribute.PROPERTY_GROUP_UUID, String.class));
      
      Set<Group> groups = findByUuids(groupIds, false);
      
      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, subject, inPrivSet);

      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    } finally {
      //put this back to the way it was
      queryOptions.sort(querySort);
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
      queryOptions.sortAsc("displayName");
    }
    //TODO update for 1.5
    String fieldId = FieldFinder.findFieldIdForAttribute(
        queryOptions.getQuerySort().getQuerySortFields().get(0).getColumn());
    String nameFieldId = FieldFinder.findFieldIdForAttribute(
        "name");

    StringBuilder sql = new StringBuilder("select distinct theAttribute from Attribute theAttribute, Attribute nameAttribute");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theAttribute.groupUuid", inPrivSet);

    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }

    sql.append("  theAttribute.groupUuid = nameAttribute.groupUuid and theAttribute.fieldId = :theFieldId " +
        " and nameAttribute.fieldId = :nameFieldId and nameAttribute.value like :scope");
    
    QuerySort querySort = queryOptions.getQuerySort();
    try {
      //swap this out for value
      queryOptions.sort(new QuerySort("theAttribute.value", querySort.getQuerySortFields().get(0).isAscending()));

      List<Attribute> attributes = byHqlStatic.createQuery(sql.toString())
        .setString("theFieldId", fieldId)
        .setString("nameFieldId", nameFieldId)
        .setString("scope", scope + "%")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllGroupsSecureScope")
        .options(queryOptions)
        .list(Attribute.class);

      Set<String> groupIds = new LinkedHashSet<String>(
          GrouperUtil.propertyList(attributes, Attribute.PROPERTY_GROUP_UUID, String.class));
      
      Set<Group> groups = findByUuids(groupIds, false);
      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, subject, inPrivSet);

      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    } finally {
      //put this back to the way it was
      queryOptions.sort(querySort);
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
      queryOptions.sortAsc("displayName");
    }
    //TODO update for 1.5
    String fieldId = FieldFinder.findFieldIdForAttribute(
        queryOptions.getQuerySort().getQuerySortFields().get(0).getColumn());

    StringBuilder sql = new StringBuilder("select distinct theAttribute from Attribute theAttribute, Group as theGroup ");

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theAttribute.groupUuid", inPrivSet);

    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
    
    sql.append(" theAttribute.fieldId = :theFieldId and theGroup.parentUuid = :parent " +
    " and theAttribute.groupUuid = theGroup.uuid");
    
    QuerySort originalQuerySort = queryOptions.getQuerySort();
    try {
      //swap this out for value
      queryOptions.sort(new QuerySort("theAttribute.value", originalQuerySort.getQuerySortFields().get(0).isAscending()));

      List<Attribute> attributes = byHqlStatic.createQuery(sql.toString())
        .setString("theFieldId", fieldId)
        .setString("parent", stem.getUuid())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".getImmediateChildrenSecure")
        .options(queryOptions)
        .list(Attribute.class);

      Set<String> groupIds = new LinkedHashSet<String>(GrouperUtil.propertyList(
          attributes, Attribute.PROPERTY_GROUP_UUID, String.class));
      
      Set<Group> groups = findByUuids(groupIds, false);
      
      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, subject, inPrivSet);

      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    } finally {
      //put this back to the way it was
      queryOptions.sort(originalQuerySort);
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
      StringBuilder query = new StringBuilder("select g, a from Group as g, Attribute as a "
          + " where a.groupUuid = g.uuid and g.uuid in (");

      //add all the uuids
      byHqlStatic.setCollectionInClause(query, uuidPageList);
      query.append(")");
      List<Object[]> currentListGroupAttribute = byHqlStatic.createQuery(query.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByUuids")
        .list(Object[].class);
      Set<Group> currentList = _getGroupsFromGroupsAndAttributesQuery(currentListGroupAttribute);
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
      queryOptions.sortAsc("displayName");
    }
    //TODO update for 1.5
    String fieldId = FieldFinder.findFieldIdForAttribute(
        queryOptions.getQuerySort().getQuerySortFields().get(0).getColumn());
    String listId = Group.getDefaultList().getUuid();

    StringBuilder sql = new StringBuilder("select distinct theAttribute from Attribute theAttribute, Membership listMembership ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theAttribute.groupUuid", inPrivSet);
  
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
    
    sql.append("  theAttribute.fieldId = :theFieldId " +
    		" and listMembership.ownerUuid = theAttribute.groupUuid and listMembership.fieldId = :listId" +
        " and listMembership.memberUuid = :memberId ");
    
    Member member = MemberFinder.internal_findBySubject(subject, false);
    
    QuerySort querySort = queryOptions.getQuerySort();
    try {
      //swap this out for value
      queryOptions.sort(new QuerySort("theAttribute.value", querySort.getQuerySortFields().get(0).isAscending()));
  
      List<Attribute> attributes = byHqlStatic.createQuery(sql.toString())
        .setString("theFieldId", fieldId)
        .setString("listId", listId)
        .setString("memberId", member.getUuid())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllGroupsSecure")
        .options(queryOptions)
        .list(Attribute.class);
      
      //get a list of unique uuids
      Collection<String> groupIds = new LinkedHashSet<String>(
          GrouperUtil.propertyList(attributes, Attribute.PROPERTY_GROUP_UUID, String.class));
      
      Set<Group> groups = findByUuids(groupIds, false);
      
      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, subject, inPrivSet);
  
      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    } finally {
      //put this back to the way it was
      queryOptions.sort(querySort);
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
      queryOptions.sortAsc("displayName");
    }
    //TODO update for 1.5
    String fieldId = FieldFinder.findFieldIdForAttribute(
        queryOptions.getQuerySort().getQuerySortFields().get(0).getColumn());
    String nameFieldId = FieldFinder.findFieldIdForAttribute(
        "name");
    String listId = Group.getDefaultList().getUuid();
  
    StringBuilder sql = new StringBuilder("select distinct theAttribute from Attribute theAttribute, Attribute nameAttribute," +
    		" Membership listMembership ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theAttribute.groupUuid", inPrivSet);
  
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
  
    sql.append("  theAttribute.groupUuid = nameAttribute.groupUuid and theAttribute.fieldId = :theFieldId " +
        " and nameAttribute.fieldId = :nameFieldId and nameAttribute.value like :scope" +
        " and listMembership.ownerUuid = nameAttribute.groupUuid and listMembership.fieldId = :listId" +
        " and listMembership.memberUuid = :memberId ");
    
    Member member = MemberFinder.internal_findBySubject(subject, false);
    
    if (member == null) {
      new LinkedHashSet<Group>();
    }
    
    QuerySort querySort = queryOptions.getQuerySort();
    try {
      //swap this out for value
      queryOptions.sort(new QuerySort("theAttribute.value", querySort.getQuerySortFields().get(0).isAscending()));
  
      List<Attribute> attributes = byHqlStatic.createQuery(sql.toString())
        .setString("theFieldId", fieldId)
        .setString("nameFieldId", nameFieldId)
        .setString("listId", listId)
        .setString("memberId", member.getUuid())
        .setString("scope", scope + "%")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllGroupsSecureScope")
        .options(queryOptions)
        .list(Attribute.class);
  
      Set<String> groupIds = new LinkedHashSet<String>(
          GrouperUtil.propertyList(attributes, Attribute.PROPERTY_GROUP_UUID, String.class));
      
      Set<Group> groups = findByUuids(groupIds, false);
      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, subject, inPrivSet);
  
      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    } finally {
      //put this back to the way it was
      queryOptions.sort(querySort);
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
      queryOptions.sortAsc("displayName");
    }
    //TODO update for 1.5
    String fieldId = FieldFinder.findFieldIdForAttribute(
        queryOptions.getQuerySort().getQuerySortFields().get(0).getColumn());
    String listId = Group.getDefaultList().getUuid();

    StringBuilder sql = new StringBuilder("select distinct theAttribute from Attribute theAttribute, Group as theGroup," +
    		" Membership listMembership ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(subject, byHqlStatic, 
        sql, "theAttribute.groupUuid", inPrivSet);
  
    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }
    
    sql.append(" theAttribute.fieldId = :theFieldId and theGroup.parentUuid = :parent " +
        " and theAttribute.groupUuid = theGroup.uuid" +
        " and listMembership.ownerUuid = theAttribute.groupUuid and listMembership.fieldId = :listId" +
        " and listMembership.memberUuid = :memberId ");

    Member member = MemberFinder.internal_findBySubject(subject, false);
    
    QuerySort originalQuerySort = queryOptions.getQuerySort();
    try {
      //swap this out for value
      queryOptions.sort(new QuerySort("theAttribute.value", originalQuerySort.getQuerySortFields().get(0).isAscending()));
  
      List<Attribute> attributes = byHqlStatic.createQuery(sql.toString())
        .setString("theFieldId", fieldId)
        .setString("parent", stem.getUuid())
        .setString("listId", listId)
        .setString("memberId", member.getUuid())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".getImmediateChildrenSecure")
        .options(queryOptions)
        .list(Attribute.class);
  
      Set<String> groupIds = new LinkedHashSet<String>(GrouperUtil.propertyList(
          attributes, Attribute.PROPERTY_GROUP_UUID, String.class));
      
      Set<Group> groups = findByUuids(groupIds, false);
      
      //if the hql didnt filter, this will
      Set<Group> filteredGroups = grouperSession.getAccessResolver()
        .postHqlFilterGroups(groups, subject, inPrivSet);
  
      return filteredGroups;
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException("Problem: uuids dont match up", gnfe);
    } finally {
      //put this back to the way it was
      queryOptions.sort(originalQuerySort);
    }
  
  }

} 

