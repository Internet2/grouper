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
 * @version $Id: MemberOf.java,v 1.15 2006-02-21 17:11:32 blair Exp $
 */
class MemberOf implements Serializable {

  // Private Class Constants
  private static final EventLog EL  = new EventLog();
  private static final Log      LOG = LogFactory.getLog(MemberOf.class);


  // Private Instance Variables
  private Field           f;
  private Group           g;
  private Member          m;
  private Membership      ms;
  private Stem            ns;
  private GrouperSession  root;
  private GrouperSession  s;


  // Constructors
  private MemberOf(Membership ms) {
    this.s  = ms.getSession();
    this.ms = ms;
  } // private MemberOf(ms)


  // Protected Class Methods
  
  // Find effective memberships for addition
  protected static Set findMembersToAdd(Membership ms) 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    MemberOf mof = new MemberOf(ms);
    return mof._calculateAddition();
  } // protected static Set findMembersToAdd(ms)

  // Find effective memberships for deletion
  protected static Set findMembersToDel(Membership ms) 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    MemberOf mof = new MemberOf(ms);
    return mof._calculateDeletion();
  } // protected static Set findMembersToDel(ms)


  // Private Instance Methods

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

  // Add m's hasMembers to g
  // TODO @DRY
  private Set _addHasMembersToGroupAddition(Set hasMembers) 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    Set     mships  = new LinkedHashSet();
    Iterator iter = hasMembers.iterator();
    while (iter.hasNext()) {
      Membership hasMS = (Membership) iter.next();
      if (hasMS.getDepth() == 0) {
        try {
          Set exists = MembershipFinder.findEffectiveMemberships(
            hasMS.getSession(), ms.getGroup(), hasMS.getMember().getSubject(),
            ms.getList(), hasMS.getGroup(), 1      
          );
          // This effective mship already exists so don't try to add
          // it again.
          if (exists.size() == 1) {
            continue;  
          }
          else  if (exists.size() > 1) {
            EL.error("AHMTGA.WTF! exists=="+ exists.size() + "/="+exists);
          }
        }
        catch (Exception e) {
          // Ignore
          EL.error("AHMTGA.E="+e.getMessage());
        }
      }
      Membership  eff = Membership.newEffectiveMembership(
        this.s, this.ms, hasMS, 1
      );
      mships.add(eff);
    }
    return mships;
  } // private Set _addHasMembersToGroupAddition(hasMembers)

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
  // TODO @DRY
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

  // Add m to where g isMember
  // TODO @DRY
  private Set _addMemberToWhereGroupIsMemberAddition(Set isMember) 
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
  } // private Set _addHasMembersToWhereGroupIsMemberAddition(isMember, hasMembers)

  // Add m's hasMembers to where g isMember
  // TODO @DRY
  private Set _addHasMembersToWhereGroupIsMemberAddition(Set isMember, Set hasMembers) 
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
          if (hasMS.getDepth() == 0) {
            try {
              Set exists = MembershipFinder.findEffectiveMemberships(
                hasMS.getSession(), isMS.getGroup(), hasMS.getMember().getSubject(),
                isMS.getList(), hasMS.getGroup(), 1      
              );
              // This effective mship already exists so don't try to add
              // it again.
              if (exists.size() == 1) {
                continue;  
              }
              else  if (exists.size() > 1) {
                EL.error("AHMTWGIMA.WTF! exists=="+ exists.size() + "/="+exists);
              }
            }
            catch (Exception e) {
              // Ignore
              EL.error("AHMTWGIMA.E="+e.getMessage());
            }
          } 
          Membership  eff   = Membership.newEffectiveMembership(
            this.s, isMS, hasMS, 2
          );
          mships.add(eff);
        }
      }
    }

    return mships;
  } // private Set _addHasMembersToWhereGroupIsMemberAddition(isMember, hasMembers)

  // Perform the memberOf calculation
  private Set _calculateDeletion() 
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
  } // private Set _calculateDeletion()

  private Set _calculateAddition() 
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
    mships.addAll( this._addMemberToWhereGroupIsMemberAddition(isMember) );

    // Add members of m to g
    mships.addAll( this._addHasMembersToGroupAddition(hasMembers) ); 

    // Add members of m to where g is a member if f == "members"
    mships.addAll( this._addHasMembersToWhereGroupIsMemberAddition(isMember, hasMembers) );

    Set results = this._resetObjects(mships);
    return results;
  } // private Set _calculateAddition()

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

}

