/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITMembershipView;

/**
 * 
 */
public interface PITMembershipViewDAO extends GrouperDAO {


  /**
   * @param pitMembership
   * @return set
   */
  public Set<PITGroupSet> findPITGroupSetsJoinedWithNewPITMembership(PITMembership pitMembership);
  
  /**
   * @param pitGroupSet
   * @return set
   */
  public Set<PITMembership> findPITMembershipsJoinedWithNewPITGroupSet(PITGroupSet pitGroupSet);
  
  /**
   * @param pitMembership
   * @return set
   */
  public Set<PITGroupSet> findPITGroupSetsJoinedWithOldPITMembership(PITMembership pitMembership);
  
  /**
   * @param pitGroupSet
   * @return set
   */
  public Set<PITMembership> findPITMembershipsJoinedWithOldPITGroupSet(PITGroupSet pitGroupSet);
  
  /**
   * @param ownerId
   * @param memberId
   * @param fieldId
   * @param activeOnly
   * @return set
   */
  public Set<PITMembershipView> findByOwnerAndMemberAndField(String ownerId, String memberId, String fieldId, boolean activeOnly);
}
