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
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link GrouperSession} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateGrouperSessionDAO.java,v 1.4 2007-01-04 17:17:45 blair Exp $
 * @since   1.2.0
 */
class HibernateGrouperSessionDAO {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static GrouperSession create(GrouperSession s)
    throws  SessionException // TODO 20061220 proper exception?
  {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.saveOrUpdate( s.getMember_id() );
        hs.save(s);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        String msg = E.S_START + eH.getMessage();
        ErrorLog.fatal(HibernateGrouperSessionDAO.class, msg);
        throw new SessionException(msg, eH);
      }
      finally {
        hs.close();
      }
      return s;
    }
    catch (HibernateException eH) {
      String msg = E.S_START + eH.getMessage();
      ErrorLog.fatal(HibernateGrouperSessionDAO.class, msg);
      throw new SessionException(msg, eH);
    } 
  } // protected static GrouperSession create(s)

  // @since   1.2.0 
  protected static void delete(GrouperSession s) 
    throws  SessionException // TODO 20061220 proper exception
  {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.delete(s);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        String msg = E.S_STOP + eH.getMessage();
        ErrorLog.error(HibernateGrouperSessionDAO.class, msg);
        throw new SessionException(msg, eH);
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      String msg = E.S_STOP + eH.getMessage();
      ErrorLog.error(HibernateGrouperSessionDAO.class, msg);
      throw new SessionException(msg, eH);
    }
  } // protected static void delete(s)

} // class HibernateGrouperSessionDAO

