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
 * @version $Id: MemberOf.java,v 1.6 2005-03-20 05:15:31 blair Exp $
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
    GrouperSession.validate(s);
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

    // Ensure that the grouper list is properly loaded
    gl.load(this.s); // TODO Argh!

    // Add m to g's gl
    vals.add(gl);

    // Where is g a member?
    GrouperMember m = gl.group().toMember();
    List isMem = m.listVals( gl.groupField() );

    // Add m to groups where g is a member
    vals.addAll( this._addWhereIsMem(gl, isMem) );

    // If m is a group...
    if (gl.member().typeID().equals("group")) {
      // ...add additional list values
      vals.addAll( this._addHasMembers(gl, isMem) );
    }

    vals = this._addAndCache(vals);

    return vals;
  }


  private List _addAndCache(List vals) {
    Map   cache       = new HashMap();
    List  chainedVals = new ArrayList();

    DbSess dbSess = new DbSess();

    Iterator valIter = vals.iterator();
    while (valIter.hasNext()) {
      GrouperList lv = (GrouperList) valIter.next();
      String  chainKey  = null;
      int     idx       = 0;
      chainKey = MemberVia.load(s, lv.key(), lv.chain());
      if (chainKey == null) {
        if (cache.containsKey(lv.key())) {
          chainKey = (String) cache.get(lv.key());
          cache.put(lv.key(), chainKey);
        } else {
          chainKey = new GrouperUUID().toString();
        }
        Iterator chains = lv.chain().iterator();
        while (chains.hasNext()) {
          MemberVia mv = (MemberVia) chains.next();
          mv.key(chainKey);
          mv.idx(idx);
          dbSess.txStart();
          mv.save(dbSess);
          dbSess.txCommit();
          idx++;
        } 
      }
      lv.chainKey(chainKey);
      chainedVals.add( lv );
    }

    dbSess.stop();

    return chainedVals;
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Determine members of m that are affected by this list value change.
   */
  private List _addHasMembers(GrouperList gl, List isMem) {
    GrouperList.validate(gl);

    List vals = new ArrayList();

    GrouperGroup g = gl.member().toGroup();
    Iterator hasIter = g.listVals( gl.groupField() ).iterator();
    while (hasIter.hasNext()) {
      GrouperList glM = (GrouperList) hasIter.next();
      glM.load(this.s);
      List chain = new ArrayList();
      chain.addAll( glM.chain() );     // m's via chain...
      chain.add( new MemberVia(glM) ); // plus m
      // Add m's members to g
      vals.add(
        new GrouperList(
              this.s, gl.group(), glM.member(), gl.groupField(), chain
            )
        );
      // And now add to where g is a member
      vals.addAll( this._addWhereIsMem(glM, isMem) );
    }

    return vals;
  }

  /*
   * Add m to where g is a member
   */
  private List _addWhereIsMem(GrouperList gl, List isMem) {
    List vals = new ArrayList();

    Iterator iter = isMem.iterator();
    while (iter.hasNext()) {
      GrouperList glM = (GrouperList) iter.next();
      glM.load(this.s);
      List chain = new ArrayList();
      chain.add( new MemberVia(glM) );  // m's via chain...
      chain.addAll( glM.chain() );      // plus g's chain
        // Add m to where g is a member
      vals.add(
        new GrouperList(
              this.s, glM.group(), gl.member(), gl.groupField(), chain
            )
        );
    }

    return vals;
  }

}

