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
 * @version $Id: HibernateRegistryDAO.java,v 1.11 2007-02-22 17:40:30 blair Exp $
 * @since   1.2.0
 */
class HibernateRegistryDAO {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void resetRegistry() 
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.delete("from HibernateMembershipDAO");
        hs.delete("from HibernateGrouperSessionDAO");

        hs.delete("from HibernateCompositeDAO");
        hs.delete("from HibernateAttributeDAO"); // TODO 20070207 this should not be necessary
        HibernateGroupDAO.reset(hs);
        hs.delete("from HibernateStemDAO as ns where ns.name not like '" + Stem.ROOT_INT + "'");
        HibernateMemberDAO.reset(hs);
        // TODO 20070207 what about associated fields?
        // TODO 20070207 and tuples!
        hs.delete("from HibernateGroupTypeTupleDAO");
        hs.delete(
          "from HibernateGroupTypeDAO as t where (  "
          + "     t.name != 'base'    "
          + "and  t.name != 'naming'  "
          + ")"
        );
        // TODO 20061018 Once properly mapped I can delete the explicit attr delete
        hs.delete("from HibernateSubjectAttribute");
        hs.delete("from HibernateSubject");

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
  } // protected static void resetRegistry()

} // class HibernateRegistryDAO

