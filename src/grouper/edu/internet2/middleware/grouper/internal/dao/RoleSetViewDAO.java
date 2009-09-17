/*
 * @author mchyzer
 * $Id: RoleSetViewDAO.java,v 1.1 2009-09-17 04:19:15 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.permissions.RoleSetView;

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
