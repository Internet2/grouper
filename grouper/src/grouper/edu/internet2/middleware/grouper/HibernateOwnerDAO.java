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
 * Stub Hibernate {@link Owner} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateOwnerDAO.java,v 1.5 2007-01-09 17:30:23 blair Exp $
 * @since   1.2.0
 */
class HibernateOwnerDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateOwnerDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @return  {@link Owner} or throw {@link OwnerNotFoundException}
  // @since   1.2.0
  protected static Owner findByUuid(String uuid) 
    throws  GrouperDAOException,
            OwnerNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Owner as o where o.uuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      Owner o = (Owner) qry.uniqueResult();
      hs.close();
      if (o == null) {
        throw new OwnerNotFoundException();
      } 
      return o;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // private static Owner findByUuid(uuid)

  // @since   1.2.0
  protected static void update(Owner o) 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.update(o);
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
  } // protected static void update(o)

} // class HibernateOwnerDAO

