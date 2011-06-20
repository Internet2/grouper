/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITMembershipView;
import edu.internet2.middleware.subject.Source;

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
  
  /**
   * Get members by owner and field.
   * @param ownerId
   * @param fieldId
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @param sources
   * @param queryOptions
   * @return set of members
   */
  public Set<Member> findAllMembersByOwnerAndField(String ownerId, String fieldId, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo, Set<Source> sources, QueryOptions queryOptions);
  
  /**
   * Get memberships by owner, member, and field.
   * @param ownerId
   * @param memberId
   * @param fieldId
   * @param pointInTimeFrom 
   * @param pointInTimeTo 
   * @param queryOptions
   * @return set of pit memberships
   */
  public Set<PITMembershipView> findAllByOwnerAndMemberAndField(String ownerId, String memberId, String fieldId, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo, QueryOptions queryOptions);
}
