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

package edu.internet2.middleware.grouper;
import  java.io.Serializable;
import  java.util.Date;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link Group} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateGroupDAO.java,v 1.13 2007-02-20 20:29:20 blair Exp $
 * @since   1.2.0
 */
class HibernateGroupDAO extends HibernateDAO implements Lifecycle {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateGroupDAO.class.getName();


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
  private Set     types;
  private String  uuid;


  // PUBLIC INSTANCE METHODS //

  // @since   1.2.0 
  public boolean onDelete(Session hs) 
    throws  CallbackException
  {
    // TODO 20070201 onDelete not implemented - should delete attrs
    return Lifecycle.NO_VETO;
  } // public boolean onDelete(hs)

  // @since   1.2.0
  public void onLoad(Session hs, Serializable id) {
    // TODO 20070201 onLoad not implemented
  } // public void onLoad(hs, id)

  // @since   1.2.0
  public boolean onSave(Session hs) 
    throws  CallbackException
  {
    try {
      this._updateAttributes(hs);
    }
    catch (HibernateException eH) {
      throw new CallbackException( eH.getMessage(), eH );
    }
    return Lifecycle.NO_VETO;
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


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void addType(Group g, GroupType t) 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        HibernateGroupTypeTupleDAO gtt = new HibernateGroupTypeTupleDAO();
        gtt.setGroupUuid( g.getDTO().getUuid() );
        gtt.setTypeUuid( t.getDTO().getTypeUuid() );
        hs.save(gtt); // new group-type tuple
        hs.saveOrUpdate( Rosetta.getDAO(g) ); // modified group
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
  } // protected static void addType(g, t)

  // @since   1.2.0
  protected static void deleteType(Group g, GroupType t) 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        HibernateGroupTypeTupleDAO gtt = HibernateGroupTypeTupleDAO.findByGroupAndType( g.getDTO(), t.getDTO() );
        hs.delete(gtt); // delete group-type tuple
        hs.saveOrUpdate( Rosetta.getDAO(g) ); // save modified group
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
  } // protected static void deleteType(g, t)

  // @since   1.2.0
  protected static Map findAllAttributesByGroup(String uuid)
    throws  GrouperDAOException
  {
    Map attrs = new HashMap();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateAttributeDAO as a where a.groupUuid = :uuid");
      qry.setCacheable(true);
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
  } // protected static Map findAllAttributesByGroup(uuid)

  // @since   1.2.0
  protected static Set findAllByAnyApproximateAttr(String val) 
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateAttributeDAO as a where lower(a.value) like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByAnyApproximateAttr");
      qry.setString( "value", "%" + val.toLowerCase() + "%" );
      Iterator it = qry.iterate();
      while (it.hasNext()) {
        groups.add( HibernateGroupDAO.findByUuid( ( (HibernateAttributeDAO) it.next()).getGroupUuid() ) );
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
  } // protected static Set findAllByAnyApproximateAttr(val)

  // @since   1.2.0
  protected static Set findAllByApproximateAttr(String attr, String val) 
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateAttributeDAO as a where a.attrName = :field and lower(a.value) like :value"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByApproximateAttr");
      qry.setString("field", attr);
      qry.setString( "value", "%" + val.toLowerCase() + "%" );
      Iterator it = qry.iterate();
      while (it.hasNext()) {
        groups.add( HibernateGroupDAO.findByUuid( ( (HibernateAttributeDAO) it.next()).getGroupUuid() ) );
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
  } // protected static Set findAllByApproximateAttr(attr, val)

  // @since   1.2.0
  protected static Set findAllByApproximateName(String name) 
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
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByApproximateName");
      qry.setString( "value", "%" + name.toLowerCase() + "%" );
      Iterator it = qry.iterate();
      while (it.hasNext()) {
        groups.add( HibernateGroupDAO.findByUuid( ( (HibernateAttributeDAO) it.next()).getGroupUuid() ) );
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
  } // protected static Set internal_findAllByApproximateName(name)

  // @since   1.2.0
  protected static Set findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupDAO as g where g.createTime > :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedAfter");
      qry.setLong( "time", d.getTime() );
      groups.addAll( GroupDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set findAllByCreatedAfter(d)

  // @since   1.2.0
  protected static Set findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupDAO as g where g.createTime < :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedBefore");
      qry.setLong( "time", d.getTime() );
      groups.addAll( GroupDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set findAllByCreatedBefore(d)

  // @since   1.2.0
  protected static Set findAllByModifiedAfter(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupDAO as g where g.modifyTime > :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByModifiedAfter");
      qry.setLong( "time", d.getTime() );
      groups.addAll( GroupDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set findAllByModifiedAfter(d)

  // @since   1.2.0
  protected static Set findAllByModifiedBefore(Date d) 
    throws  GrouperDAOException
  {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupDAO as g where g.modifyTime < :time");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByModifiedBefore");
      qry.setLong( "time", d.getTime() );
      groups.addAll( GroupDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  } // protected static Set findAllByModifiedBefore(d)

  // TODO 20061127 can i use a variant of this query in `GroupType.delete()`?
  // @since   1.2.0
  protected static Set findAllByType(GroupType type) 
    throws  GrouperDAOException
  {
    // TODO 20070207 this could probably be more elegant
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupTypeTupleDAO as gtt where gtt.typeUuid = :type");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByType");
      qry.setString( "type", type.getDTO().getTypeUuid() );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        HibernateGroupTypeTupleDAO gtt = (HibernateGroupTypeTupleDAO) it.next();
        groups.add( findByUuid( gtt.getGroupUuid() ) );
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
  } // protected static Set findAllByType(s, type)

  // @since   1.2.0
  protected static GroupDTO findByAttribute(String attr, String val) 
    throws  GrouperDAOException,
            GroupNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateAttributeDAO as a where a.attrName = :field and a.value like :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByAttribute");
      qry.setString("field", attr);
      qry.setString("value", val);
      HibernateAttributeDAO a = (HibernateAttributeDAO) qry.uniqueResult();
      hs.close();
      if (a == null) {
        throw new GroupNotFoundException();
      }
      return HibernateGroupDAO.findByUuid( a.getGroupUuid() );
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static GroupDTO findByAttribute(attr, val)

  // @since   1.2.0
  protected static GroupDTO findByName(String name) 
    throws  GrouperDAOException,
            GroupNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateAttributeDAO as a where a.attrName = 'name' and a.value = :value");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByName");
      qry.setString("value", name);
      HibernateAttributeDAO a = (HibernateAttributeDAO) qry.uniqueResult();
      hs.close();
      if (a == null) {
        throw new GroupNotFoundException();
      }
      return HibernateGroupDAO.findByUuid( a.getGroupUuid() );
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static GroupDTO findByName(name)

  // @since   1.2.0
  protected static GroupDTO findByUuid(String uuid) 
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
  } // private static GroupDTO findByUuid(uuid)

  // @since   1.2.0
  protected static void revokePriv(GroupDTO dto, MemberOf mof)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = mof.internal_getDeletes().iterator();
        while (it.hasNext()) {
          hs.delete( Rosetta.getDAO( it.next() ) );
        }
        it = mof.internal_getSaves().iterator();
        while (it.hasNext()) {
          hs.saveOrUpdate( Rosetta.getDAO( it.next() ) );
        }
        hs.update( Rosetta.getDAO(dto) );
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
  } // protected static void revokePriv(dto, mof)

  // @since   1.2.0
  protected static void revokePriv(GroupDTO dto, Set toDelete)
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
        hs.update( Rosetta.getDAO(dto) );
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
  } // protected static void revokePriv(dto, toDelete)


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
      HibernateGroupTypeTupleDAO  gtt;
      Iterator                    it  = qry.iterate();
      while (it.hasNext()) {
        gtt = (HibernateGroupTypeTupleDAO) it.next();
        types.add( HibernateGroupTypeDAO.findByUuid( gtt.getTypeUuid() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    catch (SchemaException eS) { // TODO 20070207 this should NOT be thrown here
      throw new GrouperDAOException( eS.getMessage(), eS );
    }
    return types;
  } // private static Set _findAllTypesByGroup(uuid)


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  private void _updateAttributes(Session hs) 
    throws  HibernateException
  {
    // TODO 20070201 refactor.  this is too big.
    Transaction tx  = hs.beginTransaction();
    Query       qry = hs.createQuery("from HibernateAttributeDAO as a where a.groupUuid = :uuid");
    qry.setCacheable(true);
    qry.setCacheRegion(KLASS + "._UpdateAttributes");
    qry.setString("uuid", this.uuid);
    HibernateAttributeDAO a;
    Map                   attrs = new HashMap(this.attributes);
    String                k;
    Iterator it = qry.iterate();
    try {
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
      tx.commit();
    }
    catch (HibernateException eH) {
      tx.rollback();
      throw eH;
    }
  } // private void _updateAttributes(hs)


  // GETTERS //

  protected Map getAttributes() {
    return findAllAttributesByGroup( this.getUuid() );
  }
  protected String getCreateSource() {
    return this.createSource;
  }
  protected long getCreateTime() {
    return this.createTime;
  }
  protected String getCreatorUuid() {
    return this.creatorUUID;
  }
  protected String getId() {
    return this.id;
  }
  protected String getModifierUuid() {
    return this.modifierUUID;
  }
  protected String getModifySource() {
    return this.modifySource;
  }
  protected long getModifyTime() {
    return this.modifyTime;
  }
  protected String getParentUuid() {
    return this.parentUUID;
  }
  protected Set getTypes() {
    return _findAllTypesByGroup( this.getUuid() );
  }
  protected String getUuid() {
    return this.uuid;
  }


  // SETTERS //

  protected void setAttributes(Map attributes) {
    this.attributes = attributes;
  }
  protected void setCreateSource(String createSource) {
    this.createSource = createSource;
  }
  protected void setCreateTime(long createTime) {
    this.createTime = createTime;
  }
  protected void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
  }
  protected void setModifySource(String modifySource) {
    this.modifySource = modifySource;
  }
  protected void setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
  }
  protected void setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
  }
  protected void setTypes(Set types) {
    this.types = types;
  }
  protected void setUuid(String uuid) {
    this.uuid = uuid;
  }

} // class HibernateGroupDAO extends HibernateDAO implements Lifecycle

