package edu.internet2.middleware.signet;

import java.util.Set;

/**
 * This interface represents a single Permission held by a single
 * PrivilegedSubject, along with its limits.
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
   * and/or a set of classrooms, and/or some "scope" expressed as a Limit
   * object. Note that a Privilege may have one or zero Scopes, but no other
   * number is allowed. Also note that Conditions are never included in a
   * Privilege.
   * 
   * @return the LimitValues associated with this Privilege.
   */
  Set getLimitValues();
}
