/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.misc;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.MembershipAlreadyExistsException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.CompositeMembershipValidator;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.ImmediateMembershipValidator;

/** 
 * Perform <i>member of</i> calculation.
 * <p/>
 * @author  blair christensen.
 * @version $Id: DefaultMemberOf.java,v 1.18 2009-06-09 22:55:40 shilen Exp $
 * @since   1.2.0
 */
@GrouperIgnoreDbVersion
public class DefaultMemberOf extends BaseMemberOf implements GrouperCloneable {

  /** */
  private boolean validateImmediateMembership = true;

  /** */
  @GrouperIgnoreFieldConstant @GrouperIgnoreClone @GrouperIgnoreDbVersion
  private Map<MultiKey, Boolean> groupsAndMembersSaves = new HashMap<MultiKey, Boolean>();

  /** */
  @GrouperIgnoreFieldConstant @GrouperIgnoreClone @GrouperIgnoreDbVersion
  private Map<MultiKey, Boolean> groupsAndMembersDeletes = new HashMap<MultiKey, Boolean>();

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: _m */
  public static final String FIELD__M = "_m";

  /** constant for field name for: _ms */
  public static final String FIELD__MS = "_ms";

  /** constant for field name for: c */
  public static final String FIELD_C = "c";

  /** constant for field name for: deletes */
  public static final String FIELD_DELETES = "deletes";

  /** constant for field name for: effDeletes */
  public static final String FIELD_EFF_DELETES = "effDeletes";

  /** constant for field name for: effSaves */
  public static final String FIELD_EFF_SAVES = "effSaves";

  /** constant for field name for: f */
  public static final String FIELD_F = "f";

  /** constant for field name for: g */
  public static final String FIELD_G = "g";

  /** constant for field name for: modifiedGroups */
  public static final String FIELD_MODIFIED_GROUPS = "modifiedGroups";

  /** constant for field name for: modifiedStems */
  public static final String FIELD_MODIFIED_STEMS = "modifiedStems";

  /** constant for field name for: ns */
  public static final String FIELD_NS = "ns";

  /** constant for field name for: ownerGroupId */
  public static final String FIELD_OWNER_GROUP_ID = "ownerGroupId";

  /** constant for field name for: ownerStemId */
  public static final String FIELD_OWNER_STEM_ID = "ownerStemId";

  /** constant for field name for: saves */
  public static final String FIELD_SAVES = "saves";

  /** constant for field name for: validateImmediateMembership */
  public static final String FIELD_VALIDATE_IMMEDIATE_MEMBERSHIP = "validateImmediateMembership";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD__M, FIELD__MS, FIELD_C, FIELD_DELETES, 
      FIELD_EFF_DELETES, FIELD_EFF_SAVES, FIELD_F, FIELD_G, 
      FIELD_MODIFIED_GROUPS, FIELD_MODIFIED_STEMS, FIELD_NS, FIELD_OWNER_GROUP_ID, 
      FIELD_OWNER_STEM_ID, FIELD_SAVES, FIELD_VALIDATE_IMMEDIATE_MEMBERSHIP);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * deep clone the fields in this object
   */
  @Override
  public Object clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  // PUBLIC INSTANCE METHODS //
 
  /**
   * @param s 
   * @param g 
   * @param c 
   * @throws IllegalStateException 
   * @since   1.2.0
   */
  public void addComposite(GrouperSession s, final Group g, final Composite c)
    throws  IllegalStateException
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        DefaultMemberOf.this.setComposite(c);
        DefaultMemberOf.this.setGroup(g);
        DefaultMemberOf.this.addSave( c );
        DefaultMemberOf.this._evaluateAddCompositeMembership();
        return null;
      }
      
    });
  } 

  /**
   * @param s 
   * @param g 
   * @param c 
   * @throws IllegalStateException 
   * @since   1.2.0
   */
  public void deleteComposite(GrouperSession s, final Group g, final Composite c)
    throws  IllegalStateException
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        DefaultMemberOf.this.setComposite(c);
        DefaultMemberOf.this.setGroup(g);
        DefaultMemberOf.this._evaluateDeleteCompositeMembership();
        DefaultMemberOf.this.addDelete( c );
        return null;
      }
      
    });
  }

  /**
   * @param s 
   * @param g 
   * @param f 
   * @param _m 
   * @throws IllegalStateException 
   * @since   1.2.0
   */
  public void addImmediate(GrouperSession s, Group g, Field f, Member _m)
    throws  IllegalStateException 
  {
    //note, no need for GrouperSession inverse of control
    this.setGroup(g);
    this._evaluateAddImmediateMembership(s, f, _m);
  } 

  /**
   * @param s 
   * @param g 
   * @param f 
   * @param _m 
   * @throws IllegalStateException 
   * @since   1.3.1
   */
  public void addImmediateWithoutValidation(GrouperSession s, Group g, Field f, Member _m)
    throws  IllegalStateException 
  {
    this.validateImmediateMembership = false;
    addImmediate(s, g, f, _m);
  } 

  /**
   * @param s 
   * @param ns 
   * @param f 
   * @param _m 
   * @throws IllegalStateException 
   * @since   1.2.0
   */
  public void addImmediate(GrouperSession s, Stem ns, Field f, Member _m)
    throws  IllegalStateException
  {
    //note, no need for GrouperSession inverse of control
    this.setStem(ns);
    this._evaluateAddImmediateMembership(s, f, _m);
  }

  /**
   * @param s 
   * @param ns 
   * @param f 
   * @param _m 
   * @throws IllegalStateException 
   * @since   1.3.1
   */
  public void addImmediateWithoutValidation(GrouperSession s, Stem ns, Field f, Member _m)
    throws  IllegalStateException 
  {
    this.validateImmediateMembership = false;
    addImmediate(s, ns, f, _m);
  } 

  /**
   * @param s 
   * @param g 
   * @param _ms 
   * @param _m 
   * @throws IllegalStateException 
   * @since   1.2.0
   */
  public void deleteImmediate(GrouperSession s, Group g, Membership _ms, Member _m)
    throws  IllegalStateException
  {
    //note, no need for GrouperSession inverse of control
    this.setGroup(g);
    this._evaluateDeleteImmediateMembership(s, _ms, _m);
  }

  /**
   * @param s 
   * @param ns 
   * @param _ms 
   * @param _m 
   * @throws IllegalStateException 
   * @since   1.2.0
   */
  public void deleteImmediate(GrouperSession s, Stem ns, Membership _ms, Member _m)
    throws  IllegalStateException
  {
    //note, no need for GrouperSession inverse of control
    this.setStem(ns);
    this._evaluateDeleteImmediateMembership(s, _ms, _m);
  } 

  /**
   * Add m's hasMembers to o
   * @since   1.2.0
   * @param hasMembers
   * @return set
   * @throws IllegalStateException
   */
  private Set _addHasMembersToOwner(GroupSet immediateGroupSet, Set<GroupSet> hasMembers) 
    throws  IllegalStateException
  {
    Set<GroupSet> groupSets = new LinkedHashSet();
    Iterator<GroupSet> it = hasMembers.iterator();

    // cache values outside of iterator
    String ownerGroupId = immediateGroupSet.getOwnerGroupId();
    String ownerStemId = immediateGroupSet.getOwnerStemId();
    String creatorUUID = GrouperSession.staticGrouperSession().getMember().getUuid();
    String fieldId = immediateGroupSet.getFieldId();

    Map<String, Set<GroupSet>> parentToChildrenMap = _getParentToChildrenMap(hasMembers);
   
    while (it.hasNext()) {
      GroupSet gs = it.next();
      if (gs.getDepth() == 1) {
        Set<GroupSet> newAdditions = _addHasMembersRecursively(immediateGroupSet, gs, 
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
  private Map<String, Set<GroupSet>> _getParentToChildrenMap(Set<GroupSet> members) {
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
      if (currentGroupSet.getDepth() > 0) {
        currentGroupSet = currentGroupSet.getParentGroupSet();
      } else {
        break;
      }
    }

    return false;
  }

  /**
   * 
   * @param startGroupSet
   * @param gs
   * @param parentGroupSet
   * @param parentToChildrenMap
   * @param ownerGroupId 
   * @param ownerStemId 
   * @param creatorUUID
   * @param fieldId
   * @return set of memberships
   */
  private Set<GroupSet> _addHasMembersRecursively(GroupSet startGroupSet, 
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
        Set<GroupSet> newAdditions = _addHasMembersRecursively(startGroupSet, nextGroupSet, newGroupSet, 
          parentToChildrenMap, ownerGroupId, ownerStemId, creatorUUID, fieldId);
        newGroupSets.addAll(newAdditions);
      }
    }
    
    return newGroupSets;
  }

  /**
   * Add members of <i>member</i> to where <i>group</i> is a member.
   * @param memberGroup 
   * @param groupSetIsMember    <i>Set</i> of <i>GroupSet</i> objects.
   * @param groupSetHasMembers   <i>Set</i> of <i>GroupSet</i> objects.
   * @return set
   * @throws IllegalStateException 
   * @since   1.2.0
   */
  private Set<GroupSet> _addHasMembersToWhereGroupIsMember(Group memberGroup, Set<GroupSet> groupSetIsMember, Set<GroupSet> groupSetHasMembers) 
    throws  IllegalStateException
  {
    Set<GroupSet> groupSets = new LinkedHashSet();

    String creatorUUID  = GrouperSession.staticGrouperSession().getMember().getUuid();
    
    Iterator<GroupSet> isMembersIter = groupSetIsMember.iterator();
    Map<String, Set<GroupSet>> parentToChildrenMap = _getParentToChildrenMap(groupSetHasMembers);

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
      if (fieldId.equals(Group.getDefaultList().getUuid()) && StringUtils.equals(isGS.getOwnerGroupId(), this.getGroup().getUuid())) {
        continue;
      }
      
      GroupSet groupSet = new GroupSet();
      groupSet.setId(GrouperUuid.getUuid());
      groupSet.setCreatorId(creatorUUID);
      groupSet.setDepth(depth + 1);
      groupSet.setParentId(id);
      groupSet.setFieldId(fieldId);
      groupSet.setMemberGroupId(memberGroup.getUuid());
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
        Set<GroupSet> newAdditions = _addHasMembersRecursively(isGS, hasGS, groupSet, parentToChildrenMap,
            ownerGroupId, ownerStemId, creatorUUID, fieldId);
        groupSets.addAll(newAdditions);
      }
    }

    return groupSets;
  } 


  /**
   * 
   * @param memberUUIDs
   * @return set
   * @throws IllegalStateException
   */
  private Set _createNewCompositeMembershipObjects(Set memberUUIDs) 
    throws  IllegalStateException {
    Set               mships  = new LinkedHashSet();
    Iterator          it      = memberUUIDs.iterator();
    while (it.hasNext()) {
      Membership _ms = _createNewCompositeMembershipObject(this.getOwnerGroupId(), (String)it.next(), this.getComposite().getUuid());
      mships.add(_ms);
    }
    return mships;
  }

  /**
   * 
   * @param ownerGroupId
   * @param memberUuid
   * @param viaCompositeId
   * @return membership
   * @throws IllegalStateException
   */
  private Membership _createNewCompositeMembershipObject(String ownerGroupId, String memberUuid, String viaCompositeId)
    throws  IllegalStateException
  {
    GrouperValidator  v;
    Membership _ms;
    _ms = new Membership();
    _ms.setCreatorUuid(GrouperSession.staticGrouperSession().getMember().getUuid());
    _ms.setDepth(0);
    _ms.setFieldId(FieldFinder.findFieldId(this.getField().getName(),
      this.getField().getType().toString(), true));
    _ms.setMemberUuid(memberUuid);
    _ms.setOwnerGroupId(ownerGroupId);
    _ms.setType(Membership.COMPOSITE);
    _ms.setViaCompositeId(viaCompositeId);

    v = CompositeMembershipValidator.validate(_ms);
    if (v.isInvalid()) {
      throw new IllegalStateException( v.getErrorMessage() );
    }

    return _ms;
  }

  /**
   * make sure the composite group is not a submember of either factor
   */
  private void _errorIfCompositeIsSubMember() {
    
    String memberId = this.getGroup().toMember().getUuid();

    Set<Membership> memberships = GrouperDAOFactory.getFactory()
      .getMembership().findAllByGroupOwnerAndMemberAndField(this.getComposite().getLeftFactorUuid(), 
          memberId, Group.getDefaultList());
    
    if (GrouperUtil.length(memberships) > 0) {
      throw new IllegalStateException("Membership paths from a left factor to the composite are not allowed. " 
          + this.getComposite().getLeftFactorUuid() + ", " + memberId);
    }
    memberships = GrouperDAOFactory.getFactory()
      .getMembership().findAllByGroupOwnerAndMemberAndField(this.getComposite().getRightFactorUuid(), 
          memberId, Group.getDefaultList());
    
    if (GrouperUtil.length(memberships) > 0) {
      throw new IllegalStateException("Membership paths from a right factor to the composite are not allowed. " 
          + this.getComposite().getRightFactorUuid() + ", " + memberId);
    }

  }
  
  /**
   * Evaluate the addition of a new composite membership.
   * @throws IllegalStateException 
   * @since   1.2.0
   */
  private void _evaluateAddCompositeMembership()
    throws  IllegalStateException {

    //make sure the composite group is not a member of either factor
    this._errorIfCompositeIsSubMember();

    Set<Membership> composites = new LinkedHashSet<Membership>();
    if      ( this.getComposite().getType().equals(CompositeType.COMPLEMENT) )    {
      composites.addAll( this._evaluateAddCompositeMembershipComplement() );
    }
    else if ( this.getComposite().getType().equals(CompositeType.INTERSECTION) )  {
      composites.addAll( this._evaluateAddCompositeMembershipIntersection() );
    }
    else if ( this.getComposite().getType().equals(CompositeType.UNION) )         {
      composites.addAll( this._evaluateAddCompositeMembershipUnion() );
    }
    else {
      throw new IllegalStateException( E.MOF_CTYPE + this.getComposite().getType().toString() );
    }
    this.addEffectiveSaves(composites);

    this.addSaves( this.getEffectiveSaves() );
    DefaultMemberOf.this.fixComposites(this.getSaves(), new LinkedHashSet(), false);
    
    Set<Group> modifiedGroups = new LinkedHashSet<Group>();
    modifiedGroups.add(this.getGroup());
    
    this.setModifiedGroups(modifiedGroups);
    
    // update the membership type of the group set to 'composite'
    GroupSet selfGroupSet = 
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(this.getGroup(), Group.getDefaultList());
    selfGroupSet.setType(Membership.COMPOSITE);
    this.addUpdate(selfGroupSet);
    
    this._identifyGroupsAndStemsWithMembershipChanges();
  } 

  /**
   * 
   * @return the set of memberships
   * @throws IllegalStateException
   */
  private Set<Membership> _evaluateAddCompositeMembershipComplement() 
    throws  IllegalStateException {

    Set<String> memberUUIDs = GrouperDAOFactory.getFactory().getMember()
      ._internal_membersComplement(this.getComposite().getLeftFactorUuid(),
          this.getComposite().getRightFactorUuid());

    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 

  /**
   * 
   * @return the set of memberships
   * @throws IllegalStateException
   */
  private Set<Membership> _evaluateAddCompositeMembershipIntersection() 
    throws  IllegalStateException {
    Set<String> memberUUIDs = GrouperDAOFactory.getFactory().getMember()
      ._internal_membersIntersection(this.getComposite().getLeftFactorUuid(),
        this.getComposite().getRightFactorUuid());

    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 

  /**
   * 
   * @return the set of memberships
   * @throws IllegalStateException
   */
  private Set<Membership> _evaluateAddCompositeMembershipUnion() 
    throws  IllegalStateException {
    Set<String> memberUUIDs = GrouperDAOFactory.getFactory().getMember()
      ._internal_membersUnion(this.getComposite().getLeftFactorUuid(),
      this.getComposite().getRightFactorUuid());

    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 

  /**
   * TODO 20070531 split; this method has gotten too large
   * @param s
   * @param f
   * @param _m
   * @throws IllegalStateException
   */
  private void _evaluateAddImmediateMembership(GrouperSession s, final Field f, final Member _m) 
    throws  IllegalStateException
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        Membership _ms = new Membership();
        _ms.setCreatorUuid( grouperSession.getMember().getUuid() );
        _ms.setFieldId(FieldFinder.findFieldId(f.getName(), 
            f.getType().toString(), true));
        _ms.setMemberUuid( _m.getUuid() );
        _ms.setOwnerGroupId( DefaultMemberOf.this.getOwnerGroupId() );
        _ms.setOwnerStemId( DefaultMemberOf.this.getOwnerStemId() );
        _ms.setMember(_m);

        if (DefaultMemberOf.this.validateImmediateMembership == true) {
            GrouperValidator v = ImmediateMembershipValidator.validate(_ms);
            if (v.isInvalid()) {
              //throw a specific exception so we can trap this case
              if (StringUtils.equals(v.getErrorMessage(), ImmediateMembershipValidator.INVALID_EXISTS)) {
                throw new MembershipAlreadyExistsException(v.getErrorMessage());
              }
              throw new IllegalStateException( v.getErrorMessage() );
            }
        }

        DefaultMemberOf.this.setField( FieldFinder.find( _ms.getListName(), true ) );

        DefaultMemberOf.this.setMember(_m);
        DefaultMemberOf.this.setMembership(_ms);

        if (_m.getSubjectTypeId().equals("group")) {
  
          Set<GroupSet> results = new LinkedHashSet();
  
          Group memberGroup = _m.toGroup();
          GroupSet parent = null;
          
          // add the immediate group set
          GroupSet immediateGroupSet = new GroupSet();
          immediateGroupSet.setId(GrouperUuid.getUuid());
          immediateGroupSet.setCreatorId(GrouperSession.staticGrouperSession().getMemberUuid());
          immediateGroupSet.setDepth(1);
          immediateGroupSet.setFieldId(f.getUuid());
          immediateGroupSet.setMemberGroupId(memberGroup.getUuid());
          immediateGroupSet.setType(Membership.EFFECTIVE);
          
          if (DefaultMemberOf.this.getGroup() != null) {
            immediateGroupSet.setOwnerGroupId(DefaultMemberOf.this.getGroup().getUuid());
            parent = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(DefaultMemberOf.this.getGroup(), f);
          } else {
            immediateGroupSet.setOwnerStemId(DefaultMemberOf.this.getStem().getUuid());
            parent = GrouperDAOFactory.getFactory().getGroupSet().findSelfStem(DefaultMemberOf.this.getStem(), f);
          }
          
          immediateGroupSet.setParentId(parent.getId());
          results.add(immediateGroupSet);
          
          // Members of _m which are groups
          Set<GroupSet> groupSetHasMembers = DefaultMemberOf.this._findGroupSetsOfMember(memberGroup);
  
          // Add members of _m to owner group set
          results.addAll(DefaultMemberOf.this._addHasMembersToOwner(immediateGroupSet, groupSetHasMembers));
  
          // If we are working on a group, where is it a member and f = "members"
          if (DefaultMemberOf.this.getGroup() != null && DefaultMemberOf.this.getField().equals(Group.getDefaultList())) {
            Set<GroupSet> groupSetIsMember = GrouperDAOFactory.getFactory().getGroupSet().findAllByMemberGroup(DefaultMemberOf.this.getGroup());
  
            // Add _m and members of _m to where owner is member
            results.addAll(DefaultMemberOf.this._addHasMembersToWhereGroupIsMember(memberGroup, groupSetIsMember, groupSetHasMembers));
          }
  
          DefaultMemberOf.this.addEffectiveSaves(results);
        }

        DefaultMemberOf.this.addSave(_ms); // Save the immediate
        DefaultMemberOf.this.addSaves( DefaultMemberOf.this.getEffectiveSaves() );

        // Fix composites
        if (DefaultMemberOf.this.getGroup() != null && DefaultMemberOf.this.getField().equals(Group.getDefaultList())) {
          DefaultMemberOf.this.fixComposites(DefaultMemberOf.this.getSaves(), new LinkedHashSet(), true);
        }

        DefaultMemberOf.this._identifyGroupsAndStemsWithMembershipChanges();
        return null;
      }
      
    });
  } // private void _evaluateAddImmediateMembership(s, f, _m)

 
  /**
   * If memberships are being added to a factor, this method makes sure that the composite
   * membership is correct.
   *
   * @param existingSaves The saves prior to calling this method.
   * @param existingDeletes The deletes prior to calling this method.
   * @param searchMembersOfMember
   */
  private void fixComposites(Set<GrouperAPI> existingSaves, Set<GrouperAPI> existingDeletes, boolean searchMembersOfMember) {

    Set<Membership> existingUpdates = new LinkedHashSet<Membership>();

    // populate maps to later quickly retrieve existing Membership saves and deletes based on the group and the member.
    Iterator iter = existingSaves.iterator();
    while (iter.hasNext()) {
      Object next = iter.next();
      if (next instanceof Membership) {
        Membership m = (Membership)next;
        if (m.getFieldId().equals(Group.getDefaultList().getUuid())) {
          existingUpdates.add(m);
          groupsAndMembersSaves.put(new MultiKey(m.getOwnerGroupId(), m.getMemberUuid()), true);
        }
      }
    }

    iter = existingDeletes.iterator();
    while (iter.hasNext()) {
      Object next = iter.next();
      if (next instanceof Membership) {
        Membership m = (Membership)next;
        if (m.getFieldId().equals(Group.getDefaultList().getUuid())) {
          existingUpdates.add(m);
          groupsAndMembersDeletes.put(new MultiKey(m.getOwnerGroupId(), m.getMemberUuid()), true);
        }
      }
    }

    if (existingUpdates.size() == 0) {
      return;
    }

    Set<GrouperAPI> newSaves = new LinkedHashSet<GrouperAPI>();
    Set<GrouperAPI> newDeletes = new LinkedHashSet<GrouperAPI>();
    
    Iterator<Membership> existingUpdatesIterator = existingUpdates.iterator();
    while (existingUpdatesIterator.hasNext()) {
      Membership existingMembership = existingUpdatesIterator.next();
      
      boolean isSave = true;
      if (existingDeletes.contains(existingMembership)) {
        isSave = false;
      }

      String existingMembershipMemberId = existingMembership.getMemberUuid();
      String existingMembershipOwnerGroupId = existingMembership.getOwnerGroupId();

      Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(existingMembershipOwnerGroupId, true);
      Set<Composite> factorsSet = GrouperDAOFactory.getFactory().getComposite().findAsFactor(group);
    
      Set<GroupSet> isMemberOfOwnerGroup = GrouperDAOFactory.getFactory().getGroupSet()
          .findAllByMemberGroupAndField(group.getUuid(), Group.getDefaultList());
      
      Iterator<GroupSet> isMemberOfOwnerGroupIter = isMemberOfOwnerGroup.iterator();
      while (isMemberOfOwnerGroupIter.hasNext()) {
        String isMemberOfOwnerGroupId = isMemberOfOwnerGroupIter.next().getOwnerGroupId();
        factorsSet.addAll(GrouperDAOFactory.getFactory().getComposite().findAsFactor(isMemberOfOwnerGroupId)); 
        
        if (isSave) {
          groupsAndMembersSaves.put(new MultiKey(isMemberOfOwnerGroupId, existingMembershipMemberId), true);
        } else {
          groupsAndMembersDeletes.put(new MultiKey(isMemberOfOwnerGroupId, existingMembershipMemberId), true);
        }
        
        if (searchMembersOfMember) {
          Member member = existingMembership.getMember();
          try {
            Group g2 = member.toGroup();
            Iterator<Member> members = g2.getMembers().iterator();
            while (members.hasNext()) {
              Member currMember = members.next();
              if (isSave) {
                groupsAndMembersSaves.put(new MultiKey(isMemberOfOwnerGroupId, currMember.getUuid()), true);
              } else {
                groupsAndMembersDeletes.put(new MultiKey(isMemberOfOwnerGroupId, currMember.getUuid()), true);
              }
            }
          } catch (GroupNotFoundException e) {
            // this is okay.
          }
        }
      }
            
      Set<String> memberUuids = new LinkedHashSet<String>();
      memberUuids.add(existingMembershipMemberId);
      
      if (searchMembersOfMember) {
        Member member = existingMembership.getMember();
        try {
          Group g = member.toGroup();
          Iterator<Member> members = g.getMembers().iterator();
          while (members.hasNext()) {
            Member currMember = members.next();
            memberUuids.add(currMember.getUuid());
            
            if (isSave) {
              groupsAndMembersSaves.put(new MultiKey(group.getUuid(), currMember.getUuid()), true);
            } else {
              groupsAndMembersDeletes.put(new MultiKey(group.getUuid(), currMember.getUuid()), true);
            }
          }
        } catch (GroupNotFoundException e) {
          // this is okay.
        }
      }
      
      Iterator<String> memberUuidsIter = memberUuids.iterator();
      while (memberUuidsIter.hasNext()) {
        String memberUuid = memberUuidsIter.next();
        
        Iterator<Composite> factorsIterator = factorsSet.iterator();

        while (factorsIterator.hasNext()) {
          // check to see if the composite and the factors have the member.
          Composite c = factorsIterator.next();
          Group left = GrouperDAOFactory.getFactory().getGroup().findByUuid(c.getLeftFactorUuid(), true);
          Group right = GrouperDAOFactory.getFactory().getGroup().findByUuid(c.getRightFactorUuid(), true);
          Group owner = GrouperDAOFactory.getFactory().getGroup().findByUuid(c.getFactorOwnerUuid(), true);

          // we're not allowing membership paths from a factor to the composite
          if (StringUtils.equals(memberUuid, owner.toMember().getUuid())) {
            throw new IllegalStateException("Membership paths from a factor to the composite are not allowed.");
          }

          boolean rightHasMember = hasMember(right, memberUuid);
          boolean leftHasMember = hasMember(left, memberUuid);
          boolean ownerHasMember = hasMember(owner, memberUuid);
          boolean compositeShouldHaveMember = false;

          // check to see if the composite *should* have the member
          if (c.getType().equals(CompositeType.UNION) && (rightHasMember || leftHasMember)) {
            compositeShouldHaveMember = true;
          } else if (c.getType().equals(CompositeType.INTERSECTION) && (rightHasMember && leftHasMember)) {
            compositeShouldHaveMember = true;
          } else if (c.getType().equals(CompositeType.COMPLEMENT) && (!rightHasMember && leftHasMember)) {
            compositeShouldHaveMember = true;
          }

          // fix the composite membership if necessary
          if (compositeShouldHaveMember && !ownerHasMember) {
            Membership ms = _createNewCompositeMembershipObject(owner.getUuid(), memberUuid, c.getUuid());
            newSaves.add(ms);
          } else if (!compositeShouldHaveMember && ownerHasMember) {
            Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
              owner.getUuid(), memberUuid, Group.getDefaultList(), Membership.COMPOSITE, true);
            newDeletes.add(ms);
          }
        }
      }
    }


    DefaultMemberOf.this.addEffectiveSaves(newSaves);
    DefaultMemberOf.this.addSaves(newSaves);
    DefaultMemberOf.this.addEffectiveDeletes(newDeletes);
    DefaultMemberOf.this.addDeletes(newDeletes);

    fixComposites(newSaves, newDeletes, false);
  }

  /**
   * Checks to see if a subject is a member of a group.  The trick here is to not only check
   * the database, but to also check the current set of saves and deletes.
   *
   * @param g The group
   * @param memberUuid The member UUID.
   * @return if has member
   */
  private boolean hasMember(Group g, String memberUuid) {
    Set mships = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndMemberAndField(
      g.getUuid(), memberUuid, Group.getDefaultList());
    
    int count = mships.size();

    Boolean saves = groupsAndMembersSaves.get(new MultiKey(g.getUuid(), memberUuid));
    if (saves != null) {
      count++;
    }

    Boolean deletes = groupsAndMembersDeletes.get(new MultiKey(g.getUuid(), memberUuid));
    if (deletes != null) {
      count--;
    }

    if (count > 0) {
      return true;
    }

    return false;
  }

  /**
   * 
   * @throws IllegalStateException
   */
  private void _evaluateDeleteCompositeMembership() 
    throws  IllegalStateException
  {
    Iterator<Membership> mshipsIter = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndField( 
      this.getGroup().getUuid(), Group.getDefaultList()).iterator();
    while (mshipsIter.hasNext()) {
      this.addDelete(mshipsIter.next());
    }

    this.fixComposites(new LinkedHashSet(), this.getDeletes(), false);

    Set<Group> modifiedGroups = new LinkedHashSet<Group>();
    modifiedGroups.add(this.getGroup());
    
    this.setModifiedGroups(modifiedGroups);
    
    // update the membership type of the group set to 'immediate'
    GroupSet selfGroupSet = 
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(this.getGroup(), Group.getDefaultList());
    selfGroupSet.setType(Membership.IMMEDIATE);
    this.addUpdate(selfGroupSet);
    
    this._identifyGroupsAndStemsWithMembershipChanges();
  } // private void _evalulateDeleteCompositeMembership()

  /**
   * @param s 
   * @param _ms 
   * @param _m 
   * @throws IllegalStateException 
   * @since   1.2.0
   */
  private void _evaluateDeleteImmediateMembership(GrouperSession s, final Membership _ms, final Member _m) 
    throws  IllegalStateException
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        DefaultMemberOf.this.setField( FieldFinder.find( _ms.getListName(), true ) );
        DefaultMemberOf.this.setMember(_m);
        DefaultMemberOf.this.setMembership(_ms);

        if (_m.getSubjectTypeId().equals("group")) {
          Set<GroupSet> groupSetsToDelete = new LinkedHashSet<GroupSet>();
          
          Group memberGroup = _m.toGroup();

          // get the immediate group set (depth = 1)
          GroupSet groupSet = null;
          if (DefaultMemberOf.this.getGroup() != null) {
            groupSet = GrouperDAOFactory.getFactory().getGroupSet()
              .findImmediateByOwnerGroupAndMemberGroupAndField(_ms.getOwnerGroupId(), memberGroup.getUuid(), DefaultMemberOf.this.getField());
          } else {
            groupSet = GrouperDAOFactory.getFactory().getGroupSet()
              .findImmediateByOwnerStemAndMemberGroupAndField(_ms.getOwnerStemId(), memberGroup.getUuid(), DefaultMemberOf.this.getField());
          }

          // Get all children of this group set
          Set<GroupSet> childResults = GrouperDAOFactory.getFactory().getGroupSet().findAllChildren(groupSet);

          groupSetsToDelete.addAll(childResults);
          groupSetsToDelete.add(groupSet);

          // Find all effective memberships that need deletion
          if (DefaultMemberOf.this.getGroup() != null && DefaultMemberOf.this.getField().equals(Group.getDefaultList())) {
            Set<GroupSet> groupSetIsMember = GrouperDAOFactory.getFactory().getGroupSet().findAllByMemberGroup(DefaultMemberOf.this.getGroup());
            
            Iterator<GroupSet> groupSetIsMemberIter = groupSetIsMember.iterator();
            while (groupSetIsMemberIter.hasNext()) {
              GroupSet currGroupSet = groupSetIsMemberIter.next();
              GroupSet childToDelete = GrouperDAOFactory.getFactory().getGroupSet().findImmediateChildByParentAndMemberGroup(currGroupSet, groupSet.getMemberGroupId());

              Set<GroupSet> childrenOfChildResults = GrouperDAOFactory.getFactory().getGroupSet().findAllChildren(childToDelete);

              groupSetsToDelete.addAll(childrenOfChildResults);
              groupSetsToDelete.add(childToDelete);
            }
          }

          DefaultMemberOf.this.addEffectiveDeletes(groupSetsToDelete);
          DefaultMemberOf.this.addDeletes(DefaultMemberOf.this.getEffectiveDeletes());
        }
        
        DefaultMemberOf.this.addDelete(_ms); // Delete the immediate

        // Fix composites
        if (DefaultMemberOf.this.getGroup() != null && DefaultMemberOf.this.getField().equals(Group.getDefaultList())) {
          DefaultMemberOf.this.fixComposites(new LinkedHashSet(), DefaultMemberOf.this.getDeletes(), true);
        }

        DefaultMemberOf.this._identifyGroupsAndStemsWithMembershipChanges();
        return null;
      }
      
    });
  } // private void _evaluateDeleteImmediateMembership(s, _ms, _m)

  /**
   * Find m's hasMembers which are groups
   * @param memberGroup
   * @return set of GroupSet
   */
  private Set<GroupSet> _findGroupSetsOfMember(Group memberGroup) {
    return GrouperDAOFactory.getFactory().getGroupSet().findAllByGroupOwnerAndField(
        memberGroup, Group.getDefaultList());
  } 

  /**
   * 
   */
  private void _identifyGroupsAndStemsWithMembershipChanges() {
    Set<String> groupChanges = new LinkedHashSet<String>();
    Set<String> stemChanges = new LinkedHashSet<String>();
    
    Set<GrouperAPI> changes = new LinkedHashSet<GrouperAPI>();
    changes.addAll(this.getSaves());
    changes.addAll(this.getDeletes());
    
    Iterator<GrouperAPI> changesIterator = changes.iterator();
    while (changesIterator.hasNext()) {
      GrouperAPI change = changesIterator.next();
      if (change instanceof Membership) {
        Membership ms = (Membership) change;
        if (ms.getListType().equals(FieldType.LIST.toString()) || 
            ms.getListType().equals(FieldType.ACCESS.toString())) {
          groupChanges.add(ms.getOwnerGroupId());
        } else if (ms.getListType().equals(FieldType.NAMING.toString())) {
          stemChanges.add(ms.getOwnerStemId());
        } else {
          throw new IllegalStateException( "unknown membership type: " + ms.getListType() );
        }
      }
    }
    
    this.setGroupIdsWithNewMemberships(groupChanges);
    this.setStemIdsWithNewMemberships(stemChanges);
  }

} 

