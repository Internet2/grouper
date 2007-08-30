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
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dao.GrouperSessionDAO;
import  edu.internet2.middleware.grouper.internal.dto.GrouperSessionDTO;
import  edu.internet2.middleware.grouper.internal.util.Rosetta;
import  org.hibernate.*;

/**
 * Basic Hibernate <code>GrouperSession</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3GrouperSessionDAO.java,v 1.1 2007-08-30 15:52:22 blair Exp $
 * @since   @HEAD@
 */
public class Hib3GrouperSessionDAO extends Hib3DAO implements GrouperSessionDAO {

  // HIBERNATE PROPERTIES //
  private String  id;
  private String  memberUUID;
  private long    startTime;
  private String  uuid;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   @HEAD@
   */
  public String create(GrouperSessionDTO _s)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = Hib3DAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      Hib3DAO  dao = (Hib3DAO) Rosetta.getDAO(_s);
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
  } // public String create(_s)

  /** 
   * @since   @HEAD@
   */
  public void delete(GrouperSessionDTO _s)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = Hib3DAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.delete( hs.load( Hib3GrouperSessionDAO.class, _s.getId() ) );
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
  public String getId() {
    return this.id;
  }

  /**
   * @since   @HEAD@
   */
  public String getMemberUuid() {
    return this.memberUUID;
  }

  /**
   * @since   @HEAD@
   */
  public long getStartTime() {
    return this.startTime;
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
  public GrouperSessionDAO setId(String id) {
    this.id = id;
    return this;
  } 

  /**
   * @since   @HEAD@
   */
  public GrouperSessionDAO  setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GrouperSessionDAO setStartTime(long startTime) {
    this.startTime = startTime;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public GrouperSessionDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.createQuery("delete from Hib3GrouperSessionDAO").executeUpdate();
  } 

} 

