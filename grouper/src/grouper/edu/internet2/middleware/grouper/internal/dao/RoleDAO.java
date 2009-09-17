/*
 * @author mchyzer
 * $Id: RoleDAO.java,v 1.1 2009-09-17 04:19:15 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.exception.RoleNotFoundException;
import edu.internet2.middleware.grouper.permissions.Role;

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
