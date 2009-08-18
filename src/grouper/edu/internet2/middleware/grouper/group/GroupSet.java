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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupSetNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.StemDAO;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen $Id: GroupSet.java,v 1.3 2009-08-18 23:11:38 shilen Exp $
 *
 */
@SuppressWarnings("serial")
public class GroupSet extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned {
  
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

  /** constant for field name for: memberGroupIdNull */
  public static final String FIELD_MEMBER_GROUP_ID_NULL = "memberGroupIdNull";

  /** constant for field name for: memberStemId */
  public static final String FIELD_MEMBER_STEM_ID = "memberStemId";

  /** constant for field name for: memberStemIdNull */
  public static final String FIELD_MEMBER_STEM_ID_NULL = "memberStemIdNull";

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
      FIELD_MEMBER_GROUP_ID_NULL, FIELD_MEMBER_STEM_ID, FIELD_MEMBER_STEM_ID_NULL, FIELD_OWNER_GROUP_ID, 
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
  private String type = Membership.IMMEDIATE;

  /** depth - 0 for self records, 1 for immediate memberships, > 1 for effective */
  private int depth;
  
  /** parent record */
  private String parentId;
  
  /** creator */
  private String creatorId;
  
  /** create time */
  private Long createTime = new Date().getTime();
  
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
  
  /** memberGroupId except nulls are replaced with a string so we can use this in a unique constraint */
  private String memberGroupIdNull = GroupSet.nullColumnValue;

  /** stem id for stem memberships.  this is the member. */
  private String memberStemId;
  
  /** memberStemId except nulls are replaced with a string so we can use this in a unique constraint */
  private String memberStemIdNull = GroupSet.nullColumnValue;

  /**
   * the value we're storing in the db for nulls that need a value so that we can add a unique constraint.
   */
  public static final String nullColumnValue = "<NULL>";

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
      .append(this.ownerGroupId, that.ownerGroupId)
      .append(this.ownerStemId, that.ownerStemId)
      .append(this.memberGroupId, that.memberGroupId)
      .append(this.memberStemId, that.memberStemId)
      .isEquals();
  } 

  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.fieldId)
      .append(this.type)
      .append(this.depth)
      .append(this.parentId)
      .append(this.ownerGroupId)
      .append(this.ownerStemId)
      .append(this.memberGroupId)
      .append(this.memberStemId)
      .toHashCode();
  }
  

  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /** we're using this to save effective memberships onPreSave and onPreDelete so they can be used onPostSave and onPostDelete */
  private Set<Membership> effectiveMembershipsForHooks;

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    effectiveMembershipsForHooks = new LinkedHashSet<Membership>();

    if (this.createTime == null) {
      this.createTime = System.currentTimeMillis();
    }
    
    if (this.creatorId == null) {
      this.creatorId = GrouperSession.staticGrouperSession().getMember().getUuid();
    }
    
    // now we need to take care of firing hooks for effective memberships
    // note that effective membership hooks are also fired on pre and post events on Membership
    if (this.getDepth() > 0) {
      Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndFieldAndDepth(this.getMemberGroupId(), Group.getDefaultList(), 0, true);
      Iterator<Membership> membershipsIter = memberships.iterator();
      while (membershipsIter.hasNext()) {
        Membership effectiveMembership = this.internal_createEffectiveMembershipObjectForHooks(membershipsIter.next());
        effectiveMembershipsForHooks.add(effectiveMembership);
        GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
            MembershipHooks.METHOD_MEMBERSHIP_PRE_INSERT, HooksMembershipBean.class, 
            effectiveMembership, Membership.class, VetoTypeGrouper.MEMBERSHIP_PRE_INSERT, false, false);
      }
    }
  }
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    super.onPostSave(hibernateSession);
    
    // hooks for effective memberships
    // note that effective membership hooks are also fired on pre and post events on Membership
    Iterator<Membership> effectiveMembershipsIter = this.effectiveMembershipsForHooks.iterator();
    while (effectiveMembershipsIter.hasNext()) {
      Membership effectiveMembership = effectiveMembershipsIter.next();
      
      GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
          MembershipHooks.METHOD_MEMBERSHIP_POST_INSERT, HooksMembershipBean.class, 
          effectiveMembership, Membership.class, VetoTypeGrouper.MEMBERSHIP_POST_INSERT, true, false);

      //do these second so the right object version is set, and dbVersion is ok
      GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
          MembershipHooks.METHOD_MEMBERSHIP_POST_COMMIT_INSERT, HooksMembershipBean.class, 
          effectiveMembership, Membership.class);
    }
    
    this.effectiveMembershipsForHooks = null;

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
  }
  
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  public void onPostDelete(HibernateSession hibernateSession) {
    super.onPostDelete(hibernateSession);
    
    // hooks for effective memberships
    // note that effective membership hooks are also fired on pre and post events on Membership
    Iterator<Membership> effectiveMembershipsIter = this.effectiveMembershipsForHooks.iterator();
    while (effectiveMembershipsIter.hasNext()) {
      Membership effectiveMembership = effectiveMembershipsIter.next();
      
      GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.MEMBERSHIP, 
          MembershipHooks.METHOD_MEMBERSHIP_POST_COMMIT_DELETE, HooksMembershipBean.class, 
          effectiveMembership, Membership.class);

      GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
          MembershipHooks.METHOD_MEMBERSHIP_POST_DELETE, HooksMembershipBean.class, 
          effectiveMembership, Membership.class, VetoTypeGrouper.MEMBERSHIP_POST_DELETE, false, true);
    }
    
    this.effectiveMembershipsForHooks = null;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
    effectiveMembershipsForHooks = new LinkedHashSet<Membership>();

    // now we need to take care of firing hooks for effective memberships
    // note that effective membership hooks are also fired on pre and post events on Membership
    if (this.getDepth() > 0) {
      Set<Membership> memberships = GrouperDAOFactory.getFactory().getMembership()
          .findAllByGroupOwnerAndFieldAndDepth(this.getMemberGroupId(), Group.getDefaultList(), 0, true);
      Iterator<Membership> membershipsIter = memberships.iterator();
      while (membershipsIter.hasNext()) {
        Membership effectiveMembership = this.internal_createEffectiveMembershipObjectForHooks(membershipsIter.next());
        effectiveMembershipsForHooks.add(effectiveMembership);
        GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.MEMBERSHIP, 
            MembershipHooks.METHOD_MEMBERSHIP_PRE_DELETE, HooksMembershipBean.class, 
            effectiveMembership, Membership.class, VetoTypeGrouper.MEMBERSHIP_PRE_DELETE, false, false);
      }
    }
    
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
    
    if (immediateGroupSet.getOwnerGroupId() != null) {
      groupIds.add(immediateGroupSet.getOwnerGroupId());
    } else {
      stemIds.add(immediateGroupSet.getOwnerStemId());
    }
    
    Iterator<GroupSet> iter = effectiveGroupSets.iterator();
    while (iter.hasNext()) {
      GroupSet effectiveGroupSet = iter.next();
      if (effectiveGroupSet.getOwnerGroupId() != null) {
        groupIds.add(effectiveGroupSet.getOwnerGroupId());
      } else {
        stemIds.add(effectiveGroupSet.getOwnerStemId());
      }
    }
    
    if (GrouperConfig.getPropertyBoolean("stems.updateLastMembershipTime", true)) {
      StemDAO dao = GrouperDAOFactory.getFactory().getStem();
      Iterator<String> stemIdsIter = stemIds.iterator();
      while (stemIdsIter.hasNext()) {
        dao.updateLastMembershipChange(stemIdsIter.next());
      }
    }
    
    if (GrouperConfig.getPropertyBoolean("groups.updateLastMembershipTime", true)) {
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
   * @return
   * @throws IllegalStateException
   */
  private Set<GroupSet> addHasMembersToWhereGroupIsMember(String memberGroupId,
      Set<GroupSet> groupSetIsMember, Set<GroupSet> groupSetHasMembers)
      throws IllegalStateException {
    Set<GroupSet> groupSets = new LinkedHashSet();

    String creatorUUID = GrouperSession.staticGrouperSession().getMember().getUuid();

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
      groupSet.setCreatorId(creatorUUID);
      groupSet.setDepth(depth + 1);
      groupSet.setParentId(id);
      groupSet.setFieldId(fieldId);
      groupSet.setMemberGroupId(memberGroupId);
      groupSet.setOwnerGroupId(ownerGroupId);
      groupSet.setOwnerStemId(ownerStemId);
      groupSet.setType(Membership.EFFECTIVE);

      // if we're forming a circular path, skip this isMember
      if (isCircular(groupSet, isGS)) {
        continue;
      }

      groupSets.add(groupSet);

      Iterator<GroupSet> itHM = hasMembersOneDepth.iterator();
      while (itHM.hasNext()) {
        GroupSet hasGS = itHM.next();
        Set<GroupSet> newAdditions = addHasMembersRecursively(isGS, hasGS, groupSet,
            parentToChildrenMap, ownerGroupId, ownerStemId, creatorUUID, fieldId);
        groupSets.addAll(newAdditions);
      }
    }

    return groupSets;
  } 

  
  /**
   * @param immediateGroupSet
   * @param hasMembers
   * @return
   * @throws IllegalStateException
   */
  private Set addHasMembersToOwner(GroupSet immediateGroupSet, Set<GroupSet> hasMembers) 
    throws  IllegalStateException
  {
    Set<GroupSet> groupSets = new LinkedHashSet();
    Iterator<GroupSet> it = hasMembers.iterator();

    // cache values outside of iterator
    String ownerGroupId = immediateGroupSet.getOwnerGroupId();
    String ownerStemId = immediateGroupSet.getOwnerStemId();
    String creatorUUID = GrouperSession.staticGrouperSession().getMember().getUuid();
    String fieldId = immediateGroupSet.getFieldId();

    Map<String, Set<GroupSet>> parentToChildrenMap = getParentToChildrenMap(hasMembers);
   
    while (it.hasNext()) {
      GroupSet gs = it.next();
      if (gs.getDepth() == 1) {
        Set<GroupSet> newAdditions = addHasMembersRecursively(immediateGroupSet, gs, 
            immediateGroupSet, parentToChildrenMap, ownerGroupId, ownerStemId, creatorUUID, fieldId);
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
   * @param ownerGroupId
   * @param ownerStemId
   * @param creatorUUID
   * @param fieldId
   * @return
   */
  private Set<GroupSet> addHasMembersRecursively(GroupSet startGroupSet, 
      GroupSet gs, GroupSet parentGroupSet, Map<String, Set<GroupSet>> parentToChildrenMap, 
      String ownerGroupId, String ownerStemId, String creatorUUID, String fieldId) {

      GroupSet newGroupSet = new GroupSet();
      newGroupSet.setId(GrouperUuid.getUuid());
      newGroupSet.setCreatorId(creatorUUID);
      newGroupSet.setFieldId(fieldId);
      newGroupSet.setOwnerGroupId(ownerGroupId);
      newGroupSet.setOwnerStemId(ownerStemId);
      newGroupSet.setMemberGroupId(gs.getMemberGroupId());
      newGroupSet.setDepth(parentGroupSet.getDepth() + 1);
      newGroupSet.setParentId(parentGroupSet.getId());
      newGroupSet.setType(Membership.EFFECTIVE);

      // if we're forming a circular path, return an empty Set.
      if (isCircular(newGroupSet, startGroupSet)) {
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
            parentToChildrenMap, ownerGroupId, ownerStemId, creatorUUID, fieldId);
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
  private boolean isCircular(GroupSet newGroupSet, GroupSet startGroupSet) {

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
   * return field id
   * @param fieldId
   */
  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  
  /**
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
  public void setViaGroupId(String viaGroupId) {
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
    setMemberGroupIdNull(memberGroupId);
    if (memberGroupId == null) {
      setMemberGroupIdNull(GroupSet.nullColumnValue);
    }
  }

  /**
   * This is for internal use only.  This is the same as getMemberGroupId() except nulls are replaced with
   * a constant string.
   * @return group id for the member if the member is a group
   */  
  public String getMemberGroupIdNull() {
    return memberGroupIdNull;
  }

  /**
   * Set group id for the member if the member is a group.  This is for internal use only.
   * @param memberGroupIdNull
   */  
  public void setMemberGroupIdNull(String memberGroupIdNull) {
    this.memberGroupIdNull = memberGroupIdNull;
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
    setMemberStemIdNull(memberStemId);
    if (memberStemId == null) {
      setMemberStemIdNull(GroupSet.nullColumnValue);
    }
  }

  /**
   * This is for internal use only.  This is the same as getMemberStemId() except nulls are replaced with
   * a constant string.
   * @return stem id for the member if the member is a stem
   */  
  public String getMemberStemIdNull() {
    return memberStemIdNull;
  }

  /**
   * Set stem id for the member if the member is a stem.  This is for internal use only.
   * @param memberStemIdNull
   */  
  public void setMemberStemIdNull(String memberStemIdNull) {
    this.memberStemIdNull = memberStemIdNull;
  }
  

  
  /**
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
  public Membership internal_createEffectiveMembershipObjectForHooks(Membership immediateOrCompositeMembership) {
    Membership effectiveMembership = immediateOrCompositeMembership.clone();
    effectiveMembership.setUuid(immediateOrCompositeMembership.getImmediateMembershipId() + ":" + this.getId());
    effectiveMembership.setGroupSetId(this.getId());
    effectiveMembership.setFieldId(this.getFieldId());
    effectiveMembership.setOwnerGroupId(this.getOwnerGroupId());
    effectiveMembership.setOwnerStemId(this.getOwnerStemId());
    effectiveMembership.setViaGroupId(this.getViaGroupId());
    effectiveMembership.setViaCompositeId(null);
    effectiveMembership.setDepth(this.getDepth());
    effectiveMembership.setType(Membership.EFFECTIVE);
    effectiveMembership.setGroupSetParentId(this.getParentId());
    effectiveMembership.setGroupSetCreatorUuid(this.getCreatorId());
    effectiveMembership.setGroupSetCreateTimeLong(this.getCreateTime());
    
    return effectiveMembership;
  }
}
