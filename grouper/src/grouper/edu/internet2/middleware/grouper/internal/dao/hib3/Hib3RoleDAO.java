/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.internal.dao.hib3;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.RoleNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.RoleDAO;
import edu.internet2.middleware.grouper.permissions.role.Role;

/**
 * Data Access Object for role
 * @author  mchyzer
 * @version $Id: Hib3RoleDAO.java,v 1.2 2009-10-02 05:57:58 mchyzer Exp $
 */
public class Hib3RoleDAO extends Hib3DAO implements RoleDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3RoleDAO.class.getName();

  /**
   * @param id 
   * @param exceptionIfNotFound
   * retrieve by id
   * @return  role
   */
  public Role findById(String id, boolean exceptionIfNotFound) {
    
    Group group = HibernateSession.byHqlStatic()
      .createQuery("from Group as g where g.uuid = :id and g.typeOfGroupDb = 'role'")
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindById")
      .setString("id", id).uniqueResult(Group.class);
    
    if (group == null && exceptionIfNotFound) {
      throw new RoleNotFoundException("Cant find role by id: " + id);
    }

    return group;
  }

  /**
   * save or update
   * @param role 
   */
  public void saveOrUpdate(Role role) {
    HibernateSession.byObjectStatic().saveOrUpdate(role);
  }

  /**
   * @param name
   * @param exceptionIfNotFound
   * @return group
   * @throws GrouperDAOException
   * @throws RoleNotFoundException 
   * @throws GroupNotFoundException
   */
  public Role findByName(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, RoleNotFoundException {
    
    Group group = HibernateSession.byHqlStatic()
      .createQuery("select g from Group as g where (g.nameDb = :value or g.alternateNameDb = :value)" +
      		" and g.typeOfGroupDb = 'role'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindByName")
      .setString("value", name).uniqueResult(Group.class);

    //handle exceptions out of data access method...
    if (group == null && exceptionIfNotFound) {
      throw new RoleNotFoundException("Cannot find role with name: '" + name + "'");
    }
    return group;

  }

} 

