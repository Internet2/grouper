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
import  java.util.*;

/** 
 * Perform <i>member of</i> calculation.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberOf.java,v 1.38 2007-02-08 16:25:25 blair Exp $
 */
class MemberOf {

  // PRIVATE INSTANCE VARIABLES //
  private Composite       c;
  private Set             deletes     = new LinkedHashSet();
  private MembershipDTO   ms;
  private Set             effDeletes  = new LinkedHashSet();
  private Set             effSaves    = new LinkedHashSet();
  private Field           f;
  private Owner           o;
  private Member          m;
  private GrouperSession  root;
  private GrouperSession  s;    
  private Set             saves       = new LinkedHashSet();


  // CONSTRUCTORS //

  // @since   1.2.0
  private MemberOf(GrouperSession s, Owner o, Member m, MembershipDTO ms) 
    throws  SchemaException
  {
    this.f  = FieldFinder.find( ms.getListName() );
    this.m  = m;
    this.ms = ms;
    this.o  = o;
    this.s  = s;
    this._setSessions();
  } // private MemberOf(s, o, m, ms)

  // @since 1.0
  private MemberOf(GrouperSession s, Owner o, Composite c) {
    this.c    = c;
    this.f    = Group.getDefaultList();
    this.o    = o;
    this.s    = s;
    this._setSessions();
  } // private MemberOf(s, o, c)


  // PROTECTED CLASS METHODS //
 
  // Calculate addition of a composite membership 
  // @since   1.2.0
  protected static MemberOf internal_addComposite(GrouperSession s, Group g, Composite c)
    throws  ModelException
  {
    MemberOf mof = new MemberOf(s, g, c);
    mof.effSaves.addAll(  mof._evalComposite()  );  // Find the composites
    mof.saves.addAll(     mof.effSaves          );
    mof._resetSessions();
    ( (Group) mof.o ).internal_setModified();
    mof.saves.add(mof.c);     // Save the composite
    mof.saves.add(mof.o);     // Update the owner
    return mof;
  } // protected static MemberOf internal_addComposite(s, g, c)

  // @since   1.2.0
  protected static MemberOf internal_addImmediate(
    GrouperSession s, Owner o, MembershipDTO dto, Member m
  )
    throws  ModelException
  {
    try {
      MemberOf  mof = new MemberOf(s, o, m, dto);
      mof.saves.add(m);     // Save the member
      mof.effSaves.addAll( mof._evalAddImmediate()  );  // Find the new effs
      mof.saves.addAll(   mof.effSaves              );
      mof._resetSessions();
      // TODO 20070130 bah
      if (o instanceof Group) {
        ( (Group) mof.o ).internal_setModified();
      }
      else {
        ( (Stem) mof.o ).internal_setModified();
      }
      mof.saves.add(dto);   // Save the immediate
      mof.saves.add(mof.o); // Update the owner
      return mof;
    }
    catch (SchemaException eS) {
      throw new ModelException( eS.getMessage(), eS );
    }
  } // protected static MemberOf internal_addImmediate(s, o, m, dto)

  // Calculate deletion of a composite membership 
  // @since   1.2.0
  protected static MemberOf internal_delComposite(GrouperSession s, Owner o, Composite c)
    throws  ModelException
  {
    //  TODO  20061011 In theory I shouldn't need to pass along `o` as I can just get it 
    //        via `c.getOwner()` *but* `TestGroup36` throws a `HibernateException`
    //        when I retrieve `o` that way.
    
    //  TODO  20061011 I'm really uncertain about this code.  Expect it to be
    //        both flawed and evolving for quite some time.
    
    MemberOf  mof   = new MemberOf(s, o, c);

    // Delete this group's members
    Membership  ms;
    Iterator    iterH = ( (Group) o ).getMemberships().iterator();
    while (iterH.hasNext()) {
      ms = (Membership) iterH.next();
      try {
        MemberOf msMof = MemberOf.internal_delImmediate( s, o, ms.getDTO(), ms.getMember() );
        mof.deletes.addAll( msMof.internal_getDeletes() );
      }
      catch (Exception e) {
        throw new ModelException(e);
      }
    }

    mof._resetSessions();
    // TODO 20070130 bah
    if (o instanceof Group) {
      ( (Group) mof.o ).internal_setModified();
    }
    else {
      ( (Stem) mof.o ).internal_setModified();
    }
    mof.deletes.add(mof.c);   // Delete the composite
    mof.saves.add(mof.o);     // Update the owner
    return mof;
  } // protected static MemberOf internal_delComposite(s, o, c)

  // @since   1.2.0
  protected static MemberOf internal_delImmediate(
    GrouperSession s, Owner o, MembershipDTO dto, Member m
  )
    throws  MemberDeleteException
  {
    try {
      MemberOf  mof = new MemberOf(s, o, m, dto);

      // Find child memberships that need deletion
      MembershipDTO child;
      Set           children  = new LinkedHashSet();
      Iterator      it        = MembershipFinder.internal_findAllChildrenNoPriv(dto).iterator();
      while (it.hasNext()) {
        children.add( (MembershipDTO) it.next() );
      }
      mof.effDeletes.addAll(children);
      // Find all effective memberships that need deletion
      mof.effDeletes.addAll( 
        MembershipFinder.internal_findAllForwardMembershipsNoPriv(s, dto, children) 
      );

      // And now set everything else
      mof.deletes.addAll(mof.effDeletes);
      mof._resetSessions();
      // TODO 20070130 bah    
      if (o instanceof Group) {
        ( (Group) mof.o ).internal_setModified();
      }
      else {
        ( (Stem) mof.o ).internal_setModified();
      }
      mof.deletes.add(dto); // Delete the immediate
      // TODO 20070201 disabled
      // mof.saves.add(mof.o); // Update the owner
      return mof;
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException( eS.getMessage(), eS );
    }
  } // protected static MemberOf internal_delImmediate(s, o, m, dto)


  // PROTECTED INSTANCE METHODS //

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
    Membership    hasMS;
    Membership    eff;
    MembershipDTO dto;
    Iterator      iter    = hasMembers.iterator();
    while (iter.hasNext()) {
      hasMS = (Membership) iter.next();

      dto = new MembershipDTO();
      dto.setCreateTime( new Date().getTime() );
      dto.setCreatorUuid( this.s.getMember().getUuid() );
      dto.setDepth( this.ms.getDepth() + hasMS.getDTO().getDepth() + 1 );
      dto.setListName( this.ms.getListName() );
      dto.setListType( this.ms.getListType() );
      dto.setMemberUuid( hasMS.getDTO().getMemberUuid() );
      dto.setMembershipUuid( GrouperUuid.internal_getUuid() );
      dto.setOwnerUuid( this.ms.getOwnerUuid() );
      dto.setType(Membership.EFFECTIVE);
      if ( hasMS.getDTO().getDepth() == 0 ) {
        dto.setViaUuid( hasMS.getDTO().getOwnerUuid() );  // hasMember m was immediate
        dto.setParentUuid( this.ms.getMembershipUuid() );
      }
      else {
        dto.setViaUuid( hasMS.getDTO().getViaUuid() ); // hasMember m was effective
        if ( hasMS.getDTO().getParentUuid() != null ) {
          dto.setParentUuid( hasMS.getDTO().getParentUuid() );
        }
        else {
          dto.setParentUuid( hasMS.getDTO().getMembershipUuid() );
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
    if (this.f.equals(Group.getDefaultList())) {
      Membership    isMS;
      Iterator      hasIter;
      Membership    hasMS;
      Membership    eff;
      MembershipDTO dto;
      Iterator      isIter  = isMember.iterator();
      while (isIter.hasNext()) {
        isMS = (Membership) isIter.next();
        hasIter = hasMembers.iterator();
        while (hasIter.hasNext()) {
          hasMS = (Membership) hasIter.next();

          dto = new MembershipDTO();
          dto.setCreateTime( new Date().getTime() );
          dto.setCreatorUuid( this.s.getMember().getUuid() );
          dto.setDepth( isMS.getDTO().getDepth() + hasMS.getDTO().getDepth() + 2 );
          dto.setListName( isMS.getDTO().getListName() );
          dto.setListType( isMS.getDTO().getListType() );
          dto.setMemberUuid( hasMS.getDTO().getMemberUuid() );
          dto.setMembershipUuid( GrouperUuid.internal_getUuid() );
          dto.setOwnerUuid( isMS.getDTO().getOwnerUuid() );
          dto.setType(Membership.EFFECTIVE);
          if ( hasMS.getDTO().getDepth() == 0 ) {
            dto.setViaUuid( hasMS.getDTO().getOwnerUuid() );  // hasMember m was immediate
            dto.setParentUuid( isMS.getDTO().getMembershipUuid() );
          }
          else {
            dto.setViaUuid( hasMS.getDTO().getViaUuid() ); // hasMember m was effective
            if ( hasMS.getDTO().getParentUuid() != null ) {
              dto.setParentUuid( hasMS.getDTO().getParentUuid() );
            }
            else {
              dto.setParentUuid( hasMS.getDTO().getMembershipUuid() );
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
    if (this.f.equals(Group.getDefaultList())) {
      Membership    isMS;
      Membership    eff;
      MembershipDTO dto;
      Iterator      isIter  = isMember.iterator();
      while (isIter.hasNext()) {
        isMS  = (Membership) isIter.next();

        dto = new MembershipDTO();
        dto.setCreateTime( new Date().getTime() );
        dto.setCreatorUuid( this.s.getMember().getUuid() );
        dto.setDepth( isMS.getDTO().getDepth() + this.ms.getDepth() + 1 );
        dto.setListName( isMS.getDTO().getListName() );
        dto.setListType( isMS.getDTO().getListType() );
        dto.setMemberUuid( this.ms.getMemberUuid() );
        dto.setMembershipUuid( GrouperUuid.internal_getUuid() );
        dto.setOwnerUuid( isMS.getDTO().getOwnerUuid() );
        dto.setType(Membership.EFFECTIVE);
        if ( this.ms.getDepth() == 0 ) {
          dto.setViaUuid( this.ms.getOwnerUuid() );  // ms m was immediate
          dto.setParentUuid( isMS.getDTO().getMembershipUuid() );
        }
        else {
          dto.setViaUuid( this.ms.getViaUuid() ); // ms m was effective
          if ( this.ms.getParentUuid() != null ) {
            dto.setParentUuid( this.ms.getParentUuid() );
          }
          else {
            dto.setParentUuid( this.ms.getMembershipUuid() );
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
    Membership    imm;
    MembershipDTO dto;
    Iterator      iter    = members.iterator();
    while (iter.hasNext()) {
      m   = (Member) iter.next();

      dto = new MembershipDTO();
      dto.setCreateTime( new Date().getTime() );
      dto.setCreatorUuid( this.s.getMember().getUuid() );
      dto.setDepth(0);
      dto.setListName( this.f.getName() );
      dto.setListType( this.f.getType().toString() );
      dto.setMemberUuid( m.getUuid() );
      dto.setMembershipUuid( GrouperUuid.internal_getUuid() );
      dto.setOwnerUuid( o.getUuid() );
      dto.setParentUuid(null);
      dto.setType(Membership.COMPOSITE);
      dto.setViaUuid( this.c.getUuid() );
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
    if      (this.c.getType().equals(CompositeType.COMPLEMENT))   {
      results.addAll( this._evalCompositeComplement() );
    }
    else if (this.c.getType().equals(CompositeType.INTERSECTION)) {
      results.addAll( this._evalCompositeIntersection() );
    }
    else if (this.c.getType().equals(CompositeType.UNION))        {
      results.addAll( this._evalCompositeUnion() );
    }
    else {
      throw new ModelException(E.MOF_CTYPE + c.getType().toString());
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
      Group left  = this.c.getLeftGroup();
      Group right = this.c.getRightGroup();
      tmp.addAll( left.getMembers() );
      tmp.removeAll( right.getMembers() );
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
      Group left  = this.c.getLeftGroup();
      Group right = this.c.getRightGroup();
      tmp.addAll(     left.getMembers()   );
      tmp.retainAll(  right.getMembers()  );
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
      Group left  = this.c.getLeftGroup();
      Group right = this.c.getRightGroup();
      tmp.addAll( left.getMembers() );
      tmp.addAll( right.getMembers()  );
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
    Set results   = new LinkedHashSet();
    // If we are working on a group, where is it a member
    Set isMember  = new LinkedHashSet();
    if (this.o instanceof Group) {
      isMember = MembershipFinder.internal_findAllByMember( 
        ( (Group) o ).getSession(), ( (Group) o ).toMember() 
      );
    }
    // Members of m if o is a group
    Set hasMembers  = this._findMembersOfMember();
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
    Set     hasMembers  = new LinkedHashSet();

    // If member is a group, convert to group and find its members.
    if (this.m.getSubjectTypeId().equals("group")) {
      try {
        // Convert member back to a group
        Group gAsM = this.m.toGroup();
        // And attach root session for better looking up of memberships
        gAsM.setSession(this.root);
        // Find members of m 
        hasMembers = gAsM.getMemberships();
      }
      catch (GroupNotFoundException eGNF) {
        throw new ModelException(eGNF);
      }
    }
    return hasMembers;
  } // private Set _findMembersOfMember()

  // Reset attached sessions to their original state
  private void _resetSessions() {
    if (this.o != null) {
      if (this.o instanceof Group) {
        ( (Group) this.o ).setSession(this.s);
      }
      else {
        ( (Stem) this.o ).setSession(this.s);
      }
    }
    // TODO 20070124 WTF!?!?
    Set           tmp = new LinkedHashSet();
    MembershipDTO ms;
    Iterator      it  = this.effSaves.iterator();
    while ( it.hasNext() ) {
      ms = (MembershipDTO) it.next();
      tmp.add(ms);
    }
    this.saves  = tmp;
    tmp         = new LinkedHashSet();
    it          = this.deletes.iterator();
    while ( it.hasNext() ) {
      ms = (MembershipDTO) it.next();
      tmp.add(ms);
    }
    this.deletes = tmp;
  } // private void _resetSessions()

  // Switch all sessions to root sessions
  private void _setSessions() {
    this.root = this.s.getDTO().getRootSession();
    if (this.m != null) {
      this.m.setSession(this.root);
    }
    if (this.o != null) {
      if (this.o instanceof Group) {
        ( (Group) this.o ).setSession(this.root);
      }
      else {
        ( (Stem) this.o ).setSession(this.root);
      }
    }
  } // private void _setSessions()

}

