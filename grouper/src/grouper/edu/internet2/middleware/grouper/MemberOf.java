/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  net.sf.hibernate.*;

/** 
 * Perform <i>member of</i> calculation.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MemberOf.java,v 1.28 2006-07-20 00:16:36 blair Exp $
 */
class MemberOf {

  // PRIVATE INSTANCE VARIABLES //
  private Composite       c;
  private Set             deletes     = new LinkedHashSet();
  private Set             effDeletes  = new LinkedHashSet();
  private Set             effSaves    = new LinkedHashSet();
  private Field           f;
  private Member          m;
  private Membership      ms;
  private Owner           o;
  private GrouperSession  root;
  private GrouperSession  s;    
  private Set             saves       = new LinkedHashSet();


  // CONSTRUCTORS //
  private MemberOf(GrouperSession s, Owner o, Member m, Membership ms) {
    this.f  = ms.getList();
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
  // @since 1.0
  protected static MemberOf addComposite(
    GrouperSession s, Owner o, Composite c
  )
    throws  ModelException
  {
    MemberOf mof = new MemberOf(s, o, c);
    mof.effSaves.addAll(  mof._evalComposite()  );  // Find the composites
    mof.saves.addAll(     mof.effSaves          );
    mof._resetSessions();
    mof.o.setModified();
    mof.saves.add(mof.c);     // Save the composite
    mof.saves.add(mof.o);     // Update the owner
    return mof;
  } // protected static MemberOf addComposite(s, o, c)

  // @since 1.0
  protected static MemberOf addImmediate(
    GrouperSession s, Owner o, Membership ms, Member m
  )
    throws  ModelException
  {
    MemberOf  mof = new MemberOf(s, o, m, ms);
    mof.saves.add(m);       // Save the member
    mof.effSaves.addAll( mof._evalAddImmediate()  );  // Find the new effs
    mof.saves.addAll(   mof.effSaves              );
    mof._resetSessions();
    mof.o.setModified();
    mof.saves.add(ms);      // Save the immediate
    mof.saves.add(mof.o);   // Update the owner
    return mof;
  } // protected static MemberOf addImmediate(s, o, m, ms)

  // Calculate deletion of a composite membership 
  // @since 1.0
  //  TODO  Why do I need to include o?  Can't I just get that from c?
  protected static MemberOf delComposite(
    GrouperSession s, Owner o, Composite c
  )
    throws  ModelException
  {
    //  TODO  I'm really uncertain about this code.  Expect it to be
    //        both flawed and evolving for quite some time.
    MemberOf  mof   = new MemberOf(s, o, c);

    //  Delete this group's members
    //  TODO  I have performance concerns with this code
    Membership  ms;
    Iterator    iterH = ( (Group) o ).getMemberships().iterator();
    while (iterH.hasNext()) {
      ms = (Membership) iterH.next();
      try {
        MemberOf    msMof = MemberOf.delImmediate(
          s, o, ms, ms.getMember()
        );
        mof.deletes.addAll(     msMof.getDeletes()    );
      }
      catch (Exception e) {
        throw new ModelException(e);
      }
    }

    mof._resetSessions();
    mof.o.setModified();
    mof.deletes.add(mof.c);   // Delete the composite
    mof.saves.add(mof.o);     // Update the owner
    return mof;
  } // protected static MemberOf delComposite(s, o, c)

  // @since 1.0
  protected static MemberOf delImmediate(
    GrouperSession s, Owner o, Membership ms, Member m
  )
    throws  GroupNotFoundException,   // TODO
            HibernateException,
            MemberNotFoundException,  // TODO
            MembershipNotFoundException
  {
    MemberOf  mof = new MemberOf(s, o, m, ms);

    // Find child memberships that need deletion
    Membership  child;
    Set         children  = new LinkedHashSet();
    Iterator    iter      = MembershipFinder.findAllChildrenNoSessionNoPriv(ms).iterator();
    while (iter.hasNext()) {
      child = (Membership) iter.next();
      child.setSession(s);
      children.add(child);
    }
    mof.effDeletes.addAll(children);
    // Find all effective memberships that need deletion
    mof.effDeletes.addAll( 
      MembershipFinder.findAllForwardMembershipsNoPriv(s, ms, children) 
    );

    // And now set everything else
    mof.deletes.addAll(mof.effDeletes);
    mof.deletes.add(ms);
    mof._resetSessions();
    mof.o.setModified();
    mof.deletes.add(ms);    // Delete the immediate
    mof.saves.add(mof.o);   // Update the owner
    return mof;
  } // protected static MemberOf delImmediate(s, o, m, ms)


  // PROTECTED INSTANCE METHODS //

  // @since 1.0
  protected Set getDeletes() {
    return this.deletes;
  } // protected Set getDeletes()
  // @since 1.0
  protected Set getEffDeletes() {
    return this.effDeletes;
  } // protected Set getEffDeletes()
  // @since 1.0
  protected Set getEffSaves() {
    return this.effSaves;
  } // protected Set getEffSaves()
  // @since 1.0
  protected Set getSaves() {
    return this.saves;
  } // protected Set getSaves()


  // PRIVATE INSTANCE METHODS //

  // Add m's hasMembers to o
  private Set _addHasMembersToOwner(Set hasMembers) 
    throws  ModelException
  {
    Set         mships  = new LinkedHashSet();
    Membership  hasMS;
    Membership  eff;
    Iterator    iter    = hasMembers.iterator();
    while (iter.hasNext()) {
      hasMS = (Membership) iter.next();
      eff   = new Membership(this.s, this.ms, hasMS, 1);
      mships.add(eff);
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
      Membership  isMS;
      Iterator    hasIter;
      Membership  hasMS;
      Membership  eff;
      Iterator    isIter  = isMember.iterator();
      while (isIter.hasNext()) {
        isMS = (Membership) isIter.next();
        hasIter = hasMembers.iterator();
        while (hasIter.hasNext()) {
          hasMS = (Membership) hasIter.next();
          eff   = new Membership(this.s, isMS, hasMS, 2);
          mships.add(eff);
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
      Membership  isMS;
      Membership  eff;
      Iterator    isIter  = isMember.iterator();
      while (isIter.hasNext()) {
        isMS  = (Membership) isIter.next();
        eff   = new Membership(this.s, isMS, this.ms, 1);
        mships.add(eff);
      }
    }

    return mships;
  } // private Set _addHasMembersToWhereGroupIsMember(isMember, hasMembers)

  // Convert a set of memberships into composite memberships
  private Set _createNewMembershipObjects(Set members) 
    throws  ModelException
  {
    Set         mships  = new LinkedHashSet();
    Member      m;
    Membership  imm;
    Iterator    iter    = members.iterator();
    while (iter.hasNext()) {
      m   = (Member) iter.next();
      imm = new Membership(o, m, this.f, c);
      imm.setSession(root);
      mships.add(imm);
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
      tmp.addAll(     left.getMembers()   );
      tmp.removeAll(  right.getMembers()  );
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
      isMember = ( (Group) o).toMember().getAllMemberships();
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
    if (this.c != null) {
      this.c.setSession(this.s);
    }
    if (this.o != null) {
      this.o.setSession(this.s);
    }
    Set         tmp       = new LinkedHashSet();
    Membership  msS;
    Iterator    saveIter  = this.effSaves.iterator();
    while (saveIter.hasNext()) {
      msS = (Membership) saveIter.next();
      msS.setSession(this.s);
      tmp.add(msS);
    }
    this.saves            = tmp;
    tmp                   = new LinkedHashSet();
    Membership  msD;
    Iterator    delIter   = this.deletes.iterator();
    while (delIter.hasNext()) {
      msD = (Membership) delIter.next();
      msD.setSession(this.s);
      tmp.add(msD);
    }
    this.deletes = tmp;
  } // private void _resetSessions()

  // Switch all sessions to root sessions
  private void _setSessions() {
    this.root = GrouperSession.startTransient();
    if (this.c != null) {
      this.c.setSession(this.root);
    }
    if (this.m != null) {
      this.m.setSession(this.root);
    }
    if (this.o != null) {
      this.o.setSession(this.root);
    }
  } // private void _setSessions()

}

