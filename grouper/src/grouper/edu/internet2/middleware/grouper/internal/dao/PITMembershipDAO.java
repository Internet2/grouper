/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.pit.PITMembership;

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
   * insert or update
   * @param pitMemberships
   */
  public void saveOrUpdate(Set<PITMembership> pitMemberships);
  
  /**
   * delete
   * @param pitMembership
   */
  public void delete(PITMembership pitMembership);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITMembership
   */
  public PITMembership findBySourceIdActive(String id, boolean exceptionIfNotFound);

  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITMembership
   */
  public PITMembership findById(String id, boolean exceptionIfNotFound);

  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return set of PITMembership
   */
  public Set<PITMembership> findBySourceId(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITMembership
   */
  public PITMembership findBySourceIdUnique(String id, boolean exceptionIfNotFound);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return PITMembership
   */
  public PITMembership findBySourceIdMostRecent(String id, boolean exceptionIfNotFound);
  
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
   * Get memberships by owner.
   * @param ownerId
   * @return set of pit memberships
   */
  public Set<PITMembership> findAllByOwner(String ownerId);
  
  /**
   * Get memberships by member.
   * @param memberId
   * @return set of pit memberships
   */
  public Set<PITMembership> findAllByMember(String memberId);
  
  /**
   * @return active memberships that are missing in point in time
   */
  public Set<Membership> findMissingActivePITMemberships();
  
  /**
   * @return active point in time memberships that should be inactive
   */
  public Set<PITMembership> findMissingInactivePITMemberships();
}
