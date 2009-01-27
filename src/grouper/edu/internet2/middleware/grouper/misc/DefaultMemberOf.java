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
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.exception.CompositeNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MembershipAlreadyExistsException;
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
 * @version $Id: DefaultMemberOf.java,v 1.11 2009-01-27 12:09:24 mchyzer Exp $
 * @since   1.2.0
 */
@GrouperIgnoreDbVersion
public class DefaultMemberOf extends BaseMemberOf implements GrouperCloneable {

  /** */
  private boolean validateImmediateMembership = true;

  /** */
  @GrouperIgnoreFieldConstant @GrouperIgnoreClone @GrouperIgnoreDbVersion
  private Map<MultiKey, Set> groupsAndMembersSaves = new HashMap<MultiKey, Set>();

  /** */
  @GrouperIgnoreFieldConstant @GrouperIgnoreClone @GrouperIgnoreDbVersion
  private Map<MultiKey, Set> groupsAndMembersDeletes = new HashMap<MultiKey, Set>();

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
   * m->{ "group" | "stems" } = { group or stem uuid = group or stem object }
   * @since   1.2.0 
   * @param m 
   * @param it 
   * @return  map
   * @throws IllegalStateException 
   * 
   */
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
    String        ownerGroupId;
    String        ownerStemId;
    Group      _g;
    Stem       _ns;
    try {
      GroupDAO  gDAO  = GrouperDAOFactory.getFactory().getGroup();
      StemDAO   nsDAO = GrouperDAOFactory.getFactory().getStem();
      while (it.hasNext()) {
        _ms = (Membership) it.next();
        ownerGroupId   = _ms.getOwnerGroupId();
        ownerStemId   = _ms.getOwnerStemId();
        if      ( _ms.getListType().equals(FieldType.LIST.toString()) || _ms.getListType().equals(FieldType.ACCESS.toString()) ) {
          if ( !groups.containsKey(ownerGroupId) ) {
            _g = gDAO.findByUuid(ownerGroupId);
            _g.setModifierUuid(modifierUuid);
            _g.setModifyTimeLong(modifyTime);
            _g.setDontSetModified(true);
            groups.put(ownerGroupId, _g);
          }
        }
        else if ( _ms.getListType().equals(FieldType.NAMING.toString()) ) {
          if ( !stems.containsKey(ownerStemId) ) {
            _ns = nsDAO.findByUuid(ownerStemId);
            _ns.setModifierUuid(modifierUuid);
            _ns.setModifyTimeLong(modifyTime);
            stems.put(ownerStemId, _ns);
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

  /**
   * Add m's hasMembers to o
   * @since   1.2.0
   * @param hasMembers
   * @return set
   * @throws IllegalStateException
   */
  private Set _addHasMembersToOwner(Set<Membership> hasMembers) 
    throws  IllegalStateException
  {
    Set<Membership> mships = new LinkedHashSet();
    Membership hasMS;
    Iterator<Membership> it = hasMembers.iterator();

    // cache values outside of iterator
    String ownerGroupId = this.getMembership().getOwnerGroupId();
    String ownerStemId = this.getMembership().getOwnerStemId();
    String creatorUUID = GrouperSession.staticGrouperSession().getMember().getUuid();
    String fieldId = this.getMembership().getFieldId();

    Map<String, Set> parentToChildrenMap = _getParentToChildrenMap(hasMembers);
   
    while (it.hasNext()) {
      hasMS = it.next();
      if (hasMS.getDepth() == 0) {
        Set<Membership> newAdditions = _addHasMembersRecursively(this.getMembership(), hasMS, 
          this.getMembership(), parentToChildrenMap, ownerGroupId, ownerStemId, creatorUUID, fieldId);
        mships.addAll(newAdditions);
      }
    }

    return mships;
  }

  /**
   * Given a set of memberships, return a map that will allow retrieval of 
   * the children of a parent membership.
   * @param members
   * @return the map
   */
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
   * @param ownerGroupId
   * @return if equal
   */
  private boolean checkEquality(String memberUUID, String ownerGroupId) {
    try {
      String memberUUID2 = GrouperDAOFactory.getFactory().getGroup().findByUuid(ownerGroupId).toMember().getUuid();
      if (memberUUID.equals(memberUUID2)) {
        return true;
      }
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
   * @return if circular
   * @returns true if the new membership will cause a circular membership.
   */
  private boolean isCircular(Membership newMembership, Membership startMembership) {
    // for the default list, a group should not be an indirect member of itself ....

    if (newMembership.getFieldId().equals(Group.getDefaultList().getUuid()) && 
      checkEquality(newMembership.getMemberUuid(), newMembership.getOwnerGroupId())) {
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

  /**
   * 
   * @param startMembership
   * @param currentMembership
   * @param parentMembership
   * @param parentToChildrenMap
   * @param ownerGroupId 
   * @param ownerStemId 
   * @param creatorUUID
   * @param fieldId
   * @return set of memberships
   */
  private Set<Membership> _addHasMembersRecursively(Membership startMembership, 
    Membership currentMembership, Membership parentMembership, Map<String, Set> parentToChildrenMap, 
    String ownerGroupId, String ownerStemId, String creatorUUID, String fieldId) {

    Membership _newMembership = new Membership();
    _newMembership.setCreatorUuid(creatorUUID);
    _newMembership.setFieldId(fieldId);
    _newMembership.setOwnerGroupId(ownerGroupId);
    _newMembership.setOwnerStemId(ownerStemId);
    _newMembership.setType(Membership.EFFECTIVE);
    _newMembership.setMemberUuid(currentMembership.getMemberUuid());
    _newMembership.setDepth(parentMembership.getDepth() + 1);
    _newMembership.setParentUuid(parentMembership.getUuid());

    if (currentMembership.getDepth() == 0) {
      _newMembership.setViaGroupId(currentMembership.getOwnerGroupId());
    } else {
      _newMembership.setViaGroupId(currentMembership.getViaGroupId());
      _newMembership.setViaCompositeId(currentMembership.getViaCompositeId());
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
          parentToChildrenMap, ownerGroupId, ownerStemId, creatorUUID, fieldId);
        newMemberships.addAll(newAdditions);
      }
    }
    
    return newMemberships;
  }

  /**
   * Add members of <i>member</i> to where <i>group</i> is a member.
   * @param   isMember    <i>Set</i> of <i>Membership</i> objects.
   * @param   hasMembers   <i>Set</i> of <i>Membership</i> objects.
   * @param   isComposite <i>boolean</i> true if adding a composite membership
   * @return  set
   * @throws IllegalStateException 
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

      String ownerGroupId = isMS.getOwnerGroupId();
      String ownerStemId = isMS.getOwnerStemId();
      String fieldId = isMS.getFieldId();
      String type = isMS.getType();
      String uuid = isMS.getUuid();
      int depth = isMS.getDepth();

      // If the isMember's owner is the same as the immediate member's owner and this is for a default member, 
      // then we can skip this isMember.
      if (fieldId.equals(Group.getDefaultList().getUuid()) && StringUtils.equals(isMS.getOwnerGroupId(),this.getGroup().getUuid())) {
        continue;
      }

      // we're handling composites separately based on a factor membership changing
      if (type.equals(Membership.COMPOSITE) || isViaGroupComposite) {
        continue;
      }
      if (!isComposite) {
        _ms = new Membership();
        _ms.setCreatorUuid( creatorUUID );
        _ms.setDepth( depth + 1 );
        _ms.setViaGroupId( this.getMembership().getOwnerGroupId() );
        _ms.setParentUuid( uuid );
        _ms.setFieldId( fieldId );
        _ms.setMemberUuid( this.getMembership().getMemberUuid() );
        _ms.setOwnerGroupId( ownerGroupId );
        _ms.setOwnerStemId( ownerStemId );
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
      }

      Iterator<Membership> itHM = hasMembersZeroDepth.iterator();
      while (itHM.hasNext()) {
        hasMS = itHM.next();
        if (isComposite) {
          Set<Membership> newAdditions = _addHasMembersRecursively(isMS, hasMS, isMS, parentToChildrenMap,
              ownerGroupId, ownerStemId, creatorUUID, fieldId);
          mships.addAll(newAdditions);
        } else {
          Set<Membership> newAdditions = _addHasMembersRecursively(isMS, hasMS, _ms, parentToChildrenMap,
              ownerGroupId, ownerStemId, creatorUUID, fieldId);
          mships.addAll(newAdditions);
        }
      }
    }

    return mships;
  } 


  /**
   * Checks if the membership is via a composite group.
   *
   * @param membership the membership to check
   * @return true if the membership is via a composite group.
   * @throws IllegalStateException
   */
  private boolean isViaGroupComposite(Membership membership) 
    throws IllegalStateException {

    // return false if there's no via uuid.
    String uuid = membership.getViaGroupId();
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
      this.getField().getType().toString()));
    _ms.setMemberUuid(memberUuid);
    _ms.setOwnerGroupId(ownerGroupId);
    _ms.setParentUuid(null);
    _ms.setType(Membership.COMPOSITE);
    _ms.setViaCompositeId(viaCompositeId);

    v = CompositeMembershipValidator.validate(_ms);
    if (v.isInvalid()) {
      throw new IllegalStateException( v.getErrorMessage() );
    }

    return _ms;
  }

  /**
   * Evaluate the addition of a new composite membership.
   * @throws IllegalStateException 
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
    DefaultMemberOf.this.fixComposites(this.getSaves(), new LinkedHashSet());

    this._identifyGroupsAndStemsToMarkAsModified();
  } 

  /**
   * 
   * @return set
   * @throws IllegalStateException
   */
  private Set _evaluateAddCompositeMembershipComplement() 
    throws  IllegalStateException {
    Set memberUUIDs = new LinkedHashSet();
    String compositeGroupUuid = this.getGroup().toMember().getUuid();
    Set<String> leftMembers = this._findMemberUUIDs(this.getComposite().getLeftFactorUuid());
    Set<String> rightMembers = this._findMemberUUIDs(this.getComposite().getRightFactorUuid());

    if (leftMembers.contains(compositeGroupUuid) || rightMembers.contains(compositeGroupUuid)) {
      throw new IllegalStateException("Membership paths from a factor to the composite are not allowed.");
    }

    memberUUIDs.addAll(leftMembers);
    memberUUIDs.removeAll(rightMembers);

    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 

  /**
   * 
   * @return set
   * 
   * @throws IllegalStateException
   */
  private Set _evaluateAddCompositeMembershipIntersection() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    String compositeGroupUuid = this.getGroup().toMember().getUuid();
    Set<String> leftMembers = this._findMemberUUIDs(this.getComposite().getLeftFactorUuid());
    Set<String> rightMembers = this._findMemberUUIDs(this.getComposite().getRightFactorUuid());

    if (leftMembers.contains(compositeGroupUuid) || rightMembers.contains(compositeGroupUuid)) {
      throw new IllegalStateException("Membership paths from a factor to the composite are not allowed.");
    }

    memberUUIDs.addAll(leftMembers);
    memberUUIDs.retainAll(rightMembers);

    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 

  /**
   * 
   * @return set
   * @throws IllegalStateException
   */
  private Set _evaluateAddCompositeMembershipUnion() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    String compositeGroupUuid = this.getGroup().toMember().getUuid();
    Set<String> leftMembers = this._findMemberUUIDs(this.getComposite().getLeftFactorUuid());
    Set<String> rightMembers = this._findMemberUUIDs(this.getComposite().getRightFactorUuid());

    if (leftMembers.contains(compositeGroupUuid) || rightMembers.contains(compositeGroupUuid)) {
      throw new IllegalStateException("Membership paths from a factor to the composite are not allowed.");
    }

    memberUUIDs.addAll(leftMembers);
    memberUUIDs.addAll(rightMembers);

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
            f.getType().toString()));
        _ms.setMemberUuid( _m.getUuid() );
        _ms.setOwnerGroupId( DefaultMemberOf.this.getOwnerGroupId() );
        _ms.setOwnerStemId( DefaultMemberOf.this.getOwnerStemId() );

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

        // Add members of _m to owner
        results.addAll(DefaultMemberOf.this._addHasMembersToOwner(hasMembers));

        // If we are working on a group, where is it a member and f = "members"
        if (DefaultMemberOf.this.getGroup() != null && DefaultMemberOf.this.getField().equals(Group.getDefaultList())) {
          Set<Membership> isMember = GrouperDAOFactory.getFactory().getMembership().findAllByMember( DefaultMemberOf.this.getGroup().toMember().getUuid() );

          // Add _m and members of _m to where owner is member
          results.addAll( DefaultMemberOf.this._addHasMembersToWhereGroupIsMember(isMember, hasMembers, false) );
        }

        DefaultMemberOf.this.addEffectiveSaves(results);

        DefaultMemberOf.this.addSave(_ms); // Save the immediate
        DefaultMemberOf.this.addSaves( DefaultMemberOf.this.getEffectiveSaves() );

        // Fix composites
        if (DefaultMemberOf.this.getGroup() != null && DefaultMemberOf.this.getField().equals(Group.getDefaultList())) {
          DefaultMemberOf.this.fixComposites(DefaultMemberOf.this.getSaves(), new LinkedHashSet());
        }

        DefaultMemberOf.this._identifyGroupsAndStemsToMarkAsModified();
        return null;
      }
      
    });
  } // private void _evaluateAddImmediateMembership(s, f, _m)

 
  /**
   * If memberships are being added to a factor, this method makes sure that the composite
   * membership is correct and also corrects all groups and stems that have the composite group
   * as a member.
   *
   * @param existingSaves The saves prior to calling this method.
   * @param existingDeletes The deletes prior to calling this method.
   */
  private void fixComposites(Set<GrouperAPI> existingSaves, Set<GrouperAPI> existingDeletes) {

    Set<Membership> existingUpdates = new LinkedHashSet<Membership>();

    // populate maps to later quickly retrieve existing Membership saves and deletes based on the group and the member.
    Iterator iter = existingSaves.iterator();
    while (iter.hasNext()) {
      Object next = iter.next();
      if (next instanceof Membership) {
        Membership m = (Membership)next;
        if (m.getFieldId().equals(Group.getDefaultList().getUuid())) {
          existingUpdates.add(m);
          Set<Membership> mships = groupsAndMembersSaves.get(new MultiKey(m.getOwnerGroupId(), m.getMemberUuid()));
          if (mships == null) {
            mships = new LinkedHashSet<Membership>();
          }
          mships.add(m);
          groupsAndMembersSaves.put(new MultiKey(m.getOwnerGroupId(), m.getMemberUuid()), mships);
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
          Set<Membership> mships = groupsAndMembersDeletes.get(new MultiKey(m.getOwnerGroupId(), m.getMemberUuid()));
          if (mships == null) {
            mships = new LinkedHashSet<Membership>();
          }
          mships.add(m);
          groupsAndMembersDeletes.put(new MultiKey(m.getOwnerGroupId(), m.getMemberUuid()), mships);
        }
      }
    }

    if (existingUpdates.size() == 0) {
      return;
    }

    Set<GrouperAPI> newSaves = new LinkedHashSet<GrouperAPI>();
    Set<GrouperAPI> newDeletes = new LinkedHashSet<GrouperAPI>();

    // cache to keep track of the composites for some factors.
    // this should probably be done using ehcache...
    Map<String, Set> factorsToComposites = new HashMap<String, Set>();
    
    Iterator<Membership> existingUpdatesIterator = existingUpdates.iterator();
    while (existingUpdatesIterator.hasNext()) {
      Membership existingMembership = existingUpdatesIterator.next();

      String memberUuid = existingMembership.getMemberUuid();
      String ownerGroupId = existingMembership.getOwnerGroupId();

      try {
        Iterator<Composite> factorsIterator;

        if (factorsToComposites.containsKey(ownerGroupId)) {
          factorsIterator = factorsToComposites.get(ownerGroupId).iterator();
        } else {
          Group g = GrouperDAOFactory.getFactory().getGroup().findByUuid(ownerGroupId);
          Set<Composite> factorsSet = GrouperDAOFactory.getFactory().getComposite().findAsFactor(g);
          factorsIterator = factorsSet.iterator();
          factorsToComposites.put(ownerGroupId, factorsSet);
        }

        while (factorsIterator.hasNext()) {
          // check to see if the composite and the factors have the member.
          Composite c = factorsIterator.next();
          Group left = GrouperDAOFactory.getFactory().getGroup().findByUuid(c.getLeftFactorUuid());
          Group right = GrouperDAOFactory.getFactory().getGroup().findByUuid(c.getRightFactorUuid());
          Group owner = GrouperDAOFactory.getFactory().getGroup().findByUuid(c.getFactorOwnerUuid());

          // we're not allowing membership paths from a factor to the composite
          if (checkEquality(memberUuid, owner.getUuid()) == true) {
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

            Set<Membership> isMember = GrouperDAOFactory.getFactory().getMembership().findAllByMember(ms.getGroup().toMember().getUuid());
            Set<Membership> hasMember = new LinkedHashSet<Membership>();
            hasMember.add(ms);
            newSaves.addAll(_addHasMembersToWhereGroupIsMember(isMember, hasMember, true));
          } else if (!compositeShouldHaveMember && ownerHasMember) {
            Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
              owner.getUuid(), memberUuid, Group.getDefaultList(), Membership.COMPOSITE);
            newDeletes.add(ms);

            Set<Membership> forwardMemberships = MembershipFinder.internal_findAllForwardMembershipsNoPriv(ms, new LinkedHashSet());
            newDeletes.addAll(forwardMemberships);
          }
        }
      } catch (GroupNotFoundException e) {
        // this should not happen
        throw new IllegalStateException(e.getMessage(), e);
      } catch (MembershipNotFoundException e) {
        // this should not happen
        throw new IllegalStateException(e.getMessage(), e);
      } catch (SchemaException e) {
        // this should not happen
        throw new IllegalStateException(e.getMessage(), e);
      }
    }


    DefaultMemberOf.this.addEffectiveSaves(newSaves);
    DefaultMemberOf.this.addSaves(newSaves);
    DefaultMemberOf.this.addEffectiveDeletes(newDeletes);
    DefaultMemberOf.this.addDeletes(newDeletes);

    fixComposites(newSaves, newDeletes);
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

    Set<Membership> saves = groupsAndMembersSaves.get(new MultiKey(g.getUuid(), memberUuid));
    if (saves != null) {
      Iterator<Membership> i = saves.iterator();
      while (i.hasNext()) {
        Membership m = i.next();
        mships.add(m);
      }
    }

    Set<Membership> deletes = groupsAndMembersDeletes.get(new MultiKey(g.getUuid(), memberUuid));
    if (deletes != null) {
      Iterator<Membership> i = deletes.iterator();
      while (i.hasNext()) {
        Membership m = i.next();
        mships.remove(m);
      }
    }

    if (mships.size() > 0) {
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
    Membership _ms;
    DefaultMemberOf      mof;
    Iterator      it  = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndField( 
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

    this.fixComposites(new LinkedHashSet(), this.getDeletes());
    this._identifyGroupsAndStemsToMarkAsModified();
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
          if (DefaultMemberOf.this.getGroup() != null && DefaultMemberOf.this.getField().equals(Group.getDefaultList())) {
          DefaultMemberOf.this.addEffectiveDeletes( MembershipFinder.internal_findAllForwardMembershipsNoPriv(_ms, children) );
        }
        }
        catch (SchemaException eS) {
          throw new IllegalStateException( eS.getMessage(), eS );
        }

        // And now set everything else
        DefaultMemberOf.this.addDeletes( DefaultMemberOf.this.getEffectiveDeletes() );
        DefaultMemberOf.this.addDelete(_ms); // Delete the immediate

        // Fix composites
        if (DefaultMemberOf.this.getGroup() != null && DefaultMemberOf.this.getField().equals(Group.getDefaultList())) {
          DefaultMemberOf.this.fixComposites(new LinkedHashSet(), DefaultMemberOf.this.getDeletes());
        }

        DefaultMemberOf.this._identifyGroupsAndStemsToMarkAsModified();
        return null;
      }
      
    });
  } // private void _evaluateDeleteImmediateMembership(s, _ms, _m)

  /**
   * Find m's hasMembers
   * @return set of memberships
   */
  private Set<Membership> _findMembersOfMember() {
    Set<Membership> hasMembers = new LinkedHashSet();
    if (this.getMember().getSubjectTypeId().equals("group")) {
      hasMembers = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndField(
        this.getMember().getSubjectId(), Group.getDefaultList()
      );
    }
    return hasMembers;
  } 

  /**
   * Given a group uuid, find the uuids of all of its members
   * @param groupUUID
   * @return set
   * @throws IllegalStateException
   */
  private Set _findMemberUUIDs(String groupUUID) 
    throws  IllegalStateException {
    try {
      Set       memberUUIDs = new LinkedHashSet();
      Group  _g          = GrouperDAOFactory.getFactory().getGroup().findByUuid(groupUUID);
      Iterator  it          = GrouperDAOFactory.getFactory().getMembership().findAllByGroupOwnerAndField(
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
  } 

  /**
   * 
   */
  private void _identifyGroupsAndStemsToMarkAsModified() {
    Map modified  = new HashMap();
    modified      = this.identifyGroupsAndStemsToMarkAsModified( modified, this.getEffectiveSaves().iterator() );
    modified      = this.identifyGroupsAndStemsToMarkAsModified( modified, this.getEffectiveDeletes().iterator() );
    Map groups    = (Map) modified.get("groups");
    Map stems     = (Map) modified.get("stems");
    this.setModifiedGroups( new LinkedHashSet( groups.values() ) );
    this.setModifiedStems( new LinkedHashSet( stems.values() ) );
  } 

} 

