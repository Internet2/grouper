/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

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
}