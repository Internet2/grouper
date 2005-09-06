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


import  java.util.*;
import  org.apache.commons.logging.Log;
import  org.apache.commons.logging.LogFactory;


/** 
 * Internal class implementing the memberOf algorithm.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: MemberOf.java,v 1.27 2005-09-06 18:44:53 blair Exp $
 */
public class MemberOf {

  /*
   * PRIVATE CLASS VARIABLES
   */
  private static Log log = LogFactory.getLog(MemberVia.class);


  /*
   * TODO This is all un(der)documented magic and madness.
   */

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private GrouperSession s;


  /*
   * CONSTRUCTORS
   */

  /**
   * Create a new {@link MemberOf} object.
   * <p />
   * @param   s Operate within this {@link GrouperSession}.
   */
  public MemberOf(GrouperSession s) {
    GrouperSession.validate(s);
    this.s = s;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Perform a member of calculation.
   * <p />
   * @param   gl  The list value that is being acted upon.
   * @return  A list of {@link GrouperList} objects.
   */
  public List memberOf(GrouperList gl) {
    List vals = new ArrayList();
    List effs = new ArrayList(); // TODO What am I trying to accomplish?

    // Ensure that the grouper list is properly loaded
    gl.setSession(this.s);  // TODO Is this needed here?
    log.debug("memberOf calculation for " + gl);

    // Add m to g's gl
    vals.add(gl);
    log.debug("mship: " + gl);

    List isMem = new ArrayList();

    /*
     * We only need to propagate this new membership out to
     * everywhere _g_ is a member if we are adding to _g_'s "members"
     * list.  Thus, if we are adding _m_ to _g_'s "members" list, _m_
     * should be added *everywhere* _g_ is a member, across all list
     * types.  However, if we are adding _m_ to _g_'s "admins" list,
     * we do not need to add _m_ to any of the other lists that _g_
     * belongs to within the registry.
     */
    if (gl.groupField().equals(Grouper.DEF_LIST_TYPE)) {
      // TODO This seems fragile.
      if (!gl.group().type().equals("naming")) {
        // Add _m_ to all lists where _g_ is a member
        GrouperMember m = ( (GrouperGroup) gl.group() ).toMember();
        isMem = m.listValsAll();
        effs.addAll( this._addWhereIsMem(gl, gl, isMem) );
      }
    }

    // If m is a group...
    if (gl.member().typeID().equals("group")) {
      // ...add additional list values
      effs.addAll( this._addHasMembers(gl, isMem) );
    }

    // Save the chains
    vals.addAll( this._saveChains(effs) );

    log.debug("vals: " + vals.size());
    return vals;
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Determine members of m that are affected by this list value change.
   * @return List of {@link GrouperList} objects.
   */
  private List _addHasMembers(GrouperList gl, List isMem) {
    GrouperList.validate(gl);

    List vals = new ArrayList();

    Group g = gl.member().toGroup();
    log.debug("looking for members of " + g);
    // We only want "members", not other list types
    Iterator hasIter = g.listVals().iterator();
    while (hasIter.hasNext()) {
      GrouperList glM = (GrouperList) hasIter.next();
      glM.setSession(this.s);  // TODO Is this needed here?
      log.debug("hasMember: " + glM);
      List chain = new ArrayList();

      // TODO Is this correct?  More tests needed.
      chain.addAll( glM.chain() );        // Add the chain leading to m
      chain.add( MemberVia.create(gl) );  // Add gl

      // Add m's members to g
      GrouperList lv = new GrouperList(
        this.s, gl.group(), glM.member(), gl.groupField(), chain
      );
      vals.add(lv);
      log.debug("mship/hasMember: " + lv);

      // And now add to where g is a member
      vals.addAll( this._addWhereIsMem(glM, gl, isMem) );
    }

    return vals;
  }

  /*
   * Add m to where g is a member.
   * @return List of {@link GrouperList} objects.
   */
  private List _addWhereIsMem(GrouperList gl, GrouperList orig, List isMem) {
    List vals = new ArrayList();

    Iterator iter = isMem.iterator();
    while (iter.hasNext()) {
      GrouperList glM = (GrouperList) iter.next();
      glM.setSession(this.s);  // TODO Is this needed here?
      log.debug("isMember: " + glM);
      List chain = new ArrayList();

      // TODO Is this correct?  More tests needed.
      chain.addAll( gl.chain() );   // Add the chain leading to gl
      if (!gl.equals(orig)) {
        chain.add( MemberVia.create(orig) );
      }
      // Add g's mship.  If immediate, the values in glM are fine.  If
      // eff, we want the immediate mship that causes the eff mship.
      Group group = glM.group();
      if (glM.via() != null) {
        group = glM.via();
      }
      chain.add( 
        MemberVia.create(
          new GrouperList(
            this.s, group, glM.member(), gl.groupField()
          )
        )
      );
      chain.addAll( glM.chain() );  // Add the chain leading to g's mship
        
      // Add m to where g is a member
      // Be sure to use the list type from g's membership (glM)
      GrouperList lv = new GrouperList(
        this.s, glM.group(), gl.member(), glM.groupField(), chain
      );
      vals.add(lv);
      log.debug("mship/whereIsMem: " + lv);    
    }

    return vals;
  }

  /*
   * TODO This should probably be broken down into multiple methods.
   * Assign proper chainKeys and listKeys and save new chains.
   * @return List of updated {@link GrouperList} objects.
   */
  private List _saveChains(List vals) {
    Map   cache       = new HashMap();
    List  chainedVals = new ArrayList();

    Grouper.dbSess().txStart();

    /*
     * We need to step through the list values generated by memberOf
     * and:
     *  - Identify if the chain already exists
     *  - Flesh out and save any newly discovered chains
     *  - Attach the appropriate chainKey to each list value
     *
     * And, no, I'm not entirely sure if this is completely accurate.
     */
    Iterator valIter = vals.iterator();
    while (valIter.hasNext()) {
      GrouperList lv = (GrouperList) valIter.next();
      String  chainKey  = null;
      int     idx       = 0;
      /*
       * FIXME Chain reuse is *not* working so why even pretend at this
       *       point.  This really needs to be fixed.  Whether I can
       *       get it fixed before 0.5.5 is another matter, however.
       *       [grouperzilla#329]
       */
      chainKey = new GrouperUUID().toString();
      Iterator chains = lv.chain().iterator();
      while (chains.hasNext()) {
        MemberVia mv = (MemberVia) chains.next();
        mv.key(chainKey);
        mv.idx(idx);
        mv.save(Grouper.dbSess());
        idx++;
      } 
      lv.chainKey(chainKey);
      chainedVals.add( lv );
    }

    Grouper.dbSess().txCommit();

    return chainedVals;
  }

}

