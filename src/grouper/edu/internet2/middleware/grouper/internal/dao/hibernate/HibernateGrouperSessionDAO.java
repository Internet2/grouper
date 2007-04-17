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
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dao.GrouperSessionDAO;
import  edu.internet2.middleware.grouper.internal.dto.GrouperSessionDTO;
import  edu.internet2.middleware.grouper.internal.util.Rosetta;
import  java.util.Date;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link GrouperSession} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateGrouperSessionDAO.java,v 1.2 2007-04-17 17:13:26 blair Exp $
 * @since   1.2.0
 */
public class HibernateGrouperSessionDAO extends HibernateDAO implements GrouperSessionDAO {

  // HIBERNATE PROPERTIES //
  private String  id;
  private String  memberUUID;
  private Date    startTime;
  private String  uuid;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public String create(GrouperSessionDTO _s)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = (HibernateDAO) Rosetta.getDAO(_s);
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
   * @since   1.2.0
   */
  public void delete(GrouperSessionDTO _s)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.delete( hs.load( HibernateGrouperSessionDAO.class, _s.getId() ) );
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
  public String getId() {
    return this.id;
  }

  /**
   * @since   1.2.0
   */
  public String getMemberUuid() {
    return this.memberUUID;
  }

  /**
   * @since   1.2.0
   */
  public Date getStartTime() {
    return this.startTime;
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
  public GrouperSessionDAO setId(String id) {
    this.id = id;
    return this;
  } 

  /**
   * @since   1.2.0
   */
  public GrouperSessionDAO  setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GrouperSessionDAO setStartTime(Date startTime) {
    this.startTime = startTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public GrouperSessionDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  // FIXME 20070416 visibility
  public static void reset(Session hs) 
    throws  HibernateException
  {
    hs.delete("from HibernateGrouperSessionDAO");
  } 

} 

