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
 * @version $Id: DefaultMemberOf.java,v 1.10 2008-06-24 06:07:03 mchyzer Exp $
 * @since   1.2.0
 */
public class DefaultMemberOf extends BaseMemberOf {

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
        DefaultMemberOf.this.addSave( c.getDTO() );
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
        DefaultMemberOf.this.addDelete( c.getDTO() );
        return null;
      }
      
    });
  }

  /**
   * @since   1.2.0
   */
  public void addImmediate(GrouperSession s, Group g, Field f, MemberDTO _m)
    throws  IllegalStateException 
  {
    //note, no need for GrouperSession inverse of control
    this.setGroup(g);
    this._evaluateAddImmediateMembership(s, f, _m);
  } 

  /**
   * @since   1.2.0
   */
  public void addImmediate(GrouperSession s, Stem ns, Field f, MemberDTO _m)
    throws  IllegalStateException
  {
    //note, no need for GrouperSession inverse of control
    this.setStem(ns);
    this._evaluateAddImmediateMembership(s, f, _m);
  }

  /**
   * @since   1.2.0
   */
  public void deleteImmediate(GrouperSession s, Group g, MembershipDTO _ms, MemberDTO _m)
    throws  IllegalStateException
  {
    //note, no need for GrouperSession inverse of control
    this.setGroup(g);
    this._evaluateDeleteImmediateMembership(s, _ms, _m);
  }

  /**
   * @since   1.2.0
   */
  public void deleteImmediate(GrouperSession s, Stem ns, MembershipDTO _ms, MemberDTO _m)
    throws  IllegalStateException
  {
    //note, no need for GrouperSession inverse of control
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
    String        modifierUuid  = GrouperSession.staticGrouperSession().getMember().getUuid();
    long          modifyTime    = new java.util.Date().getTime();

    // TODO 20070531 this is horribly ugly.
    Map<String, GroupDTO> groups = new HashMap<String, GroupDTO>();
    if (m.containsKey("groups")) {
      groups = (Map) m.get("groups");
    }
    Map<String, StemDTO> stems = new HashMap<String, StemDTO>();
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
  private Set _addHasMembersToOwner(Set<MembershipDTO> hasMembers) 
    throws  IllegalStateException
  {
    Set           mships      = new LinkedHashSet();
    MembershipDTO hasMS;
    MembershipDTO _ms;
    Iterator<MembershipDTO>      it          = hasMembers.iterator();
    // cache values outside of iterator
    int           depth       = this.getMembershipDTO().getDepth();
    String        listName    = this.getMembershipDTO().getListName();
    String        listType    = this.getMembershipDTO().getListType();
    String        memberUUID  = GrouperSession.staticGrouperSession().getMember().getUuid();
    String        msUUID      = this.getMembershipDTO().getUuid();
    String        ownerUUID   = this.getMembershipDTO().getOwnerUuid();
    while (it.hasNext()) {
      hasMS = it.next();

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
            .setCreatorUuid( GrouperSession.staticGrouperSession().getMember().getUuid() )
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
          .setCreatorUuid( GrouperSession.staticGrouperSession().getMember().getUuid() )
          .setDepth( isMS.getDepth() + this.getMembershipDTO().getDepth() + 1 )
          .setListName( isMS.getListName() )
          .setListType( isMS.getListType() )
          .setMemberDTO(this.getMemberDTO())
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
      _ms.setCreatorUuid( GrouperSession.staticGrouperSession().getMember().getUuid() );
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
        composites
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
  // TODO 20070531 split; this method has gotten too large
  private void _evaluateAddImmediateMembership(GrouperSession s, final Field f, final MemberDTO _m) 
    throws  IllegalStateException
  {
    GrouperSession.callbackGrouperSession(s, new GrouperSessionHandler() {

      public Object callback(GrouperSession grouperSession)
          throws GrouperSessionException {
        MembershipDTO _ms = new MembershipDTO();
        _ms.setCreatorUuid( grouperSession.getMember().getUuid() );
        _ms.setListName( f.getName() );
        _ms.setListType( f.getType().toString() );
        _ms.setMemberUuid( _m.getUuid() );
        _ms.setOwnerUuid( DefaultMemberOf.this.getOwnerUuid() );

        GrouperValidator v = ImmediateMembershipValidator.validate(_ms);
        if (v.isInvalid()) {
          throw new IllegalStateException( v.getErrorMessage() );
        }

        try {
          // TODO 20070531 look into finding a way to avoid SchemaExceptions here
          DefaultMemberOf.this.setField( FieldFinder.find( _ms.getListName() ) );
        }
        catch (SchemaException eS) {
          throw new IllegalStateException( eS.getMessage(), eS );
        }
        DefaultMemberOf.this.setMemberDTO(_m);
        DefaultMemberOf.this.setMembershipDTO(_ms);

        Set results = new LinkedHashSet();
        // If we are working on a group, where is it a member
        Set isMember = new LinkedHashSet();
        if ( DefaultMemberOf.this.getGroup() != null ) {
          isMember = GrouperDAOFactory.getFactory().getMembership().findAllByMember( DefaultMemberOf.this.getGroup().toMember().getUuid() );
        }
        // Members of _m if owner is a group
        Set<MembershipDTO> hasMembers = DefaultMemberOf.this._findMembersOfMember();
        // Add _m to where owner is member if f == "members"
        results.addAll( DefaultMemberOf.this._addMemberToWhereGroupIsMember(isMember) );
        // Add members of _m to owner
        results.addAll( DefaultMemberOf.this._addHasMembersToOwner(hasMembers) );
        // Add members of _m to where owner is member if f == "members"
        results.addAll( DefaultMemberOf.this._addHasMembersToWhereGroupIsMember(isMember, hasMembers) );

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
  private void _evaluateDeleteImmediateMembership(GrouperSession s, final MembershipDTO _ms, final MemberDTO _m) 
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
        DefaultMemberOf.this.setMemberDTO(_m);
        DefaultMemberOf.this.setMembershipDTO(_ms);

        // Find child memberships that need deletion
        Set       children  = new LinkedHashSet();
        Iterator  it        = MembershipFinder.internal_findAllChildrenNoPriv(_ms).iterator();
        while (it.hasNext()) {
          children.add( (MembershipDTO) it.next() );
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
  private Set<MembershipDTO> _findMembersOfMember() {
    Set<MembershipDTO> hasMembers = new LinkedHashSet();
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

