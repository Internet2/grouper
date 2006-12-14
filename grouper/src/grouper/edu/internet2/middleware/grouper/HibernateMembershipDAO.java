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
 * @version $Id: HibernateMembershipDAO.java,v 1.2 2006-12-14 18:43:41 blair Exp $
 * @since   1.2.0
 */
class HibernateMembershipDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateMembershipDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Membership find(String id) 
    throws  MembershipNotFoundException
  {
    try {
      Session     hs = HibernateHelper.getSession();
      Membership  ms = (Membership) hs.load(Membership.class, id);
      hs.close();
      return ms;
    }
    catch (HibernateException eH) {
      throw new MembershipNotFoundException( eH.getMessage(), eH );
    }
  } // protected static Membership find(id)

  // @since   1.2.0
  protected static Membership findByUuid(String uuid) 
    throws  MembershipNotFoundException 
  {
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Membership as ms where ms.uuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      Membership ms = (Membership) qry.uniqueResult();
      hs.close();
      if (ms == null) {
        throw new MembershipNotFoundException("could not find membership with uuid: " + U.q(uuid));
      }
      return ms;
    }
    catch (HibernateException eH) {
      throw new MembershipNotFoundException( eH.getMessage(), eH );
    }

  } // protected static Membership findByUuid(uuid)

  // @since   1.2.0
  protected static Set findChildMemberships(Membership ms) {
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Membership as ms where ms.parent_membership = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindChildMemberships");
      qry.setString( "uuid", ms.getUuid() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061214 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected sdtatic Set findChildMemberships(ms)

  // @since   1.2.0  
  protected static Set findMemberships(Member m, Field f) {
    Set mships = new LinkedHashSet();
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
  } // protected static Set findMemberships(m, f)

} // class HibernateMembershipDAO

