/*
 * @author mchyzer
 * $Id: RoleSetDAO.java,v 1.3 2009-09-17 22:40:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.exception.RoleSetNotFoundException;
import edu.internet2.middleware.grouper.permissions.Role;
import edu.internet2.middleware.grouper.permissions.RoleSet;

/**
 * attribute def name set, links up attributes with other attributes (probably for privs)
 */
public interface RoleSetDAO extends GrouperDAO {
  
  /**
   * delete role sets by owner, so the role can be deleted
   * @param role
   */
  public void deleteByIfHasRole(Role role);
  
  /** 
   * insert or update an attribute def name set
   * @param roleSet 
   */
  public void saveOrUpdate(RoleSet roleSet);
  
  /** 
   * delete a role set
   * @param roleSet 
   */
  public void delete(RoleSet roleSet);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the attribute def name set or null if not there
   * @throws RoleSetNotFoundException 
   */
  public RoleSet findById(String id, boolean exceptionIfNotFound)
    throws RoleSetNotFoundException;

  /**
   * find by set owner
   * @param id
   * @return the role set or null if not there
   */
  public Set<RoleSet> findByIfHasRoleId(String id);

  /**
   * find by member
   * @param id
   * @return the role set or null if not there
   */
  public Set<RoleSet> findByThenHasRoleId(String id);

  /**
   * <pre>
   * this will help with deletes.  It will find sets who have if's which match thens provided, and thens which 
   * match ifs provided.
   * 
   * So if there is this path: A -> B -> C -> D
   * And the inputs here are B and C (removing that path)
   * Then return A -> C, A -> D, B -> C, B -> D
   * 
   * </pre>
   * @param roleSetForThens
   * @param roleSetForIfs
   * @return the attribute def name set or null if not there
   */
  public Set<RoleSet> findByIfThenHasRoleId(String roleSetForThens, 
      String roleSetForIfs);

  /**
   * find by if and then (not same) with depth of 1 (immediate)
   * @param roleIdIf
   * @param roleIdThen
   * @param exceptionIfNotFound 
   * @return the roleSet
   */
  public RoleSet findByIfThenImmediate(String roleIdIf, 
      String roleIdThen, boolean exceptionIfNotFound);
  
  /**
   * get all the IF rows from rowSet about this id.  So if this is seniorLoanAdministator,
   * loanAdministrator would be returned.  Dont return the role for the id passed in
   * @param roleId
   * @return the role
   */
  public Set<Role> rolesInheritPermissionsToThis(String roleId);

  /**
   * get all the IF rows from rowSet about this id (immediate only).  So if this is seniorLoanAdministator,
   * loanAdministrator would be returned.  Dont return the role for the id passed in
   * @param roleId
   * @return the role
   */
  public Set<Role> rolesInheritPermissionsToThisImmediate(String roleId);

  /**
   * get all the THEN rows from rowSet about this id.  So if this is loanAdministrator,
   * seniorLoanAdministator would be returned.  Dont return the role for the id passed in
   * @param roleId
   * @return the role
   */
  public Set<Role> rolesInheritPermissionsFromThis(String roleId);

  /**
   * get all the THEN rows from rowSet about this id (immediate only).  So if this is loanAdministrator,
   * seniorLoanAdministator would be returned.  Dont return the role for the id passed in
   * @param roleId
   * @return the role
   */
  public Set<Role> rolesInheritPermissionsFromThisImmediate(String roleId);
}
