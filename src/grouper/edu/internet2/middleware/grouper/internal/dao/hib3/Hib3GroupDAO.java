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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.CallbackException;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.DefaultMemberOf;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.GrouperDAOFactory;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import edu.internet2.middleware.grouper.internal.util.Rosetta;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * Basic Hibernate <code>Group</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3GroupDAO.java,v 1.11 2008-03-20 16:40:22 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3GroupDAO extends Hib3DAO implements GroupDAO, Lifecycle {


  private               Map                       attributes = null;
  private               String                    createSource;
  private               long                      createTime;
  private               String                    creatorUUID;
  private static        HashMap<String, Boolean>  existsCache = new HashMap<String, Boolean>();
  private static  final String                    KLASS                         = Hib3GroupDAO.class.getName();
  private               String                    id;
  private               String                    modifierUUID;
  private               String                    modifySource;
  private               long                      modifyTime;
  private               String                    parentUUID;
  private               String                    uuid;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   @HEAD@
   */
  public void addType(final GroupDTO _g, final GroupTypeDTO _gt) 
    throws  GrouperDAOException {
    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs = hibernateSession.getSession();
            hs.save(  // new group-type tuple
                new Hib3GroupTypeTupleDAO()
                  .setGroupUuid( _g.getUuid() )
                  .setTypeUuid( _gt.getUuid() )
              );
            hs.saveOrUpdate( Rosetta.getDAO(_g) ); // modified group
            //let HibernateSession commit or rollback depending on if problem or enclosing transaction
            return null;
          }
      
    });
  } 

  /**
   * @since   @HEAD@
   */
  public void delete(final GroupDTO _g, final Set mships)
    throws  GrouperDAOException {

    
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            // delete memberships
            Iterator it = mships.iterator();
            while (it.hasNext()) {
              GrouperDAO grouperDAO = Rosetta.getDAO( it.next() );
              hs.delete( grouperDAO );
            }
            hs.flush();
            
            // delete attributes
            Query qry = hs.createQuery("delete from Hib3AttributeDAO where group_id = :group");
            qry.setString("group", _g.getUuid() );
            qry.executeUpdate();
            // delete type tuples
            qry = hs.createQuery("delete from Hib3GroupTypeTupleDAO where group_uuid = :group");
            qry.setString("group", _g.getUuid() );
            qry.executeUpdate();
            // delete group
            hs.delete( Rosetta.getDAO(_g) );
            return null;
          }
    });

  } 

  /**
   * @since   @HEAD@
   */
  public void deleteType(final GroupDTO _g, final GroupTypeDTO _gt) 
    throws  GrouperDAOException
  {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            hs.delete( Hib3GroupTypeTupleDAO.findByGroupAndType(_g, _gt) );
            hs.saveOrUpdate( Rosetta.getDAO(_g) ); 
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
      .createQuery("select g.id from Hib3GroupDAO as g where g.uuid = :uuid")
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
   * @since   @HEAD@
   */
  public Map findAllAttributesByGroup(final String uuid) throws  GrouperDAOException {
    final Map attrs = new HashMap();

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            Query   qry = hs.createQuery("from Hib3AttributeDAO as a where a.groupUuid = :uuid");
            qry.setCacheable(false);
            qry.setCacheRegion(KLASS + ".FindAllAttributesByGroup");
            qry.setString("uuid", uuid);
            Hib3AttributeDAO a;
            //TODO CH 20080217: replace with query.list() and see if p6spy generates fewer queries
            Iterator              it = qry.iterate();
            while (it.hasNext()) {
              a = (Hib3AttributeDAO) it.next();
              attrs.put( a.getAttrName(), a.getValue() );
            }
            return null;
          }
    });
    
    return attrs;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByAnyApproximateAttr(final String val) 
    throws  GrouperDAOException,
            IllegalStateException {

    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            Query   qry = hs.createQuery(
                "select g, a from Hib3GroupDAO as g, Hib3AttributeDAO as a where a.groupUuid in " +
                "(select a2.groupUuid from Hib3AttributeDAO as a2 where lower(a2.value) like :value) " +
                "and a.groupUuid = g.uuid"
              );
            qry.setCacheable(false);
            qry.setCacheRegion(KLASS + ".FindAllByAnyApproximateAttr");
            qry.setString( "value", "%" + val.toLowerCase() + "%" );

            Set groups = _getGroupsFromGroupsAndAttributesQuery(hs, qry);
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
  public Set findAllByApproximateAttr(final String attr, final String val) 
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            //boolean strategy1 = true;
            //if (strategy1) {
               //CH 2008022: change to 3 joins to improve performance
              Query   qry = hs.createQuery(
                  "select g, a from Hib3GroupDAO as g, Hib3AttributeDAO as a," +
                  "Hib3AttributeDAO as a2 where a.groupUuid = g.uuid " +
                  "and a.groupUuid = a2.groupUuid and a2.attrName = :field and lower(a2.value) like :value"
                );
  //              Query   qry = hs.createQuery(
  //                  "select g, a from Hib3GroupDAO as g, Hib3AttributeDAO as a where a.groupUuid in " + 
  //                  "(select a2.groupUuid from Hib3AttributeDAO as a2 where a2.attrName = :field and lower(a2.value) like :value) " +
  //                  "and a.groupUuid = g.uuid"
  //                );
              qry.setCacheable(false);
              qry.setCacheRegion(KLASS + ".FindAllByApproximateAttr");
              qry.setString("field", attr);
              qry.setString( "value", "%" + val.toLowerCase() + "%" );
  
              Set groups = _getGroupsFromGroupsAndAttributesQuery(hs, qry);
              return groups;
            //} 
            //this is not faster:
            //Stats (strategy1):
            //JAMon Label=runPerfProblem2Helper, Units=ms.: (LastValue=2391.0, Hits=10.0, Avg=2481.2, Total=24812.0, Min=2391.0, Max=2610.0, Active=0.0, Avg Active=1.0, Max Active=1.0, First Access=Wed Mar 05 04:06:13 EST 2008, Last Access=Wed Mar 05 04:06:35 EST 2008)
            //
            //Stats (strategy2):
            //JAMon Label=runPerfProblem2Helper, Units=ms.: (LastValue=2735.0, Hits=10.0, Avg=2753.1, Total=27531.0, Min=2656.0, Max=3062.0, Active=0.0, Avg Active=1.0, Max Active=1.0, First Access=Wed Mar 05 04:08:58 EST 2008, Last Access=Wed Mar 05 04:09:22 EST 2008)

            //if not old way, try new way
//            Query   qry = hs.createQuery(
//                "select a from Hib3AttributeDAO as a," +
//                "Hib3AttributeDAO as a2 " +
//                "where a.groupUuid = a2.groupUuid and a2.attrName = :field and lower(a2.value) like :value"
//              );
//            qry.setCacheable(false);
//            qry.setCacheRegion(KLASS + ".FindAllByApproximateAttr");
//            qry.setString("field", attr);
//            qry.setString( "value", "%" + val.toLowerCase() + "%" );
//
//            Set groups = _getGroupsFromGroupsAndAttributesQuery2(hs, qry);
//            return groups;
            
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
  public Set findAllByApproximateName(final String name) 
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            Query   qry = hs.createQuery(
                "select g, a from Hib3GroupDAO as g, Hib3AttributeDAO as a where a.groupUuid in " +
                "(select a2.groupUuid from Hib3AttributeDAO as a2 where " +
                  "   (a2.attrName = 'name'              and lower(a2.value) like :value) " +
                  "or (a2.attrName = 'displayName'       and lower(a2.value) like :value) " +
                  "or (a2.attrName = 'extension'         and lower(a2.value) like :value) " +
                  "or (a2.attrName = 'displayExtension'  and lower(a2.value) like :value) " +
                ") " + "and a.groupUuid = g.uuid"
              );
              qry.setCacheable(false);
              qry.setCacheRegion(KLASS + ".FindAllByApproximateName");
              qry.setString( "value", "%" + name.toLowerCase() + "%" );

            Set  groups = _getGroupsFromGroupsAndAttributesQuery(hs, qry);
            return groups;
          }
    });
    return resultGroups;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByCreatedAfter(final Date d) 
    throws  GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            Query   qry = hs.createQuery(
                "select g, a from Hib3GroupDAO as g, Hib3AttributeDAO as a where g.createTime > :time " +
                "and a.groupUuid = g.uuid"
              );
            qry.setCacheable(false);
            qry.setCacheRegion(KLASS + ".FindAllByCreatedAfter");
            qry.setLong( "time", d.getTime() );

            Set groups = _getGroupsFromGroupsAndAttributesQuery(hs, qry);
            return groups;
          }
    });
    return resultGroups;
  }

  /**
   * @since   @HEAD@
   */
  public Set findAllByCreatedBefore(final Date d) 
    throws  GrouperDAOException {
    Set resultGroups = (Set) HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

      public Object callback(HibernateSession hibernateSession) {
        Session hs  = hibernateSession.getSession();
        Query   qry = hs.createQuery(
            "select g, a from Hib3GroupDAO as g, Hib3AttributeDAO as a where g.createTime < :time " +
            "and a.groupUuid = g.uuid"
          );
        qry.setCacheable(false);
        qry.setCacheRegion(KLASS + ".FindAllByCreatedBefore");
        qry.setLong( "time", d.getTime() );

        Set groups = _getGroupsFromGroupsAndAttributesQuery(hs, qry);
        return groups ;
      }
    });
    return resultGroups;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByModifiedAfter(final Date d) 
    throws  GrouperDAOException {
    
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

      public Object callback(HibernateSession hibernateSession) {
        Session hs  = hibernateSession.getSession();
        Query   qry = hs.createQuery(
            "select g, a from Hib3GroupDAO as g, Hib3AttributeDAO as a where g.modifyTime > :time " +
            "and a.groupUuid = g.uuid"
          );
        qry.setCacheable(false);
        qry.setCacheRegion(KLASS + ".FindAllByModifiedAfter");
        qry.setLong( "time", d.getTime() );

        Set groups = _getGroupsFromGroupsAndAttributesQuery(hs, qry);
        return groups;
      }
    });
    return resultGroups;
  }

  /**
   * @since   @HEAD@
   */
  public Set findAllByModifiedBefore(final Date d) 
    throws  GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            Query   qry = hs.createQuery(
                "select g, a from Hib3GroupDAO as g, Hib3AttributeDAO as a where g.modifyTime < :time " +
                "and a.groupUuid = g.uuid"
              );
            qry.setCacheable(false);
            qry.setCacheRegion(KLASS + ".FindAllByModifiedBefore");
            qry.setLong( "time", d.getTime() );

            Set groups = _getGroupsFromGroupsAndAttributesQuery(hs, qry);
            return groups;
          }
    });
    return resultGroups;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByType(final GroupTypeDTO _gt) 
    throws  GrouperDAOException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            Query   qry = hs.createQuery(
                "select g, a from Hib3GroupDAO as g, Hib3AttributeDAO as a, Hib3GroupTypeTupleDAO as gtt " +
                "where gtt.typeUuid = :type " +
                "and a.groupUuid = gtt.groupUuid and a.groupUuid = g.uuid"
              );
            qry.setCacheable(false);
            qry.setCacheRegion(KLASS + ".FindAllByType");
            qry.setString( "type", _gt.getUuid() );

            Set groups = _getGroupsFromGroupsAndAttributesQuery(hs, qry);
            return groups;
          }
    });
    return resultGroups;
  } 

  /**
   * @since   @HEAD@
   */
  public GroupDTO findByAttribute(String attr, String val) 
    throws  GrouperDAOException,
            GroupNotFoundException {
    Hib3AttributeDAO a = HibernateSession.byHqlStatic()
      .createQuery("from Hib3AttributeDAO as a where a.attrName = :field and a.value like :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByAttribute")
      .setString("field", attr).setString("value", val).uniqueResult(Hib3AttributeDAO.class);
    
     if (a == null) {
       throw new GroupNotFoundException();
     }
     return this.findByUuid( a.getGroupUuid() );
  } 

  /**
   * @since   @HEAD@
   */
  public GroupDTO findByName(final String name) 
    throws  GrouperDAOException,
            GroupNotFoundException {
    
    Hib3GroupDAO hib3GroupDAO = HibernateSession.byHqlStatic()
      .createQuery("select g from Hib3AttributeDAO as a, Hib3GroupDAO as g where a.groupUuid = g.uuid " +
      		"and a.attrName = 'name' and a.value = :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByName")
      .setString("value", name).uniqueResult(Hib3GroupDAO.class);

    //handle exceptions out of data access method...
    if (hib3GroupDAO == null) {
      throw new GroupNotFoundException("Cannot find group with name: '" + name + "'");
    }
    return GroupDTO.getDTO(hib3GroupDAO);
  } 

  /**
   * <p><b>Implementation Notes.</b</p>
   * <ol>
   * <li>Hibernate caching is enabled.</li>
   * </ol>
   * @see     GroupDAO#findByUuid(String)
   * @since   @HEAD@
   */
  public GroupDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            GroupNotFoundException  {
    Hib3GroupDAO dao = HibernateSession.byHqlStatic()
      .createQuery("from Hib3GroupDAO as g where g.uuid = :uuid")
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid).uniqueResult(Hib3GroupDAO.class);
    if (dao == null) {
       throw new GroupNotFoundException();
    }
    return GroupDTO.getDTO(dao);
  } 

  /**
   * @since   @HEAD@
   */
  public Map getAttributes() {
    if (this.attributes == null) {
      this.attributes = findAllAttributesByGroup( this.getUuid() );
    }
    return this.attributes;
  }
  
  /**
   * @since   @HEAD@
   */
  public String getCreateSource() {
    return this.createSource;
  }

  /**
   * @since   @HEAD@
   */
  public long getCreateTime() {
    return this.createTime;
  }

  /**
   * @since   @HEAD@
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @since   @HEAD@
   */
  public String getId() {
    return this.id;
  }

  /**
   * @since   @HEAD@
   */
  public String getModifierUuid() {
    return this.modifierUUID;
  }

  /**
   * @since   @HEAD@
   */
  public String getModifySource() {
    return this.modifySource;
  }

  /**
   * @since   @HEAD@
   */
  public long getModifyTime() {
    return this.modifyTime;
  }

  /**
   * @since   @HEAD@
   */
  public String getParentUuid() {
    return this.parentUUID;
  }

  /**
   * @since   @HEAD@
   */
  public Set getTypes() {
    return _findAllTypesByGroup( this.getUuid() );
  }
  
  /**
   * @since   @HEAD@
   */
  public String getUuid() {
    return this.uuid;
  }

  // @since   @HEAD@ 
  public boolean onDelete(Session hs) 
    throws  CallbackException
  {
    existsCache.put( this.getUuid(), false );
    return Lifecycle.NO_VETO;
  } 

  // @since   @HEAD@
  public void onLoad(Session hs, Serializable id) {
    // nothing
  } // public void onLoad(hs, id)

  // @since   @HEAD@
  public boolean onSave(Session hs) 
    throws  CallbackException
  {
    existsCache.put( this.getUuid(), true );
    return Lifecycle.NO_VETO;
  } 

  // @since   @HEAD@
  public boolean onUpdate(Session hs) 
    throws  CallbackException
  {
    try {
      this._updateAttributes(hs);
    }
    catch (HibernateException eH) {
      throw new CallbackException( eH.getMessage(), eH );
    } 
    return Lifecycle.NO_VETO;
  } // public boolean onUpdate(hs)k

  /**
   * @since   @HEAD@
   */
  public void revokePriv(final GroupDTO _g, final DefaultMemberOf mof)
    throws  GrouperDAOException {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            Iterator it = mof.getDeletes().iterator();
            while (it.hasNext()) {
              GrouperDAO grouperDAO = Rosetta.getDAO( it.next() );
              hs.delete( grouperDAO );
            }
            hs.flush();
            
            it = mof.getSaves().iterator();
            while (it.hasNext()) {
              GrouperDAO grouperDAO = Rosetta.getDAO( it.next() );
              hs.saveOrUpdate( grouperDAO);
            }
            hs.flush();
            
            hs.update( Rosetta.getDAO(_g) );
            return null;
          }
    });
  } 

  /**
   * @since   @HEAD@
   */
  public void revokePriv(final GroupDTO _g, final Set toDelete)
    throws  GrouperDAOException {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            Iterator it = toDelete.iterator();
            while (it.hasNext()) {
              hs.delete( Rosetta.getDAO( it.next() ) );
            }
            hs.update( Rosetta.getDAO(_g) );
            return null;
          }
    });
  } 

  /**
   * @since   @HEAD@
   */
  public GroupDAO setAttributes(Map attributes) {
    this.attributes = attributes;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupDAO setCreateSource(String createSource) {
    this.createSource = createSource;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupDAO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupDAO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupDAO setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupDAO setModifySource(String modifySource) {
    this.modifySource = modifySource;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupDAO setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupDAO setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupDAO setTypes(Set types) {
    // TODO 20070405 try to make this behave more like the rest of the *etters.
    //               as types are retrieved dynamically we don't need to cache
    //               them locally.  and, yes, that could be considered a poor 
    //               design decision.
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GroupDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public void update(GroupDTO _g)
    throws  GrouperDAOException {
    
    HibernateSession.byObjectStatic().update(_g.getDAO());
    
  } 


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    // TODO 20070307 ideally i would just put hooks for associated tables into "onDelete()" 
    //               but right now that is blowing up due to the session being flushed.
    hs.createQuery("delete from Hib3GroupTypeTupleDAO").executeUpdate();
    hs.createQuery("delete from Hib3AttributeDAO").executeUpdate(); 
    hs.createQuery("delete from Hib3GroupDAO").executeUpdate();
    existsCache = new HashMap<String, Boolean>();
  } 


  // PRIVATE CLASS METHODS //

  // @since   @HEAD@
  private static Set _findAllTypesByGroup(final String uuid)
    throws  GrouperDAOException {
    Set types = new LinkedHashSet();
    List<Hib3GroupTypeTupleDAO> hib3GroupTypeTupleDAOs = 
      HibernateSession.byHqlStatic()
        .createQuery("from Hib3GroupTypeTupleDAO as gtt where gtt.groupUuid = :group")
        .setCacheable(false).setString("group", uuid).list(Hib3GroupTypeTupleDAO.class);
    
    GroupTypeDAO dao = GrouperDAOFactory.getFactory().getGroupType(); 
    try {
      for (Hib3GroupTypeTupleDAO  gtt : hib3GroupTypeTupleDAOs) {
        types.add( dao.findByUuid( gtt.getTypeUuid() ) );
      }
    } catch (SchemaException eS) {
      throw new GrouperDAOException( "Problem with finding by uuid: " + uuid + ", " + eS.getMessage(), eS );
    }
    return types;
  } // private static Set _findAllTypesByGroup(uuid)


  // PRIVATE INSTANCE METHODS //

  // @since   @HEAD@
  private void _updateAttributes(Session hs) 
    throws  HibernateException
  {
    // TODO 20070531 split and test
    Query qry = hs.createQuery("from Hib3AttributeDAO as a where a.groupUuid = :uuid");
    qry.setCacheable(false);
    qry.setCacheRegion(KLASS + "._UpdateAttributes");
    qry.setString("uuid", this.uuid);
    Hib3AttributeDAO a;
    Map                   attrs = new HashMap(this.attributes);
    String                k;
    //TODO CH 20080217: replace with query.list() and see if p6spy generates fewer queries
    Iterator              it = qry.iterate();
    while (it.hasNext()) {
      a = (Hib3AttributeDAO) it.next();
      k = a.getAttrName();
      if ( attrs.containsKey(k) ) {
        // attr both in db and in memory.  compare.
        if ( !a.getValue().equals( (String) attrs.get(k) ) ) {
          a.setValue( (String) attrs.get(k) );
          hs.update(a);
        }
        attrs.remove(k);
      }
      else {
        // attr only in db.
        hs.delete(a);
        attrs.remove(k);
      }
    }
    // now handle entries that were only in memory
    Map.Entry kv;
    it = attrs.entrySet().iterator();
    while (it.hasNext()) {
      kv = (Map.Entry) it.next();
      Hib3AttributeDAO dao = new Hib3AttributeDAO(); 
      dao.setAttrName( (String) kv.getKey() );
      dao.setGroupUuid(this.uuid);
      dao.setValue( (String) kv.getValue() );
      hs.save(dao);
    }
  } // private void _updateAttributes(hs)


  // @since 1.2.1         
  private Set _getGroupsFromGroupsAndAttributesQuery(Session session, Query qry)
    throws  HibernateException {   
    Set groups = new LinkedHashSet();
    List<Object[]> hib3GroupAttributeDAOs = qry.list();
    Iterator it = hib3GroupAttributeDAOs.iterator();
    HashMap results = new HashMap();
        
    while (it.hasNext()) {
      Object[] tuple = (Object[])it.next();
      Hib3GroupDAO currGroupDAO = (Hib3GroupDAO)tuple[0];
      String groupId = currGroupDAO.getId();
      Map currAttributes = null;
      if (results.containsKey(groupId)) {
        currGroupDAO = (Hib3GroupDAO)results.get(groupId);
        currAttributes = currGroupDAO.getAttributes();
      } else {
        currAttributes = new HashMap();
      }
      Hib3AttributeDAO currAttributeDAO = (Hib3AttributeDAO)tuple[1];
      HibUtils.evict(null, session, currAttributeDAO, true);
      currAttributes.put(currAttributeDAO.getAttrName(), currAttributeDAO.getValue());
      currGroupDAO.setAttributes(currAttributes);
      results.put(groupId, currGroupDAO);
    }
    
    Iterator values = results.values().iterator();
    while (values.hasNext()) {
      Hib3GroupDAO hib3GroupDao = (Hib3GroupDAO)values.next();
      groups.add(GroupDTO.getDTO(hib3GroupDao));
      HibUtils.evict(null, session, hib3GroupDao, true);
    } 
      
    return groups;
  } // private Set _getGroupsFromGroupsAndAttributesQuery(qry)
  
  /**
   * this is a way to get groups and attributes from only attributes, seeing if it were any faster, and it was slower...
   * @param session
   * @param qry
   * @return the groups (with attributes attached)
   * @throws HibernateException
   */
  private Set<GroupDTO> _getGroupsFromGroupsAndAttributesQuery2(Session session, Query qry)
    throws  HibernateException {
    List<Hib3AttributeDAO> hib3AttributeDAOs = qry.list();
    //map of group id to the map of attributes for the group
    Map<String, Map<String, String>> groupIdsAttributes = new LinkedHashMap<String, Map<String, String>>(
        GrouperUtil.length(hib3AttributeDAOs)/4);
    
    for (Hib3AttributeDAO hib3AttributeDAO : hib3AttributeDAOs) {
      
      //get the group id
      String groupUuid = hib3AttributeDAO.getGroupUuid();
      //get the map
      Map<String, String> attributes = groupIdsAttributes.get(groupUuid);
      //init if not there
      if (attributes == null) {
        attributes = new HashMap<String, String>();
        groupIdsAttributes.put(groupUuid, attributes);
      }
      
      //put the attribute in there
      attributes.put(hib3AttributeDAO.getAttrName(), hib3AttributeDAO.getValue());
      
      HibUtils.evict(null, session, hib3AttributeDAO, true);
    }
    
    //now we have the attributes and the list of groupIds, now lets get the groups in batches of 200
    List<String> groupUuids = new ArrayList<String>(groupIdsAttributes.keySet());
    int batchSize = 200;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupUuids, batchSize);
    //result
    Set<GroupDTO> groups = new LinkedHashSet<GroupDTO>();
    //go through each batch of 200
    for (int batchIndex=0;batchIndex<numberOfBatches;batchIndex++) {
      
      List<String> batchOfGroupIds = GrouperUtil.batchList(groupUuids, batchSize, batchIndex);
      List<Hib3GroupDAO> hib3GroupDAOs = HibernateSession.byCriteriaStatic()
        .list(Hib3GroupDAO.class, Restrictions.in("uuid", batchOfGroupIds));
      
      //for each one, add attributes
      for (Hib3GroupDAO hib3GroupDAO : hib3GroupDAOs) {
        Map<String, String> attributeMap = groupIdsAttributes.get(hib3GroupDAO.getUuid());
        hib3GroupDAO.setAttributes(attributeMap);
        groups.add(GroupDTO.getDTO(hib3GroupDAO));
        HibUtils.evict(null, session, hib3GroupDAO, true);
      }
      
    }
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
  public Set<GroupDTO> findAllByAttr(final String attr, final String val) throws GrouperDAOException,
      IllegalStateException {
    Set resultGroups = (Set)HibernateSession.callbackHibernateSession(GrouperTransactionType.READONLY_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session hs  = hibernateSession.getSession();
            Query   qry = hs.createQuery(
                "select g, a from Hib3GroupDAO as g, Hib3AttributeDAO as a," +
                "Hib3AttributeDAO as a2 where a.groupUuid = g.uuid " +
                "and a.groupUuid = a2.groupUuid and a2.attrName = :field and a2.value = :value"
              );
            qry.setCacheable(false);
            qry.setCacheRegion(KLASS + ".FindAllByAttr");
            qry.setString("field", attr);
            qry.setString( "value", val );

            Set groups = _getGroupsFromGroupsAndAttributesQuery(hs, qry);
            return groups;
            
          }
    });

    return resultGroups;
  }

} 

