/**
 * @author shilen
 * $Id: GroupSetDAO.java,v 1.1 2009-06-09 22:55:39 shilen Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.group.GroupSet;


/**
 * 
 */
public interface GroupSetDAO extends GrouperDAO {

  /**
   * insert a group set object
   * @param groupSet
   */
  public void save(GroupSet groupSet);
  
  /**
   * delete a group set object
   * @param groupId
   * @param fieldId 
   */
  public void deleteByOwnerGroupAndField(String groupId, String fieldId);

  /**
   * delete a group set object
   * @param stemId
   */
  public void deleteByOwnerStem(String stemId);

  /**
   * @param group
   * @param field
   * @return group set
   */
  public Set<GroupSet> findAllByGroupOwnerAndField(Group group, Field field);

  /**
   * @param groupSet
   * @return the parent group set
   */
  public GroupSet findParentGroupSet(GroupSet groupSet);

  /**
   * @param group
   * @return all the group sets where this group is a member and where depth > 0.
   */
  public Set<GroupSet> findAllByMemberGroup(Group group);

  /**
   * @param groupSetId
   * @return group set
   */
  public GroupSet findById(String groupSetId);

  /**
   * @param groupSet
   * @return all nested children of the group set
   */
  public Set<GroupSet> findAllChildren(GroupSet groupSet);

  /**
   * @param parentGroupSet
   * @param memberGroupId
   * @return group set
   */
  public GroupSet findImmediateChildByParentAndMemberGroup(GroupSet parentGroupSet,
      String memberGroupId);

  /**
   * @param group
   * @param field 
   * @return group set
   */
  public GroupSet findSelfGroup(Group group, Field field);

  /**
   * @param stem
   * @param field 
   * @return group set
   */
  public GroupSet findSelfStem(Stem stem, Field field);

  /**
   * @param group
   */
  public void deleteByOwnerGroup(Group group);

  /**
   * @param memberId
   * @param field
   * @return set of group sets
   */
  public Set<GroupSet> findAllByMemberGroupAndField(String memberId, Field field);

  /**
   * Returns an immediate group set (having depth = 1 and mship_type = effective)
   * @param ownerGroupId
   * @param memberGroupId
   * @param field
   * @return group set
   */
  public GroupSet findImmediateByOwnerGroupAndMemberGroupAndField(String ownerGroupId,
      String memberGroupId, Field field);

  /**
   * Returns an immediate group set (having depth = 1 and mship_type = effective)
   * @param ownerStemId
   * @param memberGroupId
   * @param field
   * @return group set
   */
  public GroupSet findImmediateByOwnerStemAndMemberGroupAndField(String ownerStemId,
      String memberGroupId, Field field);
}
