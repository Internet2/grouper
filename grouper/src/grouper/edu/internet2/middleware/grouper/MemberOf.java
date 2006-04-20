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


import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.logging.*;


/** 
 * Perform <i>member of</i> calculation.
 * <p />
 * @author  blair christensen.
 * @version $Id: MemberOf.java,v 1.14.2.1 2006-04-20 17:45:20 blair Exp $
 */
class MemberOf implements Serializable {

  // Protected Class Constants //
  protected static final String ERR_CT  = "invalid composite type: ";

  // Private Class Constants //
  private static final Log LOG = LogFactory.getLog(MemberOf.class);


  // Private Instance Variables //
  private Composite       c;
  private Set             deletes = new LinkedHashSet();
  private Field           f;
  private Group           g;    // FIXME Replace with this.o
  private Member          m;
  private Membership      ms;
  private Owner           o;
  private Stem            ns;   // FIXME Replace with this.o
  private GrouperSession  root;
  private GrouperSession  s;    
  private Set             saves   = new LinkedHashSet();


  // Constructors //
  private MemberOf(GrouperSession s, Membership ms) {
    this.s  = s;
    this.ms = ms;
  } // private MemberOf(s, ms)

  private MemberOf(GrouperSession s, Owner o, Composite c) {
    this.root = GrouperSessionFinder.getTransientRootSession();
    this.s    = s;
    this.o    = o;
    this.c    = c;
    this.c.setSession(this.root);
    this.o.setSession(this.root);
  } // private MemberOf(s, o, c)

  protected Set getDeletes() {
    return this.deletes;
  } // protected Set getDeletes()
  protected Set getSaves() {
    return this.saves;
  } // protected Set getSaves()


  // Protected Class Methods //
 
  // Calculate addition of a composite membership 
  protected static MemberOf addComposite(
    GrouperSession s, Owner o, Composite c
  )
    throws  ModelException
  {
    MemberOf mof = new MemberOf(s, o, c);
    mof.saves.addAll( mof._evalComposite() );
    mof._resetSessions();
    mof.o.setModified();
    mof.saves.add(mof.c);     // Save the composite
    mof.saves.add(mof.o);     // Update the owner
    return mof;
  } // protected static MemberOf addComposite(s, o, c)

  // Find effective memberships, whether for addition or deletion
  protected static Set doMemberOf(GrouperSession s, Membership ms) 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    MemberOf mof = new MemberOf(s, ms);
    return mof._calculate();
  } // protected static Set doMemberOf(s, ms)


  // Private Instance Methods //

  // Add m's hasMembers to g
  private Set _addHasMembersToGroup(Set hasMembers) 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    Set     mships  = new LinkedHashSet();
    Iterator iter = hasMembers.iterator();
    while (iter.hasNext()) {
      Membership hasMS = (Membership) iter.next();
      Membership  eff = Membership.newEffectiveMembership(
        this.s, this.ms, hasMS, 1
      );
      mships.add(eff);
    }
    return mships;
  } // private Set _addHasMembersToGroup(hasMembers)

  // Add m's hasMembers to where g isMember
  private Set _addHasMembersToWhereGroupIsMember(Set isMember, Set hasMembers) 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    Set     mships  = new LinkedHashSet();

    // Add the members of m to where g is a member but only if f == "members"
    if (this.f.equals(Group.getDefaultList())) {
      Iterator isIter = isMember.iterator();
      while (isIter.hasNext()) {
        Membership isMS = (Membership) isIter.next();
        Iterator hasIter = hasMembers.iterator();
        while (hasIter.hasNext()) {
          Membership  hasMS = (Membership) hasIter.next();
          Membership  eff   = Membership.newEffectiveMembership(
            this.s, isMS, hasMS, 2
          );
          mships.add(eff);
        }
      }
    }

    return mships;
  } // private Set _addHasMembersToWhereGroupIsMember(isMember, hasMembers)

  // Add m to where g isMember
  private Set _addMemberToWhereGroupIsMember(Set isMember) 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    Set     mships  = new LinkedHashSet();

    // Add m to where g is a member if f == "members"
    if (this.f.equals(Group.getDefaultList())) {
      Iterator isIter = isMember.iterator();
      while (isIter.hasNext()) {
        Membership  isMS  = (Membership) isIter.next();
        Membership  eff   = Membership.newEffectiveMembership(
          this.s, isMS, this.ms, 1
        );
        mships.add(eff);
      }
    }

    return mships;
  } // private Set _addHasMembersToWhereGroupIsMember(isMember, hasMembers)

  // Perform the memberOf calculation
  private Set _calculate() 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    this._extractObjects();

    Set mships  = new LinkedHashSet();

    // If we are working on a group, where is it a member
    Set isMember = new LinkedHashSet();
    if (this.g != null) {
      isMember = this.g.toMember().getAllMemberships();
    }

    // Members of m if m is a group
    Set hasMembers  = this._findMembersOfMember();

    // Add m to where g is a member if f == "members"
    mships.addAll( this._addMemberToWhereGroupIsMember(isMember) );

    // Add members of m to g
    mships.addAll( this._addHasMembersToGroup(hasMembers) ); 

    // Add members of m to where g is a member if f == "members"
    mships.addAll( this._addHasMembersToWhereGroupIsMember(isMember, hasMembers) );

    Set results = this._resetObjects(mships);
    return results;
  } // private Set _calculate()

  // Convert a set of memberships into composite memberships
  private Set _createNewMembershipObjects(Set members) 
    throws  ModelException
  {
    Set       mships  = new LinkedHashSet();
    Iterator iter     = members.iterator();
    while (iter.hasNext()) {
      Member      m   = (Member) iter.next();
      Membership  imm = new Membership(o, m, Group.getDefaultList(), c);
      imm.setSession(root);
      mships.add(imm);
    }
    return mships;
  } // private Set _createNewMembershipObjects(members)

  // Evaluate a composite membership
  private Set _evalComposite() 
    throws  ModelException 
  {
    Set results = new LinkedHashSet();
    if (this.c.getType().equals(CompositeType.UNION)) {
      results.addAll( this._evalCompositeUnion() );
    }
    else {
      throw new ModelException(ERR_CT + c.getType().toString());
    }
    return results;
  } // private Set _evalComposite()

  // Evaluate a union composite membership
  private Set _evalCompositeUnion() 
    throws  ModelException
  {
    Set results = new LinkedHashSet();
    Set tmp     = new LinkedHashSet();
    Group left  = (Group) this.c.getLeft();
    Group right = (Group) this.c.getRight();
    tmp.addAll( left.getMembers() );
    tmp.addAll( right.getMembers()  );
    return this._createNewMembershipObjects(tmp);
  } // private Set _evalCompositeUnion()

  // Setup instance variables based upon the membership passed
  private void _extractObjects() 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    this.root = GrouperSessionFinder.getRootSession();
    try {
      this.g = this.ms.getGroup();
      this.g.setSession(this.root);
    }
    catch (GroupNotFoundException eGNF) {
      try {
        this.ns = this.ms.getStem();
        this.ns.setSession(this.root);
      }
      catch (StemNotFoundException eSNF) {
        throw new GroupNotFoundException(eGNF.getMessage());
      }
    }
    this.m    = this.ms.getMember();
    this.m.setSession(this.root);
    this.f    = this.ms.getList();
  } // private void _extractObjects()

  // Find m's hasMembers
  private Set _findMembersOfMember() 
    throws  GroupNotFoundException
  {
    Set     hasMembers  = new LinkedHashSet();

    // If member is a group, convert to group and find its members.
    if (this.m.getSubjectTypeId().equals("group")) {
      // Convert member back to a group
      Group gAsM = this.m.toGroup();
      // And attach root session for better looking up of memberships
      gAsM.setSession(this.root);
      // Find members of m 
      hasMembers = gAsM.getMemberships();
    }
    return hasMembers;
  } // private Set _findMembersOfMember()

  // Reset the session on identified objects 
  private Set _resetObjects(Set mships) {
    Set results = new LinkedHashSet();
    // Now reset everything to the proper session
    if (this.g != null) {
      g.setSession(this.s);
    }
    if (this.ns != null) {
      ns.setSession(this.s);
    }
    m.setSession(this.s);
    // TODO Don't I already have a method to do this in bulk?
    //      But on the other hand, I **do** want to have the option of
    //      logging each-and-every one of these.
    Iterator iter = mships.iterator();
    while (iter.hasNext()) {
      Membership found = (Membership) iter.next();    
      found.setSession(this.s);
      results.add(found);
    }
    return results;
  } // private Set _resetObjects(mships)

  // Reset attached sessions to their original state
  private void _resetSessions() {
    if (this.c != null) {
      this.c.setSession(s);
    }
    if (this.o != null) {
      this.o.setSession(s);
    }
  } // private void _resetSessions()

}

