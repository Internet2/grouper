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
import  java.util.Date;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link GrouperSession} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateGrouperSessionDAO.java,v 1.8 2007-02-14 17:06:28 blair Exp $
 * @since   1.2.0
 */
class HibernateGrouperSessionDAO extends HibernateDAO {

  // HIBERNATE PROPERTIES //
  private String  id;
  private String  memberUUID;
  private String  sessionUUID;
  private Date    startTime;


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static String create(GrouperSessionDTO dto)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = Rosetta.getDAO(dto);
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
  } // protected static String create(dto)

  // @since   1.2.0 
  protected static void delete(GrouperSessionDTO dto)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.delete( hs.load( HibernateGrouperSessionDAO.class, dto.getId() ) );
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
      String msg = E.S_STOP + eH.getMessage();
      ErrorLog.fatal(HibernateGrouperSessionDAO.class, msg);
      throw new GrouperDAOException(msg, eH);
    }
  } // protected static void delete(dto)


  // GETTERS //
  protected String getId() {
    return this.id;
  }
  protected String getMemberUuid() {
    return this.memberUUID;
  }
  protected String getSessionUuid() {
    return this.sessionUUID;
  }
  protected Date getStartTime() {
    return this.startTime;
  }


  // SETTERS //
  protected void setId(String id) {
    this.id = id;
  }
  protected void setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
  }
  protected void setSessionUuid(String sessionUUID) {
    this.sessionUUID = sessionUUID;
  }
  protected void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

} // class HibernateGrouperSessionDAO extends HibernateDAO

