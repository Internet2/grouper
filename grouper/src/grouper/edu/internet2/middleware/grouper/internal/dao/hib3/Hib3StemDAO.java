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
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import edu.internet2.middleware.grouper.DefaultMemberOf;
import edu.internet2.middleware.grouper.ErrorLog;
import edu.internet2.middleware.grouper.GrouperDAOFactory;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.StemDAO;
import edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import edu.internet2.middleware.grouper.internal.dto.GroupTypeDTO;
import edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import edu.internet2.middleware.grouper.internal.dto.StemDTO;
import edu.internet2.middleware.grouper.internal.util.Rosetta;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Basic Hibernate <code>Stem</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3StemDAO.java,v 1.6.4.1 2008-08-23 18:48:46 shilen Exp $
 * @since   @HEAD@
 */
public class Hib3StemDAO extends Hib3DAO implements StemDAO {

  // PRIVATE CLASS CONSTANTS //
  /** */
  private static final String KLASS = Hib3StemDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  /** */
  private String  createSource;
  /** */
  private long    createTime;
  /** */
  private String  creatorUUID;
  /** */
  private String  description;
  /** */
  private String  displayExtension;
  /** */
  private String  displayName;
  /** */
  private String  extension;
  /** */
  private String  id;
  /** */
  private String  name;
  /** */
  private String  modifierUUID;
  /** */
  private String  modifySource;
  /** */
  private long    modifyTime;
  /** */
  private String  parentUUID;
  /** */
  private String  uuid;

  /**
   * @since   @HEAD@
   */
  public String createChildGroup(final StemDTO _parent, final GroupDTO _child, final MemberDTO _m)
    throws  GrouperDAOException {
    
    try {
      String id = (String)HibernateSession.callbackHibernateSession(
          GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
          new HibernateHandler() {

            public Object callback(HibernateSession hibernateSession) {
              Session       hs  = hibernateSession.getSession();
              Hib3DAO  dao = (Hib3DAO) Rosetta.getDAO(_child);
              hs.save(dao);

              // add attributes
              Map.Entry kv;
              Iterator attrIter = _child.getAttributes().entrySet().iterator();
              while (attrIter.hasNext()) {
                kv = (Map.Entry) attrIter.next();
                Hib3AttributeDAO attrDao = new Hib3AttributeDAO();
                attrDao.setAttrName( (String) kv.getKey() );
                attrDao.setGroupUuid( _child.getUuid() );
                attrDao.setValue( (String) kv.getValue() );
                hs.save(attrDao);
              }

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
              return dao.getId();
            }
        
      });
      return id;
    } catch (GrouperDAOException e) {
      String error = "Problem create child stem: " + GrouperUtil.toStringSafe(_parent)
        + ", child: " + GrouperUtil.toStringSafe(_child) + ", memberDto: " 
        + GrouperUtil.toStringSafe(_m) + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public String createChildStem(final StemDTO _parent, final StemDTO _child)
    throws  GrouperDAOException {
    try {
      String id = (String)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
          new HibernateHandler() {

            public Object callback(HibernateSession hibernateSession) {

              Session       hs  = hibernateSession.getSession();
              Hib3DAO  dao = (Hib3DAO) Rosetta.getDAO(_child);
              hs.save(dao);
              hs.update( Rosetta.getDAO(_parent) );
              return dao.getId();
            }
        
      });
      return id;
    }
    catch (GrouperDAOException e) {
      String error = "Problem creating child stem: " + GrouperUtil.toStringSafe(_parent)
        + ", privs: " + GrouperUtil.toStringSafe(_child) + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public String createRootStem(StemDTO _root)
    throws  GrouperDAOException {
    try {
      Hib3StemDAO  dao = (Hib3StemDAO) _root.getDAO();
      HibernateSession.byObjectStatic().save(dao);
      return dao.getId();
    }
    catch (GrouperDAOException e) {
      String error = "Problem creating root stem: " + GrouperUtil.toStringSafe(_root)
        + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public void delete(StemDTO _ns)
    throws  GrouperDAOException {
    try {
      HibernateSession.byObjectStatic().delete(_ns.getDAO());
    } catch (GrouperDAOException e) {
      String error = "Problem deleting: " + GrouperUtil.toStringSafe(_ns)
        + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public boolean exists(String uuid) 
    throws  GrouperDAOException {
    try {
      Object id = HibernateSession.byHqlStatic()
        .createQuery("select ns.id from Hib3StemDAO ns where ns.uuid = :uuid")
        .setString("uuid", uuid).uniqueResult(Object.class);
      
      boolean rv  = false;
      if ( id != null ) {
        rv = true; 
      }
      return rv;
    }
    catch (GrouperDAOException e) {
      String error = "Problem querying stem by uuid: '" + uuid + "', " + e.getMessage();
      ErrorLog.fatal( Hib3StemDAO.class, error );
      throw new GrouperDAOException( error, e );
    }
  } 
  
  /**
   * @since   @HEAD@
   */
  public Set findAllByApproximateDisplayExtension(String val) 
    throws  GrouperDAOException {
    Set stems = new LinkedHashSet();
    try {
      List<Hib3StemDAO> hib3StemDAOs = HibernateSession.byHqlStatic()
        .createQuery("from Hib3StemDAO as ns where lower(ns.displayExtension) like lower(:value)")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateDisplayExtension")
        .setString(  "value" , "%" + val.toLowerCase() + "%" )
        .list(Hib3StemDAO.class);
      for (Hib3StemDAO hib3StemDAO : hib3StemDAOs) {
        stems.add( StemDTO.getDTO( hib3StemDAO ) );
      }
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate display extension: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
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
      List<Hib3StemDAO> hib3StemDAOs = HibernateSession.byHqlStatic()
        .createQuery("from Hib3StemDAO as ns where lower(ns.displayName) like lower(:value)")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateDisplayName")
        .setString(  "value" , "%" + val.toLowerCase() + "%" ).list(Hib3StemDAO.class);
      for (Hib3StemDAO hib3StemDAO : hib3StemDAOs) {
        stems.add( StemDTO.getDTO( hib3StemDAO ) );
      }
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate display name: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
    return stems;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByApproximateExtension(String val) 
    throws  GrouperDAOException {
    Set stems = new LinkedHashSet();
    try {
      List<Hib3StemDAO> hib3StemDAOs = HibernateSession.byHqlStatic()
        .createQuery("from Hib3StemDAO as ns where lower(ns.extension) like lower(:value)")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateExtension")
        .setString(  "value" , "%" + val.toLowerCase() + "%" )
        .list(Hib3StemDAO.class);
      for (Hib3StemDAO hib3StemDAO : hib3StemDAOs) {
        stems.add( StemDTO.getDTO( hib3StemDAO ) );
      }
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate extension: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
    return stems;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByApproximateName(String val) 
    throws  GrouperDAOException {
    Set stems = new LinkedHashSet();
    try {
      List<Hib3StemDAO> hib3StemDAOs = HibernateSession.byHqlStatic()
        .createQuery("from Hib3StemDAO as ns where lower(ns.name) like lower(:value)")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByApproximateName")
        .setString(  "value" , "%" + val.toLowerCase() + "%" )
        .list(Hib3StemDAO.class);
      for (Hib3StemDAO hib3StemDAO : hib3StemDAOs) {
        stems.add( StemDTO.getDTO( hib3StemDAO ) );
      }
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate name: '" + val + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
    return stems;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByApproximateNameAny(String name) 
    throws  GrouperDAOException {
    Set stems = new LinkedHashSet();
    try {
      List<Hib3StemDAO> hib3StemDAOs = HibernateSession.byHqlStatic()
        .createQuery(
        "from Hib3StemDAO as ns where "
        + "   lower(ns.name)              like :name "
        + "or lower(ns.displayName)       like :name "
        + "or lower(ns.extension)         like :name "
        + "or lower(ns.displayExtension)  like :name" )
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByApproximateNameAny")
        .setString("name", "%" + name.toLowerCase() + "%")
        .list(Hib3StemDAO.class);
      for (Hib3StemDAO hib3StemDAO : hib3StemDAOs) {
        stems.add( StemDTO.getDTO( hib3StemDAO ) );
      }
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by approximate any: '" + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
    return stems;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByCreatedAfter(Date d) 
    throws  GrouperDAOException {
    Set stems = new LinkedHashSet();
    try {
      List<Hib3StemDAO> hib3StemDAOs = HibernateSession.byHqlStatic()
        .createQuery("from Hib3StemDAO as ns where ns.createTime > :time")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
        .setLong( "time", d.getTime() )
        .list(Hib3StemDAO.class);
      for (Hib3StemDAO hib3StemDAO : hib3StemDAOs) {
        stems.add( StemDTO.getDTO( hib3StemDAO ) );
      }
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by created after: '" + d + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
    return stems;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByCreatedBefore(Date d) 
    throws  GrouperDAOException {
    Set stems = new LinkedHashSet();
    try {
      List<Hib3StemDAO> hib3StemDAOs = HibernateSession.byHqlStatic()
        .createQuery("from Hib3StemDAO as ns where ns.createTime < :time")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByCreatedBefore")
        .setLong( "time", d.getTime() )
        .list(Hib3StemDAO.class);
      for (Hib3StemDAO hib3StemDAO : hib3StemDAOs) {
        stems.add( StemDTO.getDTO( hib3StemDAO ) );
      }
    }
    catch (GrouperDAOException e) {
      String error = "Problem find all stem by created before: '" + d + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
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
    throws  GrouperDAOException {
    // Optimization hack.  See "Implementation Notes" above for more details.
    // TODO 20070803  the reliance upon ":" is rather fragile.  why can't a StemDTO say
    //                whether it is the root stem?
    if ( (Stem.Scope.SUB == scope) && ( ":".equals( ns.getName() ) ) ) {
      return GrouperDAOFactory.getFactory().getGroup().findAllByApproximateAttr("name", "%");
    }

    Set<GroupDTO> groups = new LinkedHashSet();
    try {
      List<Hib3GroupDAO> hib3GroupDAOs = HibernateSession.byHqlStatic()
        .createQuery("from Hib3GroupDAO as g where g.parentUuid = :parent")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindChildGroups")
        .setString( "parent", ns.getUuid() )
        .list(Hib3GroupDAO.class);
      for (Hib3GroupDAO hib3GroupDAO : hib3GroupDAOs) {
        groups.add( GroupDTO.getDTO( hib3GroupDAO ) );
      }
      if (Stem.Scope.SUB == scope) { // recurse through child stems looking for child groups
        for ( StemDTO childStem : this.findAllChildStems(ns, Stem.Scope.SUB) ) {
          groups.addAll( this.findAllChildGroups(childStem, scope) ); 
        }
      }

    }
    catch (GrouperDAOException e) {
      String error = "Problem find all child groups, stem name: '" 
        + (ns == null ? null : ns.getName()) + "', scope: '" + scope + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
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
            IllegalStateException {
    Set<StemDTO> stems = new LinkedHashSet();
    try {
      List<Hib3StemDAO> hib3StemDAOs = HibernateSession.byHqlStatic()
        .createQuery("from Hib3StemDAO as ns where ns.parentUuid = :parent")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindChildStems")
        .setString( "parent", ns.getUuid() )
        .list(Hib3StemDAO.class);

      StemDTO           child;
      for (Hib3StemDAO hib3StemDAO : hib3StemDAOs) {
        child = StemDTO.getDTO( hib3StemDAO );
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

    }
    catch (GrouperDAOException e) {
      String error = "Problem find all child stems, stem name: '" 
        + (ns == null ? null : ns.getName()) + "', scope: '" + scope + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
    return stems;
  } 

  /**
   * @since   @HEAD@
   */
  public StemDTO findByName(String name) 
    throws  GrouperDAOException,
            StemNotFoundException {
    try {
      Hib3StemDAO dao = HibernateSession.byHqlStatic()
        .createQuery("from Hib3StemDAO as ns where ns.name = :name")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByName")
        .setString("name", name)
        .uniqueResult(Hib3StemDAO.class);
      if (dao == null) {
        throw new StemNotFoundException("Can't find stem by name: '" + name + "'");
      }
      return StemDTO.getDTO(dao);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find stem by name: '" 
        + name + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public StemDTO findByUuid(String uuid)
    throws  GrouperDAOException,
            StemNotFoundException {
    try {
      Hib3StemDAO dao = HibernateSession.byHqlStatic()
        .createQuery("from Hib3StemDAO as ns where ns.uuid = :uuid")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByUuid")
        .setString("uuid", uuid)
        .uniqueResult(Hib3StemDAO.class);
      if (dao == null) {
        throw new StemNotFoundException("Can't find stem by uuid: '" + uuid + "'");
      }
      return StemDTO.getDTO(dao);
    }
    catch (GrouperDAOException e) {
      String error = "Problem find stem by uuid: '" 
        + uuid + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public Set<StemDTO> getAllStems()
    throws  GrouperDAOException {
    Set<StemDTO> stems = new LinkedHashSet<StemDTO>();
    try {
      List<Hib3StemDAO> hib3StemDAOs = HibernateSession.byHqlStatic()
        .createQuery("from Hib3StemDAO as ns")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllStems")
        .list(Hib3StemDAO.class);
      for (Hib3StemDAO hib3StemDAO : hib3StemDAOs) {
        stems.add(StemDTO.getDTO(hib3StemDAO));
      } 
    } 
    catch (GrouperDAOException e) {
      String error = "Problem getting all stems: " + e.getMessage();
      throw new GrouperDAOException(error, e);
    }
    return stems;
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
  public void revokePriv(final StemDTO _ns, final DefaultMemberOf mof)
    throws  GrouperDAOException {
    try {
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
          new HibernateHandler() {

            public Object callback(HibernateSession hibernateSession) {
              Session     hs  = hibernateSession.getSession();
              Iterator it = mof.getDeletes().iterator();
              while (it.hasNext()) {
                GrouperDAO grouperDAO = Rosetta.getDAO( it.next());
                hs.delete(  grouperDAO );
              }
              hs.flush();
              
              it = mof.getSaves().iterator();
              while (it.hasNext()) {
                hs.saveOrUpdate( it.next() );
              }
              hs.flush();
              
              hs.update( _ns.getDAO() );
              return null;
            }
        
      });
    }
    catch (GrouperDAOException e) {
      String error = "Problem revoking priv: " + GrouperUtil.toStringSafe(_ns)
      + ", defaultMemberOf: " + GrouperUtil.toStringSafe(mof) + ", " + e.getMessage();

      throw new GrouperDAOException( error, e );
    }
  } 

  /**
   * @since   @HEAD@
   */
  public void renameStemAndChildren(final StemDTO _ns, final Set children)
    throws  GrouperDAOException {
    try {
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
          new HibernateHandler() {

            public Object callback(HibernateSession hibernateSession) {
              Session     hs  = hibernateSession.getSession();
              Iterator it = children.iterator();
              while (it.hasNext()) {
                hs.update( Rosetta.getDAO( it.next() ) );
              }
              hs.update( Rosetta.getDAO(_ns) );
              return null;
            }
        
      });
    }
    catch (GrouperDAOException e) {
      String error = "Problem revoking priv: " + GrouperUtil.toStringSafe(_ns)
        + ", children: " + GrouperUtil.toStringSafe(children) + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 
  
  /**
   * @since   @HEAD@
   */
  public void revokePriv(final StemDTO _ns, final Set toDelete)
    throws  GrouperDAOException
  {
    try {
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
          new HibernateHandler() {

            public Object callback(HibernateSession hibernateSession) {
              Session     hs  = hibernateSession.getSession();
              Iterator it = toDelete.iterator();
              while (it.hasNext()) {
                hs.delete( Rosetta.getDAO( it.next() ) );
              }
              hs.update( Rosetta.getDAO(_ns) );
              return null;
            }
        
      });
    }
    catch (GrouperDAOException e) {
      String error = "Problem revoking priv: " + GrouperUtil.toStringSafe(_ns)
        + ", privs: " + GrouperUtil.toStringSafe(toDelete) + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
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
    throws  GrouperDAOException {

    try {
      HibernateSession.byObjectStatic().update(_ns.getDAO());
    }
    catch (GrouperDAOException e) {
      String error = "Problem with hib update: " + GrouperUtil.toStringSafe(_ns)
       + ",\n" + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  } 


  // PROTECTED CLASS METHODS //

  /**
   * @param hs 
   * @throws HibernateException 
   * 
   */
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    // To appease Oracle the root stem is named ":" internally.
    List<Hib3StemDAO> hib3StemDAOs = 
      hs.createQuery("from Hib3StemDAO as ns where ns.name not like :stem order by name desc")
      .setParameter("stem", Stem.DELIM)
      .list()
      ;

    // Deleting each stem from the time created in descending order. This is necessary to prevent
    // deleting parent stems before child stems which causes integrity constraint violations  on some
    // databases.
    for (Hib3StemDAO hib3StemDAO : hib3StemDAOs) {
      hs.createQuery("delete from Hib3StemDAO ns where ns.uuid=:uuid")
      .setString("uuid", hib3StemDAO.getUuid())
      .executeUpdate();
    }

    // Reset "modify" columns.  Setting the modifierUuid property to null is important to avoid foreign key issues.
    hs.createQuery("update Hib3StemDAO as ns set ns.modifierUuid = :id, ns.modifyTime = :time, ns.modifySource = :source")
      .setParameter("id", null, Hibernate.STRING)
      .setParameter("time", new Long(0), Hibernate.LONG)
      .setParameter("source", null, Hibernate.STRING)
      .executeUpdate();
      ;

  }

} 

