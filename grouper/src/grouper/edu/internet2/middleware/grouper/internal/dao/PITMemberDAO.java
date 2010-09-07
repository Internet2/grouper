/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.pit.PITMember;

/**
 * 
 */
public interface PITMemberDAO extends GrouperDAO {

  /**
   * insert or update
   * @param pitMember
   */
  public void saveOrUpdate(PITMember pitMember);
  
  /**
   * insert in batch
   * @param pitMembers
   */
  public void saveBatch(Set<PITMember> pitMembers);
  
  /**
   * delete
   * @param pitMember
   */
  public void delete(PITMember pitMember);
  
  /**
   * @param pitMemberId
   * @return pit member
   */
  public PITMember findById(String pitMemberId);
}
