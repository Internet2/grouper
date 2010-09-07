/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

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
}
