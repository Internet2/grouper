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
import  java.util.LinkedHashSet;
import  java.util.Set;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link Membership} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateMembershipDAO.java,v 1.1 2006-12-14 16:22:05 blair Exp $
 * @since   1.2.0
 */
class HibernateMembershipDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateMembershipDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0  
  protected static Set findMemberships(GrouperSession s, Member m, Field f) {
    GrouperSessionValidator.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindMemberships");
      qry.setParameter( "member", m                      );
      qry.setString(    "fname" , f.getName()            );
      qry.setString(    "ftype" , f.getType().toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061214 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findMemberships(s, m, f)

} // class HibernateMembershipDAO

