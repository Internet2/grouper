/**
 * Copyright 2014 Internet2
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
 * @author mchyzer
 * $Id: Hib3RegistrySubjectAttributeDAO.java,v 1.3 2008-07-21 18:05:44 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Set;

import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.RegistrySubjectAttribute;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.RegistrySubjectAttributeDAO;


/**
 * marker class for hbm loading
 */
public class Hib3RegistrySubjectAttributeDAO extends Hib3DAO implements RegistrySubjectAttributeDAO {

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RegistrySubjectAttributeDAO#create(edu.internet2.middleware.grouper.RegistrySubjectAttribute)
   */
  public void create(RegistrySubjectAttribute _subjAttribute) {
    HibernateSession.byObjectStatic().save(_subjAttribute);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RegistrySubjectAttributeDAO#delete(edu.internet2.middleware.grouper.RegistrySubjectAttribute)
   */
  public void delete(RegistrySubjectAttribute _subjAttr) {
    HibernateSession.byObjectStatic().delete(_subjAttr);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RegistrySubjectAttributeDAO#find(java.lang.String, java.lang.String, boolean)
   */
  public RegistrySubjectAttribute find(String subjectId, String attributeName, boolean exceptionIfNotFound) {
    RegistrySubjectAttribute subjAttribute = HibernateSession.byHqlStatic()
        .createQuery(
          "from RegistrySubjectAttribute as rsa where " 
          + "     rsa.subjectId   = :subjectId and rsa.name = :attributeName   ")
        .setCacheable(false) 
        .setString( "subjectId",   subjectId   )
        .setString( "attributeName",   attributeName   )
        .uniqueResult(RegistrySubjectAttribute.class);
    if (subjAttribute == null && exceptionIfNotFound) {
      throw new RuntimeException("subject attribute not found"); 
    }
    return subjAttribute;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RegistrySubjectAttributeDAO#findByRegistrySubjectId(java.lang.String)
   */
  public Set<RegistrySubjectAttribute> findByRegistrySubjectId(String subjectId) {
    Set<RegistrySubjectAttribute> subjAttributes = HibernateSession.byHqlStatic()
        .createQuery(
          "from RegistrySubjectAttribute as rsa where " 
          + "     rsa.subjectId   = :subjectId    ")
        .setCacheable(false) 
        .setString( "subjectId",   subjectId   ).listSet(RegistrySubjectAttribute.class);
    
    return subjAttributes;
  }

  // @since   @HEAD@
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    hibernateSession.byHql().createQuery("delete from RegistrySubjectAttribute").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.RegistrySubjectAttributeDAO#update(edu.internet2.middleware.grouper.RegistrySubjectAttribute)
   */
  public void update(RegistrySubjectAttribute _subjAttr) {
    HibernateSession.byObjectStatic().update(_subjAttr);
  } 


}
