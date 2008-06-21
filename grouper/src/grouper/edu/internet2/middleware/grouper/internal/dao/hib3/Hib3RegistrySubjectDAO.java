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

package edu.internet2.middleware.grouper.internal.dao.hib3;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.RegistrySubjectDAO;
import edu.internet2.middleware.grouper.internal.dto.RegistrySubjectDTO;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Basic Hibernate <code>RegistrySubject</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3RegistrySubjectDAO.java,v 1.3 2008-06-21 04:16:12 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3RegistrySubjectDAO extends Hib3DAO implements RegistrySubjectDAO {
  
  /**
   * @since   @HEAD@
   */
  public void create(RegistrySubjectDTO _subj)
    throws  GrouperDAOException {
    HibernateSession.byObjectStatic().save(_subj);
  } 

  /**
   * @since   @HEAD@
   */
  public void delete(RegistrySubjectDTO _subj)
    throws  GrouperDAOException {
    HibernateSession.byObjectStatic().delete(_subj);
  }

  /**
   * @since   @HEAD@
   */
  public RegistrySubjectDTO find(String id, String type) 
    throws  GrouperDAOException,
            SubjectNotFoundException
  {
    RegistrySubjectDTO subj = HibernateSession.byHqlStatic()
      .createQuery(
        "from RegistrySubjectDTO as rs where " 
        + "     rs.id   = :id    "
        + " and rs.type = :type  ")
      .setCacheable(false) 
      .setString( "id",   id   )
      .setString( "type", type )
      .uniqueResult(RegistrySubjectDTO.class);
    if (subj == null) {
      throw new SubjectNotFoundException("subject not found"); 
    }
    return subj;
  } // public Hib3RegistrySubjectDAO find(id, type)

  // @since   @HEAD@
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    hibernateSession.byHql().createQuery("delete from RegistrySubjectAttributeDTO").executeUpdate();
    hibernateSession.byHql().createQuery("delete from RegistrySubjectDTO").executeUpdate();
  } 

} 

