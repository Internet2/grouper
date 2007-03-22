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

package edu.internet2.middleware.grouper;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;

/** 
 * Perform <i>member of</i> calculation.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberOf.java,v 1.50 2007-03-22 16:40:04 blair Exp $
 */
class MemberOf extends BaseMemberOf {

  // PRIVATE INSTANCE VARIABLES // TODO 20070221 move these to "BaseMemberOf"?
  private Set deletes     = new LinkedHashSet();
  private Set effDeletes  = new LinkedHashSet();
  private Set effSaves    = new LinkedHashSet();
  private Set saves       = new LinkedHashSet();


  // PROTECTED INSTANCE METHODS //
 
  // @since   1.2.0
  protected void addComposite(GrouperSession s, Group g, Composite c)
    throws  IllegalStateException
  {
    this.setComposite(c);
    this.setGroup(g);
    this.setSession(s);
    this._evaluateAddCompositeMembership(); // find memberships to add
    this.saves.add(c);                      // add the composite
  } // protected void addComposite(s, g, c)

  // @since   1.2.0
  protected void deleteComposite(GrouperSession s, Group g, Composite c)
    throws  IllegalStateException
  {
    this.setComposite(c);
    this.setGroup(g);
    this.setSession(s);
    this._evaluateDeleteCompositeMembership();  // find memberships to delete
    this.deletes.add(c);                        // delete the composite
  } // protected void deleteComposite(s, o, c)

  // @since   1.2.0
  protected void addImmediate(GrouperSession s, Group g, Field f, MemberDTO _m)
    throws  IllegalStateException 
  {
    this.setGroup(g);
    this._evaluateAddImmediateMembership(s, f, _m);
  } // protected void addImmediate(s, g, f, _m)

  // @since   1.2.0
  protected void addImmediate(GrouperSession s, Stem ns, Field f, MemberDTO _m)
    throws  IllegalStateException
  {
    this.setStem(ns);
    this._evaluateAddImmediateMembership(s, f, _m);
  } // protected void addImmediate(s, ns, f, _m)

  // @since   1.2.0
  protected void deleteImmediate(GrouperSession s, Group g, MembershipDTO _ms, MemberDTO _m)
    throws  IllegalStateException
  {
    this.setGroup(g);
    this._evaluateDeleteImmediateMembership(s, _ms, _m);
  } // protected void deleteImmediate(s, g, ms, m)

  // @since   1.2.0
  protected void deleteImmediate(GrouperSession s, Stem ns, MembershipDTO _ms, MemberDTO _m)
    throws  IllegalStateException
  {
    this.setStem(ns);
    this._evaluateDeleteImmediateMembership(s, _ms, _m);
  } // protected void deleteImmediate(s, ns, ms, m)


  // PROTECTED INSTANCE METHODS //

  // m->{ "group" | "stem" } = { group or stem uuid = group or stem object }
  // @since   1.2.0 
  protected Map identifyGroupsAndStemsToMarkAsModified(Map m, Iterator it) 
    throws  IllegalStateException
  {
    // This method is still a lot bigger and more hackish than I'd like but...

    // So that everything has the same modify time within here
    String        modifierUuid  = this.getSession().getMember().getUuid();
    long          modifyTime    = new java.util.Date().getTime();

    // TODO 20070214 this is a little hackish
    Map           groups        = new HashMap();
    if (m.containsKey("groups")) {
      groups = (Map) m.get("groups");
    }
    Map           stems         = new HashMap();
    if (m.containsKey("stems")) {
      stems = (Map) m.get("stems");
    }
    
    MembershipDTO _ms;
    String        k;
    GroupDTO      _g;
    StemDTO       _ns;
    try {
      while (it.hasNext()) {
        _ms = (MembershipDTO) it.next();
        k   = _ms.getOwnerUuid();
        if      ( _ms.getListType().equals(FieldType.LIST.toString()) || _ms.getListType().equals(FieldType.ACCESS.toString()) ) {
          if ( !groups.containsKey(k) ) {
            _g = HibernateGroupDAO.findByUuid(k);
            _g.setModifierUuid(modifierUuid);
            _g.setModifyTime(modifyTime);
            groups.put(k, _g);
          }
        }
        else if ( _ms.getListType().equals(FieldType.NAMING.toString()) ) {
          if ( !stems.containsKey(k) ) {
            _ns = HibernateStemDAO.findByUuid(k);
            _ns.setModifierUuid(modifierUuid);
            _ns.setModifyTime(modifyTime);
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
      _g = this.getGroup().getDTO();
      _g.setModifierUuid(modifierUuid);
      _g.setModifyTime(modifyTime);
      groups.put( _g.getUuid(), _g );
    }
    else if ( this.getStem() != null )  {
      _ns = this.getStem().getDTO();
      _ns.setModifierUuid(modifierUuid);
      _ns.setModifyTime(modifyTime);
      stems.put( _ns.getUuid(), _ns );
    }
    else {
      throw new IllegalStateException("MemberOf has no group or stem as owner");
    }
    // assemble the map
    m.put("groups", groups);
    m.put("stems", stems);
    return m;
  } // protected Map identifyGroupsAndStemsToMarkAsModified(m, it)

  // @since   1.2.0
  protected Set internal_getDeletes() {
    return this.deletes;
  } // protected Set internal_getDeletes()

  // @since   1.2.0
  protected Set internal_getEffDeletes() {
    return this.effDeletes;
  } // protected Set internal_getEffDeletes()

  // @since   1.2.0
  protected Set internal_getEffSaves() {
    return this.effSaves;
  } // protected Set internal_getEffSaves()

  // @since   1.2.0
  protected Set internal_getSaves() {
    return this.saves;
  } // protected Set internal_getSaves()


  // PRIVATE INSTANCE METHODS //

  // Add m's hasMembers to o
  // @since   1.2.0
  private Set _addHasMembersToOwner(Set hasMembers) 
    throws  IllegalStateException
  {
    Set           mships      = new LinkedHashSet();
    MembershipDTO hasMS;
    MembershipDTO _ms;
    Iterator      it          = hasMembers.iterator();
    // cache values outside of iterator
    int           depth       = this.getMembershipDTO().getDepth();
    String        listName    = this.getMembershipDTO().getListName();
    String        listType    = this.getMembershipDTO().getListType();
    String        memberUUID  = this.getSession().getMember().getUuid();
    String        msUUID      = this.getMembershipDTO().getMembershipUuid();
    String        ownerUUID   = this.getMembershipDTO().getOwnerUuid();
    while (it.hasNext()) {
      hasMS = (MembershipDTO) Rosetta.getDTO( it.next() );

      _ms = new MembershipDTO();
      _ms.setCreatorUuid(memberUUID);
      _ms.setDepth(depth + hasMS.getDepth() + 1);
      _ms.setListName(listName);
      _ms.setListType(listType);
      _ms.setMemberUuid( hasMS.getMemberUuid() );
      _ms.setOwnerUuid(ownerUUID);
      _ms.setType(Membership.EFFECTIVE);
      if ( hasMS.getDepth() == 0 ) {
        _ms.setViaUuid( hasMS.getOwnerUuid() );  // hasMember m was immediate
        _ms.setParentUuid(msUUID);
      }
      else {
        _ms.setViaUuid( hasMS.getViaUuid() ); // hasMember m was effective
        if ( hasMS.getParentUuid() != null ) {
          _ms.setParentUuid( hasMS.getParentUuid() );
        }
        else {
          _ms.setParentUuid( hasMS.getMembershipUuid() );
        }
      }
      EffectiveMembershipValidator v = EffectiveMembershipValidator.validate(_ms);
      if (v.isInvalid()) {
        throw new IllegalStateException( v.getErrorMessage() );
      }

      mships.add(_ms);
    }
    return mships;
  } // private Set _addHasMembersToOwner(hasMembers)

  // Add m's hasMembers to where g isMember
  // @since   1.2.0
  private Set _addHasMembersToWhereGroupIsMember(Set isMember, Set hasMembers) 
    throws  IllegalStateException
  {
    Set     mships  = new LinkedHashSet();

    // Add the members of m to where g is a member but only if f == "members"
    if (this.getField().equals(Group.getDefaultList())) {
      MembershipDTO isMS;
      Iterator      hasIt;
      MembershipDTO hasMS;
      MembershipDTO dto;
      Iterator      isIt    = isMember.iterator();
      while (isIt.hasNext()) {
        isMS = (MembershipDTO) Rosetta.getDTO( isIt.next() );
        hasIt = hasMembers.iterator();
        while (hasIt.hasNext()) {
          hasMS = (MembershipDTO) Rosetta.getDTO( hasIt.next() );

          dto = new MembershipDTO();
          dto.setCreatorUuid( this.getSession().getMember().getUuid() );
          dto.setDepth( isMS.getDepth() + hasMS.getDepth() + 2 );
          dto.setListName( isMS.getListName() );
          dto.setListType( isMS.getListType() );
          dto.setMemberUuid( hasMS.getMemberUuid() );
          dto.setOwnerUuid( isMS.getOwnerUuid() );
          dto.setType(Membership.EFFECTIVE);
          if ( hasMS.getDepth() == 0 ) {
            dto.setViaUuid( hasMS.getOwnerUuid() );  // hasMember m was immediate
            dto.setParentUuid( isMS.getMembershipUuid() );
          }
          else {
            dto.setViaUuid( hasMS.getViaUuid() ); // hasMember m was effective
            if ( hasMS.getParentUuid() != null ) {
              dto.setParentUuid( hasMS.getParentUuid() );
            }
            else {
              dto.setParentUuid( hasMS.getMembershipUuid() );
            }
          }
          EffectiveMembershipValidator v = EffectiveMembershipValidator.validate(dto);
          if (v.isInvalid()) {
            throw new IllegalStateException( v.getErrorMessage() );
          }

          mships.add(dto);
        }
      }
    }

    return mships;
  } // private Set _addHasMembersToWhereGroupIsMember(isMember, hasMembers)

  // Add m to where g isMember
  // @since   1.2.0
  private Set _addMemberToWhereGroupIsMember(Set isMember) 
    throws  IllegalStateException
  {
    Set     mships  = new LinkedHashSet();

    // Add m to where g is a member if f == "members"
    if (this.getField().equals(Group.getDefaultList())) {
      MembershipDTO isMS;
      MembershipDTO dto;
      Iterator      isIt  = isMember.iterator();
      while (isIt.hasNext()) {
        isMS = (MembershipDTO) Rosetta.getDTO( isIt.next() );

        dto = new MembershipDTO();
        dto.setCreatorUuid( this.getSession().getMember().getUuid() );
        dto.setDepth( isMS.getDepth() + this.getMembershipDTO().getDepth() + 1 );
        dto.setListName( isMS.getListName() );
        dto.setListType( isMS.getListType() );
        dto.setMemberUuid( this.getMembershipDTO().getMemberUuid() );
        dto.setOwnerUuid( isMS.getOwnerUuid() );
        dto.setType(Membership.EFFECTIVE);
        if ( this.getMembershipDTO().getDepth() == 0 ) {
          dto.setViaUuid( this.getMembershipDTO().getOwnerUuid() );  // ms m was immediate
          dto.setParentUuid( isMS.getMembershipUuid() );
        }
        else {
          dto.setViaUuid( this.getMembershipDTO().getViaUuid() ); // ms m was effective
          if ( this.getMembershipDTO().getParentUuid() != null ) {
            dto.setParentUuid( this.getMembershipDTO().getParentUuid() );
          }
          else {
            dto.setParentUuid( this.getMembershipDTO().getMembershipUuid() );
          }
        }
        EffectiveMembershipValidator v = EffectiveMembershipValidator.validate(dto);
        if (v.isInvalid()) {
          throw new IllegalStateException( v.getErrorMessage() );
        }

        mships.add(dto);
      }
    }

    return mships;
  } // private Set _addHasMembersToWhereGroupIsMember(isMember, hasMembers)

  // @since   1.2.0
  private Set _createNewCompositeMembershipObjects(Set memberUUIDs) 
    throws  IllegalStateException
  {
    CompositeMembershipValidator  v;
    Set                           mships  = new LinkedHashSet();
    MembershipDTO                 _ms;
    Iterator                      it      = memberUUIDs.iterator();
    while (it.hasNext()) {
      _ms = new MembershipDTO();
      _ms.setCreatorUuid( this.getSession().getMember().getUuid() );
      _ms.setDepth(0);
      _ms.setListName( this.getField().getName() );
      _ms.setListType( this.getField().getType().toString() );
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

  // @since   1.2.0
  private void _evaluateAddCompositeMembership()
    throws  IllegalStateException
  {
    if      ( this.getComposite().getType().equals(CompositeType.COMPLEMENT) )    {
      this.effSaves.addAll( this._evaluateAddCompositeMembershipComplement() );
    }
    else if ( this.getComposite().getType().equals(CompositeType.INTERSECTION) )  {
      this.effSaves.addAll( this._evaluateAddCompositeMembershipIntersection() );
    }
    else if ( this.getComposite().getType().equals(CompositeType.UNION) )         {
      this.effSaves.addAll( this._evaluateAddCompositeMembershipUnion() );
    }
    else {
      throw new IllegalStateException( E.MOF_CTYPE + this.getComposite().getType().toString() );
    }
    this.saves.addAll(this.effSaves);
    this._identifyGroupsAndStemsToMarkAsModified();
  } // private void _evaluateAddCompositeMembership()

  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipComplement() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getDTO().getLeftFactorUuid() ) );
    memberUUIDs.removeAll( this._findMemberUUIDs( this.getComposite().getDTO().getRightFactorUuid() ) );
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } // private Set _evaluateAddCompositeMembershipComplement()
  
  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipIntersection() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getDTO().getLeftFactorUuid() ) );
    memberUUIDs.retainAll( this._findMemberUUIDs( this.getComposite().getDTO().getRightFactorUuid() ) );
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } // private Set _evaluateAddCompositeMembershipIntersection()

  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipUnion() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getDTO().getLeftFactorUuid() ) );
    memberUUIDs.addAll( this._findMemberUUIDs( this.getComposite().getDTO().getRightFactorUuid() ) );
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } // private Set _evaluateAddCompositeMembershipUnion()

  // @since   1.2.0
  // TODO 20070220 split; this method has gotten too large
  private void _evaluateAddImmediateMembership(GrouperSession s, Field f, MemberDTO _m) 
    throws  IllegalStateException
  {
    MembershipDTO _ms = new MembershipDTO();
    _ms.setCreatorUuid( s.getMember().getUuid() );
    _ms.setListName( f.getName() );
    _ms.setListType( f.getType().toString() );
    _ms.setMemberUuid( _m.getMemberUuid() );
    _ms.setOwnerUuid( this.getOwnerUuid() );

    ImmediateMembershipValidator v = ImmediateMembershipValidator.validate(_ms);
    if (v.isInvalid()) {
      throw new IllegalStateException( v.getErrorMessage() );
    }

    try {
      // TODO 20070220 i really shouldn't be throwing schema exceptions here
      this.setField( FieldFinder.find( _ms.getListName() ) );
    }
    catch (SchemaException eS) {
      throw new IllegalStateException( eS.getMessage(), eS );
    }
    this.setMemberDTO(_m);
    this.setMembershipDTO(_ms);
    this.setSession(s);

    Set results = new LinkedHashSet();
    // If we are working on a group, where is it a member
    Set isMember = new LinkedHashSet();
    if ( this.getGroup() != null ) {
      isMember = PrivilegeResolver.internal_canViewMemberships(
        this.getGroup().getSession(),
        HibernateMembershipDAO.findAllByMember( this.getGroup().toMember().getUuid() )
      );
    }
    // Members of _m if owner is a group
    Set hasMembers = this._findMembersOfMember();
    // Add _m to where owner is member if f == "members"
    results.addAll( this._addMemberToWhereGroupIsMember(isMember) );
    // Add members of _m to owner
    results.addAll( this._addHasMembersToOwner(hasMembers) );
    // Add members of _m to where owner is member if f == "members"
    results.addAll( this._addHasMembersToWhereGroupIsMember(isMember, hasMembers) );

    this.effSaves.addAll(results);

    this.saves.addAll( this.effSaves );
    this._identifyGroupsAndStemsToMarkAsModified();

    this.saves.add(_ms);   // Save the immediate
  } // private void _evaluateAddImmediateMembership(s, f, _m)

  // @since   1.2.0
  private void _evaluateDeleteCompositeMembership() 
    throws  IllegalStateException
  {
    MembershipDTO _ms;
    MemberOf      mof;
    Iterator      it  = HibernateMembershipDAO.findAllByOwnerAndField( 
      this.getGroup().getUuid(), Group.getDefaultList() 
    ).iterator();
    try {
      while (it.hasNext()) {
        _ms = (MembershipDTO) it.next();
        mof = new MemberOf();
        mof.deleteImmediate( 
          this.getSession(), this.getGroup(), _ms, this.getSession().cachingFindMemberByUuid( _ms.getMemberUuid() ) 
        );
        this.deletes.addAll( mof.internal_getDeletes() );
      }
    }
    catch (MemberNotFoundException eMNF) {
      throw new IllegalStateException( eMNF.getMessage(), eMNF );
    }
    this._identifyGroupsAndStemsToMarkAsModified();
  } // private void _evalulateDeleteCompositeMembership()

  // @since   1.2.0
  private void _evaluateDeleteImmediateMembership(GrouperSession s, MembershipDTO _ms, MemberDTO _m) 
    throws  IllegalStateException
  {
    try {
      this.setField( FieldFinder.find( _ms.getListName() ) );
    }
    catch (SchemaException eS) {
      throw new IllegalStateException( eS.getMessage(), eS );
    }
    this.setMemberDTO(_m);
    this.setMembershipDTO(_ms);
    this.setSession(s);

    // Find child memberships that need deletion
    Set       children  = new LinkedHashSet();
    Iterator  it        = MembershipFinder.internal_findAllChildrenNoPriv(_ms).iterator();
    while (it.hasNext()) {
      children.add( (MembershipDTO) it.next() );
    }
    this.effDeletes.addAll(children);
    // Find all effective memberships that need deletion
    try {
      this.effDeletes.addAll( MembershipFinder.internal_findAllForwardMembershipsNoPriv(s, _ms, children) );
    }
    catch (SchemaException eS) {
      throw new IllegalStateException( eS.getMessage(), eS );
    }

    // And now set everything else
    this.deletes.addAll(this.effDeletes);
    this._identifyGroupsAndStemsToMarkAsModified();

    this.deletes.add(_ms); // Delete the immediate
  } // private void _evaluateDeleteImmediateMembership(s, _ms, _m)

  // Find m's hasMembers
  private Set _findMembersOfMember() {
    Set hasMembers = new LinkedHashSet();
    if (this.getMemberDTO().getSubjectTypeId().equals("group")) {
      hasMembers = HibernateMembershipDAO.findAllByOwnerAndField(
        this.getMemberDTO().getSubjectId(), Group.getDefaultList()
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
      GroupDTO  _g          = HibernateGroupDAO.findByUuid(groupUUID);
      Iterator  it          = HibernateMembershipDAO.findAllByOwnerAndField( _g.getUuid(), Group.getDefaultList() ).iterator();
      while (it.hasNext()) {
        memberUUIDs.add( ( (MembershipDTO) it.next() ).getMemberUuid() );  
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
    modified      = this.identifyGroupsAndStemsToMarkAsModified( modified, this.effSaves.iterator() );
    modified      = this.identifyGroupsAndStemsToMarkAsModified( modified, this.effDeletes.iterator() );
    Map groups    = (Map) modified.get("groups");
    Map stems     = (Map) modified.get("stems");
    this.setModifiedGroups( new LinkedHashSet( groups.values() ) );
    this.setModifiedStems( new LinkedHashSet( stems.values() ) );
  } // private void _identifyGroupsAndStemsToMarkAsModified()

} // class MemberOf extends BaseMemberOf

