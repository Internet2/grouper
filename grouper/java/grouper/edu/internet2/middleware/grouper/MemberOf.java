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


/** 
 * Perform <i>member of</i> calculation.
 * <p />
 * @author  blair christensen.
 * @version $Id: MemberOf.java,v 1.5 2005-12-02 17:17:01 blair Exp $
 */
class MemberOf implements Serializable {

  // Protected Class Methods
  
  // Find effective memberships, whether for addition or deletion
  protected static Set doMemberOf(GrouperSession s, Group g, Member m) 
    throws GroupNotFoundException
  {
    Set mships    = new LinkedHashSet();

    // Find where g is a member
    Set isMember  = g.toMember().getMemberships();

    // Add m to where g is a member
    mships.addAll(
      _findMembershipsWhereGroupIsMember(s, g, m, isMember)
    );

    // Add members of m to g
    // Add members of m to where g is a member
    mships.addAll(
      _findGroupAsMember(s, g.getUuid(), m, isMember)
    );

    return mships;
  } // protected static Set doMemberOf(s, m)

  // Find effective memberships, whether for addition or deletion
  protected static Set doMemberOf(GrouperSession s, Stem ns, Member m) 
    throws  GroupNotFoundException
  {
    Set mships    = new LinkedHashSet();

    // Stems can't be members
    Set isMember  = new LinkedHashSet();

    // Add members of m to ns
    // Add members of m to where ns is a member
    mships.addAll(
      _findGroupAsMember(s, ns.getUuid(), m, isMember)
    );

    return mships;
  } // protected static Set doMemberOf(s, m)


  // Private Class Methods

  // More effective membership voodoo
  private static Set _findGroupAsMember(
    GrouperSession s, String oid, Member m, Set isMember
  )
    throws  GroupNotFoundException
  {
    Set mships      = new LinkedHashSet();
    Set hasMembers  = new LinkedHashSet();
    
    if (m.getSubjectTypeId().equals("group")) {
      // Convert member back to a group
      Group gm = m.toGroup();

      // Find members of m
      hasMembers = gm.getMemberships();

      // Add members of m to g
      // Add members of m to where g is a member
      mships.addAll(
        _findMembershipsOfMember(
          s, oid, gm.getUuid(), isMember, hasMembers
        )
      );
    }
    return mships; 
  } // private static Set _findGroupAsMember(s, oid, m, isMember)

  // More effective membership voodoo
  private static Set _findMembershipsOfMember(
    GrouperSession s, String oid, String gmid, Set isMember, Set hasMembers
  ) 
  {
    Set mships = new LinkedHashSet();

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
      Membership msMofM = new Membership(
        s, oid, mofm.getMember_id(),
        Group.getDefaultList(), vid, depth
      );
      mships.add(msMofM);
      // ... and add to wherever this group is a member
      Iterator iterGisM = isMember.iterator();
      while (iterGisM.hasNext()) {
        Membership gism = (Membership) iterGisM.next();
        Membership msGisM = new Membership(
          s, gism.getOwner_id(), mofm.getMember_id(),
          Group.getDefaultList(), vid, depth + gism.getDepth() 
        );
        mships.add(msGisM);
      }
    }

    return mships;
  } // private static Set _findMembershipsOfMember(s, oid, gmid, isMember, hasMembers)

  // More effective membership voodoo
  private static Set _findMembershipsWhereGroupIsMember(
    GrouperSession s, Group g, Member m, Set isMember
  ) 
  {
    Set mships = new LinkedHashSet();
    // Add m to where g is a member
    Iterator iter = isMember.iterator();
    while (iter.hasNext()) {
      Membership  ms    = (Membership) iter.next();
      int         depth = ms.getDepth() + 1;
      String      vid   = ms.getVia_id();
      if (vid == null) {
        vid = g.getUuid();
      }
      Membership msGisM = new Membership(
        s, ms.getOwner_id(), m.getUuid(),
        Group.getDefaultList(), vid, depth
      );
      mships.add(msGisM);
    }
    return mships;
  } // private static Set _findMembershipsWhereGroupIsMember(s, g, m, isMember)

}

