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

package edu.internet2.middleware.grouper.internal.dao.hibernate;
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dao.RegistrySubjectDAO;
import  edu.internet2.middleware.grouper.internal.dto.RegistrySubjectDTO;
import  edu.internet2.middleware.subject.*;
import  net.sf.hibernate.*;

/**
 * Basic Hibernate <code>RegistrySubject</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: HibernateRegistrySubjectDAO.java,v 1.6 2007-05-31 18:52:26 blair Exp $
 * @since   1.2.0
 */
public class HibernateRegistrySubjectDAO extends HibernateDAO implements RegistrySubjectDAO {
  
  // PRIVATE INSTANCE VARIABLES //
  private String id;
  private String name;
  private String type;


  // PUBLIC INSTANCE METHODS //

  /**
   * @since   1.2.0
   */
  public String create(RegistrySubjectDTO _subj)
    throws  GrouperDAOException 
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = (HibernateDAO) _subj.getDAO();
      try {
        hs.save(dao);
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      } 
      return dao.getId();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public void delete(RegistrySubjectDTO _subj)
    throws  GrouperDAOException
  {
    try {
      Session       hs  = HibernateDAO.getSession();
      Transaction   tx  = hs.beginTransaction();
      HibernateDAO  dao = (HibernateDAO) _subj.getDAO();
      try { 
        hs.delete(dao);
        tx.commit();
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
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  }

  /**
   * @since   1.2.0
   */
  public RegistrySubjectDAO find(String id, String type) 
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
  public RegistrySubjectDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public RegistrySubjectDAO setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public RegistrySubjectDAO setType(String type) {
    this.type = type;
    return this;
  }
   
 
  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.delete("from HibernateRegistrySubjectAttributeDAO");
    hs.delete("from HibernateRegistrySubjectDAO");
  } 

} 

