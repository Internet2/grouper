/*
 * @author mchyzer
 * $Id: RoleSetViewDAO.java,v 1.2 2009-10-02 05:57:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.permissions.role.RoleSetView;

/**
 * attribute def name set views, links up attributes with other attributes (probably for privs)
 */
public interface RoleSetViewDAO extends GrouperDAO {
  
  /**
   * find all attribute def name set views by related attribute def names (generally this is for testing)
   * @param roleNames
   * @return the attr def name set views
   */
  public Set<RoleSetView> findByRoleSetViews(Set<String> roleNames);
  
}
