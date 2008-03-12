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
import  edu.internet2.middleware.grouper.ErrorLog;
import  edu.internet2.middleware.grouper.GrouperDAOFactory;
import  edu.internet2.middleware.grouper.DefaultMemberOf;
import  edu.internet2.middleware.grouper.Stem;
import  edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dao.StemDAO;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import  edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import  edu.internet2.middleware.grouper.internal.dto.StemDTO;
import  edu.internet2.middleware.grouper.internal.util.Rosetta;
import  java.util.Date;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;
import  net.sf.hibernate.*;

/**
 * Basic Hibernate <code>Stem</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: HibernateStemDAO.java,v 1.16 2008-03-12 12:42:59 shilen Exp $
 * @since   1.2.0
 */
public class HibernateStemDAO extends HibernateDAO implements StemDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateStemDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  private String  createSource;
  private long    createTime;
  private String  creatorUUID;
  private String  description;
  private String  displayExtension;
  private String  displayName;
  private String  extension;
  private String  id;
  private String  name;
  private String  modifierUUID;
  private String  modifySource;
  private long    modifyTime;
  private String  parentUUID;
  private String  uuid;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public String createChildGroup(StemDTO _parent, GroupDTO _child, MemberDTO _m)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = (HibernateDAO) Rosetta.getDAO(_child);
      try {
        hs.save(dao);

        // add attributes
        Map.Entry kv;
        Iterator attrIter = _child.getAttributes().entrySet().iterator();
        while (attrIter.hasNext()) {
          kv = (Map.Entry) attrIter.next();
          HibernateAttributeDAO attrDao = new HibernateAttributeDAO();
          attrDao.setAttrName( (String) kv.getKey() );
          attrDao.setGroupUuid( _child.getUuid() );
          attrDao.setValue( (String) kv.getValue() );
          hs.save(attrDao);
        }

        // add group-type tuples
        HibernateGroupTypeTupleDAO  tuple = new HibernateGroupTypeTupleDAO();
        Iterator                    it    = _child.getTypes().iterator();
        while (it.hasNext()) {
          tuple.setGroupUuid( _child.getUuid() );
          tuple.setTypeUuid( ( (GroupTypeDTO) it.next() ).getUuid() );
          hs.save(tuple); // new group-type tuple
        }
        hs.update( Rosetta.getDAO(_parent) );
        if ( !GrouperDAOFactory.getFactory().getMember().exists( _m.getUuid() ) ) {
          hs.save( Rosetta.getDAO(_m) );
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return dao.getId();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public String createChildStem(StemDTO _parent, StemDTO _child)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = (HibernateDAO) Rosetta.getDAO(_child);
      try {
        hs.save(dao);
        hs.update( Rosetta.getDAO(_parent) );
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return dao.getId();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public String createRootStem(StemDTO _root)
    throws  GrouperDAOException
  {
    try {
      Session           hs  = HibernateDAO.getSession();
      Transaction       tx  = hs.beginTransaction();
      HibernateStemDAO  dao = (HibernateStemDAO) _root.getDAO();
      try {
        hs.save(dao);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return dao.getId();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public void delete(StemDTO _ns)
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.delete( _ns.getDAO() );
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
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("select ns.id from HibernateStemDAO ns where ns.uuid = :uuid");
      qry.setString("uuid", uuid);
      boolean rv  = false;
      if ( qry.uniqueResult() != null ) {
        rv = true; 
      }
      hs.close();
      return rv;
    }
    catch (HibernateException eH) {
      ErrorLog.fatal( HibernateStemDAO.class, eH.getMessage() );
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 
  
  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateDisplayExtension(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where lower(ns.displayExtension) like lower(:value)");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByApproximateDisplayExtension");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        stems.add( StemDTO.getDTO( (StemDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateDisplayName(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where lower(ns.displayName) like lower(:value)");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByApproximateDisplayName");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        stems.add( StemDTO.getDTO( (StemDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateExtension(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where lower(ns.extension) like lower(:value)");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByApproximateExtension");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        stems.add( StemDTO.getDTO( (StemDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateName(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where lower(ns.name) like lower(:value)");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByApproximateName");
      qry.setString(  "value" , "%" + val.toLowerCase() + "%" );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        stems.add( StemDTO.getDTO( (StemDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByApproximateNameAny(String name) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateStemDAO as ns where "
        + "   lower(ns.name)              like :name "
        + "or lower(ns.displayName)       like :name "
        + "or lower(ns.extension)         like :name "
        + "or lower(ns.displayExtension)  like :name" 
      );
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByApproximateNameAny");
      qry.setString("name", "%" + name.toLowerCase() + "%");
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        stems.add( StemDTO.getDTO( (StemDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.createTime > :time");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedAfter");
      qry.setLong( "time", d.getTime() );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        stems.add( StemDTO.getDTO( (StemDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.createTime < :time");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedBefore");
      qry.setLong( "time", d.getTime() );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        stems.add( StemDTO.getDTO( (StemDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } 
  
  /**
   * <p><b>Implementation Notes</b></p>
   * <p> This method should perform better with flatter registry structures as a deep search 
   * scope can result in a large number of SQL queries.  An initial query is done to find 
   * groups that are immediate children of <i>ns</i>.  After that, if <i>scope</i> is 
   * <code>SUB</code> additional queries will be generated to find all child stems within scope 
   * and then all child groups within those child stems.  As an optimization, if <i>ns</i> is 
   * the root stem of the registry and a <code>SUB</code> search scope is being used none of 
   * those queries will be performed.  
   * {@link HibernateGroupDAO#findAllByApproximateAttr(String, String)} will be called
   * instead.  While that will trigger a full table scan of the Gruop attributes table it
   * will still probably be faster than recursing through the registry.</p>
   * @see     StemDAO#findAllChildGroups(StemDTO, Stem.Scope)
   * @since   1.2.1
   */
  public Set<GroupDTO> findAllChildGroups(StemDTO ns, Stem.Scope scope)
    throws  GrouperDAOException
  {
    // Optimization hack.  See "Implementation Notes" above for more details.
    // TODO 20070803  the reliance upon ":" is rather fragile.  why can't a StemDTO say
    //                whether it is the root stem?
    if ( (Stem.Scope.SUB == scope) && ( ":".equals( ns.getName() ) ) ) {
      return GrouperDAOFactory.getFactory().getGroup().findAllByApproximateAttr("name", "%");
    }

    Set<GroupDTO> groups = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateGroupDAO as g where g.parentUuid = :parent");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindChildGroups");
      qry.setString( "parent", ns.getUuid() );

      Iterator<GroupDAO> it = qry.list().iterator();
      while ( it.hasNext() ) {
        groups.add( GroupDTO.getDTO( it.next() ) );
      }
      if (Stem.Scope.SUB == scope) { // recurse through child stems looking for child groups
        for ( StemDTO childStem : this.findAllChildStems(ns, Stem.Scope.SUB) ) {
          groups.addAll( this.findAllChildGroups(childStem, scope) ); 
        }
      }

      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return groups;
  }
  
  /**
   * @see     StemDAO#findAllChildStems(StemDTO, Stem.Scope)
   * @throws  IllegalStateException if unknown scope.
   * @since   1.2.1
   */
  public Set<StemDTO> findAllChildStems(StemDTO ns, Stem.Scope scope)
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set<StemDTO> stems = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.parentUuid = :parent");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindChildStems");
      qry.setString( "parent", ns.getUuid() );

      StemDTO           child;
      Iterator<StemDAO> it    = qry.list().iterator();
      while ( it.hasNext() ) {
        child = StemDTO.getDTO( it.next() );
        stems.add(child);
        if      (Stem.Scope.ONE == scope) {
          continue;
        }
        else if (Stem.Scope.SUB == scope) {
          stems.addAll( this.findAllChildStems(child, scope) ); // recurse and find children-of-child
        }
        else {
          throw new IllegalStateException("unknown search scope: " + scope);
        }
      }

      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return stems;
  } 

  /**
   * @since   1.2.0
   */
  public StemDTO findByName(String name) 
    throws  GrouperDAOException,
            StemNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.name = :name");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByName");
      qry.setString("name", name);
      HibernateStemDAO dao = (HibernateStemDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new StemNotFoundException("Stem '" + name + "' is not found");
      }
      return StemDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public StemDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            StemNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateStemDAO as ns where ns.uuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      HibernateStemDAO dao = (HibernateStemDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new StemNotFoundException();
      }
      return StemDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
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
  public String getDescription() {
    return this.description;
  }

  /**
   * @since   1.2.0
   */
  public String getDisplayExtension() {
    return this.displayExtension;
  }

  /**
   * @since   1.2.0
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * @since   1.2.0
   */
  public String getExtension() {
    return this.extension;
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
  public String getName() {
    return this.name;
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
  public String getUuid() {
    return this.uuid;
  }

  /** 
   * @since   1.2.0
   */
  public void revokePriv(StemDTO _ns, DefaultMemberOf mof)
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
          hs.saveOrUpdate( it.next() );
        }
        hs.update( _ns.getDAO() );
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
  public void renameStemAndChildren(StemDTO _ns, Set children)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = children.iterator();
        while (it.hasNext()) {
          hs.update( Rosetta.getDAO( it.next() ) );
        }
        hs.update( Rosetta.getDAO(_ns) );
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
  public void revokePriv(StemDTO _ns, Set toDelete)
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
        hs.update( Rosetta.getDAO(_ns) );
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
  public StemDAO setCreateSource(String createSource) {
    this.createSource = createSource;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }
  
  /**
   * @since   1.2.0
   */
  public StemDAO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setDisplayExtension(String displayExtension) {
    this.displayExtension = displayExtension;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setExtension(String extension) {
    this.extension = extension;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setModifySource(String modifySource) {
    this.modifySource = modifySource;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public StemDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public void update(StemDTO _ns)
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.update( _ns.getDAO() );
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
    // To appease Oracle the root stem is named ":" internally.
    hs.delete("from HibernateStemDAO as ns where ns.name not like '" + Stem.DELIM + "'");
  } 

} 

