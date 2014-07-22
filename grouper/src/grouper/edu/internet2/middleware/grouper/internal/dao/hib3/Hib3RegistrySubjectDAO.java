/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import edu.internet2.middleware.grouper.RegistrySubject;
import edu.internet2.middleware.grouper.RegistrySubjectAttribute;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.RegistrySubjectDAO;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Basic Hibernate <code>RegistrySubject</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3RegistrySubjectDAO.java,v 1.8 2009-10-26 02:26:07 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3RegistrySubjectDAO extends Hib3DAO implements RegistrySubjectDAO {
  
  /**
   * @since   @HEAD@
   */
  public void create(RegistrySubject _subj)
    throws  GrouperDAOException {
    HibernateSession.byObjectStatic().save(_subj);
    
    for (String key : GrouperUtil.nonNull(_subj.getAttributes()).keySet()) {
      RegistrySubjectAttribute registrySubjectAttribute = new RegistrySubjectAttribute();
      registrySubjectAttribute.setName(key);
      String value = _subj.getAttributeValue(key);
      registrySubjectAttribute.setValue(value);
      registrySubjectAttribute.setSearchValue(value == null ? null : value.toLowerCase());
      registrySubjectAttribute.setSubjectId(_subj.getId());
      HibernateSession.byObjectStatic().save(registrySubjectAttribute);    
    }
    
  } 

  /**
   * @since   @HEAD@
   */
  public void delete(RegistrySubject _subj)
    throws  GrouperDAOException {
    HibernateSession.byObjectStatic().delete(_subj);
  }

  /**
   * @since
   * @deprecated
   */
  @Deprecated
  public RegistrySubject find(String id, String type) 
    throws  GrouperDAOException,
            SubjectNotFoundException {
    return find(id, type, true);
  }

  /**
   * @since
   */
  public RegistrySubject find(String id, String type, boolean exceptionIfNotFound) 
    throws  GrouperDAOException,
            SubjectNotFoundException {
    RegistrySubject subj = HibernateSession.byHqlStatic()
      .createQuery(
        "from RegistrySubject as rs where " 
        + "     rs.id   = :id    "
        + " and rs.typeString = :type  ")
      .setCacheable(false) 
      .setString( "id",   id   )
      .setString( "type", type )
      .uniqueResult(RegistrySubject.class);
    if (subj == null && exceptionIfNotFound) {
      throw new SubjectNotFoundException("subject not found"); 
    }
    return subj;
  }

  // @since   @HEAD@
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    hibernateSession.byHql().createQuery("delete from RegistrySubjectAttribute").executeUpdate();
    hibernateSession.byHql().createQuery("delete from RegistrySubject").executeUpdate();
  } 

} 

