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
 * Stub Hibernate {@link GroupType} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateGroupTypeDAO.java,v 1.1 2006-12-19 19:16:41 blair Exp $
 * @since   1.2.0
 */
class HibernateGroupTypeDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateGroupTypeDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set findAll() 
    throws  GrouperRuntimeException
  {
    Set types = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from GroupType order by name asc");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAll");
      types.addAll( qry.list() );
      hs.close();  
    }
    catch (HibernateException eH) {
      throw new GrouperRuntimeException( eH.getMessage(), eH ); // TODO 20061219 throw something else
    }
    return types;
  } // protected static Set findAll()

} // class HibernateGroupTypeDAO

