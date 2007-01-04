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
 * @version $Id: HibernateOwnerDAO.java,v 1.3 2007-01-04 17:17:45 blair Exp $
 * @since   1.2.0
 */
class HibernateOwnerDAO {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void update(Owner o) 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateHelper.getSession();
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

