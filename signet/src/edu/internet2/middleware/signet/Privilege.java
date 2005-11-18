package edu.internet2.middleware.signet;

import java.util.Set;

/**
 * This interface represents a single {@link Permission} held by a single
 * {@link PrivilegedSubject}, along with its {@link Limit}s.
 */

public interface Privilege
{
  /**
   * Get the Permission that lies at the core of this Privilege.
   * @return the Permission that lies at the core of this Privilege.
   */
  Permission getPermission();
  
  /**
   * Get the LimitValues associated with this Privilege. Limits affect the
   * extent to which this Privilege can be exercised, e.g. a dollar amount
   * and/or a set of classrooms.
   * 
   * Note that Conditions are never included in a Privilege.
   * 
   * @return the LimitValues associated with this Privilege.
   */
  Set getLimitValues();
  
  /**
   * Gets the scope (usually an organization) of this Privilege.
   * 
   * @return the scope (usually an organization) of this Privilege.
   */
  public edu.internet2.middleware.signet.tree.TreeNode getScope();
}
