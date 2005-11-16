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
 * @version $Id: MemberOf.java,v 1.1 2005-11-16 21:51:05 blair Exp $
 */
class MemberOf implements Serializable {

  // Protected Class Methods
  
  // Find effective memberships, whether for addition or deletion
  protected static Set doMemberOf(GrouperSession s, Group g, Member m) 
    throws GroupNotFoundException
  {
    Set mships    = new HashSet();

    // Find where g is a member
    Set isMember  = g.toMember().getMemberships();

    // Add m to where g is a member
    mships.addAll(
      _findMembershipsWhereGroupIsMember(s, g, m, isMember)
    );

    Set hasMembers  = new HashSet();
    if (m.getSubjectTypeId().equals("group")) {
      // Convert member back to a group
      Group gm = m.toGroup();

      // Find members of m
      hasMembers = gm.getMemberships();

      // Add members of m to g
      // Add members of m to where g is a member
      mships.addAll(
        _findMembershipsOfMember(s, g, gm, isMember, hasMembers)
      );
    }
    return mships;
  } // protected static Set doMemberOf(s, m)

  // Find effective memberships, whether for addition or deletion
  protected static Set doMemberOf(GrouperSession s, Stem ns, Member m) 
    throws GroupNotFoundException
  {
    Set mships    = new HashSet();

    // Stems can't be members
    Set isMember  = new HashSet();

    Set hasMembers  = new HashSet();
/*
    if (m.getSubjectTypeId().equals("group")) {
      // Convert member back to a group
      Group gm = m.toGroup();

      // Find members of m
      hasMembers = gm.getMemberships();

      // Add members of m to g
      // Add members of m to where g is a member
      mships.addAll(
        _findMembershipsOfMember(s, g, gm, isMember, hasMembers)
      );
    }
*/
    return mships;
  } // protected static Set doMemberOf(s, m)


  // Private Class Methods

  // Part of the effective membership|memberOf voodoo  
  private static Set _findMembershipsOfMember(
    GrouperSession s, Group g, Group gm, Set isMember, Set hasMembers
  ) 
  {
    Set mships = new HashSet();

    // Add members of m to where this group is a member

    // For every member of m...
    Iterator iterMofM = hasMembers.iterator();
    while (iterMofM.hasNext()) {
      Membership  mofm  = (Membership) iterMofM.next();
      // ... add to this group
      int         depth = mofm.getDepth() + 1;
      String      vid   = mofm.getVia_id();
      if (vid == null) {
        vid = gm.getUuid();
      }
      mships.add(
        new Membership(
          s, g.getUuid(), mofm.getMember_id(),
          Group.LIST, vid, depth
        )
      );
      // ... and add to wherever this group is a member
      Iterator iterGisM = isMember.iterator();
      while (iterGisM.hasNext()) {
        Membership gism = (Membership) iterGisM.next();
        mships.add(
          new Membership(
            s, gism.getOwner_id(), mofm.getMember_id(),
            Group.LIST, vid, depth + gism.getDepth() 
          )
        );
      }
    }

    return mships;
  } // private static Set _findMembershipsOfMember(s, g, gm, isMember, hasMembers)

  // Part of the effective membership|memberOf voodoo  
  private static Set _findMembershipsWhereGroupIsMember(
    GrouperSession s, Group g, Member m, Set isMember
  ) 
  {
    Set mships = new HashSet();
    // Add m to where g is a member
    Iterator iter = isMember.iterator();
    while (iter.hasNext()) {
      Membership  ms    = (Membership) iter.next();
      int         depth = ms.getDepth() + 1;
      String      vid   = ms.getVia_id();
      if (vid == null) {
        vid = g.getUuid();
      }
      mships.add(
        new Membership(
          s, ms.getOwner_id(), ms.getMember_id(), 
          Group.LIST, vid, depth
        )
      );
    }
    return mships;
  } // private static Set _findMembershipsWhereGroupIsMember(s, g, m, isMember)

}

