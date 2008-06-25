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

import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.DefaultMemberOf;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeTuple;
import edu.internet2.middleware.grouper.GrouperDAOFactory;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.hibernate.ByHql;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Basic Hibernate <code>Group</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GroupDAO.java,v 1.14 2008-06-25 05:46:05 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3GroupDAO extends Hib3DAO implements GroupDAO {

  /** */
  private static HashMap<String, Boolean> existsCache = new HashMap<String, Boolean>();

  /** */
  private static final String KLASS = Hib3GroupDAO.class.getName();

  /**
   * put in cache
   * @param uuid
   * @param exists
   */
  public void putInExistsCache(String uuid, boolean exists) {
    existsCache.put(uuid, exists);
  }

  /**
   * @since   @HEAD@
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
            //note this used to be saveOrUpdate
            hibernateSession.byObject().update( _g ); // modified group
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
   * @param _g 
   * @param _gt 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void deleteType(final Group _g, final GroupType _gt) 
    throws  GrouperDAOException
  {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            
            hibernateSession.byObject().delete( Hib3GroupTypeTupleDAO.findByGroupAndType(_g, _gt) );
            
            //NOTE: used to be saveOrUpdate
            hibernateSession.byObject().update( _g ); 
            
            return null;
          }
    });
  } 

  /**
   * @since   @HEAD@
   */
  public boolean exists(String uuid)
    throws  GrouperDAOException {
    if ( existsCache.containsKey(uuid) ) {
      return existsCache.get(uuid).booleanValue();
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
    existsCache.put(uuid, rv);
    return rv;
  } 

  /**
   * @param uuid 
   * @return 
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
   * @return 
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

            Set groups = _getGroupsFromGroupsAndAttributesQuery(hibernateSession, groupAttributes);
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
            //boolean strategy1 = true;
            //if (strategy1) {
               //CH 2008022: change to 3 joins to improve performance
              List<Object[]> groupAttributes = hibernateSession.byHql().createQuery(
                  "select g, a from Group as g, Attribute as a," +
                  "Attribute as a2 where a.groupUuid = g.uuid " +
                  "and a.groupUuid = a2.groupUuid and a2.attrName = :field and lower(a2.value) like :value"
                ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByApproximateAttr")
                .setString("field", attr)
                .setString( "value", "%" + val.toLowerCase() + "%" ).list(Object[].class);
  
              Set groups = _getGroupsFromGroupsAndAttributesQuery(hibernateSession, groupAttributes);
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
                "(select a2.groupUuid from Attribute as a2 where " +
                  "   (a2.attrName = 'name'              and lower(a2.value) like :value) " +
                  "or (a2.attrName = 'displayName'       and lower(a2.value) like :value) " +
                  "or (a2.attrName = 'extension'         and lower(a2.value) like :value) " +
                  "or (a2.attrName = 'displayExtension'  and lower(a2.value) like :value) " +
                ") " + "and a.groupUuid = g.uuid"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByApproximateName")
              .setString( "value", "%" + name.toLowerCase() + "%" ).list(Object[].class);

            Set  groups = _getGroupsFromGroupsAndAttributesQuery(hibernateSession, groupAttributes);
            return groups;
          }
    });
    return resultGroups;
  } 

  /**
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

            Set groups = _getGroupsFromGroupsAndAttributesQuery(hibernateSession, groupAttributes);
            return groups;
          }
    });
    return resultGroups;
  }

  /**
   * @param d 
   * @return 
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

        Set groups = _getGroupsFromGroupsAndAttributesQuery(hibernateSession, groupAttributes);
        return groups ;
      }
    });
    return resultGroups;
  } 

  /**
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

        Set groups = _getGroupsFromGroupsAndAttributesQuery(hibernateSession, groupAttributes);
        return groups;
      }
    });
    return resultGroups;
  }

  /**
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

            Set groups = _getGroupsFromGroupsAndAttributesQuery(hibernateSession, groupAttributes);
            return groups;
          }
    });
    return resultGroups;
  } 

  /**
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

            Set groups = _getGroupsFromGroupsAndAttributesQuery(hibernateSession, groupAttributes);
            return groups;
          }
    });
    return resultGroups;
  } 

  /**
   * @since   @HEAD@
   */
  public Group findByAttribute(String attr, String val) 
    throws  GrouperDAOException,
            GroupNotFoundException {
    Attribute a = HibernateSession.byHqlStatic()
      .createQuery("from Attribute as a where a.attrName = :field and a.value like :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByAttribute")
      .setString("field", attr).setString("value", val).uniqueResult(Attribute.class);
    
     if (a == null) {
       throw new GroupNotFoundException();
     }
     return this.findByUuid( a.getGroupUuid() );
  } 

  /**
   * @since   @HEAD@
   */
  public Group findByName(final String name) 
    throws  GrouperDAOException,
            GroupNotFoundException {
    
    Group group = HibernateSession.byHqlStatic()
      .createQuery("select g from Attribute as a, Group as g where a.groupUuid = g.uuid " +
      		"and a.attrName = 'name' and a.value = :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByName")
      .setString("value", name).uniqueResult(Group.class);

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
       throw new GroupNotFoundException();
    }
    return dto;
  } 

  /**
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
   * @since   @HEAD@
   */
  public void update(Group _g)
    throws  GrouperDAOException {
    
    HibernateSession.byObjectStatic().update(_g);
    
  } 


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    // TODO 20070307 ideally i would just put hooks for associated tables into "onDelete()" 
    //               but right now that is blowing up due to the session being flushed.
    hibernateSession.byHql().createQuery("delete from GroupTypeTuple").executeUpdate();
    hibernateSession.byHql().createQuery("delete from Attribute").executeUpdate(); 
    hibernateSession.byHql().createQuery("delete from Group").executeUpdate();
    existsCache = new HashMap<String, Boolean>();
  } 


  // PRIVATE CLASS METHODS //

  // @since   @HEAD@
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


  // PRIVATE INSTANCE METHODS //

  // @since   @HEAD@
  /**
   * update the attributes for a group
   * @param hibernateSession 
   * @param checkExisting true if an update, false if insert
   */
  public void _updateAttributes(HibernateSession hibernateSession, boolean checkExisting, Group group) {
    ByObject byObject = hibernateSession.byObject();
    // TODO 20070531 split and test
    ByHql byHql = hibernateSession.byHql();
    byHql.createQuery("from Attribute as a where a.groupUuid = :uuid");
    byHql.setCacheable(false);
    byHql.setCacheRegion(KLASS + "._UpdateAttributes");
    byHql.setString("uuid", group.getUuid());
    Attribute a;
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
      attributeDto.setAttrName( (String) kv.getKey() );
      attributeDto.setGroupUuid(group.getUuid());
      attributeDto.setValue( (String) kv.getValue() );
      byObject.save(attributeDto);
    }
  } // private void _updateAttributes(hs)

  // @since 1.2.1         
  /**
   * @param hibernateSession 
   * @param hib3GroupAttributes 
   * @return the set of dtos
   * @throws HibernateException 
   * 
   */
  private Set<Group> _getGroupsFromGroupsAndAttributesQuery(HibernateSession hibernateSession, List<Object[]> hib3GroupAttributes)
    throws  HibernateException {   
    Iterator it = hib3GroupAttributes.iterator();
    Map<String, Group> results = new HashMap<String, Group>();
        
    while (it.hasNext()) {
      Object[] tuple = (Object[])it.next();
      Group group = (Group)tuple[0];
      String groupId = group.getId();
      Map currAttributes = null;
      if (results.containsKey(groupId)) {
        group = (Group)results.get(groupId);
        currAttributes = group.getAttributesDb();
      } else {
        currAttributes = new HashMap();
      }
      Attribute currAttribute = (Attribute)tuple[1];
      HibUtils.evict(hibernateSession, currAttribute, true);
      currAttributes.put(currAttribute.getAttrName(), currAttribute.getValue());
      group.setAttributes(currAttributes);
      results.put(groupId, group);
    }
    
    Set groups = new LinkedHashSet(results.values());
    HibUtils.evict(hibernateSession, results.values(), true);
      
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
                "select g, a from Group as g, Attribute as a," +
                "Attribute as a2 where a.groupUuid = g.uuid " +
                "and a.groupUuid = a2.groupUuid and a2.attrName = :field and a2.value = :value"
              ).setCacheable(false).setCacheRegion(KLASS + ".FindAllByAttr")
              .setString("field", attr).setString( "value", val ).list(Object[].class);

            Set groups = _getGroupsFromGroupsAndAttributesQuery(hibernateSession, groupAttributes);
            return groups;
            
          }
    });

    return resultGroups;
  }

} 

