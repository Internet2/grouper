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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.GrouperSessionDAO;

/**
 * Basic Hibernate <code>GrouperSession</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3GrouperSessionDAO.java,v 1.6 2008-07-28 20:12:27 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3GrouperSessionDAO extends Hib3DAO implements GrouperSessionDAO {

  /**
   * @since   @HEAD@
   */
  public void create(GrouperSession _s)
    throws  GrouperDAOException {
    
    HibernateSession.byObjectStatic().save(_s);
  } // public String create(_s)

  /** 
   * @since   @HEAD@
   */
  public void delete(final GrouperSession _s)
    throws  GrouperDAOException
  {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            ByObject byObject = hibernateSession.byObject();
            //CH 20080628: this used to load and delete, now it just deletes
            //if it loads, then we lose state for hooks...
            //byObject.delete( byObject.load( GrouperSession.class, _s.getId() ) );
            byObject.delete( _s );
            return null;
          }
      
    });
  } 

  // @since   @HEAD@
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    hibernateSession.byHql().createQuery("delete from GrouperSession").executeUpdate();
  } 

} 

