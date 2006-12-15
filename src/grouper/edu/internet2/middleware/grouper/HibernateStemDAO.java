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
 * Stub Hibernate {@link Stem} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateStemDAO.java,v 1.1 2006-12-15 17:30:52 blair Exp $
 * @since   1.2.0
 */
class HibernateStemDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateStemDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set findChildGroups(Stem ns) {
    Set groups = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Group as g where g.parent_stem = :id");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindChildGroups");
      qry.setString( "id", ns.getId() );
      groups.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061215 this should throw some flavor of exception
      ErrorLog.error( HibernateStemDAO.class, eH.getMessage() );
    }
    return groups;
  } // protected sdtatic Set findChildGroups(ns)

  // @since   1.2.0
  protected static Set findChildStems(Stem ns) {
    Set stems = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Stem as ns where ns.parent_stem = :id");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindChildStems");
      qry.setString( "id", ns.getId() );
      stems.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061215 this should throw some flavor of exception
      ErrorLog.error( HibernateStemDAO.class, eH.getMessage() );
    }
    return stems;
  } // protected sdtatic Set findChildStems(ns)

} // class HibernateStemDAO

