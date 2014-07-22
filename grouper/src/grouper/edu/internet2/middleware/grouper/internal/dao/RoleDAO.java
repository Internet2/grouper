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
 * @author mchyzer
 * $Id: RoleDAO.java,v 1.2 2009-10-02 05:57:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.exception.RoleNotFoundException;
import edu.internet2.middleware.grouper.permissions.role.Role;

/**
 * role data access methods
 */
public interface RoleDAO extends GrouperDAO {
  
  /** 
   * insert or update a role object 
   * @param role
   */
  public void saveOrUpdate(Role role);
  
  /**
   * @param id
   * @param exceptionIfNotFound
   * @return the role or null if not there
   */
  public Role findById(String id, boolean exceptionIfNotFound);
  
  /**
   * find an attribute def name by name
   * @param name 
   * @param exceptionIfNotFound 
   * @return  name
   * @throws GrouperDAOException 
   * @throws RoleNotFoundException 
   */
  public Role findByName(String name, boolean exceptionIfNotFound) 
    throws GrouperDAOException, RoleNotFoundException;

}
