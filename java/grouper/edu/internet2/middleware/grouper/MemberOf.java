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
 * @version $Id: MemberOf.java,v 1.11 2005-12-06 17:40:21 blair Exp $
 */
class MemberOf implements Serializable {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(MemberOf.class);


  // Protected Class Methods
  
  // Find effective memberships, whether for addition or deletion
  protected static Set doMemberOf(GrouperSession s, Group g, Member m, Field f) 
    throws  GroupNotFoundException
  {
    // In order to bypass privilege constraints since a subject may be
    // privileged to adjust memberships but not be privileged to see,
    // let alone alter, all of the effective memberships that change as
    // a result.
    GrouperSession  root    = GrouperSessionFinder.getRootSession();
    Set             mships  = new LinkedHashSet();
    String          msg     = "doMemberOf for '" + g.getName() + "'/'" 
      + m + "'/'" + f.getName() + "'";
    GrouperLog.debug(LOG, s, msg);

    // Proxy as root for a short period of time
    g.setSession(root);  

    // Find *everywhere* where g is a member - but do it as root
    Set isMember = g.toMember().getAllMemberships();

    // Add m to where g is a member - as root - but only we are adding
    // to g's "members" list
    if (f.equals(Group.getDefaultList())) {
      Set temp0 = _findMembershipsWhereGroupIsMember(root, s, g, m, isMember);
      mships.addAll(temp0);
    }

    // Add members of m to g - as root
    // Add members of m to where g is a member - as root
    mships.addAll(_findGroupAsMember(root, s, g.getUuid(), m, f, isMember));

    // Now reset everything to the proper session
    g.setSession(s);
    Set resetMships = new LinkedHashSet();
    // TODO Don't I already have a method to do this in bulk?
    Iterator iter = mships.iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();    
      ms.setSession(s);
      resetMships.add(ms);
      GrouperLog.debug(LOG, s, msg + " found: " + ms);
    }

    GrouperLog.debug(LOG, s, msg + ": " + resetMships.size());
    return resetMships;
  } // protected static Set doMemberOf(s, g, m, f)

  // Find effective memberships, whether for addition or deletion
  protected static Set doMemberOf(GrouperSession s, Stem ns, Member m, Field f) 
    throws  StemNotFoundException
  {
    // TODO Add logging as above
    Set mships    = new LinkedHashSet();

    // Stems can't be members
    Set isMember  = new LinkedHashSet();

    // Add members of m to ns
    // Add members of m to where ns is a member
    try {
      mships.addAll(
        _findGroupAsMember(s, s, ns.getUuid(), m, f, isMember)
      );
    }
    catch (GroupNotFoundException eGNF) {
      throw new StemNotFoundException(eGNF.getMessage());
    }

    return mships;
  } // protected static Set doMemberOf(s, ns, m, f)


  // Private Class Methods

  // If m is a group, find its members and add them to g and where g is
  // a member
  private static Set _findGroupAsMember(
    GrouperSession s, GrouperSession orig, String oid, Member m, 
    Field f, Set isMember
  )
    throws  GroupNotFoundException
  {
    Set     mships      = new LinkedHashSet();
    Set     hasMembers  = new LinkedHashSet();
    String  msg         = "findGroupAsMember";
    GrouperLog.debug(LOG, orig, msg);

    if (m.getSubjectTypeId().equals("group")) {
      // Convert member back to a group
      Group gm = m.toGroup();
      GrouperLog.debug(LOG, orig, msg + " member is group: " + m);
      // And attach root session for better looking up of memberships
      gm.setSession(GrouperSessionFinder.getRootSession());

      // Find members of m
      Set hasMships = gm.getMemberships();
      GrouperLog.debug(LOG, orig, msg + " has members: " + hasMships.size());

      // Add members of m to g
      // Add members of m to where g is a member
      mships.addAll(
        _findMembershipsOfMember(
          s, orig, oid, gm.getUuid(), f, isMember, hasMships
        )
      );
    }
    GrouperLog.debug(LOG, orig, msg + ": " + mships.size());
    return mships; 
  } // private static Set _findGroupAsMember(s, orig, oid, m, f, isMember)

  // Member is a group.  Look for its memberships and add them to where
  // the containing object is a member.
  private static Set _findMembershipsOfMember(
    GrouperSession s, GrouperSession orig, String oid, String gmid, Field f, 
    Set isMember, Set hasMembers
  ) 
  {
    // TODO Refactor into smaller components
    Set     mships  = new LinkedHashSet();
    String  msg     = "findMembershipsOfMember";
    GrouperLog.debug(LOG, orig, "findMembershipsOfMember");

    // Add members of m to where this group is a member

    // For every member of m...
    Iterator iterMofM = hasMembers.iterator();
    while (iterMofM.hasNext()) {
      Membership  mofm  = (Membership) iterMofM.next();
      // ... add to this group
      int         depth = mofm.getDepth() + 1;
      String      vid   = mofm.getVia_id();
      if (vid == null) {
        vid = gmid;
      }
      try {
        Membership msMofM = new Membership(
          s, oid, mofm.getMember(), f, vid, depth
        );
        GrouperLog.debug(LOG, orig, msg + " msMofM: " + msMofM);
        mships.add(msMofM);
      }
      catch (MemberNotFoundException eMNF0) {
        // TODO
        GrouperLog.warn(LOG, orig, eMNF0.getMessage());
      }
      // ... and add to wherever this is a member - as root - but only
      // if we were adding to g's "members" list
      if (f.equals(Group.getDefaultList())) {
        Iterator iterGisM = isMember.iterator();
        while (iterGisM.hasNext()) {
          Membership gism = (Membership) iterGisM.next();
          try {
            Membership msGisM = new Membership(
              s, gism.getOwner_id(), mofm.getMember(), Group.getDefaultList(), 
              vid, depth + gism.getDepth() 
            );
            GrouperLog.debug(LOG, orig, msg + " msGisM: " + msGisM);
            mships.add(msGisM);
          }
          catch (MemberNotFoundException eMNF1) {
            // TODO
            GrouperLog.warn(LOG, orig, eMNF1.getMessage());
          }
        }
      }
    }

    GrouperLog.debug(LOG, orig, msg + ": " + mships.size());
    return mships;
  } // private static Set _findMembershipsOfMember(s, orig, oid, gmid, f, isMember, hasMembers)

  // More effective membership voodoo
  private static Set _findMembershipsWhereGroupIsMember(
    GrouperSession s, GrouperSession orig, Group g, Member m, Set isMember
  ) 
  {
    Set     mships  = new LinkedHashSet();
    String  msg     = "findMembershipsWhereGroupIsMember";
    GrouperLog.debug(LOG, orig, msg);
    // Add m to where g is a member
    Iterator iter = isMember.iterator();
    while (iter.hasNext()) {
      Membership  ms    = (Membership) iter.next();
      ms.setSession(s);
      GrouperLog.debug(LOG, orig, msg + " group isMember: " + ms);
      int         depth = ms.getDepth() + 1;
      String      vid   = ms.getVia_id();
      if (vid == null) {
        vid = g.getUuid();
      }
      Membership msGisM = new Membership(
        s, ms.getOwner_id(), m, ms.getList(), vid, depth
      );
      mships.add(msGisM);
    }
    GrouperLog.debug(LOG, orig, msg + ": " + mships.size());
    return mships;
  } // private static Set _findMembershipsWhereGroupIsMember(s, orig, g, m, isMember)

}

