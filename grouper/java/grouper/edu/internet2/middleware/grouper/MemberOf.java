/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
 * @version $Id: MemberOf.java,v 1.12 2005-12-09 07:35:38 blair Exp $
 */
class MemberOf implements Serializable {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(MemberOf.class);


  // Private Instance Variables
  private Field           f;
  private Group           g;
  private Member          m;
  private Membership      ms;
  private String          msg;
  private Stem            ns;
  private GrouperSession  root;
  private GrouperSession  s;


  // Constructors
  private MemberOf(GrouperSession s, Membership ms) {
    this.s    = s;
    this.ms   = ms;
    this.msg  = "doMemberOf for " + ms;
  }

  // Protected Class Methods
  
  // Find effective memberships, whether for addition or deletion
  protected static Set doMemberOf(GrouperSession s, Membership ms) 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    MemberOf mof = new MemberOf(s, ms);
    return mof._calculate();
  } // protected static Set doMemberOf(s, ms)


  // Private Instance Methods

  // Add m's hasMembers to g
  private Set _addHasMembersToGroup(Set hasMembers) 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    Set     mships  = new LinkedHashSet();
    String  _msg    = this.msg + " addHasMembersToGroup";
    GrouperLog.debug(LOG, this.s, _msg);
    Iterator iter = hasMembers.iterator();
    while (iter.hasNext()) {
      Membership hasMS = (Membership) iter.next();
      GrouperLog.debug(LOG, this.s, _msg + " hasMember: " + hasMS);
      Membership  eff = Membership.newEffectiveMembership(
        this.s, this.ms, hasMS, 1
      );
      mships.add(eff);
      GrouperLog.debug(LOG, this.s, _msg + " found: " + eff);
    }
    GrouperLog.debug(LOG, this.s, _msg + ": " + mships.size());
    return mships;
  } // private Set _addHasMembersToGroup(hasMembers)

  // Add m's hasMembers to where g isMember
  private Set _addHasMembersToWhereGroupIsMember(Set isMember, Set hasMembers) 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    Set     mships  = new LinkedHashSet();
    String  _msg    = this.msg + " addHasMembersToWhereGroupIsMember";
    GrouperLog.debug(LOG, this.s, _msg);

    // Add the members of m to where g is a member but only if f == "members"
    if (this.f.equals(Group.getDefaultList())) {
      Iterator isIter = isMember.iterator();
      while (isIter.hasNext()) {
        Membership isMS = (Membership) isIter.next();
        GrouperLog.debug(LOG, this.s, _msg + " isMember: " + isMS);
        Iterator hasIter = hasMembers.iterator();
        while (hasIter.hasNext()) {
          Membership  hasMS = (Membership) hasIter.next();
          GrouperLog.debug(LOG, this.s, _msg + " hasMembers: " + hasMS);
          Membership  eff   = Membership.newEffectiveMembership(
            this.s, isMS, hasMS, 2
          );
          mships.add(eff);
          GrouperLog.debug(LOG, this.s, _msg + " found: " + eff);
        }
      }
    }

    GrouperLog.debug(LOG, this.s, _msg + ": " + mships.size());
    return mships;
  } // private Set _addHasMembersToWhereGroupIsMember(isMember, hasMembers)

  // Add m to where g isMember
  private Set _addMemberToWhereGroupIsMember(Set isMember) 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    Set     mships  = new LinkedHashSet();
    String  _msg    = this.msg + " addMemberToWhereGroupIsMember";
    GrouperLog.debug(LOG, this.s, _msg);

    // Add m to where g is a member if f == "members"
    if (this.f.equals(Group.getDefaultList())) {
      Iterator isIter = isMember.iterator();
      while (isIter.hasNext()) {
        Membership  isMS  = (Membership) isIter.next();
        GrouperLog.debug(LOG, this.s, _msg + " isMember: " + isMS);
        Membership  eff   = Membership.newEffectiveMembership(
          this.s, isMS, this.ms, 1
        );
        mships.add(eff);
        GrouperLog.debug(LOG, this.s, _msg + " found: " + eff);
      }
    }

    GrouperLog.debug(LOG, this.s, _msg + ": " + mships.size());
    return mships;
  } // private Set _addHasMembersToWhereGroupIsMember(isMember, hasMembers)

  // Perform the memberOf calculation
  private Set _calculate() 
    throws  GroupNotFoundException,
            MemberNotFoundException
  {
    this._extractObjects();
    GrouperLog.debug(LOG, s, msg);

    Set mships  = new LinkedHashSet();

    // If we are working on a group, where is it a member
    Set isMember = new LinkedHashSet();
    if (this.g != null) {
      isMember = this.g.toMember().getAllMemberships();
    }
    GrouperLog.debug(LOG, this.s, this.msg + " g isMember: " + isMember.size());

    // Members of m if m is a group
    Set hasMembers  = this._findMembersOfMember();
    GrouperLog.debug(LOG, this.s, this.msg + " m hasMembers: " + hasMembers.size());

    // Add m to where g is a member if f == "members"
    mships.addAll( this._addMemberToWhereGroupIsMember(isMember) );

    // Add members of m to g
    mships.addAll( this._addHasMembersToGroup(hasMembers) ); 

    // Add members of m to where g is a member if f == "members"
    mships.addAll( this._addHasMembersToWhereGroupIsMember(isMember, hasMembers) );

    Set results = this._resetObjects(mships);
    GrouperLog.debug(LOG, this.s, this.msg + " total: " + results.size());
    return results;
  } // private Set _calculate()

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
    String  _msg        = this.msg + " findMembersOfMember " + m;
    GrouperLog.debug(LOG, this.s, _msg);

    // If member is a group, convert to group and find its members.
    if (this.m.getSubjectTypeId().equals("group")) {
      // Convert member back to a group
      Group gAsM = this.m.toGroup();
      GrouperLog.debug(LOG, this.s, _msg + " is group");
      // And attach root session for better looking up of memberships
      gAsM.setSession(this.root);
      // Find members of m 
      hasMembers = gAsM.getMemberships();
    }
    GrouperLog.debug(LOG, this.s, _msg + " found: " + hasMembers.size());
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
      GrouperLog.debug(LOG, this.s, this.msg + " found: " + found);
    }
    return results;
  } // private Set _resetObjects(mships)

}

