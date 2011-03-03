/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import edu.internet2.middleware.grouper.pit.PITPermissionAllView;

/**
 * 
 */
public interface PITPermissionAllViewDAO extends GrouperDAO {

  /**
   * @param actionSetId
   * @return set
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterActionSetAddOrDelete(String actionSetId);

  /**
   * @param roleSetId
   * @return set
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterRoleSetAddOrDelete(String roleSetId);
  
  /**
   * @param groupSetId
   * @return set
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterGroupSetAddOrDelete(String groupSetId);
  
  /**
   * @param attributeDefNameSetId
   * @return set
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterAttributeDefNameSetAddOrDelete(String attributeDefNameSetId);
  
  /**
   * @param membershipId
   * @return set
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterMembershipAddOrDelete(String membershipId);
  
  /**
   * @param attributeAssignId
   * @return set
   */
  public Set<PITPermissionAllView> findNewOrDeletedFlatPermissionsAfterAttributeAssignAddOrDelete(String attributeAssignId);
  
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
  public Set<PITPermissionAllView> findPermissions(Collection<String> attributeDefIds, Collection<String> attributeDefNameIds, 
      Collection<String> roleIds, Collection<String> actions, Collection<String> memberIds, Timestamp pointInTimeFrom, 
      Timestamp pointInTimeTo);
}