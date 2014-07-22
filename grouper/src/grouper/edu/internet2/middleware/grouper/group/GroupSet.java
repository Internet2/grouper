/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.group;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupSetNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.StemDAO;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen $Id: GroupSet.java,v 1.12 2009-12-07 07:31:09 mchyzer Exp $
 *
 */
@SuppressWarnings("serial")
public class GroupSet extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned {
  
  /**
   * @see Object#toString()
   */
  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();
    
    result.append("id: ").append(this.id);
    result.append(", type: ").append(this.type);
    result.append(", depth: ").append(this.depth);
    result.append(", parent: ").append(this.parentId);
    result.append(", field: ").append(FieldFinder.findById(this.fieldId, true).getName());
    result.append(", memberField: ").append(FieldFinder.findById(this.memberFieldId, true).getName());
    result.append(", parent: ").append(this.parentId);
    
    GrouperSession.callbackGrouperSession(GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {
 
      /**
       * 
       */
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        if (!StringUtils.isBlank(GroupSet.this.ownerGroupId)) {
          Group ownerGroup = GroupFinder.findByUuid(grouperSession, GroupSet.this.ownerGroupId, true);
          result.append(", ownerGroup: ").append(ownerGroup.getName());
          Group memberGroup = GroupFinder.findByUuid(grouperSession, GroupSet.this.memberGroupId, true);
          result.append(", memberGroup: ").append(memberGroup.getName());
        }
        return null;
      }
    });
    
    return result.toString();
  }
  
  /** db id for this row */
  public static final String COLUMN_ID = "id";
  
  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** field represented by this group set */
  public static final String COLUMN_FIELD_ID = "field_id";
  
  /** type of membership represented by this group set, immediate or composite or effective */
  public static final String COLUMN_MSHIP_TYPE = "mship_type";
  
  /** same as member_group_id if depth is greater than 0, otherwise null. */
  public static final String COLUMN_VIA_GROUP_ID = "via_group_id";
  
  /** number of hops in directed graph */
  public static final String COLUMN_DEPTH = "depth";
  
  /** parent group set */
  public static final String COLUMN_PARENT_ID = "parent_id";
  
  /** member uuid of the creator of this record */
  public static final String COLUMN_CREATOR_ID = "creator_id";
  
  /** number of millis since 1970 that this record was created */
  public static final String COLUMN_CREATE_TIME = "create_time";

  /** owner id */
  public static final String COLUMN_OWNER_ID = "owner_id";
  
  /** owner group if applicable */
  public static final String COLUMN_OWNER_GROUP_ID = "owner_group_id";
  
  /** same as owner_group_id except nulls are replaced with the string '<NULL>' */
  public static final String COLUMN_OWNER_GROUP_ID_NULL = "owner_group_id_null";
  
  /** owner attribute def if applicable */
  public static final String COLUMN_OWNER_ATTR_DEF_ID = "owner_attr_def_id";
  
  /** same as owner_attr_def_id except nulls are replaced with the string '<NULL>' */
  public static final String COLUMN_OWNER_ATTR_DEF_ID_NULL = "owner_attr_def_id_null";
  
  /** owner stem if applicable */
  public static final String COLUMN_OWNER_STEM_ID = "owner_stem_id";
  
  /** same as owner_stem_id except nulls are replaced with the string '<NULL>' */
  public static final String COLUMN_OWNER_STEM_ID_NULL = "owner_stem_id_null";
  
  /** member group if applicable */
  public static final String COLUMN_MEMBER_GROUP_ID = "member_group_id";
  
  /** member attr def if applicable */
  public static final String COLUMN_MEMBER_ATTR_DEF_ID = "member_attr_def_id";
  
  /** member stem if applicable */
  public static final String COLUMN_MEMBER_STEM_ID = "member_stem_id";
  
  /** member id */
  public static final String COLUMN_MEMBER_ID = "member_id";
  
  /** field id used in joining this record with entries in grouper_memberships */
  public static final String COLUMN_MEMBER_FIELD_ID = "member_field_id";
  
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorId */
  public static final String FIELD_CREATOR_ID = "creatorId";

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";
  
  /** constant for field name for: viaGroupId */
  public static final String FIELD_VIA_GROUP_ID = "viaGroupId";

  /** constant for field name for: fieldId */
  public static final String FIELD_FIELD_ID = "fieldId";

  /** constant for field name for: type */
  public static final String FIELD_MSHIP_TYPE = "type";
  
  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: memberGroupId */
  public static final String FIELD_MEMBER_GROUP_ID = "memberGroupId";

  /** constant for field name for: memberStemId */
  public static final String FIELD_MEMBER_STEM_ID = "memberStemId";

  /** constant for field name for: ownerId */
  public static final String FIELD_OWNER_ID = "ownerId";
  
  /** constant for field name for: ownerGroupId */
  public static final String FIELD_OWNER_GROUP_ID = "ownerGroupId";

  /** constant for field name for: ownerGroupIdNull */
  public static final String FIELD_OWNER_GROUP_ID_NULL = "ownerGroupIdNull";

  /** constant for field name for: ownerStemId */
  public static final String FIELD_OWNER_STEM_ID = "ownerStemId";

  /** constant for field name for: ownerStemIdNull */
  public static final String FIELD_OWNER_STEM_ID_NULL = "ownerStemIdNull";

  /** constant for field name for: parentId */
  public static final String FIELD_PARENT_ID = "parentId";
  
  /** constant for field name for: memberFieldId */
  public static final String FIELD_MEMBER_FIELD_ID = "memberFieldId";

  /**
   * fields which are included in db version
   */
  /*
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATE_TIME, FIELD_CREATOR_ID, FIELD_DEPTH, FIELD_VIA_GROUP_ID, 
      FIELD_FIELD_ID, FIELD_MSHIP_TYPE, FIELD_ID, FIELD_MEMBER_GROUP_ID, FIELD_MEMBER_GROUP_ID_NULL, 
      FIELD_MEMBER_STEM_ID, FIELD_MEMBER_STEM_ID_NULL, FIELD_OWNER_GROUP_ID, FIELD_OWNER_GROUP_ID_NULL, 
      FIELD_OWNER_STEM_ID, FIELD_OWNER_STEM_ID_NULL, FIELD_PARENT_ID);
  */

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATE_TIME, FIELD_CREATOR_ID, FIELD_DEPTH, FIELD_VIA_GROUP_ID,
      FIELD_FIELD_ID, FIELD_MSHIP_TYPE, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_MEMBER_GROUP_ID, 
      FIELD_MEMBER_STEM_ID, FIELD_OWNER_GROUP_ID, FIELD_MEMBER_FIELD_ID, FIELD_OWNER_ID,
      FIELD_OWNER_GROUP_ID_NULL, FIELD_OWNER_STEM_ID, FIELD_OWNER_STEM_ID_NULL, FIELD_PARENT_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//


  /**
   * name of the group set table in the database.
   */
  public static final String TABLE_GROUPER_GROUP_SET = "grouper_group_set";

  /** id of this type */
  private String id;
  
  /** context id ties multiple db changes */
  private String contextId;
  
  /** field associated with this record */
  private String fieldId;
  
  /** membership type -- immediate, effective, or composite */
  private String type = MembershipType.IMMEDIATE.getTypeString();

  /** depth - 0 for self records, 1 for immediate memberships, > 1 for effective */
  private int depth;
  
  /** parent record */
  private String parentId;
  
  /** creator */
  private String creatorId;
  
  /** create time */
  private Long createTime = new Date().getTime();
  
  /** owner id */
  private String ownerId;
  
  /** group id for group memberships.  this is the owner. */
  private String ownerGroupId;
  
  /** ownerGroupId except nulls are replaced with a string so we can use this in a unique constraint */
  private String ownerGroupIdNull = GroupSet.nullColumnValue;

  /** stem id for stem memberships.  this is the owner. */
  private String ownerStemId;
  
  /** ownerStemId except nulls are replaced with a string so we can use this in a unique constraint */
  private String ownerStemIdNull = GroupSet.nullColumnValue;
  
  /** group id for group memberships.  this is the member. */
  private String memberGroupId;

  /** stem id for stem memberships.  this is the member. */
  private String memberStemId;
  
  /** member field id */
  private String memberFieldId;

  /**
   * the value we're storing in the db for nulls that need a value so that we can add a unique constraint.
   */
  public static final String nullColumnValue = "<NULL>";

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    
    if (!(other instanceof GroupSet)) {
      return false;
    }
    
    GroupSet that = (GroupSet) other;
    return new EqualsBuilder()
      .append(this.fieldId, that.fieldId)
      .append(this.type, that.type)
      .append(this.depth, that.depth)
      .append(this.parentId, that.parentId)
      .append(this.ownerAttrDefId, that.ownerAttrDefId)
      .append(this.ownerGroupId, that.ownerGroupId)
      .append(this.ownerStemId, that.ownerStemId)
      .append(this.memberAttrDefId, that.memberAttrDefId)
      .append(this.memberGroupId, that.memberGroupId)
      .append(this.memberStemId, that.memberStemId)
      .isEquals();
  } 

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.fieldId)
      .append(this.type)
      .append(this.depth)
      .append(this.parentId)
      .append(this.ownerAttrDefId)
      .append(this.ownerGroupId)
      .append(this.ownerStemId)
      .append(this.memberAttrDefId)
      .append(this.memberGroupId)
      .append(this.memberStemId)
      .toHashCode();
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /** attr def id for attr def memberships.  this is the member. */
  private String memberAttrDefId;

  /** attr def id for attr def memberships.  this is the owner. */
  private String ownerAttrDefId;

  /** ownerAttrDefId except nulls are replaced with a string so we can use this in a unique constraint */
  private String ownerAttrDefIdNull = GroupSet.nullColumnValue;

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);

    if (this.createTime == null) {
      this.createTime = System.currentTimeMillis();
    }
    
    if (this.creatorId == null) {
      this.creatorId = GrouperSession.staticGrouperSession().getMember().getUuid();
    }
    
    if (this.depth == 0) {
      this.memberFieldId = new String(this.fieldId);
    } else {
      this.memberFieldId = Group.getDefaultList().getUuid();
    }
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    
    // take care of effective group sets
    if (this.getDepth() == 1 && this.getMemberGroupId() != null) {
      Set<GroupSet> results = new LinkedHashSet<GroupSet>();
      Set<GroupSet> groupSetHasMembers = GrouperDAOFactory.getFactory().getGroupSet().findAllByGroupOwnerAndField(
          this.getMemberGroupId(), Group.getDefaultList());
      
      // Add members of member to owner group set
      results.addAll(addHasMembersToOwner(this, groupSetHasMembers));
  
      // If we are working on a group, where is it a member and field is the default list
      if (this.getOwnerGroupId() != null && this.getFieldId().equals(Group.getDefaultList().getUuid())) {
        Set<GroupSet> groupSetIsMember = GrouperDAOFactory.getFactory().getGroupSet().findAllByMemberGroup(this.getOwnerGroupId());
  
        // Add member and members of member to where owner is member
        results.addAll(addHasMembersToWhereGroupIsMember(this.getMemberGroupId(), groupSetIsMember, groupSetHasMembers));
      }
      
      GrouperDAOFactory.getFactory().getGroupSet().save(results);
      
      // update last membership change time
      this.updateLastMembershipChange(this, results);
    }
    
    super.onPostSave(hibernateSession);
  }

  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);

    // take care of effective group sets
    if (this.getDepth() == 1) {
      Set<GroupSet> groupSetsToDelete = new LinkedHashSet<GroupSet>();
      
      // Get all children of this group set
      Set<GroupSet> childResults = GrouperDAOFactory.getFactory().getGroupSet().findAllChildren(this);
  
      groupSetsToDelete.addAll(childResults);
  
      // Find all effective group sets that need deletion
      if (this.getOwnerGroupId() != null && this.getFieldId().equals(Group.getDefaultList().getUuid())) {
        Set<GroupSet> groupSetIsMember = GrouperDAOFactory.getFactory().getGroupSet().findAllByMemberGroup(this.getOwnerGroupId());
        
        Iterator<GroupSet> groupSetIsMemberIter = groupSetIsMember.iterator();
        while (groupSetIsMemberIter.hasNext()) {
          GroupSet currGroupSet = groupSetIsMemberIter.next();
          GroupSet childToDelete = GrouperDAOFactory.getFactory().getGroupSet().findImmediateChildByParentAndMemberGroup(currGroupSet, this.getMemberGroupId());
  
          if (childToDelete != null) {
            Set<GroupSet> childrenOfChildResults = GrouperDAOFactory.getFactory().getGroupSet().findAllChildren(childToDelete);
    
            groupSetsToDelete.addAll(childrenOfChildResults);
            groupSetsToDelete.add(childToDelete);
          }
        }
      }
      
      GrouperDAOFactory.getFactory().getGroupSet().delete(groupSetsToDelete);
      
      // update last membership change time
      this.updateLastMembershipChange(this, groupSetsToDelete);
    }
  }
  
  /**
   * If enabled, update last_membership_change for groups and stems
   * @param immediateGroupSet
   * @param effectiveGroupSets
   */
  private void updateLastMembershipChange(GroupSet immediateGroupSet, Set<GroupSet> effectiveGroupSets) {
    Set<String> groupIds = new LinkedHashSet<String>();
    Set<String> stemIds = new LinkedHashSet<String>();
    Set<String> attrDefIds = new LinkedHashSet<String>();
    
    if (immediateGroupSet.getOwnerGroupId() != null) {
      groupIds.add(immediateGroupSet.getOwnerGroupId());
    } else if (immediateGroupSet.getOwnerStemId() != null) {
      stemIds.add(immediateGroupSet.getOwnerStemId());
    } else if (immediateGroupSet.getOwnerAttrDefId() != null) {
      attrDefIds.add(immediateGroupSet.getOwnerAttrDefId());
    } else {
      throw new RuntimeException("Cant find owner! " + immediateGroupSet);
    }
    
    Iterator<GroupSet> iter = effectiveGroupSets.iterator();
    while (iter.hasNext()) {
      GroupSet effectiveGroupSet = iter.next();
      if (effectiveGroupSet.getOwnerGroupId() != null) {
        groupIds.add(effectiveGroupSet.getOwnerGroupId());
      } else if (effectiveGroupSet.getOwnerStemId() != null) {
        stemIds.add(effectiveGroupSet.getOwnerStemId());
      } else if (effectiveGroupSet.getOwnerAttrDefId() != null) {
        attrDefIds.add(effectiveGroupSet.getOwnerAttrDefId());
      } else {
        throw new RuntimeException("Cant find owner! " + effectiveGroupSet);
      }
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("stems.updateLastMembershipTime", false)) {
      StemDAO dao = GrouperDAOFactory.getFactory().getStem();
      Iterator<String> stemIdsIter = stemIds.iterator();
      while (stemIdsIter.hasNext()) {
        dao.updateLastMembershipChange(stemIdsIter.next());
      }
    }
    
    if (GrouperConfig.retrieveConfig().propertyValueBoolean("groups.updateLastMembershipTime", false)) {
      GroupDAO dao = GrouperDAOFactory.getFactory().getGroup();
      Iterator<String> groupIdsIter = groupIds.iterator();
      while (groupIdsIter.hasNext()) {
        dao.updateLastMembershipChange(groupIdsIter.next());
      }
    }
  }

  /**
   * @param memberGroupId
   * @param groupSetIsMember
   * @param groupSetHasMembers
   * @return group set
   * @throws IllegalStateException
   */
  private Set<GroupSet> addHasMembersToWhereGroupIsMember(String memberGroupId,
      Set<GroupSet> groupSetIsMember, Set<GroupSet> groupSetHasMembers)
      throws IllegalStateException {
    Set<GroupSet> groupSets = new LinkedHashSet();

    Iterator<GroupSet> isMembersIter = groupSetIsMember.iterator();
    Map<String, Set<GroupSet>> parentToChildrenMap = getParentToChildrenMap(groupSetHasMembers);

    // lets get all the hasMembers with a depth of 1 before the while loop
    Set<GroupSet> hasMembersOneDepth = new LinkedHashSet<GroupSet>();
    Iterator<GroupSet> hasMembersIter = groupSetHasMembers.iterator();
    while (hasMembersIter.hasNext()) {
      GroupSet gs = hasMembersIter.next();
      if (gs.getDepth() == 1) {
        hasMembersOneDepth.add(gs);
      }
    }

    while (isMembersIter.hasNext()) {
      GroupSet isGS = isMembersIter.next();

      String ownerGroupId = isGS.getOwnerGroupId();
      String ownerStemId = isGS.getOwnerStemId();
      String ownerAttrDefId = isGS.getOwnerAttrDefId();
      String fieldId = isGS.getFieldId();
      String id = isGS.getId();
      int depth = isGS.getDepth();

      // If the isMember's owner is the same as the immediate member's owner and this is for a default member, 
      // then we can skip this isMember.
      if (fieldId.equals(Group.getDefaultList().getUuid())
          && StringUtils.equals(isGS.getOwnerGroupId(), this.getOwnerGroupId())) {
        continue;
      }

      GroupSet groupSet = new GroupSet();
      groupSet.setId(GrouperUuid.getUuid());
      groupSet.setCreatorId(this.getCreatorId());
      groupSet.setCreateTime(this.getCreateTime());
      groupSet.setDepth(depth + 1);
      groupSet.setParentId(id);
      groupSet.setFieldId(fieldId);
      groupSet.setMemberGroupId(memberGroupId);
      groupSet.setOwnerGroupId(ownerGroupId);
      groupSet.setOwnerAttrDefId(ownerAttrDefId);
      groupSet.setOwnerStemId(ownerStemId);
      groupSet.setType(MembershipType.EFFECTIVE.getTypeString());

      // if we're forming a circular path, skip this isMember
      if (internal_isCircular(groupSet, isGS)) {
        continue;
      }

      groupSets.add(groupSet);

      Iterator<GroupSet> itHM = hasMembersOneDepth.iterator();
      while (itHM.hasNext()) {
        GroupSet hasGS = itHM.next();
        Set<GroupSet> newAdditions = addHasMembersRecursively(isGS, hasGS, groupSet,
            parentToChildrenMap, ownerAttrDefId, ownerGroupId, ownerStemId, this.getCreatorId(), fieldId);
        groupSets.addAll(newAdditions);
      }
    }

    return groupSets;
  } 

  
  /**
   * @param immediateGroupSet
   * @param hasMembers
   * @return set
   * @throws IllegalStateException
   */
  private Set<GroupSet> addHasMembersToOwner(GroupSet immediateGroupSet, Set<GroupSet> hasMembers) 
    throws  IllegalStateException
  {
    Set<GroupSet> groupSets = new LinkedHashSet();
    Iterator<GroupSet> it = hasMembers.iterator();

    // cache values outside of iterator
    String ownerAttrDefId = immediateGroupSet.getOwnerAttrDefId();
    String ownerGroupId = immediateGroupSet.getOwnerGroupId();
    String ownerStemId = immediateGroupSet.getOwnerStemId();
    String fieldId = immediateGroupSet.getFieldId();

    Map<String, Set<GroupSet>> parentToChildrenMap = getParentToChildrenMap(hasMembers);
   
    while (it.hasNext()) {
      GroupSet gs = it.next();
      if (gs.getDepth() == 1) {
        Set<GroupSet> newAdditions = addHasMembersRecursively(immediateGroupSet, gs, 
            immediateGroupSet, parentToChildrenMap, ownerAttrDefId, ownerGroupId, ownerStemId, this.getCreatorId(), fieldId);
        groupSets.addAll(newAdditions);
      }
    }

    return groupSets;
  }
  
  /**
   * Given a set of group sets, return a map that will allow retrieval of 
   * the children of a parent group set.
   * @param members
   * @return the map
   */
  private Map<String, Set<GroupSet>> getParentToChildrenMap(Set<GroupSet> members) {
    Map<String, Set<GroupSet>> parentToChildrenMap = new HashMap<String, Set<GroupSet>>();

    Iterator<GroupSet> iterator = members.iterator();
    while (iterator.hasNext()) {
      GroupSet gs = iterator.next();
      String parentId = gs.getParentId();

      if (parentId != null && !parentId.equals("")) {
        Set<GroupSet> children = parentToChildrenMap.get(parentId);
        if (children == null) {
          children = new LinkedHashSet<GroupSet>();
        }

        children.add(gs);
        parentToChildrenMap.put(parentId, children);
      }
    }

    return parentToChildrenMap;
  }
  
  /**
   * @param startGroupSet
   * @param gs
   * @param parentGroupSet
   * @param parentToChildrenMap
   * @param ownerGroupId1
   * @param ownerStemId1
   * @param ownerAttrDefId1 
   * @param creatorUUID
   * @param fieldId1
   * @return set of group sets
   */
  private Set<GroupSet> addHasMembersRecursively(GroupSet startGroupSet, 
      GroupSet gs, GroupSet parentGroupSet, Map<String, Set<GroupSet>> parentToChildrenMap, 
      String ownerAttrDefId1,
      String ownerGroupId1, String ownerStemId1, String creatorUUID, String fieldId1) {

      GroupSet newGroupSet = new GroupSet();
      newGroupSet.setId(GrouperUuid.getUuid());
      newGroupSet.setCreatorId(creatorUUID);
      newGroupSet.setCreateTime(this.getCreateTime());
      newGroupSet.setFieldId(fieldId1);
      newGroupSet.setOwnerAttrDefId(ownerAttrDefId1);
      newGroupSet.setOwnerGroupId(ownerGroupId1);
      newGroupSet.setOwnerStemId(ownerStemId1);
      newGroupSet.setMemberGroupId(gs.getMemberGroupId());
      newGroupSet.setDepth(parentGroupSet.getDepth() + 1);
      newGroupSet.setParentId(parentGroupSet.getId());
      newGroupSet.setType(MembershipType.EFFECTIVE.getTypeString());

      // if we're forming a circular path, return an empty Set.
      if (internal_isCircular(newGroupSet, startGroupSet)) {
        return new LinkedHashSet<GroupSet>();
      }

      Set<GroupSet> newGroupSets = new LinkedHashSet<GroupSet>();
      newGroupSets.add(newGroupSet);

      Set<GroupSet> children = parentToChildrenMap.get(gs.getId());
      if (children != null) {
        Iterator<GroupSet> it = children.iterator();
        while (it.hasNext()) {
          GroupSet nextGroupSet = it.next();
          Set<GroupSet> newAdditions = addHasMembersRecursively(startGroupSet, nextGroupSet, newGroupSet, 
            parentToChildrenMap, ownerAttrDefId1, ownerGroupId1, ownerStemId1, creatorUUID, fieldId1);
          newGroupSets.addAll(newAdditions);
        }
      }
      
      return newGroupSets;
    }
  
  /**
   * Check if the new group set being added will cause a circular group set.
   *
   * @param newGroupSet group set being added
   * @param startGroupSet group set that's a parent of newGroupSet which will be used
   *                        as a starting point to check if we're forming a circular group set
   * @return true if the new group set will cause a circular group set.
   */
  public boolean internal_isCircular(GroupSet newGroupSet, GroupSet startGroupSet) {

    // for the default list, a group should not be an indirect member of itself ....
    if (newGroupSet.getFieldId().equals(Group.getDefaultList().getUuid()) && 
        newGroupSet.getMemberGroupId().equals(newGroupSet.getOwnerGroupId())) {
      return true;
    }

    // now let's go through the parents... 
    // if the member of a parent is equal to the member of the new group set,
    // then we have a circular group set.
    if (newGroupSet.getDepth() < 3) {
      return false;
    }

    GroupSet currentGroupSet = startGroupSet;
    while (true) {
      if (currentGroupSet.getMemberGroupId().equals(newGroupSet.getMemberGroupId())) {
        return true;
      }
      if (currentGroupSet.getDepth() > 1) {
        currentGroupSet = currentGroupSet.getParentGroupSet();
      } else {
        break;
      }
    }

    return false;
  }

  
  /**
   * @return the parent group set
   */
  public GroupSet getParentGroupSet() {
    if (depth == 0) {
      throw new GroupSetNotFoundException("no parent");
    }
    
    GroupSet parent = GrouperDAOFactory.getFactory().getGroupSet().findParentGroupSet(this) ;
    return parent;
  }
  
  
  /**
   * @return id
   */
  public String getId() {
    return id;
  }

  
  /**
   * set id
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return context id
   */
  public String getContextId() {
    return contextId;
  }

  
  /**
   * set context id
   * @param contextId
   */
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  /**
   * @return field id
   */
  public String getFieldId() {
    return fieldId;
  }

  /**
   * @param fieldId
   */
  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }
  
  /**
   * @return field id used in joining with grouper_memberships table
   */
  public String getMemberFieldId() {    
    return memberFieldId;
  }

  /**
   * Internal use only.
   * @param memberFieldId
   */
  public void setMemberFieldId(String memberFieldId) {
    this.memberFieldId = memberFieldId;
  }

  
  /**
   * This is 0 for self memberships (where the owner and member are the same).
   * Otherwise, it's the number of hops in a directed graph from the member to the group.
   * @return depth
   */
  public int getDepth() {
    return depth;
  }

  
  /**
   * set depth
   * @param depth
   */
  public void setDepth(int depth) {
    this.depth = depth;
  }

  /**
   * @return via group id
   */
  public String getViaGroupId() {
    if (depth == 0) {
      return null;
    }
    
    return memberGroupId;
  }

  
  /**
   * Set via group id.  This is for internal use only.
   * @param viaGroupId
   */
  public void setViaGroupId(@SuppressWarnings("unused") String viaGroupId) {
    // not used
  }
  
  /**
   * @return parent id
   */
  public String getParentId() {
    return parentId;
  }

  
  /**
   * set parent id
   * @param parentId
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  
  /**
   * @return creator
   */
  public String getCreatorId() {
    return creatorId;
  }

  
  /**
   * set creator
   * @param creatorId
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }


  /**
   * @return create time
   */
  public Long getCreateTime() {
    return createTime;
  }

  
  /**
   * set create time
   * @param createTime
   */
  public void setCreateTime(Long createTime) {
    this.createTime = createTime;
  }

  /**
   * @return owner id
   */
  public String getOwnerId() {
    return ownerId;
  }
  
  /**
   * Set owner id.  This is for internal use only.
   * @param ownerId
   */
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }
  
  /**
   * @return group id for the owner if this is a group membership
   */
  public String getOwnerGroupId() {
    return ownerGroupId;
  }

  /**
   * Set group id for the owner if this is a group membership
   * @param ownerGroupId
   */
  public void setOwnerGroupId(String ownerGroupId) {
    this.ownerGroupId = ownerGroupId;
    setOwnerGroupIdNull(ownerGroupId);
    if (ownerGroupId == null) {
      setOwnerGroupIdNull(GroupSet.nullColumnValue);
    } else {
      setOwnerId(ownerGroupId);
    }
  }

  /**
   * This is for internal use only.  This is the same as getOwnerGroupId() except nulls are replaced with
   * a constant string.
   * @return group id for the owner if this is a group membership
   */
  public String getOwnerGroupIdNull() {
    return ownerGroupIdNull;
  }

  
  /**
   * Set group id for the owner if this is a group membership.  This is for internal use only.
   * @param ownerGroupIdNull
   */
  public void setOwnerGroupIdNull(String ownerGroupIdNull) {
    this.ownerGroupIdNull = ownerGroupIdNull;
  }

  /**
   * @return stem id for the owner if this is a stem membership
   */
  public String getOwnerStemId() {
    return ownerStemId;
  }

  
  /**
   * Set stem id for the owner if this is a stem membership
   * @param ownerStemId
   */
  public void setOwnerStemId(String ownerStemId) {
    this.ownerStemId = ownerStemId;
    setOwnerStemIdNull(ownerStemId);
    if (ownerStemId == null) {
      setOwnerStemIdNull(GroupSet.nullColumnValue);
    } else {
      setOwnerId(ownerStemId);
    }
  }

  
  /**
   * This is for internal use only.  This is the same as getOwnerStemId() except nulls are replaced with
   * a constant string.
   * @return stem id for the owner if this is a stem membership
   */
  public String getOwnerStemIdNull() {
    return ownerStemIdNull;
  }

  /**
   * Set stem id for the owner if this is a stem membership.  This is for internal use only.
   * @param ownerStemIdNull
   */
  public void setOwnerStemIdNull(String ownerStemIdNull) {
    this.ownerStemIdNull = ownerStemIdNull;
  }

  /**
   * @return group id for the member if the member is a group
   */
  public String getMemberGroupId() {
    return memberGroupId;
  }

  
  /**
   * Set group id for the member if the member is a group
   * @param memberGroupId
   */
  public void setMemberGroupId(String memberGroupId) {
    this.memberGroupId = memberGroupId;
  }
  
  /**
   * @return stem id for the member if the member is a stem
   */
  public String getMemberStemId() {
    return memberStemId;
  }

  
  /**
   * Set stem id for the member if the member is a stem
   * @param memberStemId
   */
  public void setMemberStemId(String memberStemId) {
    this.memberStemId = memberStemId;
  }

  
  /**
   * This is 'immediate' for self memberships (owner and member are the same) except if the group is a composite in which case this will be 'composite'.
   * For non-self memberships, this is 'effective'.
   * @return membership type (immediate, effective, or composite)
   */
  public String getType() {
    return type;
  }

  
  /**
   * set membership type
   * @param type
   */
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   * Since pre hooks on effective memberships have to fire before the membership can be queried,
   * I have this method to manually combine a GroupSet and ImmediateMembershipEntry.
   * @param immediateOrCompositeMembership
   * @return membership
   */
  /*
  public Membership internal_createEffectiveMembershipObject(Membership immediateOrCompositeMembership) {
    Membership effectiveMembership = immediateOrCompositeMembership.clone();
    effectiveMembership.setUuid(immediateOrCompositeMembership.getImmediateMembershipId() + ":" + this.getId());
    effectiveMembership.setGroupSetId(this.getId());
    effectiveMembership.setFieldId(this.getFieldId());
    effectiveMembership.setOwnerAttrDefId(this.getOwnerAttrDefId());
    effectiveMembership.setOwnerGroupId(this.getOwnerGroupId());
    effectiveMembership.setOwnerStemId(this.getOwnerStemId());
    effectiveMembership.setViaGroupId(this.getViaGroupId());
    effectiveMembership.setViaCompositeId(null);
    effectiveMembership.setDepth(this.getDepth());
    effectiveMembership.setType(MembershipType.EFFECTIVE.getTypeString());
    effectiveMembership.setGroupSetParentId(this.getParentId());
    effectiveMembership.setGroupSetCreatorUuid(this.getCreatorId());
    effectiveMembership.setGroupSetCreateTimeLong(this.getCreateTime());
    
    return effectiveMembership;
  }
  */

  /**
   * @return group id for the member if the member is a group
   */
  public String getMemberAttrDefId() {
    return this.memberAttrDefId;
  }

  /**
   * @return attrdef id for the owner if this is a attrdef membership
   */
  public String getOwnerAttrDefId() {
    return this.ownerAttrDefId;
  }

  /**
   * This is for internal use only.  This is the same as getOwnerAttrDefId() except nulls are replaced with
   * a constant string.
   * @return attr def id for the owner if this is a attrdef membership
   */
  public String getOwnerAttrDefIdNull() {
    return this.ownerAttrDefIdNull;
  }

  /**
   * Set attr def id for the member if the member is a attrdef
   * @param memberAttrDefId1
   */
  public void setMemberAttrDefId(String memberAttrDefId1) {
    this.memberAttrDefId = memberAttrDefId1;
  }

  /**
   * Set attrdef id for the owner if this is a attrdef membership
   * @param ownerAttrDefId1
   */
  public void setOwnerAttrDefId(String ownerAttrDefId1) {
    this.ownerAttrDefId = ownerAttrDefId1;
    this.setOwnerAttrDefIdNull(ownerAttrDefId1);
    if (ownerAttrDefId1 == null) {
      this.setOwnerAttrDefIdNull(GroupSet.nullColumnValue);
    } else {
      setOwnerId(ownerAttrDefId1);
    }
  }

  /**
   * Set attrdef id for the owner if this is a attrdef membership.  This is for internal use only.
   * @param ownerAttrDefIdNull1
   */
  public void setOwnerAttrDefIdNull(String ownerAttrDefIdNull1) {
    this.ownerAttrDefIdNull = ownerAttrDefIdNull1;
  }
  
  /**
   * get the member id
   * @return the member id
   */
  public String getMemberId() {
    if (this.memberAttrDefId != null) {
      return this.memberAttrDefId;
    }
    
    if (this.memberGroupId != null) {
      return this.memberGroupId;
    }
    
    if (this.memberStemId != null) {
      return this.memberStemId;
    }
    
    throw new RuntimeException("No value for member.");
  }
  
  /**
   * This is for internal use only.
   * @param member
   */
  public void setMemberId(String member) {
    // not used
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    if (this.depth == 0) {
      this.memberFieldId = new String(this.fieldId);
    } else {
      this.memberFieldId = Group.getDefaultList().getUuid();
    }
  }
  
  /**
   * @param forceDisablePITEntry should only be used if we're removing a corrupt group set
   */
  public void delete(final boolean forceDisablePITEntry) {
    
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            GrouperDAOFactory.getFactory().getGroupSet().delete(GroupSet.this);
            
            if (forceDisablePITEntry) {
              PITGroupSet pit = GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceIdActive(GroupSet.this.getId(), false);
              if (pit != null) {
                pit.internal_disable();
              }
            }
            
            return null;
          }
        });
  }
}
