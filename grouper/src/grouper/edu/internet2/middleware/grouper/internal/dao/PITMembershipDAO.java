/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.subject.Source;

/**
 * 
 */
public interface PITMembershipDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitMembership
   */
  public void saveOrUpdate(PITMembership pitMembership);
  
  /**
   * delete
   * @param pitMembership
   */
  public void delete(PITMembership pitMembership);
  
  /**
   * @param pitMembershipId
   * @return pit membership
   */
  public PITMembership findById(String pitMembershipId);
  
  /**
   * @param oldId
   * @param newId
   */
  public void updateId(String oldId, String newId);
  
  /**
   * Delete records that ended before the given date.
   * @param time
   */
  public void deleteInactiveRecords(Timestamp time);
  
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
  public Set<PITMembership> findAllByOwnerAndMemberAndField(String ownerId, String memberId, String fieldId, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo, QueryOptions queryOptions);
}
