/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.flat.FlatMembership;


/**
 * 
 */
public interface FlatMembershipDAO extends GrouperDAO {

  /**
   * insert or update a flat membership object
   * @param flatMembership
   */
  public void saveOrUpdate(FlatMembership flatMembership);
  
  /**
   * insert a batch of flat membership objects
   * @param flatMemberships
   */
  public void saveBatch(Set<FlatMembership> flatMemberships);

  /**
   * insert or update a set of flat membership objects
   * @param flatMemberships
   */
  public void saveOrUpdate(Set<FlatMembership> flatMemberships);
  
  /**
   * delete a flat membership object
   * @param flatMembership
   */
  public void delete(FlatMembership flatMembership);
  
  /**
   * delete a set of flat membership objects
   * @param flatMemberships
   */
  public void delete(Set<FlatMembership> flatMemberships);
  
  /**
   * delete a batch of flat memberships
   * @param flatMemberships
   */
  public void deleteBatch(Set<FlatMembership> flatMemberships);
  
  /**
   * @param flatMembershipId
   * @return flat membership
   */
  public FlatMembership findById(String flatMembershipId);
  
  /**
   * @param ownerId
   * @return set of flat membership
   */
  public Set<FlatMembership> findByOwnerId(String ownerId);
  
  /**
   * @param ownerId 
   * @param memberId 
   * @param fieldId 
   * @return flat membership
   */
  public FlatMembership findByOwnerAndMemberAndField(String ownerId, String memberId, String fieldId);
  
  /**
   * @param ownerId
   * @param fieldId
   * @return set of members
   */
  public Set<Member> findMembersToAddByGroupOwnerAndField(String ownerId, String fieldId);
  
  /**
   * @param ownerId
   * @param fieldId
   * @return set of members
   */
  public Set<Member> findMembersToAddByStemOwnerAndField(String ownerId, String fieldId);
  
  /**
   * @param ownerId
   * @param fieldId
   * @return set of members
   */
  public Set<Member> findMembersToAddByAttrDefOwnerAndField(String ownerId, String fieldId);
  
  /**
   * @param ownerId
   * @param fieldId
   * @return set of flat memberships that should be deleted
   */
  public Set<FlatMembership> findMembersToDeleteByGroupOwnerAndField(String ownerId, String fieldId);
  
  /**
   * @param ownerId
   * @param fieldId
   * @return set of flat memberships that should be deleted
   */
  public Set<FlatMembership> findMembersToDeleteByStemOwnerAndField(String ownerId, String fieldId);
  
  /**
   * @param ownerId
   * @param fieldId
   * @return set of flat memberships that should be deleted
   */
  public Set<FlatMembership> findMembersToDeleteByAttrDefOwnerAndField(String ownerId, String fieldId);
  
  /**
   * @param memberId
   * @return set of flat memberships
   */
  public Set<FlatMembership> findByMemberId(String memberId);
  
  /**
   * find missing flat memberships
   * @param page
   * @param batchSize
   * @return set of memberships that need flat memberships
   */
  public Set<Membership> findMissingFlatMemberships(int page, int batchSize);
  
  /**
   * find missing flat memberships count
   * @return long
   */
  public long findMissingFlatMembershipsCount();
  
  /**
   * remove bad flat memberships
   * @return set of flat memberships that should be removed
   */
  public Set<FlatMembership> findBadFlatMemberships();
}
