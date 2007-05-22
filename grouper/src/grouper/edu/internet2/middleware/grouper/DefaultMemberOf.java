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
import  edu.internet2.middleware.grouper.internal.dao.GroupDAO;
import  edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import  edu.internet2.middleware.grouper.internal.dao.StemDAO;
import  edu.internet2.middleware.grouper.internal.dto.CompositeDTO;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import  edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import  edu.internet2.middleware.grouper.internal.dto.StemDTO;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;

/** 
 * Perform <i>member of</i> calculation.
 * <p/>
 * @author  blair christensen.
 * @version $Id: DefaultMemberOf.java,v 1.3 2007-05-22 14:09:44 blair Exp $
 * @since   1.2.0
 */
public class DefaultMemberOf extends BaseMemberOf {

  // PUBLIC INSTANCE METHODS //
 
  /**
   * @since   1.2.0
   */
  public void addComposite(GrouperSession s, Group g, Composite c)
    throws  IllegalStateException
  {
    this.setComposite(c);
    this.setGroup(g);
    this.setSession(s);
    this._evaluateAddCompositeMembership();
    this.addSave( c.getDTO() );
  } 

  /**
   * @since   1.2.0
   */
  public void deleteComposite(GrouperSession s, Group g, Composite c)
    throws  IllegalStateException
  {
    this.setComposite(c);
    this.setGroup(g);
    this.setSession(s);
    this._evaluateDeleteCompositeMembership();
    this.addDelete( c.getDTO() );
  }

  /**
   * @since   1.2.0
   */
  public void addImmediate(GrouperSession s, Group g, Field f, MemberDTO _m)
    throws  IllegalStateException 
  {
    this.setGroup(g);
    this._evaluateAddImmediateMembership(s, f, _m);
  } 

  /**
   * @since   1.2.0
   */
  public void addImmediate(GrouperSession s, Stem ns, Field f, MemberDTO _m)
    throws  IllegalStateException
  {
    this.setStem(ns);
    this._evaluateAddImmediateMembership(s, f, _m);
  }

  /**
   * @since   1.2.0
   */
  public void deleteImmediate(GrouperSession s, Group g, MembershipDTO _ms, MemberDTO _m)
    throws  IllegalStateException
  {
    this.setGroup(g);
    this._evaluateDeleteImmediateMembership(s, _ms, _m);
  }

  /**
   * @since   1.2.0
   */
  public void deleteImmediate(GrouperSession s, Stem ns, MembershipDTO _ms, MemberDTO _m)
    throws  IllegalStateException
  {
    this.setStem(ns);
    this._evaluateDeleteImmediateMembership(s, _ms, _m);
  } 


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
      GroupDAO  gDAO  = GrouperDAOFactory.getFactory().getGroup();
      StemDAO   nsDAO = GrouperDAOFactory.getFactory().getStem();
      while (it.hasNext()) {
        _ms = (MembershipDTO) it.next();
        k   = _ms.getOwnerUuid();
        if      ( _ms.getListType().equals(FieldType.LIST.toString()) || _ms.getListType().equals(FieldType.ACCESS.toString()) ) {
          if ( !groups.containsKey(k) ) {
            _g = gDAO.findByUuid(k);
            _g.setModifierUuid(modifierUuid);
            _g.setModifyTime(modifyTime);
            groups.put(k, _g);
          }
        }
        else if ( _ms.getListType().equals(FieldType.NAMING.toString()) ) {
          if ( !stems.containsKey(k) ) {
            _ns = nsDAO.findByUuid(k);
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
      _g = (GroupDTO) this.getGroup().getDTO();
      _g.setModifierUuid(modifierUuid);
      _g.setModifyTime(modifyTime);
      groups.put( _g.getUuid(), _g );
    }
    else if ( this.getStem() != null )  {
      _ns = (StemDTO) this.getStem().getDTO();
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
  }


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
    String        msUUID      = this.getMembershipDTO().getUuid();
    String        ownerUUID   = this.getMembershipDTO().getOwnerUuid();
    while (it.hasNext()) {
      hasMS = (MembershipDTO) it.next();

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
          _ms.setParentUuid( hasMS.getUuid() );
        }
      }
      GrouperValidator v = EffectiveMembershipValidator.validate(_ms);
      if (v.isInvalid()) {
        throw new IllegalStateException( v.getErrorMessage() );
      }

      mships.add(_ms);
    }
    return mships;
  } // private Set _addHasMembersToOwner(hasMembers)

  /**
   * Add members of <i>member</i> to where <i>group</i> is a member.
   * @param   isMember  <i>Set</i> of <i>MembershipDTO</i> objects.
   * @since   1.2.0
   */
  private Set _addHasMembersToWhereGroupIsMember(Set isMember, Set hasMembers) 
    throws  IllegalStateException
  {
    Set mships = new LinkedHashSet();

    // Add the members of m to where g is a member but only if f == "members"
    if (this.getField().equals(Group.getDefaultList())) {
      MembershipDTO _ms;
      MembershipDTO hasMS;
      MembershipDTO isMS;
      Iterator      itHM; 
      Iterator      itIM    = isMember.iterator();
      while (itIM.hasNext()) {
        isMS = (MembershipDTO) itIM.next();
        
        itHM = hasMembers.iterator();
        while (itHM.hasNext()) {
          hasMS = (MembershipDTO) itHM.next();

          _ms = new MembershipDTO()
            .setCreatorUuid( this.getSession().getMember().getUuid() )
            .setDepth( isMS.getDepth() + hasMS.getDepth() + 2 )
            .setListName( isMS.getListName() )
            .setListType( isMS.getListType() )
            .setMemberUuid( hasMS.getMemberUuid() )
            .setOwnerUuid( isMS.getOwnerUuid() )
            .setType(Membership.EFFECTIVE)
            ;
          if ( hasMS.getDepth() == 0 ) { // hasMember m was immediate
            _ms.setViaUuid( hasMS.getOwnerUuid() )
              .setParentUuid( isMS.getUuid() )
              ;
          }
          else { // hasMember m was effective
            _ms.setViaUuid( hasMS.getViaUuid() );
            if ( hasMS.getParentUuid() != null ) {
              _ms.setParentUuid( hasMS.getParentUuid() );
            }
            else {
              _ms.setParentUuid( hasMS.getUuid() );
            }
          }
          GrouperValidator v = EffectiveMembershipValidator.validate(_ms);
          if (v.isInvalid()) {
            throw new IllegalStateException( v.getErrorMessage() );
          }

          mships.add(_ms);
        }
      }
    }

    return mships;
  } 

  /**
   * Add the member of each <code>isMember</code> membership to where the group is a member.
   * @param   isMember  <i>Set</i> of <i>MembershipDTO</i> objects.
   * @since   1.2.0
   */
  private Set _addMemberToWhereGroupIsMember(Set isMember) 
    throws  IllegalStateException
  {
    Set mships = new LinkedHashSet();

    // Add m to where g is a member if f == "members"
    if ( this.getField().equals( Group.getDefaultList() ) ) {
      MembershipDTO isMS;
      Iterator      itIM  = isMember.iterator();
      MembershipDTO _ms;
      while (itIM.hasNext()) {
        //isMS = (MembershipDTO) ( (Membership) isIt.next() ).getDTO();
        isMS = (MembershipDTO) itIM.next();

        _ms = new MembershipDTO()
          .setCreatorUuid( this.getSession().getMember().getUuid() )
          .setDepth( isMS.getDepth() + this.getMembershipDTO().getDepth() + 1 )
          .setListName( isMS.getListName() )
          .setListType( isMS.getListType() )
          .setMemberUuid( this.getMembershipDTO().getMemberUuid() )
          .setOwnerUuid( isMS.getOwnerUuid() )
          .setType(Membership.EFFECTIVE)
          ;
        if ( this.getMembershipDTO().getDepth() == 0 ) { // this memberhsip was immediate
          _ms.setViaUuid( this.getMembershipDTO().getOwnerUuid() )
            .setParentUuid( isMS.getUuid() )
            ;
        }
        else { // that membership was effective
          _ms.setViaUuid( this.getMembershipDTO().getViaUuid() ); 
          if ( this.getMembershipDTO().getParentUuid() != null ) {
            _ms.setParentUuid( this.getMembershipDTO().getParentUuid() );
          }
          else {
            _ms.setParentUuid( this.getMembershipDTO().getUuid() );
          }
        }
        GrouperValidator v = EffectiveMembershipValidator.validate(_ms);
        if (v.isInvalid()) {
          throw new IllegalStateException( v.getErrorMessage() );
        }

        mships.add(_ms);
      }
    }

    return mships;
  } 

  // @since   1.2.0
  private Set _createNewCompositeMembershipObjects(Set memberUUIDs) 
    throws  IllegalStateException
  {
    GrouperValidator  v;
    Set               mships  = new LinkedHashSet();
    MembershipDTO     _ms;
    Iterator          it      = memberUUIDs.iterator();
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
      this.addEffectiveSaves( this._evaluateAddCompositeMembershipComplement() );
    }
    else if ( this.getComposite().getType().equals(CompositeType.INTERSECTION) )  {
      this.addEffectiveSaves( this._evaluateAddCompositeMembershipIntersection() );
    }
    else if ( this.getComposite().getType().equals(CompositeType.UNION) )         {
      this.addEffectiveSaves( this._evaluateAddCompositeMembershipUnion() );
    }
    else {
      throw new IllegalStateException( E.MOF_CTYPE + this.getComposite().getType().toString() );
    }

    this.addSaves( this.getEffectiveSaves() );
    this._identifyGroupsAndStemsToMarkAsModified();
  } 

  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipComplement() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( ( (CompositeDTO) this.getComposite().getDTO() ).getLeftFactorUuid() ) );
    memberUUIDs.removeAll( this._findMemberUUIDs( ( (CompositeDTO) this.getComposite().getDTO() ).getRightFactorUuid() ) );
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 
  
  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipIntersection() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( ( (CompositeDTO) this.getComposite().getDTO() ).getLeftFactorUuid() ) );
    memberUUIDs.retainAll( this._findMemberUUIDs( ( (CompositeDTO) this.getComposite().getDTO() ).getRightFactorUuid() ) );
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 

  // @since   1.2.0
  private Set _evaluateAddCompositeMembershipUnion() 
    throws  IllegalStateException
  {
    Set memberUUIDs = new LinkedHashSet();
    memberUUIDs.addAll( this._findMemberUUIDs( ( (CompositeDTO) this.getComposite().getDTO() ).getLeftFactorUuid() ) );
    memberUUIDs.addAll( this._findMemberUUIDs( ( (CompositeDTO) this.getComposite().getDTO() ).getRightFactorUuid() ) );
    return this._createNewCompositeMembershipObjects(memberUUIDs);
  } 

  // @since   1.2.0
  // TODO 20070220 split; this method has gotten too large
  private void _evaluateAddImmediateMembership(GrouperSession s, Field f, MemberDTO _m) 
    throws  IllegalStateException
  {
    MembershipDTO _ms = new MembershipDTO();
    _ms.setCreatorUuid( s.getMember().getUuid() );
    _ms.setListName( f.getName() );
    _ms.setListType( f.getType().toString() );
    _ms.setMemberUuid( _m.getUuid() );
    _ms.setOwnerUuid( this.getOwnerUuid() );

    GrouperValidator v = ImmediateMembershipValidator.validate(_ms);
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
      isMember = GrouperDAOFactory.getFactory().getMembership().findAllByMember( this.getGroup().toMember().getUuid() );
    }
    // Members of _m if owner is a group
    Set hasMembers = this._findMembersOfMember();
    // Add _m to where owner is member if f == "members"
    results.addAll( this._addMemberToWhereGroupIsMember(isMember) );
    // Add members of _m to owner
    results.addAll( this._addHasMembersToOwner(hasMembers) );
    // Add members of _m to where owner is member if f == "members"
    results.addAll( this._addHasMembersToWhereGroupIsMember(isMember, hasMembers) );

    this.addEffectiveSaves(results);

    this.addSaves( this.getEffectiveSaves() );
    this._identifyGroupsAndStemsToMarkAsModified();

    this.addSave(_ms); // Save the immediate
  } // private void _evaluateAddImmediateMembership(s, f, _m)

  // @since   1.2.0
  private void _evaluateDeleteCompositeMembership() 
    throws  IllegalStateException
  {
    MembershipDTO _ms;
    DefaultMemberOf      mof;
    Iterator      it  = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField( 
      this.getGroup().getUuid(), Group.getDefaultList() 
    ).iterator();
    try {
      MemberDAO dao = GrouperDAOFactory.getFactory().getMember();
      while (it.hasNext()) {
        _ms = (MembershipDTO) it.next();
        mof = new DefaultMemberOf();
        mof.deleteImmediate( 
          this.getSession(), this.getGroup(), _ms, dao.findByUuid( _ms.getMemberUuid() ) 
        );
        this.addDeletes( mof.getDeletes() );
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
    this.addEffectiveDeletes(children);
    // Find all effective memberships that need deletion
    try {
      this.addEffectiveDeletes( MembershipFinder.internal_findAllForwardMembershipsNoPriv(s, _ms, children) );
    }
    catch (SchemaException eS) {
      throw new IllegalStateException( eS.getMessage(), eS );
    }

    // And now set everything else
    this.addDeletes( this.getEffectiveDeletes() );
    this._identifyGroupsAndStemsToMarkAsModified();

    this.addDelete(_ms); // Delete the immediate
  } // private void _evaluateDeleteImmediateMembership(s, _ms, _m)

  // Find m's hasMembers
  private Set _findMembersOfMember() {
    Set hasMembers = new LinkedHashSet();
    if (this.getMemberDTO().getSubjectTypeId().equals("group")) {
      hasMembers = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField(
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
      GroupDTO  _g          = GrouperDAOFactory.getFactory().getGroup().findByUuid(groupUUID);
      Iterator  it          = GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField(
         _g.getUuid(), Group.getDefaultList() 
      ).iterator();
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
    modified      = this.identifyGroupsAndStemsToMarkAsModified( modified, this.getEffectiveSaves().iterator() );
    modified      = this.identifyGroupsAndStemsToMarkAsModified( modified, this.getEffectiveDeletes().iterator() );
    Map groups    = (Map) modified.get("groups");
    Map stems     = (Map) modified.get("stems");
    this.setModifiedGroups( new LinkedHashSet( groups.values() ) );
    this.setModifiedStems( new LinkedHashSet( stems.values() ) );
  } // private void _identifyGroupsAndStemsToMarkAsModified()

} 

