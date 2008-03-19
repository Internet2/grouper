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
import org.hibernate.HibernateException;
import org.hibernate.Session;

import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.GrouperSessionDAO;
import edu.internet2.middleware.grouper.internal.dto.GrouperSessionDTO;
import edu.internet2.middleware.grouper.internal.util.Rosetta;

/**
 * Basic Hibernate <code>GrouperSession</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3GrouperSessionDAO.java,v 1.2.2.1 2008-03-19 18:46:11 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3GrouperSessionDAO extends Hib3HibernateVersioned implements GrouperSessionDAO {

  private String  memberUUID;
  private long    startTime;
  private String  uuid;
  /**
   * @since   @HEAD@
   */
  public long create(GrouperSessionDTO _s)
    throws  GrouperDAOException {
    
    Hib3DAO  dao = (Hib3DAO) Rosetta.getDAO(_s);
    HibernateSession.byObjectStatic().save(dao);
    return ((GrouperSessionDAO)dao).getHibernateVersion();
  } // public String create(_s)

  /** 
   * @since   @HEAD@
   */
  public void delete(final GrouperSessionDTO _s)
    throws  GrouperDAOException
  {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session     hs  = hibernateSession.getSession();
            hs.delete( hs.load( Hib3GrouperSessionDAO.class, _s.getUuid() ) );
            return null;
          }
      
    });
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

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.hib3.Hib3DAO#getId()
   */
  @Override
  protected String getId() {
    return this.uuid;
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.hib3.Hib3HibernateVersioned#setHibernateVersion(long)
   */
  @Override
  public Hib3GrouperSessionDAO setHibernateVersion(long hibernateVersion) {
    return (Hib3GrouperSessionDAO)super.setHibernateVersion(hibernateVersion);
  }
} 

