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
 * Stub Hibernate {@link Registry} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateRegistryDAO.java,v 1.15 2007-04-05 14:28:28 blair Exp $
 * @since   1.2.0
 */
class HibernateRegistryDAO implements RegistryDAO {

  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public void reset() 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        HibernateMembershipDAO.reset(hs);
        HibernateGrouperSessionDAO.reset(hs);
        HibernateCompositeDAO.reset(hs);
        HibernateGroupDAO.reset(hs);
        HibernateStemDAO.reset(hs);
        HibernateMemberDAO.reset(hs);
        HibernateGroupTypeDAO.reset(hs);
        HibernateRegistrySubjectDAO.reset(hs);
        tx.commit();
      }
      catch (HibernateException eH) {
        eH.printStackTrace();
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

} // class HibernateRegistryDAO implements RegistryDAO

