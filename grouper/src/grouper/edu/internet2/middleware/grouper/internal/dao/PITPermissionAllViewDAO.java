/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.permissions.PermissionEntry;

/**
 * 
 */
public interface PITPermissionAllViewDAO extends GrouperDAO {

  /**
   * @param attributeDefIds
   * @param attributeDefNameIds
   * @param roleIds
   * @param actions
   * @param memberIds
   * @param pointInTimeFrom
   * @param pointInTimeTo
   * @return set
   */
  public Set<PermissionEntry> findPermissions(Collection<String> attributeDefIds, Collection<String> attributeDefNameIds, 
      Collection<String> roleIds, Collection<String> actions, Collection<String> memberIds, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo);
}