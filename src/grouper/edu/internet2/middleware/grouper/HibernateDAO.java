/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
 * Stub Hibernate DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateDAO.java,v 1.1 2006-12-21 20:07:31 blair Exp $
 * @since   1.2.0
 */
class HibernateDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Object create(Object obj) 
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.save(obj);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      } 
      return obj;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static Object create(obj)

  // @since   1.2.0
  protected static void delete(Object obj) 
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.delete(obj);
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
  } // protected static void delete(obj)

  // @since   1.2.0
  protected static void update(Object obj) 
    throws  GrouperDAOException 
  {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.update(obj);
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
  } // protected static void update(obj)

} // class HibernateDAO

