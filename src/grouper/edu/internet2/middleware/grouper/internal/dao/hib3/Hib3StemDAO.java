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
import  java.util.Set;
import  org.hibernate.*;

/**
 * Basic Hibernate <code>Stem</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3StemDAO.java,v 1.1 2007-08-30 15:52:22 blair Exp $
 * @since   @HEAD@
 */
public class Hib3StemDAO extends Hib3DAO implements StemDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3StemDAO.class.getName();


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
   * @since   @HEAD@
   */
  public String createChildGroup(StemDTO _parent, GroupDTO _child, MemberDTO _m)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = Hib3DAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      Hib3DAO  dao = (Hib3DAO) Rosetta.getDAO(_child);
      try {
        hs.save(dao);
        // add group-type tuples
        Hib3GroupTypeTupleDAO  tuple = new Hib3GroupTypeTupleDAO();
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
   * @since   @HEAD@
   */
  public String createChildStem(StemDTO _parent, StemDTO _child)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = Hib3DAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      Hib3DAO  dao = (Hib3DAO) Rosetta.getDAO(_child);
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
   * @since   @HEAD@
   */
  public String createRootStem(StemDTO _root)
    throws  GrouperDAOException
  {
    try {
      Session           hs  = Hib3DAO.getSession();
      Transaction       tx  = hs.beginTransaction();
      Hib3StemDAO  dao = (Hib3StemDAO) _root.getDAO();
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
   * @since   @HEAD@
   */
  public void delete(StemDTO _ns)
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = Hib3DAO.getSession();
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
   * @since   @HEAD@
   */
  public boolean exists(String uuid) 
    throws  GrouperDAOException
  {
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("select ns.id from Hib3StemDAO ns where ns.uuid = :uuid");
      qry.setString("uuid", uuid);
      boolean rv  = false;
      if ( qry.uniqueResult() != null ) {
        rv = true; 
      }
      hs.close();
      return rv;
    }
    catch (HibernateException eH) {
      ErrorLog.fatal( Hib3StemDAO.class, eH.getMessage() );
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 
  
  /**
   * @since   @HEAD@
   */
  public Set findAllByApproximateDisplayExtension(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3StemDAO as ns where lower(ns.displayExtension) like lower(:value)");
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
   * @since   @HEAD@
   */
  public Set findAllByApproximateDisplayName(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3StemDAO as ns where lower(ns.displayName) like lower(:value)");
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
   * @since   @HEAD@
   */
  public Set findAllByApproximateExtension(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3StemDAO as ns where lower(ns.extension) like lower(:value)");
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
   * @since   @HEAD@
   */
  public Set findAllByApproximateName(String val) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3StemDAO as ns where lower(ns.name) like lower(:value)");
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
   * @since   @HEAD@
   */
  public Set findAllByApproximateNameAny(String name) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery(
        "from Hib3StemDAO as ns where "
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
   * @since   @HEAD@
   */
  public Set findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3StemDAO as ns where ns.createTime > :time");
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
   * @since   @HEAD@
   */
  public Set findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException
  {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3StemDAO as ns where ns.createTime < :time");
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
   * {@link Hib3GroupDAO#findAllByApproximateAttr(String, String)} will be called
   * instead.  While that will trigger a full table scan of the Gruop attributes table it
   * will still probably be faster than recursing through the registry.</p>
   * @see     StemDAO#findAllChildGroups(StemDTO, Stem.Scope)
   * @since   @HEAD@
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
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3GroupDAO as g where g.parentUuid = :parent");
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
   * @since   @HEAD@
   */
  public Set<StemDTO> findAllChildStems(StemDTO ns, Stem.Scope scope)
    throws  GrouperDAOException,
            IllegalStateException
  {
    Set<StemDTO> stems = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3StemDAO as ns where ns.parentUuid = :parent");
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
   * @since   @HEAD@
   */
  public StemDTO findByName(String name) 
    throws  GrouperDAOException,
            StemNotFoundException
  {
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3StemDAO as ns where ns.name = :name");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByName");
      qry.setString("name", name);
      Hib3StemDAO dao = (Hib3StemDAO) qry.uniqueResult();
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
   * @since   @HEAD@
   */
  public StemDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            StemNotFoundException
  {
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3StemDAO as ns where ns.uuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      Hib3StemDAO dao = (Hib3StemDAO) qry.uniqueResult();
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
  public String getDescription() {
    return this.description;
  }

  /**
   * @since   @HEAD@
   */
  public String getDisplayExtension() {
    return this.displayExtension;
  }

  /**
   * @since   @HEAD@
   */
  public String getDisplayName() {
    return this.displayName;
  }

  /**
   * @since   @HEAD@
   */
  public String getExtension() {
    return this.extension;
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
  public String getName() {
    return this.name;
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
  public String getUuid() {
    return this.uuid;
  }

  /** 
   * @since   @HEAD@
   */
  public void revokePriv(StemDTO _ns, DefaultMemberOf mof)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = Hib3DAO.getSession();
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
   * @since   @HEAD@
   */
  public void renameStemAndChildren(StemDTO _ns, Set children)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = Hib3DAO.getSession();
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
   * @since   @HEAD@
   */
  public void revokePriv(StemDTO _ns, Set toDelete)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = Hib3DAO.getSession();
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
   * @since   @HEAD@
   */
  public StemDAO setCreateSource(String createSource) {
    this.createSource = createSource;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }
  
  /**
   * @since   @HEAD@
   */
  public StemDAO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setDisplayExtension(String displayExtension) {
    this.displayExtension = displayExtension;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setExtension(String extension) {
    this.extension = extension;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setModifierUuid(String modifierUUID) {
    this.modifierUUID = modifierUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setModifySource(String modifySource) {
    this.modifySource = modifySource;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setModifyTime(long modifyTime) {
    this.modifyTime = modifyTime;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public StemDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public void update(StemDTO _ns)
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = Hib3DAO.getSession();
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

  // @since   @HEAD@
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    // To appease Oracle the root stem is named ":" internally.
    hs.createQuery("delete from Hib3StemDAO as ns where ns.name not like :stem")
      .setParameter("stem", Stem.DELIM)
      .executeUpdate()
      ;
  } 

} 

