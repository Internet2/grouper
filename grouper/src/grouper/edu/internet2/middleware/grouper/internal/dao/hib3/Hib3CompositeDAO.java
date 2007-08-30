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
import  edu.internet2.middleware.grouper.CompositeNotFoundException;
import  edu.internet2.middleware.grouper.internal.dao.CompositeDAO;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dto.CompositeDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.util.Rosetta;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;
import  org.hibernate.*;

/**
 * Basic Hibernate <code>Composite</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3CompositeDAO.java,v 1.1 2007-08-30 15:52:22 blair Exp $
 * @since   @HEAD@
 */
public class Hib3CompositeDAO extends Hib3DAO implements CompositeDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3CompositeDAO.class.getName();


  // PRIVATE INSTANCE VARIABLES //
  private long    createTime;
  private String  creatorUUID;
  private String  factorOwnerUUID;
  private String  id;
  private String  leftFactorUUID;
  private String  rightFactorUUID;
  private String  type;
  private String  uuid;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   @HEAD@
   */
  public Set findAsFactor(GroupDTO _g) 
    throws  GrouperDAOException
  {
    Set composites = new LinkedHashSet();
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery(
          "from Hib3CompositeDAO as c where (" 
        + " c.leftFactorUuid = :left or c.rightFactorUuid = :right "
        + ")"
      );
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAsFactor");
      qry.setString( "left",  _g.getUuid() );
      qry.setString( "right", _g.getUuid() );
      Iterator it = qry.list().iterator();
      while (it.hasNext()) {
        composites.add( CompositeDTO.getDTO( (CompositeDAO) it.next() ) );
      }
      hs.close();
    }
    catch (HibernateException eH) { 
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return composites;
  } // public Set findAsFactor(_g)

  /**
   * @since   @HEAD@
   */
  public CompositeDTO findAsOwner(GroupDTO _g) 
    throws  CompositeNotFoundException,
            GrouperDAOException
  {
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3CompositeDAO as c where c.factorOwnerUuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAsOwner");
      qry.setString( "uuid", _g.getUuid() );
      Hib3CompositeDAO dao = (Hib3CompositeDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new CompositeNotFoundException();
      }
      return CompositeDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH) ;
    }
  } // public CompositeDTO findAsOwner(_g)

  /**
   * @since   @HEAD@
   */
  public CompositeDTO findByUuid(String uuid) 
    throws  CompositeNotFoundException,
            GrouperDAOException
  {
    try {
      Session hs  = Hib3DAO.getSession();
      Query   qry = hs.createQuery("from Hib3CompositeDAO as c where c.uuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      Hib3CompositeDAO dao = (Hib3CompositeDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new CompositeNotFoundException();
      }
      return CompositeDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // public CompositeDTO findByUuid(uuid)

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
  public String getFactorOwnerUuid() {
    return this.factorOwnerUUID;
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
  public String getLeftFactorUuid() {
    return this.leftFactorUUID;
  }

  /**
   * @since   @HEAD@
   */
  public String getRightFactorUuid() {
    return this.rightFactorUUID;
  }

  /**
   * @since   @HEAD@
   */
  public String getType() {
    return this.type;
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
  public CompositeDAO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setFactorOwnerUuid(String factorOwnerUUID) {
    this.factorOwnerUUID = factorOwnerUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setLeftFactorUuid(String leftFactorUUID) {
    this.leftFactorUUID = leftFactorUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setRightFactorUuid(String rightFactorUUID) {
    this.rightFactorUUID = rightFactorUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public CompositeDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public void update(Set toAdd, Set toDelete, Set modGroups, Set modStems) 
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
        it = toAdd.iterator();
        while (it.hasNext()) {
          hs.save( Rosetta.getDAO( it.next() ) );
        }
        it = modGroups.iterator();
        while (it.hasNext()) {
          hs.update( Rosetta.getDAO( it.next() ) );
        }
        it = modStems.iterator();
        while (it.hasNext()) {
          hs.update( Rosetta.getDAO( it.next() ) );
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw new GrouperDAOException( eH.getMessage(), eH );
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH);
    }
  } // public void update(toAdd, toDelete, modGroups, modStems)


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.createQuery("delete from Hib3CompositeDAO").executeUpdate();
  } 

} 

