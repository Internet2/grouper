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
 * Stub Hibernate {@link Field} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateFieldDAO.java,v 1.1 2006-12-20 15:08:22 blair Exp $
 * @since   1.2.0
 */
class HibernateFieldDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateFieldDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set findAll() 
    throws  GrouperRuntimeException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery("from Field order by field_name asc");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAll");
      fields.addAll( qry.list() );
      hs.close();  
    }
    catch (HibernateException eH) {
      String msg = E.FIELD_FINDALL + eH.getMessage();
      ErrorLog.fatal(FieldFinder.class, msg);
      throw new GrouperRuntimeException(msg, eH);
    }
    return fields;
  } // protected Static Set findAll()

  // @since   1.2.0
  protected static Set findAllByType(FieldType type) 
    throws  SchemaException
  {
    Set fields = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Field where field_type = :type order by field_name asc"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByType");
      qry.setString( "type", type.toString() );
      fields.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      String msg = E.FIELD_FINDTYPE + eH.getMessage();
      ErrorLog.error(FieldFinder.class, msg);
      throw new SchemaException(msg, eH);
    }
    return fields;
  } // protected static Set fieldAllByType(type)

} // class HibernateFieldDAO

