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
 * @version $Id: HibernateCompositeDAO.java,v 1.10 2007-02-14 22:06:40 blair Exp $
 * @since   1.2.0
 */
class HibernateCompositeDAO extends HibernateDAO {

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


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set findAsFactor(GroupDTO g) 
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
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAsFactor");
      qry.setString( "left",  g.getUuid() );
      qry.setString( "right", g.getUuid() );
      composites.addAll( CompositeDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) { 
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return composites;
  } // protected static Set findAsFactor(g)

  // @since   1.2.0
  protected static CompositeDTO findAsOwner(GroupDTO g) 
    throws  CompositeNotFoundException,
            GrouperDAOException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateCompositeDAO as c where c.factorOwnerUuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAsOwner");
      qry.setString( "uuid", g.getUuid() );
      HibernateCompositeDAO dao = (HibernateCompositeDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) { // TODO 20070104 null or exception?
        throw new CompositeNotFoundException(E.COMP_NOTOWNER);
      }
      return CompositeDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH) ;
    }
  } // protected static CompositeDTO findAsOwner(g)

  // @since   1.2.0
  protected static CompositeDTO findByUuid(String uuid) 
    throws  CompositeNotFoundException,
            GrouperDAOException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateCompositeDAO as c where c.uuid = :uuid");
      qry.setCacheable(true);
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
  } // private static CompositeDTO findByUuid(uuid)

  // @since   1.2.0
  protected static void update(Set toAdd, Set toDelete, Set modGroups, Set modStems) 
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
  } // protected static void update(toAdd, toDelete, modGroups, modStems)


  // GETTERS //

  protected long getCreateTime() {
    return this.createTime;
  }
  protected String getCreatorUuid() {
    return this.creatorUUID;
  }
  protected String getFactorOwnerUuid() {
    return this.factorOwnerUUID;
  }
  protected String getId() {
    return this.id;
  }
  protected String getLeftFactorUuid() {
    return this.leftFactorUUID;
  }
  protected String getRightFactorUuid() {
    return this.rightFactorUUID;
  }
  protected String getType() {
    return this.type;
  }
  protected String getUuid() {
    return this.uuid;
  }

  // SETTERS //

  protected void setCreateTime(long createTime) {
    this.createTime = createTime;
  }
  protected void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  }
  protected void setFactorOwnerUuid(String factorOwnerUUID) {
    this.factorOwnerUUID = factorOwnerUUID;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setLeftFactorUuid(String leftFactorUUID) {
    this.leftFactorUUID = leftFactorUUID;
  }
  protected void setRightFactorUuid(String rightFactorUUID) {
    this.rightFactorUUID = rightFactorUUID;
  }
  protected void setType(String type) {
    this.type = type;
  }
  protected void setUuid(String uuid) {
    this.uuid = uuid;
  }

} // class HibernateCompositeDAO extends HibernateDAO 

