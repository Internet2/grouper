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

package edu.internet2.middleware.grouper.internal.dao.hibernate;
import  edu.internet2.middleware.grouper.GrouperDAOFactory;
import  edu.internet2.middleware.grouper.GroupNotFoundException;
import  edu.internet2.middleware.grouper.DefaultMemberOf;
import  edu.internet2.middleware.grouper.SchemaException;
import  edu.internet2.middleware.grouper.internal.cache.SimpleBooleanCache;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import  edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import  edu.internet2.middleware.grouper.internal.util.Rosetta;
import  java.io.Serializable;
import  java.util.Date;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;
import  net.sf.hibernate.*;

/**
 * Basic Hibernate <code>Group</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: HibernateGroupDAO.java,v 1.13 2007-08-03 16:03:30 blair Exp $
 * @since   1.2.0
 */
public class HibernateGroupDAO extends HibernateDAO implements GroupDAO, Lifecycle {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateGroupDAO.class.getName();


  // PRIVATE CLASS VARIABLES //
  private static SimpleBooleanCache existsCache = new SimpleBooleanCache();


  // PRIVATE INSTANCE VARIABLES //
  private Map     attributes;
  private String  createSource;
  private long    createTime;
  private String  creatorUUID;
  private String  id;
  private String  modifierUUID;
  private String  modifySource;
  private long    modifyTime;
  private String  parentUUID;
  private String  uuid;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public void addType(GroupDTO _g, GroupTypeDTO _gt) 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.save(  // new group-type tuple
          new HibernateGroupTypeTupleDAO()
            .setGroupUuid( _g.getUuid() )
            .setTypeUuid( _gt.getUuid() )
        );
        hs.saveOrUpdate( Rosetta.getDAO(_g) ); // modified group
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close(); 
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public void delete(GroupDTO _g, Set mships)
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        // delete memberships
        Iterator it = mships.iterator();
        while (it.hasNext()) {
          hs.delete( Rosetta.getDAO( it.next() ) );
        }
        // delete attributes
        hs.delete( "from HibernateAttributeDAO where group_id = ?", _g.getUuid(), Hibernate.STRING );
        // delete type tuples
        hs.delete( "from HibernateGroupTypeTupleDAO where group_uuid = ?", _g.getUuid(), Hibernate.STRING );
        // delete group
        hs.delete( Rosetta.getDAO(_g) );

        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      } 
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public void deleteType(GroupDTO _g, GroupTypeDTO _gt) 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.delete( HibernateGroupTypeTupleDAO.findByGroupAndType(_g, _gt) );
        hs.saveOrUpdate( Rosetta.getDAO(_g) ); 
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close(); 
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public boolean exists(String uuid)
    throws  GrouperDAOException
  {
    if ( existsCache.containsKey(uuid) ) {
      return existsCache.getBoolean(uuid).booleanValue();
    }
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("select g.id from HibernateGroupDAO as g where g.uuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".Exists");
      qry.setString("uuid", uuid);
      boolean rv  = false;
      if ( qry.uniqueResult() != null ) {
        rv = true;
      }
      hs.close();
      existsCache.put(uuid, rv);
      return rv;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public Map findAllAttributesByGroup(String uuid)
    throws  GrouperDAOException
  {
    Map attrs = new HashMap();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateAttributeDAO as a where a.groupUuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllAttributesByGroup");
      qry.setString("uuid", uuid);
      HibernateAttributeDAO a;
      Iterator              it = qry.iterate();
      while (it.hasNext()) {
        a = (HibernateAttributeDAO) it.next();
        attrs.put( a.getAttrName(), a.getValue() );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return attrs;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByAnyApproximateAttr(String val) 
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateAttributeDAO as a where lower(a.value) like :value");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByAnyApproximateAttr");
      qry.setString( "value", "%" + val.toLowerCase() + "%" );
      Iterator it = qry.iterate();
      while (it.hasNext()) {
        groups.add( this.findByUuid( ( (HibernateAttributeDAO) it.next()).getGroupUuid() ) );
      }
      hs.close();
    }
    catch (GroupNotFoundException eShouldNeverHappen) {
      throw new IllegalStateException( 
        "this should never happen: attribute without owning group: " + eShouldNeverHappen.getMessage(), eShouldNeverHappen
      );
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } 

  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This method will generate a full table scan of the attributes table.  It will not
   * perform well if there are a large number of groups.</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @see     GroupDAO#findAllByApproximateAttr(String, String)
   * @since   1.2.0
   */
  public Set findAllByApproximateAttr(String attr, String val) 
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateAttributeDAO as a where a.attrName = :field and lower(a.value) like :value"
      );
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByApproximateAttr");
      qry.setString("field", attr);
      qry.setString( "value", "%" + val.toLowerCase() + "%" );
      Iterator it = qry.iterate();
      while (it.hasNext()) {
        groups.add( this.findByUuid( ( (HibernateAttributeDAO) it.next()).getGroupUuid() ) );
      }
      hs.close();
    }
    catch (GroupNotFoundException eShouldNeverHappen) {
      throw new IllegalStateException( 
        "this should never happen: attribute without owning group: " + eShouldNeverHappen.getMessage(), eShouldNeverHappen
      );
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } 

  /**
   * <p><b>Implementation Notes.</b></p>
   * <ol>
   * <li>This method will generate a full table scan of the attributes table.  It will not
   * perform well if there are a large number of groups.</li>
   * <li>Hibernate caching is <b>not</b> enabled.</li>
   * </ol>
   * @see     GroupDAO#findAllByApproximateName(String)
   * @since   1.2.0
   */
  public Set findAllByApproximateName(String name) 
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateAttributeDAO as a where "
        + "   (a.attrName = 'name'              and lower(a.value) like :value) "
        + "or (a.attrName = 'displayName'       and lower(a.value) like :value) "
        + "or (a.attrName = 'extension'         and lower(a.value) like :value) "
        + "or (a.attrName = 'displayExtension'  and lower(a.value) like :value) "
      );
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByApproximateName");
      qry.setString( "value", "%" + name.toLowerCase() + "%" );
      Iterator it = qry.iterate();
      while (it.hasNext()) {
        groups.add( this.findByUuid( ( (HibernateAttributeDAO) it.next()).getGroupUuid() ) );
      }
      hs.close();
    }
    catch (GroupNotFoundException eShouldNeverHappen) {
      throw new IllegalStateException( 
        "this should never happen: attribute without owning group: " + eShouldNeverHappen.getMessage(), eShouldNeverHappen
      );
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupDAO as g where g.createTime > :time");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedAfter");
      qry.setLong( "time", d.getTime() );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        groups.add( GroupDTO.getDTO( (GroupDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupDAO as g where g.createTime < :time");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedBefore");
      qry.setLong( "time", d.getTime() );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        groups.add( GroupDTO.getDTO( (GroupDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByModifiedAfter(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupDAO as g where g.modifyTime > :time");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByModifiedAfter");
      qry.setLong( "time", d.getTime() );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        groups.add( GroupDTO.getDTO( (GroupDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByModifiedBefore(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupDAO as g where g.modifyTime < :time");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByModifiedBefore");
      qry.setLong( "time", d.getTime() );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        groups.add( GroupDTO.getDTO( (GroupDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByType(GroupTypeDTO _gt) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      // TODO 20070316 use a join query?
      Query   qry = hs.createQuery("select gtt.groupUuid from HibernateGroupTypeTupleDAO gtt where gtt.typeUuid = :type");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByType");
      qry.setString( "type", _gt.getUuid() );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        groups.add( findByUuid( (String) it.next() ) );
      }
      hs.close();
    }
    catch (GroupNotFoundException eGNF) {
      throw new GrouperDAOException( eGNF.getMessage(), eGNF );
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } 

  /**
   * @since   1.2.0
   */
  public GroupDTO findByAttribute(String attr, String val) 
    throws  GrouperDAOException,
            GroupNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateAttributeDAO as a where a.attrName = :field and a.value like :value");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByAttribute");
      qry.setString("field", attr);
      qry.setString("value", val);
      HibernateAttributeDAO a = (HibernateAttributeDAO) qry.uniqueResult();
      hs.close();
      if (a == null) {
        throw new GroupNotFoundException();
      }
      return this.findByUuid( a.getGroupUuid() );
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public GroupDTO findByName(String name) 
    throws  GrouperDAOException,
            GroupNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateAttributeDAO as a where a.attrName = 'name' and a.value = :value");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByName");
      qry.setString("value", name);
      HibernateAttributeDAO a = (HibernateAttributeDAO) qry.uniqueResult();
      hs.close();
      if (a == null) {
        throw new GroupNotFoundException();
      }
      return this.findByUuid( a.getGroupUuid() );
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * <p><b>Implementation Notes.</b</p>
   * <ol>
   * <li>Hibernate caching is enabled.</li>
   * </ol>
   * @see     GroupDAO#findByUuid(String)
   * @since   1.2.0
   */
  public GroupDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            GroupNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupDAO as g where g.uuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      HibernateGroupDAO dao = (HibernateGroupDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new GroupNotFoundException();
      }
      return GroupDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public Map getAttributes() {
    return findAllAttributesByGroup( this.getUuid() );
  }
  
  /**
   * @since   1.2.0
   */
  public String getCreateSource() {
    return this.createSource;
  }

  /**
   * @since   1.2.0
   */
  public long getCreateTime() {
    return this.createTime;
  }

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getId() {
    return this.id;
  }

  /**
   * @since   1.2.0
   */
  public String getModifierUuid() {
    return this.modifierUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getModifySource() {
    return this.modifySource;
  }

  /**
   * @since   1.2.0
   */
  public long getModifyTime() {
    return this.modifyTime;
  }

  /**
   * @since   1.2.0
   */
  public String getParentUuid() {
    return this.parentUUID;
  }

  /**
   * @since   1.2.0
   */
  public Set getTypes() {
    return _findAllTypesByGroup( this.getUuid() );
  }
  
  /**
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

  // @since   1.2.0 
  public boolean onDelete(Session hs) 
    throws  CallbackException
  {
    existsCache.put( this.getUuid(), false );
    return Lifecycle.NO_VETO;
  } // public boolean onDelete(hs)

  // @since   1.2.0
  public void onLoad(Session hs, Serializable id) {
    // nothing
  } // public void onLoad(hs, id)

  // @since   1.2.0
  public boolean onSave(Session hs) 
    throws  CallbackException
  {
    try {
      this._updateAttributes(hs);
      existsCache.put( this.getUuid(), true );
      return Lifecycle.NO_VETO;
    }
    catch (HibernateException eH) {
      throw new CallbackException( eH.getMessage(), eH );
    }
  } // public boolean onSave(hs)

  // @since   1.2.0
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
   * @since   1.2.0
   */
  public void revokePriv(GroupDTO _g, DefaultMemberOf mof)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = mof.getDeletes().iterator();
        while (it.hasNext()) {
          hs.delete( Rosetta.getDAO( it.next() ) );
        }
        it = mof.getSaves().iterator();
        while (it.hasNext()) {
          hs.saveOrUpdate( Rosetta.getDAO( it.next() ) );
        }
        hs.update( Rosetta.getDAO(_g) );
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close(); 
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public void revokePriv(GroupDTO _g, Set toDelete)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = toDelete.iterator();
        while (it.hasNext()) {
          hs.delete( Rosetta.getDAO( it.next() ) );
        }
        hs.update( Rosetta.getDAO(_g) );
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close(); 
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public GroupDAO setAttributes(Map attributes) {
    this.attributes = attributes;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDAO setCreateSource(String createSource) {
    this.createSource = createSource;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDAO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDAO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDAO setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDAO setModifySource(String modifySource) {
    this.modifySource = modifySource;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDAO setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDAO setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDAO setTypes(Set types) {
    // TODO 20070405 try to make this behave more like the rest of the *etters.
    //               as types are retrieved dynamically we don't need to cache
    //               them locally.  and, yes, that could be considered a poor 
    //               design decision.
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GroupDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public void update(GroupDTO _g)
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.update( _g.getDAO() );
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      } 
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    // TODO 20070307 ideally i would just put hooks for associated tables into "onDelete()" 
    //               but right now that is blowing up due to the session being flushed.
    hs.delete("from HibernateGroupTypeTupleDAO");
    hs.delete("from HibernateAttributeDAO"); 
    hs.delete("from HibernateGroupDAO");
    existsCache.removeAll(); 
  } 


  // PRIVATE CLASS METHODS //

  // @since   1.2.0
  private static Set _findAllTypesByGroup(String uuid)
    throws  GrouperDAOException
  {
    Set types = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupTypeTupleDAO as gtt where gtt.groupUuid = :group");
      qry.setCacheable(false);
      qry.setString("group", uuid);
      GroupTypeDAO                dao = GrouperDAOFactory.getFactory().getGroupType(); 
      HibernateGroupTypeTupleDAO  gtt;
      Iterator                    it  = qry.iterate();
      while (it.hasNext()) {
        gtt = (HibernateGroupTypeTupleDAO) it.next();
        types.add( dao.findByUuid( gtt.getTypeUuid() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    catch (SchemaException eS) { 
      throw new GrouperDAOException( eS.getMessage(), eS );
    }
    return types;
  } // private static Set _findAllTypesByGroup(uuid)


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  private void _updateAttributes(Session hs) 
    throws  HibernateException
  {
    // TODO 20070531 split and test
    Query qry = hs.createQuery("from HibernateAttributeDAO as a where a.groupUuid = :uuid");
    qry.setCacheable(false);
    qry.setCacheRegion(KLASS + "._UpdateAttributes");
    qry.setString("uuid", this.uuid);
    HibernateAttributeDAO a;
    Map                   attrs = new HashMap(this.attributes);
    String                k;
    Iterator              it = qry.iterate();
    while (it.hasNext()) {
      a = (HibernateAttributeDAO) it.next();
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
      HibernateAttributeDAO dao = new HibernateAttributeDAO(); 
      dao.setAttrName( (String) kv.getKey() );
      dao.setGroupUuid(this.uuid);
      dao.setValue( (String) kv.getValue() );
      hs.save(dao);
    }
  } // private void _updateAttributes(hs)

} 

