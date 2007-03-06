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
 * Stub Hibernate {@link RegistrySubject} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateRegistrySubjectDAO.java,v 1.9 2007-03-06 17:02:42 blair Exp $
 * @since   1.2.0
 */
class HibernateRegistrySubjectDAO extends HibernateDAO {
  
  // PRIVATE INSTANCE VARIABLES //
  private String id;
  private String name;
  private String type;


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static HibernateRegistrySubjectDAO find(String id, String type) 
    throws  GrouperDAOException,
            SubjectNotFoundException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateRegistrySubjectDAO as rs where " 
        + "     rs.id   = :id    "
        + " and rs.type = :type  "
      );
      qry.setCacheable(true); // This was set to false but I'm not sure why
      qry.setString( "id",   id   );
      qry.setString( "type", type );
      HibernateRegistrySubjectDAO subj = (HibernateRegistrySubjectDAO) qry.uniqueResult();
      hs.close();
      if (subj == null) { // TODO 20070108 null or ex?
        throw new SubjectNotFoundException("subject not found"); 
      }
      return subj;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static HibernateRegistrySubjectDAO find(id, type)

  // @since   1.2.0
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.delete("from HibernateRegistrySubjectAttributeDAO"); // TDOO 20061018 this shouldn't be necessary
    hs.delete("from HibernateRegistrySubjectDAO");
  } // protected static void reset(hs)


  // GETTERS //

  protected String getId() {
    return this.id;
  }
  protected String getName() {
    return this.name;
  }
  protected String getType() {
    return this.type;
  }


  // SETTERS //

  protected void setId(String id) {
    this.id = id;
  }
  protected void setName(String name) {
    this.name = name;
  }
  protected void setType(String type) {
    this.type = type;
  }
    
} // class HibernateRegistrySubjectDAO

