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
 * Stub Hibernate {@link Member} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateMemberDAO.java,v 1.3 2006-12-27 18:22:21 blair Exp $
 * @since   1.2.0
 */
class HibernateMemberDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateMemberDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Member create(Member m) 
    throws  MemberNotFoundException // TODO 20061221 what exception?
  {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        HibernateHelper.save(m);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
      return m;
    }
    catch (HibernateException eH) {
      // TODO 20061221 this probably shouldn't be here
      throw new MemberNotFoundException( "unable to save member: " + eH.getMessage(), eH );
    }
  } // protected static Member create(m)

  // @return  {@link Member} or <code>null</code>
  // @since   1.2.0
  protected static Member findBySubject(String id, String src, String type) {
    Member m = null;
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Member as m where "
        + "     m.subject_id      = :sid    "  
        + "and  m.subject_source  = :source "
        + "and  m.subject_type    = :type"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindBySubject");
      qry.setString( "sid",    id   );
      qry.setString( "type",   type );
      qry.setString( "source", src  );
      m = (Member) qry.uniqueResult(); // null if not found
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061220 exception?
      String msg = E.MEMBER_NEITHER_FOUND_NOR_CREATED + eH.getMessage();
      ErrorLog.error(HibernateMemberDAO.class, msg);
    }
    return m;
  } // protected static Member findBySubject(id, src, type)

  // @return  {@link Member} or <code>null</code>
  // @since   1.2.0
  protected static Member findByUuid(String uuid) {
    Member m = null;
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Member as m where m.member_id = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      m = (Member) qry.uniqueResult(); // null if not found
      hs.close();
    }
    catch (HibernateException eMNF) {
      // TODO 20061220 exception?
      ErrorLog.error( HibernateMemberDAO.class, eMNF.getMessage() );
    }
    return m;
  } // protected static Member findByUuid(uuid)

  // @since   1.2.0
  protected static void update(Member m) 
    throws  InsufficientPrivilegeException  // TODO 20061221 what exception?
  {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        hs.update(m);
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
      throw new InsufficientPrivilegeException( eH.getMessage(), eH );
    }
  } // protected static void update(m)

} // class HibernateMemberDAO

