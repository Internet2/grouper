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

import edu.internet2.middleware.grouper.Composite;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import edu.internet2.middleware.grouper.internal.dao.StemDAO;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.CompositeMembershipValidator;
import edu.internet2.middleware.grouper.validator.EffectiveMembershipValidator;
import edu.internet2.middleware.grouper.validator.GrouperValidator;
import edu.internet2.middleware.grouper.validator.ImmediateMembershipValidator;

/** 
 * Perform <i>member of</i> calculation.
 * <p/>
 * @author  blair christensen.
 * @version $Id: DefaultMemberOf.java,v 1.5 2008-10-06 16:46:13 shilen Exp $
 * @since   1.2.0
 */
@GrouperIgnoreDbVersion
public class DefaultMemberOf extends BaseMemberOf implements GrouperCloneable {

  private boolean validateImmediateMembership = true;

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

  /** constant for field name for: ownerUUID */
  public static final String FIELD_OWNER_UUID = "ownerUUID";

  /** constant for field name for: saves */
  public static final String FIELD_SAVES = "saves";

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD__M, FIELD__MS, FIELD_C, FIELD_DELETES, 
      FIELD_EFF_DELETES, FIELD_EFF_SAVES, FIELD_F, FIELD_G, 
      FIELD_MODIFIED_GROUPS, FIELD_MODIFIED_STEMS, FIELD_NS, FIELD_OWNER_UUID, 
      FIELD_SAVES);

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
        DefaultMemberOf.this._evaluateAddCompositeMembership();
        DefaultMemberOf.this.addSave( c );
        return null;
      }
      
    });
  } 

  /**
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
   * @since   1.3.1
   */
  public void addImmediateWithoutValidation(GrouperSession s, Group g, Field f, Member _m)
    throws  IllegalStateException 
  {
    this.validateImmediateMembership = false;
    addImmediate(s, g, f, _m);
  } 

  /**
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
   * @since   1.3.1
   */
  public void addImmediateWithoutValidation(GrouperSession s, Stem ns, Field f, Member _m)
    throws  IllegalStateException 
  {
    this.validateImmediateMembership = false;
    addImmediate(s, ns, f, _m);
  } 

  /**
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
   * @since   1.2.0
   */
  public void deleteImmediate(GrouperSession s, Stem ns, Membership _ms, Member _m)
    throws  IllegalStateException
  {
    //note, no need for GrouperSession inverse of control
    this.setStem(ns);
    this._evaluateDeleteImmediateMembership(s, _ms, _m);
  } 


  // PROTECTED INSTANCE METHODS //

  // m->{ "group" | "stem" } = { group or stem uuid = group or stem object }
  // @since   1.2.0 
  public Map identifyGroupsAndStemsToMarkAsModified(Map m, Iterator it) 
    throws  IllegalStateException
  {
    // This method is still a lot bigger and more hackish than I'd like but...

    // So that everything has the same modify time within here
    String        modifierUuid  = GrouperSession.staticGrouperSession().getMember().getUuid();
    long          modifyTime    = new java.util.Date().getTime();

    // TODO 20070531 this is horribly ugly.
    Map<String, Group> groups = new HashMap<String, Group>();
    if (m.containsKey("groups")) {
      groups = (Map) m.get("groups");
    }
    Map<String, Stem> stems = new HashMap<String, Stem>();
    if (m.containsKey("stems")) {
      stems = (Map) m.get("stems");
    }
    
    Membership _ms;
    String        k;
    Group      _g;
    Stem       _ns;
    try {
      GroupDAO  gDAO  = GrouperDAOFactory.getFactory().getGroup();
      StemDAO   nsDAO = GrouperDAOFactory.getFactory().getStem();
      while (it.hasNext()) {
        _ms = (Membership) it.next();
        k   = _ms.getOwnerUuid();
        if      ( _ms.getListType().equals(FieldType.LIST.toString()) || _ms.getListType().equals(FieldType.ACCESS.toString()) ) {
          if ( !groups.containsKey(k) ) {
            _g = gDAO.findByUuid(k);
            _g.setModifierUuid(modifierUuid);
            _g.setModifyTimeLong(modifyTime);
            groups.put(k, _g);
          }
        }
        else if ( _ms.getListType().equals(FieldType.NAMING.toString()) ) {
          if ( !stems.containsKey(k) ) {
            _ns = nsDAO.findByUuid(k);
            _ns.setModifierUuid(modifierUuid);
            _ns.setModifyTimeLong(modifyTime);
            stems.put(k, _ns);
          }
        }
        else {
          throw new IllegalStateException( "unknown membership type: " + _ms.getListType() );
        }
      }
    }
    catch (GroupNotFoundException eGNF) {
      throw new IllegalStateException( "attempt to modify a group that cannot be found: " + eGNF.getMessage() );
    }
    catch (StemNotFoundException eNSNF) {
      throw new IllegalStateException( "attempt to modify a stem that cannot be found: " + eNSNF.getMessage() );
    }
    // add the owner
    if      ( this.getGroup() != null ) {
      _g = this.getGroup();
      _g.setModifierUuid(modifierUuid);
      _g.setModifyTimeLong(modifyTime);
      groups.put( _g.getUuid(), _g );
    }
    else if ( this.getStem() != null )  {
      _ns = this.getStem();
      _ns.setModifierUuid(modifierUuid);
      _ns.setModifyTimeLong(modifyTime);
      stems.put( _ns.getUuid(), _ns );
    }
    else {
      throw new IllegalStateException("MemberOf has no group or stem as owner");
    }
    // assemble the map
    m.put("groups", groups);
    m.put("stems", stems);
    return m;
  }


  // PRIVATE INSTANCE METHODS //

  // Add m's hasMembers to o
  // @since   1.2.0
  private Set _addHasMembersToOwner(Set<Membership> hasMembers) 
    throws  IllegalStateException
  {
    Set<Membership> mships = new LinkedHashSet();
    Membership hasMS;
    Iterator<Membership> it = hasMembers.iterator();

    // cache values outside of iterator
    String ownerUUID = this.getMembership().getOwnerUuid();
    String creatorUUID = GrouperSession.staticGrouperSession().getMember().getUuid();
    String fieldId = this.getMembership().getFieldId();

    Map<String, Set> parentToChildrenMap = _getParentToChildrenMap(hasMembers);
   
    while (it.hasNext()) {
      hasMS = it.next();
      if (hasMS.getDepth() == 0) {
        Set<Membership> newAdditions = _addHasMembersRecursively(this.getMembership(), hasMS, 
          this.getMembership(), parentToChildrenMap, ownerUUID, creatorUUID, fieldId, false);
        mships.addAll(newAdditions);
      }
    }

    return mships;
  } // private Set _addHasMembersToOwner(hasMembers)

  // Given a set of memberships, return a map that will allow retrieval of 
  // the children of a parent membership.
  private Map<String, Set> _getParentToChildrenMap(Set<Membership> members) {
    Map<String, Set> parentToChildrenMap = new HashMap<String, Set>();

    Iterator<Membership> iterator = members.iterator();
    while (iterator.hasNext()) {
      Membership m = iterator.next();
      String parentUUID = m.getParentUuid();

      if (parentUUID != null && !parentUUID.equals("")) {
        Set<Membership> children = parentToChildrenMap.get(parentUUID);
        if (children == null) {
          children = new LinkedHashSet<Membership>();
        }

        children.add(m);
        parentToChildrenMap.put(parentUUID, children);
      }
    }

    return parentToChildrenMap;
  }

  /**
   * Check if the given memberUUID is the memberUUID for the group with a UUID of ownerUUID.
   *
   * @param memberUUID
   * @param ownerUUID
   * @return
   */
  private boolean checkEquality(String memberUUID, String ownerUUID) {
    try {
      Member m1 = GrouperDAOFactory.getFactory().getMember().findByUuid(memberUUID);
      Member m2 = GrouperDAOFactory.getFactory().getGroup().findByUuid(ownerUUID).toMember();
      if (m1.equals(m2)) {
        return true;
      }
    } catch (MemberNotFoundException e) {
      throw new IllegalStateException("Member object not found while testing for circular membership.", e);
    } catch (GroupNotFoundException e) {
      throw new IllegalStateException("Group object not found while testing for circular membership.", e);
    }

    return false;
  }
  
  /**
   * Check if the new membership being added will cause a circular membership.
   *
   * @param newMembership membership being added
   * @param startMembership membership that's a parent of newMembership which will be used
   *                        as a starting point to check if we're forming a circular membership
   * @returns true if the new membership will cause a circular membership.
   */
  private boolean isCircular(Membership newMembership, Membership startMembership) {
    // for the default list, a group should not be an indirect member of itself ....

    if (newMembership.getFieldId().equals(Group.getDefaultList().getUuid()) && 
      checkEquality(newMembership.getMemberUuid(), newMembership.getOwnerUuid())) {
      return true;
    }

    // now let's go through the parent memberships... 
    // if the member uuid of a parent is equal to the member uuid of the new membership,
    // then we have a circular membership.
    if (newMembership.getDepth() < 2) {
      return false;
    }

    try {
      Membership currentMembership = startMembership;
      while (true) {
        if (currentMembership.getMemberUuid().equals(newMembership.getMemberUuid())) {
          return true;
        }
        if (currentMembership.getDepth() > 0) {
          currentMembership = currentMembership.getParentMembership();
        } else {
          break;
        }
      }
    } catch (MembershipNotFoundException e) {
      throw new IllegalStateException(e.getMessage(), e);
    }

    return false;
  }

  private Set<Membership> _addHasMembersRecursively(Membership startMembership, 
    Membership currentMembership, Membership parentMembership, Map<String, Set> parentToChildrenMap, 
    String ownerUUID, String creatorUUID, String fieldId,
    boolean isStartMembershipViaGroupComposite) {

    Membership _newMembership = new Membership();
    _newMembership.setCreatorUuid(creatorUUID);
    _newMembership.setFieldId(fieldId);
    _newMembership.setOwnerUuid(ownerUUID);
    _newMembership.setType(Membership.EFFECTIVE);
    _newMembership.setMemberUuid(currentMembership.getMemberUuid());

    if (isStartMembershipViaGroupComposite) {
      _newMembership.setDepth(startMembership.getDepth());
      _newMembership.setParentUuid(parentMembership.getParentUuid());
    } else {
      _newMembership.setDepth(parentMembership.getDepth() + 1);
      _newMembership.setParentUuid(parentMembership.getUuid());
    }

    
    if (isStartMembershipViaGroupComposite) {
      _newMembership.setViaUuid(startMembership.getViaUuid());
    } else if (currentMembership.getDepth() == 0) {
      _newMembership.setViaUuid(currentMembership.getOwnerUuid());
    } else {
      _newMembership.setViaUuid(currentMembership.getViaUuid());
    }

    // if we're forming a circular path, return an empty Set.
    if (isCircular(_newMembership, startMembership)) {
      return new LinkedHashSet<Membership>();
    }

    
    GrouperValidator v = EffectiveMembershipValidator.validate(_newMembership);
    if (v.isInvalid()) {
      throw new IllegalStateException( v.getErrorMessage() );
    }

    Set<Membership> newMemberships = new LinkedHashSet();
    newMemberships.add(_newMembership);

    Set<Membership> children = parentToChildrenMap.get(currentMembership.getUuid());
    if (children != null) {
      Iterator<Membership> it = children.iterator();
      while (it.hasNext()) {
        Membership hasMS = it.next();
        Set<Membership> newAdditions = _addHasMembersRecursively(startMembership, hasMS, _newMembership, 
          parentToChildrenMap, ownerUUID, creatorUUID, fieldId, isStartMembershipViaGroupComposite);
        newMemberships.addAll(newAdditions);
      }
    }
    
    return newMemberships;
  }

  /**
   * Add members of <i>member</i> to where <i>group</i> is a member.
   * @param   isMember    <i>Set</i> of <i>Membership</i> objects.
   * @param   hasMember   <i>Set</i> of <i>Membership</i> objects.
   * @param   isComposite <i>boolean</i> true if adding a composite membership
   * @since   1.2.0
   */
  private Set _addHasMembersToWhereGroupIsMember(Set<Membership> isMember, Set<Membership> hasMembers, boolean isComposite) 
    throws  IllegalStateException
  {
    Set<Membership> mships = new LinkedHashSet();

    String creatorUUID  = GrouperSession.staticGrouperSession().getMember().getUuid();
    Membership hasMS;
    Membership isMS;
    Membership _ms = null;
    Iterator<Membership> itIM = isMember.iterator();
    Map<String, Set> parentToChildrenMap = _getParentToChildrenMap(hasMembers);

    // lets get all the hasMembers with a depth of 0 before the while loop
    Set<Membership> hasMembersZeroDepth = new LinkedHashSet<Membership>();
    Iterator<Membership> iterator = hasMembers.iterator();
    while (iterator.hasNext()) {
      Membership m = iterator.next();
      if (m.getDepth() == 0) {
        hasMembersZeroDepth.add(m);
      }
    }

    while (itIM.hasNext()) {
      isMS = itIM.next();
      boolean isViaGroupComposite = isViaGroupComposite(isMS);

      String ownerUUID = isMS.getOwnerUuid();
      String fieldId = isMS.getFieldId();
      String type = isMS.getType();
      String uuid = isMS.getUuid();
      int depth = isMS.getDepth();

      // If the isMember's owner is the same as the immediate member's owner and this is for a default member, 
      // then we can skip this isMember.
      if (fieldId.equals(Group.getDefaultList().getUuid()) && isMS.getOwnerUuid().equals(this.getGroup().getUuid())) {
        continue;
      }

      if (!isComposite) {
        if (!type.equals(Membership.COMPOSITE)) {
          _ms = new Membership();
          _ms.setCreatorUuid( creatorUUID );
          if (isViaGroupComposite) {
            _ms.setDepth( depth );
            _ms.setViaUuid( isMS.getViaUuid() );
            _ms.setParentUuid( isMS.getParentUuid() );
          } else {
            _ms.setDepth( depth + 1 );
            _ms.setViaUuid( this.getMembership().getOwnerUuid() );
            _ms.setParentUuid( uuid );
          }
          _ms.setFieldId( fieldId );
          _ms.setMemberUuid( this.getMembership().getMemberUuid() );
          _ms.setOwnerUuid( ownerUUID );
          _ms.setType(Membership.EFFECTIVE);

          GrouperValidator v = EffectiveMembershipValidator.validate(_ms);
          if (v.isInvalid()) {
            throw new IllegalStateException( v.getErrorMessage() );
          }

          // if we're forming a circular path, skip this isMember
          if (isCircular(_ms, isMS)) {
            continue;
          }

          mships.add(_ms);
        } else {
          _ms = new Membership();
          _ms.setCreatorUuid( creatorUUID );
          _ms.setDepth(0);
          _ms.setFieldId( fieldId );
          _ms.setMemberUuid( this.getMembership().getMemberUuid() );
          _ms.setOwnerUuid( ownerUUID );
          _ms.setParentUuid(null);
          _ms.setType(Membership.COMPOSITE);
          _ms.setViaUuid( isMS.getViaUuid() );
          // since the composite membership might already exist, let's check first
          if (!GrouperDAOFactory.getFactory().getMembership().exists(
            _ms.getOwnerUuid(), _ms.getMemberUuid(), _ms.getListName(), _ms.getType())) {
            GrouperValidator v = CompositeMembershipValidator.validate(_ms);
            if (v.isInvalid()) {
              throw new IllegalStateException( v.getErrorMessage() );
            }

            // if we're forming a circular path, skip this isMember
            if (isCircular(_ms, isMS)) {
              continue;
            }

            // Note that _ms may already exist in mships, but since this is a Set, that shouldn't matter.
            mships.add(_ms);
          }
        }
      }

      if (type.equals(Membership.COMPOSITE)) {
        Iterator<Membership> itHM = hasMembers.iterator();
        while (itHM.hasNext()) {
          hasMS = itHM.next();
          _ms = new Membership();
          _ms.setCreatorUuid( creatorUUID );
          _ms.setDepth(0);
          _ms.setFieldId( fieldId );
          _ms.setMemberUuid( hasMS.getMemberUuid() );
          _ms.setOwnerUuid( isMS.getOwnerUuid() );
          _ms.setParentUuid(null);
          _ms.setType(Membership.COMPOSITE);
          _ms.setViaUuid( isMS.getViaUuid() );
          // since the composite membership might already exist, let's check first
          if (!GrouperDAOFactory.getFactory().getMembership().exists(
            _ms.getOwnerUuid(), _ms.getMemberUuid(), _ms.getListName(), _ms.getType())) {
            GrouperValidator v = CompositeMembershipValidator.validate(_ms);
            if (v.isInvalid()) {
              throw new IllegalStateException( v.getErrorMessage() );
            }

            // if we're forming a circular path, skip this hasMember
            if (isCircular(_ms, isMS)) {
              continue;
            }

            // Note that _ms may already exist in mships, but since this is a Set, that shouldn't matter.
            mships.add(_ms);
          }
        } 
      } else {
        Iterator<Membership> itHM = hasMembersZeroDepth.iterator();
        while (itHM.hasNext()) {
          hasMS = itHM.next();
          if (isComposite) {
            Set<Membership> newAdditions = _addHasMembersRecursively(isMS, hasMS, isMS, parentToChildrenMap,
                ownerUUID, creatorUUID, fieldId, isViaGroupComposite);
            mships.addAll(newAdditions);
          } else {
            Set<Membership> newAdditions = _addHasMembersRecursively(isMS, hasMS, _ms, parentToChildrenMap,
                ownerUUID, creatorUUID, fieldId, isViaGroupComposite);
            mships.addAll(newAdditions);
          }
        }
      }
    }

    return mships;
  } 


  /**
   * Checks if the membership is via a composite group.
   *
   * @param membership the membership to check
   * @returns true if the membership is via a composite group.
   * @throws IllegalStateException
   */
  private boolean isViaGroupComposite(Membership membership) 
    throws IllegalStateException {

    // return false if there's no via uuid.
    String uuid = membership.getViaUuid();
    if (uuid == null) {
      return false;
    }

    // we might be adding the composite group right now...
    if (this.getComposite() != null && this.getGroup().getUuid().equals(uuid)) {
      return true;
    }

    // check the database to see if the via uuid is a composite group.
    try {
      Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid);
      GrouperDAOFactory.getFactory().getComposite().findAsOwner(group);
      return true;
    } catch (CompositeNotFoundException e) {
      return false;
    } catch (GroupNotFoundException e) {
      return false;
    }
  }

  // @since   1.2.0
  private Set _createNewCompositeMembershipObjects(Set memberUUIDs) 
    throws  IllegalStateException
  {
    GrouperValidator  v;
    Set               mships  = new LinkedHashSet();
    Membership     _ms;
    Iterator          it      = memberUUIDs.iterator();
    while (it.hasNext()) {
      _ms = new Membership();
      _ms.setCreatorUuid( GrouperSession.staticGrouperSession().getMember().getUuid() );
      _ms.setDepth(0);
      _ms.setFieldId(FieldFinder.findFieldId(this.getField().getName(), 
          this.getField().getType().toString()));

      _ms.setMemberUuid( (String) it.next() );
      _ms.setOwnerUuid( this.getOwnerUuid() );
      _ms.setParentUuid(null);
      _ms.setType(Membership.COMPOSITE);
      _ms.setViaUuid( this.getComposite().getUuid() );
     
      v = CompositeMembershipValidator.validate(_ms);
      if (v.isInvalid()) {
        throw new IllegalStateException( v.getErrorMessage() );
      }
      mships.add(_ms);
    }
    return mships;
  } // private Set _createNewCompositeMembershipObjects(memberUUIDs)

  /**
   * Evaluate the addition of a new composite membership.
   * @since   1.2.0
   */
  private void _evaluateAddCompositeMembership()
    throws  IllegalStateException
  {
    Set composites = new LinkedHashSet();
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
    // we also need to propogate changes up to where the composite owner is a member
    this.addEffectiveSaves( 
      this._addHasMembersToWhereGroupIsMember(
        GrouperDAOFactory.getFactory().getMembership().findAllByMember( this.getGroup().toMember().getUuid() ), 
        composites,
        true
      )
    );

    this.addSaves( this.getEffectiveSaves() );
    this._identifyGroupsAndStemsToMarkAsModified();
  } 

  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipComplement() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getLeftFactorUuid() ) );
    memberUUIDs.removeAll( this._findMemberUUIDs(this.getComposite() .getRightFactorUuid() ) );
    memberUUIDs.remove(this.getGroup().toMember().getUuid());
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 
  
  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipIntersection() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getLeftFactorUuid() ) );
    memberUUIDs.retainAll( this._findMemberUUIDs( this.getComposite().getRightFactorUuid() ) );
    memberUUIDs.remove(this.getGroup().toMember().getUuid());
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 

  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipUnion() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getLeftFactorUuid() ) );
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getRightFactorUuid() ) );
    memberUUIDs.remove(this.getGroup().toMember().getUuid());
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 

  // @since   1.2.0
  // TODO 20070531 split; this method has gotten too large
  private void _evaluateAddImmediateMembership(GrouperSession s, final Field f, final Member _m) 
    throws  IllegalStateException
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        Membership _ms = new Membership();
        _ms.setCreatorUuid( grouperSession.getMember().getUuid() );
        _ms.setFieldId(FieldFinder.findFieldId(f.getName(), 
            f.getType().toString()));
        _ms.setMemberUuid( _m.getUuid() );
        _ms.setOwnerUuid( DefaultMemberOf.this.getOwnerUuid() );

    if (DefaultMemberOf.this.validateImmediateMembership == true) {
        GrouperValidator v = ImmediateMembershipValidator.validate(_ms);
        if (v.isInvalid()) {
          throw new IllegalStateException( v.getErrorMessage() );
        }
    }

        try {
          // TODO 20070531 look into finding a way to avoid SchemaExceptions here
          DefaultMemberOf.this.setField( FieldFinder.find( _ms.getListName() ) );
        }
        catch (SchemaException eS) {
          throw new IllegalStateException( eS.getMessage(), eS );
        }
        DefaultMemberOf.this.setMember(_m);
        DefaultMemberOf.this.setMembership(_ms);

        Set<GrouperAPI> results = new LinkedHashSet();

        // Members of _m if owner is a group
        Set<Membership> hasMembers = DefaultMemberOf.this._findMembersOfMember();

        // If we are working on a group, where is it a member and f = "members"
        Set<Membership> isMember = new LinkedHashSet();
        if (DefaultMemberOf.this.getGroup() != null && DefaultMemberOf.this.getField().equals(Group.getDefaultList())) {
          isMember = GrouperDAOFactory.getFactory().getMembership().findAllByMember( DefaultMemberOf.this.getGroup().toMember().getUuid() );

          // Add _m and members of _m to where owner is member
          results.addAll( DefaultMemberOf.this._addHasMembersToWhereGroupIsMember(isMember, hasMembers, false) );

        }

        // Add members of _m to owner
        results.addAll(DefaultMemberOf.this._addHasMembersToOwner(hasMembers));

        DefaultMemberOf.this.addEffectiveSaves(results);

        DefaultMemberOf.this.addSave(_ms); // Save the immediate
        DefaultMemberOf.this.addSaves( DefaultMemberOf.this.getEffectiveSaves() );
        DefaultMemberOf.this._identifyGroupsAndStemsToMarkAsModified();
        return null;
      }
      
    });
  } // private void _evaluateAddImmediateMembership(s, f, _m)

  // @since   1.2.0
  private void _evaluateDeleteCompositeMembership() 
    throws  IllegalStateException
  {
    Membership _ms;
    DefaultMemberOf      mof;
    Iterator      it  = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField( 
      this.getGroup().getUuid(), Group.getDefaultList() 
    ).iterator();
    try {
      MemberDAO dao = GrouperDAOFactory.getFactory().getMember();
      while (it.hasNext()) {
        _ms = (Membership) it.next();
        mof = new DefaultMemberOf();
        mof.deleteImmediate( 
          GrouperSession.staticGrouperSession(), this.getGroup(), _ms, dao.findByUuid( _ms.getMemberUuid() ) 
        );
        this.addDeletes( mof.getDeletes() );
      }
    }
    catch (MemberNotFoundException eMNF) {
      throw new IllegalStateException( eMNF.getMessage(), eMNF );
    }
    this._identifyGroupsAndStemsToMarkAsModified();
  } // private void _evalulateDeleteCompositeMembership()

  /**
   * @since   1.2.0
   */
  private void _evaluateDeleteImmediateMembership(GrouperSession s, final Membership _ms, final Member _m) 
    throws  IllegalStateException
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        try {
          DefaultMemberOf.this.setField( FieldFinder.find( _ms.getListName() ) );
        }
        catch (SchemaException eS) {
          throw new IllegalStateException( eS.getMessage(), eS );
        }
        DefaultMemberOf.this.setMember(_m);
        DefaultMemberOf.this.setMembership(_ms);

        // Find child memberships that need deletion
        Set       children  = new LinkedHashSet();
        Iterator  it        = MembershipFinder.internal_findAllChildrenNoPriv(_ms).iterator();
        while (it.hasNext()) {
          children.add( (Membership) it.next() );
        }
        DefaultMemberOf.this.addEffectiveDeletes(children);
        // Find all effective memberships that need deletion
        try {
          DefaultMemberOf.this.addEffectiveDeletes( MembershipFinder.internal_findAllForwardMembershipsNoPriv(_ms, children) );
        }
        catch (SchemaException eS) {
          throw new IllegalStateException( eS.getMessage(), eS );
        }

        // And now set everything else
        DefaultMemberOf.this.addDeletes( DefaultMemberOf.this.getEffectiveDeletes() );
        DefaultMemberOf.this._identifyGroupsAndStemsToMarkAsModified();

        DefaultMemberOf.this.addDelete(_ms); // Delete the immediate
        return null;
      }
      
    });
  } // private void _evaluateDeleteImmediateMembership(s, _ms, _m)

  // Find m's hasMembers
  private Set<Membership> _findMembersOfMember() {
    Set<Membership> hasMembers = new LinkedHashSet();
    if (this.getMember().getSubjectTypeId().equals("group")) {
      hasMembers = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField(
        this.getMember().getSubjectId(), Group.getDefaultList()
      );
    }
    return hasMembers;
  } // private Set _findMembersOfMember()

  // Given a group uuid, find the uuids of all of its members
  // @since   1.2.0
  private Set _findMemberUUIDs(String groupUUID) 
    throws  IllegalStateException
  {
    try {
      Set       memberUUIDs = new LinkedHashSet();
      Group  _g          = GrouperDAOFactory.getFactory().getGroup().findByUuid(groupUUID);
      Iterator  it          = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField(
         _g.getUuid(), Group.getDefaultList() 
      ).iterator();
      while (it.hasNext()) {
        memberUUIDs.add( ( (Membership) it.next() ).getMemberUuid() );  
      }
      return memberUUIDs;
    }
    catch (GroupNotFoundException eGNF) {
      throw new IllegalStateException( eGNF.getMessage(), eGNF );
    }
  } // private Set _findMemberUUIDs(groupUUID)

  // @since   1.2.0
  private void _identifyGroupsAndStemsToMarkAsModified() {
    Map modified  = new HashMap();
    modified      = this.identifyGroupsAndStemsToMarkAsModified( modified, this.getEffectiveSaves().iterator() );
    modified      = this.identifyGroupsAndStemsToMarkAsModified( modified, this.getEffectiveDeletes().iterator() );
    Map groups    = (Map) modified.get("groups");
    Map stems     = (Map) modified.get("stems");
    this.setModifiedGroups( new LinkedHashSet( groups.values() ) );
    this.setModifiedStems( new LinkedHashSet( stems.values() ) );
  } // private void _identifyGroupsAndStemsToMarkAsModified()

} 

