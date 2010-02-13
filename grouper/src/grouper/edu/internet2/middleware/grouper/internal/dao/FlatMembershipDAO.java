/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.flat.FlatMembership;


/**
 * 
 */
public interface FlatMembershipDAO extends GrouperDAO {

  /**
   * insert a flat membership object
   * @param flatMembership
   */
  public void save(FlatMembership flatMembership);

  /**
   * insert a set of flat membership objects
   * @param flatMemberships
   */
  public void save(Set<FlatMembership> flatMemberships);
  
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
   * @param flatMembershipId
   * @return flat membership
   */
  public FlatMembership findById(String flatMembershipId);
  
  /**
   * @param ownerId 
   * @param memberId 
   * @param fieldId 
   * @return flat membership
   */
  public FlatMembership findByOwnerAndMemberAndField(String ownerId, String memberId, String fieldId);
}
