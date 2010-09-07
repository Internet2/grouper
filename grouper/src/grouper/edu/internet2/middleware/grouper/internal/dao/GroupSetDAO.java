/**
 * @author shilen
 * $Id: GroupSetDAO.java,v 1.7 2009-10-20 14:55:50 shilen Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
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
   * insert a set of group set objects
   * @param groupSets
   */
  public void save(Set<GroupSet> groupSets);
  
  /**
   * insert a batch of group set objects
   * @param groupSets
   */
  public void saveBatch(Set<GroupSet> groupSets);

  /**
   * delete a group set object
   * @param groupSet
   */
  public void delete(GroupSet groupSet);
  
  /**
   * delete a set of group set objects
   * @param groupSets
   */
  public void delete(Set<GroupSet> groupSets);
  
  /**
   * update a group set object
   * @param groupSet
   */
  public void update(GroupSet groupSet);
  
  /**
   * update a set of group set objects
   * @param groupSets
   */
  public void update(Set<GroupSet> groupSets);
  
  /**
   * delete a group set object
   * @param groupId
   * @param fieldId 
   */
  public void deleteSelfByOwnerGroupAndField(String groupId, String fieldId);

  /**
   * delete a group set object
   * @param stemId
   */
  public void deleteSelfByOwnerStem(String stemId);

  /**
   * delete a group set object
   * @param attrDefId
   */
  public void deleteSelfByOwnerAttrDef(String attrDefId);

  /**
   * @param groupId
   * @param field
   * @return group set
   */
  public Set<GroupSet> findAllByGroupOwnerAndField(String groupId, Field field);

  /**
   * @param groupSet
   * @return the parent group set
   */
  public GroupSet findParentGroupSet(GroupSet groupSet);

  /**
   * @param groupId
   * @return all the group sets where this group is a member and where depth > 0.
   */
  public Set<GroupSet> findAllByMemberGroup(String groupId);

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
   * @param groupId
   * @param fieldId
   * @return group set
   */
  public GroupSet findSelfGroup(String groupId, String fieldId);

  /**
   * @param stemId
   * @param fieldId
   * @return group set
   */
  public GroupSet findSelfStem(String stemId, String fieldId);

  /**
   * @param attrDefId
   * @param fieldId
   * @return group set
   */
  public GroupSet findSelfAttrDef(String attrDefId, String fieldId);

  /**
   * @param group
   */
  public void deleteSelfByOwnerGroup(Group group);

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
  
  /**
   * Returns an immediate group set (having depth = 1 and mship_type = effective)
   * @param ownerAttrDefId
   * @param memberGroupId
   * @param field
   * @return group set
   */
  public GroupSet findImmediateByOwnerAttrDefAndMemberGroupAndField(String ownerAttrDefId,
      String memberGroupId, Field field);
  
  /**
   * Returns all GroupSets with the given creator.
   * @param member
   * @return set
   */
  public Set<GroupSet> findAllByCreator(Member member);
  
  
  /**
   * Find all missing self group sets for groups.
   * @return set of array objects where the first element is the group
   * and the second element is the field.
   */
  public Set<Object[]> findMissingSelfGroupSetsForGroups();
  
  /**
   * Find all missing self group sets for stems.
   * @return set of array objects where the first element is the stem
   * and the second element is the field.
   */
  public Set<Object[]> findMissingSelfGroupSetsForStems();

  /**
   * Find all missing self group sets for stems.
   * @return set of array objects where the first element is the stem
   * and the second element is the field.
   */
  public Set<Object[]> findMissingSelfGroupSetsForAttrDefs();
  
  /**
   * Find a set of ownerStemIds for a memberGroup
   * @param groupId
   * @return set
   */
  public Set<String> findAllOwnerStemsByMemberGroup(String groupId);
  
  /**
   * Find a set of ownerGroupIds for a memberGroup
   * @param groupId
   * @return set
   */
  public Set<String> findAllOwnerGroupsByMemberGroup(String groupId);
  
  /**
   * @param ownerId
   * @return set
   */
  public Set<GroupSet> findAllSelfGroupSetsByOwnerWherePITFieldExists(String ownerId);
  
  /**
   * @param fieldId
   * @return set
   */
  public Set<GroupSet> findAllSelfGroupSetsByFieldWherePITGroupExists(String fieldId);
  
  /**
   * @param ownerId
   * @param memberId
   * @param fieldId
   * @param parentId
   * @param mshipType
   * @param exceptionIfNotFound 
   * @return group set
   */
  public GroupSet findByOwnerMemberFieldParentAndType(String ownerId, String memberId, 
      String fieldId, String parentId, String mshipType, boolean exceptionIfNotFound);
}
