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
import  edu.internet2.middleware.subject.*;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link HibernateSubject} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateRegistrySubjectDAO.java,v 1.7 2007-01-04 19:24:09 blair Exp $
 * @since   1.2.0
 */
class HibernateRegistrySubjectDAO {

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static HibernateSubject create(HibernateSubject subj)
    throws  GrouperDAOException
  {
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.save(subj);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return subj;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static HibernateSubject add(id, type, name)

  // @since   1.2.0
  protected static HibernateSubject find(String id, String type) 
    throws  GrouperDAOException,
            SubjectNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateSubject as hs where " 
        + "     hs.subjectId      = :id    "
        + " and hs.subjectTypeId  = :type  "
      );
      qry.setCacheable(true); // This was set to false but I'm not sure why
      qry.setString( "id",   id   );
      qry.setString( "type", type );
      HibernateSubject subj = (HibernateSubject) qry.uniqueResult();
      hs.close();
      if (subj == null) {
        throw new SubjectNotFoundException("subject not found"); 
      }
      return subj;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static HibernateSubject find(id, type)

} // class HibernateRegistrySubjectDAO

