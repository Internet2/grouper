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
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link Composite} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateCompositeDAO.java,v 1.14 2007-04-05 14:28:28 blair Exp $
 * @since   1.2.0
 */
class HibernateCompositeDAO extends HibernateDAO implements CompositeDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateCompositeDAO.class.getName();


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
   * @since   1.2.0
   */
  public Set findAsFactor(GroupDTO _g) 
    throws  GrouperDAOException
  {
    Set composites = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
          "from HibernateCompositeDAO as c where (" 
        + " c.leftFactorUuid = :left or c.rightFactorUuid = :right "
        + ")"
      );
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAsFactor");
      qry.setString( "left",  _g.getUuid() );
      qry.setString( "right", _g.getUuid() );
      composites.addAll( CompositeDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) { 
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return composites;
  } // public Set findAsFactor(_g)

  /**
   * @since   1.2.0
   */
  public CompositeDTO findAsOwner(GroupDTO _g) 
    throws  CompositeNotFoundException,
            GrouperDAOException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateCompositeDAO as c where c.factorOwnerUuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAsOwner");
      qry.setString( "uuid", _g.getUuid() );
      HibernateCompositeDAO dao = (HibernateCompositeDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new CompositeNotFoundException(E.COMP_NOTOWNER);
      }
      return CompositeDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH) ;
    }
  } // public CompositeDTO findAsOwner(_g)

  /**
   * @since   1.2.0
   */
  public CompositeDTO findByUuid(String uuid) 
    throws  CompositeNotFoundException,
            GrouperDAOException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateCompositeDAO as c where c.uuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      HibernateCompositeDAO dao = (HibernateCompositeDAO) qry.uniqueResult();
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
  public String getFactorOwnerUuid() {
    return this.factorOwnerUUID;
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
  public String getLeftFactorUuid() {
    return this.leftFactorUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getRightFactorUuid() {
    return this.rightFactorUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getType() {
    return this.type;
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
  public HibernateCompositeDAO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateCompositeDAO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateCompositeDAO setFactorOwnerUuid(String factorOwnerUUID) {
    this.factorOwnerUUID = factorOwnerUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateCompositeDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateCompositeDAO setLeftFactorUuid(String leftFactorUUID) {
    this.leftFactorUUID = leftFactorUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateCompositeDAO setRightFactorUuid(String rightFactorUUID) {
    this.rightFactorUUID = rightFactorUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateCompositeDAO setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateCompositeDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public void update(Set toAdd, Set toDelete, Set modGroups, Set modStems) 
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
        throw new GrouperDAOException( E.COMP_UPDATE + eH.getMessage(), eH );
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( E.COMP_UPDATE + eH.getMessage(), eH);
    }
  } // public void update(toAdd, toDelete, modGroups, modStems)


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.delete("from HibernateCompositeDAO");
  } // protected static void reset(hs)

} // class HibernateCompositeDAO extends HibernateDAO implements CompositeDAO

