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
 * @version $Id: MemberOf.java,v 1.43 2007-02-14 22:06:40 blair Exp $
 */
class MemberOf extends BaseMemberOf {

  // PRIVATE INSTANCE VARIABLES //
  private Set deletes     = new LinkedHashSet();
  private Set effDeletes  = new LinkedHashSet();
  private Set effSaves    = new LinkedHashSet();
  private Set saves       = new LinkedHashSet();


  // PROTECTED CLASS METHODS //
 
  // Calculate addition of a composite membership 
  // @since   1.2.0
  protected static MemberOf internal_addComposite(GrouperSession s, Group g, Composite c)
    throws  ModelException
  {
    MemberOf mof = new MemberOf();
    mof.setComposite(c);
    mof.setGroup(g);
    mof.setSession(s);
    mof.effSaves.addAll(  mof._evalComposite()  );  // Find the composites
    mof.saves.addAll(     mof.effSaves          );
    mof._identifyGroupsAndStemsToMarkAsModified();
    mof.saves.add( mof.getComposite() ); // Save the composite
    Set groups = mof.getModifiedGroups();
    g.internal_setModified(); // Add the owner the the modified list
    groups.add( g.getDTO() );
    mof.setModifiedGroups(groups);
    return mof;
  } // protected static MemberOf internal_addComposite(s, g, c)

  // @since   1.2.0
  protected static MemberOf internal_addImmediate(
    GrouperSession s, Owner o, MembershipDTO _ms, MemberDTO _m
  )
    throws  ModelException
  {
    try {
      MemberOf mof = new MemberOf();
      if (o instanceof Group) {
        mof.setGroup( (Group) o );
      }
      else {
        mof.setStem( (Stem) o );
      }
      mof.setField( FieldFinder.find( _ms.getListName() ) );
      mof.setMemberDTO(_m);
      mof.setMembershipDTO(_ms);
      mof.setSession(s);

      // TODO 20070213 why?
      mof.saves.add(_m);     // Save the member

      mof.effSaves.addAll( mof._evalAddImmediate()  );  // Find the new effs
      mof.saves.addAll(   mof.effSaves              );
      mof._identifyGroupsAndStemsToMarkAsModified();

      mof.saves.add(_ms);   // Save the immediate
      // TODO 20070130 bah
      if ( mof.getGroup() != null ) {
        Set   groups  = mof.getModifiedGroups();
        Group g       = mof.getGroup();
        g.internal_setModified();
        mof.setGroup(g);
        groups.add( g.getDTO() );
        mof.setModifiedGroups(groups);
      }
      else {
        Set   stems = mof.getModifiedStems();
        Stem  ns    = mof.getStem();
        ns.internal_setModified();
        mof.setStem(ns);
        stems.add( ns.getDTO() );
        mof.setModifiedStems(stems);
      }
      return mof;
    }
    catch (SchemaException eS) {
      throw new ModelException( eS.getMessage(), eS );
    }
  } // protected static MemberOf internal_addImmediate(s, o, _ms, _m)

  // Calculate deletion of a composite membership 
  // @since   1.2.0
  protected static MemberOf internal_delComposite(GrouperSession s, Group g, Composite c)
    throws  ModelException
  {
    //  TODO  20061011 In theory I shouldn't need to pass along `o` as I can just get it 
    //        via `c.getOwner()` *but* `TestGroup36` throws a `HibernateException`
    //        when I retrieve `o` that way.
    
    //  TODO  20061011 I'm really uncertain about this code.  Expect it to be
    //        both flawed and evolving for quite some time.
    
    MemberOf mof = new MemberOf();
    mof.setComposite(c);
    mof.setGroup(g);
    mof.setSession(s);

    // Delete this group's members
    Membership  ms;
    Iterator    iterH = g.getMemberships().iterator();
    while (iterH.hasNext()) {
      ms = (Membership) iterH.next();
      try {
        MemberOf msMof = MemberOf.internal_delImmediate( s, g, ms.getDTO(), ms.getMember().getDTO() );
        mof.deletes.addAll( msMof.internal_getDeletes() );
      }
      catch (Exception e) {
        throw new ModelException(e);
      }
    }
    mof._identifyGroupsAndStemsToMarkAsModified();

    mof.deletes.add( mof.getComposite() );   // Delete the composite
    Set groups = mof.getModifiedGroups();
    g.internal_setModified();
    groups.add( g.getDTO() );
    mof.setModifiedGroups(groups);
    return mof;
  } // protected static MemberOf internal_delComposite(s, o, c)

  // @since   1.2.0
  protected static MemberOf internal_delImmediate(
    GrouperSession s, Owner o, MembershipDTO _ms, MemberDTO _m
  )
    throws  MemberDeleteException
  {
    try {
      MemberOf mof = new MemberOf();
      if (o instanceof Group) {
        mof.setGroup( (Group) o );
      }
      else {
        mof.setStem( (Stem) o );
      }
      mof.setField( FieldFinder.find( _ms.getListName() ) );
      mof.setMemberDTO(_m);
      mof.setMembershipDTO(_ms);
      mof.setSession(s);

      // Find child memberships that need deletion
      Set           children  = new LinkedHashSet();
      Iterator      it        = MembershipFinder.internal_findAllChildrenNoPriv(_ms).iterator();
      while (it.hasNext()) {
        children.add( (MembershipDTO) it.next() );
      }
      mof.effDeletes.addAll(children);
      // Find all effective memberships that need deletion
      mof.effDeletes.addAll( 
        MembershipFinder.internal_findAllForwardMembershipsNoPriv(s, _ms, children) 
      );

      // And now set everything else
      mof.deletes.addAll(mof.effDeletes);
      mof._identifyGroupsAndStemsToMarkAsModified();

      mof.deletes.add(_ms); // Delete the immediate
      // TODO 20070130 bah    
      if ( mof.getGroup() != null ) {
        Set   groups  = mof.getModifiedGroups();
        Group g       = mof.getGroup();
        g.setSession( mof.getSession() );
        g.internal_setModified();
        mof.setGroup(g);
        groups.add( g.getDTO() );
        mof.setModifiedGroups(groups);
      }
      else {
        Set   stems = mof.getModifiedStems();
        Stem  ns    = mof.getStem();
        ns.setSession( mof.getSession() );
        ns.internal_setModified();
        mof.setStem(ns);
        stems.add( ns.getDTO() );
        mof.setModifiedStems(stems);
      }
      return mof;
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException( eS.getMessage(), eS );
    }
  } // protected static MemberOf internal_delImmediate(s, o, ms, m)


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
  private Set _addHasMembersToOwner(Set hasMembers) 
    throws  ModelException
  {
    Set           mships  = new LinkedHashSet();
    MembershipDTO hasMS;
    MembershipDTO dto;
    Iterator      it      = hasMembers.iterator();
    while (it.hasNext()) {
      hasMS = (MembershipDTO) Rosetta.getDTO( it.next() );

      dto = new MembershipDTO();
      dto.setCreatorUuid( this.getSession().getMember().getUuid() );
      dto.setDepth( this.getMembershipDTO().getDepth() + hasMS.getDepth() + 1 );
      dto.setListName( this.getMembershipDTO().getListName() );
      dto.setListType( this.getMembershipDTO().getListType() );
      dto.setMemberUuid( hasMS.getMemberUuid() );
      dto.setOwnerUuid( this.getMembershipDTO().getOwnerUuid() );
      dto.setType(Membership.EFFECTIVE);
      if ( hasMS.getDepth() == 0 ) {
        dto.setViaUuid( hasMS.getOwnerUuid() );  // hasMember m was immediate
        dto.setParentUuid( this.getMembershipDTO().getMembershipUuid() );
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
      MembershipValidator.internal_validateEffective(dto);

      mships.add(dto);
    }
    return mships;
  } // private Set _addHasMembersToOwner(hasMembers)

  // Add m's hasMembers to where g isMember
  private Set _addHasMembersToWhereGroupIsMember(Set isMember, Set hasMembers) 
    throws  ModelException
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
          MembershipValidator.internal_validateEffective(dto);

          mships.add(dto);
        }
      }
    }

    return mships;
  } // private Set _addHasMembersToWhereGroupIsMember(isMember, hasMembers)

  // Add m to where g isMember
  private Set _addMemberToWhereGroupIsMember(Set isMember) 
    throws  ModelException
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
        MembershipValidator.internal_validateEffective(dto);

        mships.add(dto);
      }
    }

    return mships;
  } // private Set _addHasMembersToWhereGroupIsMember(isMember, hasMembers)

  // Convert a set of memberships into composite memberships
  private Set _createNewMembershipObjects(Set members) 
    throws  ModelException
  {
    Set           mships  = new LinkedHashSet();
    Member        m;
    MembershipDTO dto;
    Iterator      iter    = members.iterator();
    while (iter.hasNext()) {
      m   = (Member) iter.next();

      dto = new MembershipDTO();
      dto.setCreatorUuid( this.getSession().getMember().getUuid() );
      dto.setDepth(0);
      dto.setListName( this.getField().getName() );
      dto.setListType( this.getField().getType().toString() );
      dto.setMemberUuid( m.getUuid() );
      dto.setOwnerUuid( this.getOwnerUuid() );
      dto.setParentUuid(null);
      dto.setType(Membership.COMPOSITE);
      dto.setViaUuid( this.getComposite().getUuid() );
      MembershipValidator.internal_validateComposite(dto);

      mships.add(dto);
    }
    return mships;
  } // private Set _createNewMembershipObjects(members)

  // Evaluate a composite membership
  // @since 1.0
  private Set _evalComposite() 
    throws  ModelException 
  {
    Set results = new LinkedHashSet();
    if      (this.getComposite().getType().equals(CompositeType.COMPLEMENT))   {
      results.addAll( this._evalCompositeComplement() );
    }
    else if (this.getComposite().getType().equals(CompositeType.INTERSECTION)) {
      results.addAll( this._evalCompositeIntersection() );
    }
    else if (this.getComposite().getType().equals(CompositeType.UNION))        {
      results.addAll( this._evalCompositeUnion() );
    }
    else {
      throw new ModelException(E.MOF_CTYPE + this.getComposite().getType().toString());
    }
    return results;
  } // private Set _evalComposite()

  // Evaluate a complement composite membership
  // @since 1.0
  private Set _evalCompositeComplement() 
    throws  ModelException
  {
    try {
      Set   tmp   = new LinkedHashSet();
      Group left  = this.getComposite().getLeftGroup();  // TODO 20070212  !privs
      Group right = this.getComposite().getRightGroup(); // TODO 20070212  !privs
      tmp.addAll( left.getMembers() );      // TODO 20070212  !privs
      tmp.removeAll( right.getMembers() );  // TODO 20070212  !privs
      return this._createNewMembershipObjects(tmp);
    }
    catch (GroupNotFoundException eGNF) {
      throw new ModelException(eGNF);
    }
  } // private Set _evalCompositeComplement()

  // Evaluate an intersection composite membership
  // @since 1.0
  private Set _evalCompositeIntersection() 
    throws  ModelException
  {
    try {
      Set   tmp   = new LinkedHashSet();
      Group left  = this.getComposite().getLeftGroup();    // TODO 20070212  !privs
      Group right = this.getComposite().getRightGroup();   // TODO 20070212  !privs
      tmp.addAll(     left.getMembers()   );  // TODO 20070212  !privs
      tmp.retainAll(  right.getMembers()  );  // TODO 20070212  !privs
      return this._createNewMembershipObjects(tmp);
    }
    catch (GroupNotFoundException eGNF) {
      throw new ModelException(eGNF);
    }
  } // private Set _evalCompositeIntersection()

  // Evaluate a union composite membership
  // @since 1.0
  private Set _evalCompositeUnion() 
    throws  ModelException
  {
    try {
      Set   tmp   = new LinkedHashSet();
      Group left  = this.getComposite().getLeftGroup();  // TODO 20070212  !privs
      Group right = this.getComposite().getRightGroup(); // TODO 20070212  !privs
      tmp.addAll( left.getMembers() );      // TODO 20070212  !privs
      tmp.addAll( right.getMembers()  );    // TODO 20070212  !privs
      return this._createNewMembershipObjects(tmp);
    }
    catch (GroupNotFoundException eGNF) {
      throw new ModelException(eGNF);
    }
  } // private Set _evalCompositeUnion()

  // Evaluate an immediate membership
  private Set _evalAddImmediate() 
    throws  ModelException
  {
    Set results = new LinkedHashSet();
    // If we are working on a group, where is it a member
    Set isMember = new LinkedHashSet();
    if ( this.getGroup() != null ) {
      isMember = PrivilegeResolver.internal_canViewMemberships(
        this.getGroup().getSession(),
        HibernateMembershipDAO.findAllByMember( this.getGroup().toMember().getUuid() )
      );
    }
    // Members of m if o is a group
    Set hasMembers = this._findMembersOfMember();
    // Add m to where o is a member if f == "members"
    results.addAll( this._addMemberToWhereGroupIsMember(isMember) );
    // Add members of m to o
    results.addAll( this._addHasMembersToOwner(hasMembers) ); 
    // Add members of m to where o is a member if f == "members"
    results.addAll( this._addHasMembersToWhereGroupIsMember(isMember, hasMembers) );
    return results;
  } // private Set _evalAddImmediate()

  // Find m's hasMembers
  private Set _findMembersOfMember() 
    throws  ModelException
  {
    Set hasMembers = new LinkedHashSet();
    if (this.getMemberDTO().getSubjectTypeId().equals("group")) {
      hasMembers = HibernateMembershipDAO.findAllByOwnerAndField(
        this.getMemberDTO().getSubjectId(), Group.getDefaultList()
      );
    }
    return hasMembers;
  } // private Set _findMembersOfMember()

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

