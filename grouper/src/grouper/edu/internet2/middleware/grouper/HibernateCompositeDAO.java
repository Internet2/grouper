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
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link Composite} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateCompositeDAO.java,v 1.4 2006-12-27 18:22:21 blair Exp $
 * @since   1.2.0
 */
class HibernateCompositeDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateCompositeDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set findAsFactor(Owner o) {
    Set composites = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
          "from Composite as c where (" 
        + " c.left = :left or c.right = :right "
        + ")"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAsFactor");
      qry.setParameter( "left",  o );
      qry.setParameter( "right", o );
      composites.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) { 
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateCompositeDAO.class, eH.getMessage() );
    }
    return composites;
  } // protected static Set findAsFactor(o)

  // @since   1.2.0
  protected static Composite findAsOwner(Owner o) 
    throws  CompositeNotFoundException
  {
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Composite as c where c.owner = :owner");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAsOwner");
      qry.setParameter("owner", o);
      Composite c = (Composite) qry.uniqueResult();
      hs.close();
      if (c == null) {
        throw new CompositeNotFoundException(E.COMP_NOTOWNER);
      }
      return c;
    }
    catch (HibernateException eH) {
      throw new CompositeNotFoundException(eH.getMessage(), eH);
    }
  } // protected static Composite findAsOwner(o)

  // @since   1.2.0
  protected static void update(Set toAdd, Set toDelete) {
    try {
      Session     hs  = HibernateHelper.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = toDelete.iterator();
        while (it.hasNext()) {
          hs.delete( it.next() );
        } 
        it = toAdd.iterator();
        while (it.hasNext()) {
          hs.save( it.next() );
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        // TODO 20061221 shouldn't we really do more than just log an error message here?
        String msg = E.COMP_UPDATE + eH.getMessage();
        ErrorLog.error(HibernateCompositeDAO.class, msg);
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      // TODO 20061221 shouldn't we really do more than just log an error message here?
      String msg = E.COMP_UPDATE + eH.getMessage();
      ErrorLog.error(HibernateCompositeDAO.class, msg);
    }
  } // protected static void update(toAdd, toDelete)

} // class HibernateCompositeDAO

