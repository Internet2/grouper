/*
 * Copyright (C) 2004-2005 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004-2005 The University Of Chicago
 * All Rights Reserved. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *  * Neither the name of the University of Chicago nor the names
 *    of its contributors nor the University Corporation for Advanced
 *   Internet Development, Inc. may be used to endorse or promote
 *   products derived from this software without explicit prior
 *   written permission.
 *
 * You are under no obligation whatsoever to provide any enhancements
 * to the University of Chicago, its contributors, or the University
 * Corporation for Advanced Internet Development, Inc.  If you choose
 * to provide your enhancements, or if you choose to otherwise publish
 * or distribute your enhancements, in source code form without
 * contemporaneously requiring end users to enter into a separate
 * written license agreement for such enhancements, then you thereby
 * grant the University of Chicago, its contributors, and the University
 * Corporation for Advanced Internet Development, Inc. a non-exclusive,
 * royalty-free, perpetual license to install, use, modify, prepare
 * derivative works, incorporate into the software or other computer
 * software, distribute, and sublicense your enhancements or derivative
 * works thereof, in binary and source code form.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND WITH ALL FAULTS.  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT ARE DISCLAIMED AND the
 * entire risk of satisfactory quality, performance, accuracy, and effort
 * is with LICENSEE. IN NO EVENT SHALL THE COPYRIGHT OWNER, CONTRIBUTORS,
 * OR THE UNIVERSITY CORPORATION FOR ADVANCED INTERNET DEVELOPMENT, INC.
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OR DISTRIBUTION OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  java.util.*;


/** 
 * Class implementing the memberOf algorithm.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: MemberOf.java,v 1.2 2005-03-15 15:37:00 blair Exp $
 */
public class MemberOf {

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private GrouperSession  s;


  /*
   * CONSTRUCTORS
   */

  /**
   * Create a new {@link MemberOf} object.
   * <p />
   * @param s Operate within this {@link GrouperSession}.
   * @return  new {@link MemberOf} object.
   */
  public MemberOf(GrouperSession s) {
    this.s = s;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Perform a memberOf calculation.
   * <p />
   * @param   gl  The list value that is being acted upon.
   * @return  A list of {@link GrouperList} objects.
   */
  public List memberOf(GrouperList gl) {
    List vals = new ArrayList();

    // TODO ADD VALIDATE CLASS!

    // Shortcuts for convenience
    GrouperGroup  g = gl.group();
    GrouperMember m = gl.member();
    String        l = gl.groupField();

    // Add m to g's gl
    vals.add(gl);

    // Where is g a member?
    GrouperMember gAsM  = GrouperMember.load(s, g.id(), "group");
    if (gAsM == null) {
      throw new RuntimeException("Error converting group to member");
    }
    List isMem = gAsM.listVals(l);

    Iterator iter = isMem.iterator();
    while (iter.hasNext()) {
      GrouperList lv = (GrouperList) iter.next();
      lv.load(this.s);
      GrouperGroup via = lv.via();
      if (via == null) {
        via = g;
      }
      // Add m to each
      vals.add(new GrouperList(lv.group(), m, l, via));
    }

    // If m is a group...
    if (m.typeID().equals("group")) {
      // ...add additional list values
      vals.addAll( this._addMembers(gl, isMem) );
    }

    return vals;
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Dtermine members of m that are affected by this list value change.
   */
  private List _addMembers(GrouperList gl, List isMem) {
    List vals = new ArrayList();

    // Shortcuts for convenience
    GrouperGroup  g = gl.group();
    GrouperMember m = gl.member();
    String        l = gl.groupField();

    // TODO Go through GG, not GB
    GrouperGroup  mAsG  = GrouperBackend.groupLoadByID(
                                  this.s, m.subjectID()
                                );
    Iterator      iterM = mAsG.listVals(l).iterator();
    while (iterM.hasNext()) {
      GrouperList lvM = (GrouperList) iterM.next();
      lvM.load(this.s);
      GrouperGroup viaM = lvM.via();
      if (lvM.via() == null) {
        viaM = mAsG;
      }
      // Add m's members to g
      vals.add( new GrouperList(g, lvM.member(), l, viaM) );

      Iterator iterG = isMem.iterator();
      while (iterG.hasNext()) {
        GrouperList lvG = (GrouperList) iterG.next();
        lvG.load(this.s);
        GrouperGroup viaG = lvG.via();
        if (lvG.via() == null) {
          viaG = mAsG;
        }
        // Add m's members to where g is a member
        vals.add(
          new GrouperList(lvG.group(), lvM.member(), l, viaG)
        );
      }
    }
    return vals;
  }

}

