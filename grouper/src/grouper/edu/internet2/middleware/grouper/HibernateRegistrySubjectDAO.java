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
 * @version $Id: HibernateRegistrySubjectDAO.java,v 1.12 2007-04-05 14:28:28 blair Exp $
 * @since   1.2.0
 */
class HibernateRegistrySubjectDAO extends HibernateDAO implements RegistrySubjectDAO {
  
  // PRIVATE INSTANCE VARIABLES //
  private String id;
  private String name;
  private String type;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public HibernateRegistrySubjectDAO find(String id, String type) 
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
      qry.setCacheable(false); 
      qry.setString( "id",   id   );
      qry.setString( "type", type );
      HibernateRegistrySubjectDAO subj = (HibernateRegistrySubjectDAO) qry.uniqueResult();
      hs.close();
      if (subj == null) {
        throw new SubjectNotFoundException("subject not found"); 
      }
      return subj;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // public HibernateRegistrySubjectDAO find(id, type)

  /**
   * @since   1.2.0
   */
  public String getId() {
    return this.id;
  }

  /**
   * @since   1.2.0
   */
  public String getName() {
    return this.name;
  }

  /**
   * @since   1.2.0
   */
  public String getType() {
    return this.type;
  }

  /**
   * @since   1.2.0
   */
  public HibernateRegistrySubjectDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateRegistrySubjectDAO setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateRegistrySubjectDAO setType(String type) {
    this.type = type;
    return this;
  }
   
 
  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.delete("from HibernateRegistrySubjectAttributeDAO"); // TDOO 20061018 this shouldn't be necessary
    hs.delete("from HibernateRegistrySubjectDAO");
  } // protected static void reset(hs)


} // class HibernateRegistrySubjectDAO extends HibernateDAO implements RegistrySubjectDAO 

