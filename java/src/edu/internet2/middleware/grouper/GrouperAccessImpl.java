/*
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
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
 * Default implementation of the {@link GrouperAccess} privilege interface.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccessImpl.java,v 1.43 2004-12-09 01:43:16 blair Exp $
 */
public class GrouperAccessImpl implements GrouperAccess {

  /*
   * PRIVATE CLASS VARIABLES
   */
  private static Map      privMap;
  private static boolean  initialized = false;
 

  /*
   * PRIVATE INSTANCE VARIABLES
   */
  private String root;


  /*
   * CONSTRUCTORS
   */
  public GrouperAccessImpl() {
    GrouperAccessImpl._init();
    this.root = Grouper.config("member.system");
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Verify whether this implementation of the {@link GrouperAccess}
   * interface can handle this privilege.
   * <p />
   *
   * @param   priv  The privilege to verify.
   * @return  True if this implementation handles the specified
   *   privilege.
   */
  public boolean can(String priv) {
    GrouperAccessImpl._init();
    if (privMap.containsKey(priv)) {
      return true;
    }
    return false;
  }

  /**
   * Grant an access privilege on a {@link GrouperGroup}.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Grant privileges on this {@link GrouperGroup}.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public boolean grant(
                       GrouperSession s, GrouperGroup g, 
                       GrouperMember m, String priv
                      ) 
  {
    GrouperAccessImpl._init();
    boolean rv = false;
    if (this.can(priv) == true) {
      if (GrouperBackend.sessionValid(s)) {
        /*
         * FIXME I should be doing a GroupField lookup on `priv'
         */
        if (this.has(s, g, Grouper.PRIV_ADMIN)) {
          if (GrouperBackend.listAddVal(s, g, m, (String) privMap.get(priv)) == true) {
            rv = true;
          }
        }
      }
    } 
    Grouper.log().grant(rv, s, g, m, priv);
    // TODO I should probably throw an exception if invalid priv
    return rv;
  }

  /**
   * List access privileges for current subject on the specified group.
   * <p />
   *
   * @param   s   Act within this {@link GrouperSession}.
   * @param   g   List privileges on this group.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, GrouperGroup g) {
    GrouperAccessImpl._init();
    List          privs = new ArrayList();
    GrouperMember m     = GrouperMember.load( s.subject() );
    Iterator      iter  = privMap.keySet().iterator();
    while (iter.hasNext()) {
      String  priv  = (String) iter.next();
      if (this.has(s, g, m, priv) == true) {
        privs.add(priv);
      }
    }
    return privs;
  }

  /**
   * List groups where the current subject has the specified privilege.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperGroup} groups.
   */
  public List has(GrouperSession s, String priv) {
    GrouperAccessImpl._init();
    List          privs = new ArrayList();
    if (this.can(priv) == true) {
      GrouperMember m     = GrouperMember.load( s.subject() ); 
      privs = GrouperBackend.listVals(s, m, (String) privMap.get(priv));
    } 
    // TODO Throw exception if invalid priv?
    return privs;
  }

  /**
   * List access privileges for specified member on the specified group.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Return privileges for this {@link GrouperGroup}.
   * @param   m     List privileges for this {@link GrouperMember}.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, GrouperGroup g, GrouperMember m) {
    GrouperAccessImpl._init();
    List      privs = new ArrayList();
    Iterator  iter  = privMap.keySet().iterator();
    while (iter.hasNext()) {
      String  priv  = (String) iter.next();
      if (this.has(s, g, m, priv) == true) {
        privs.add(priv);
      }
    }
    return privs;
  }

  /**
   * Verify whether current subject has the specified privilege on the
   * specified group.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Verify privilege for this group.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public boolean has(GrouperSession s, GrouperGroup g, String priv) {
    GrouperAccessImpl._init();
    boolean rv = false;
    if (this.can(priv) == true) {
      if (this._isRoot(s)) {
        rv = true;
      } else {
        GrouperMember m = GrouperMember.load( s.subject() );
        rv = GrouperBackend.listVal(s, g, m, (String) privMap.get(priv));
      }
    } else {
      // TODO I should probably throw an exception
      rv = false;
    }
    return rv;
  }

  /**
   * List groups where the specified member has the specified
   * privilege.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperGroup} groups.
   */
  public List has(GrouperSession s, GrouperMember m, String priv) {
    GrouperAccessImpl._init();
    List          privs = new ArrayList();
    if (this.can(priv) == true) {
      privs = GrouperBackend.listVals(s, m, (String) privMap.get(priv));
    } // TODO Exception if invalid priv?
    return privs;
  }

  /**
   * Verify whether the specified member has the specified privilege
   * on the specified group.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Verify privilege for this group.
   * @param   m     Verify privilege for this member.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public boolean has(
                     GrouperSession s, GrouperGroup g, 
                     GrouperMember m, String priv
                    )
  {
    GrouperAccessImpl._init();
    boolean rv = false;
    if (this.can(priv) == true) {
      rv = GrouperBackend.listVal(s, g, m, (String) privMap.get(priv));
    } else {
      // TODO I should probably throw an exception
      rv = false;
    }
    return rv;
  }

  /**
   * Revoke all privileges of the specified type on the specified
   * group.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Revoke privilege on this {@link GrouperGroup}.
   * @param   priv  Privilege to revoke.
   */
  public boolean revoke(GrouperSession s, GrouperGroup g, String priv) {
    GrouperAccessImpl._init();
    boolean rv = false;
    if (this.can(priv) == true) {
      if (GrouperBackend.sessionValid(s)) {
        /*
         * FIXME I should be doing a GroupField lookup on `priv'
         */
        if (this.has(s, g, Grouper.PRIV_ADMIN)) {
          Iterator iter = this.whoHas(s, g, priv).iterator();
          while (iter.hasNext()) {
            GrouperMember m = (GrouperMember) iter.next();
            this.revoke(s, g, m, priv);
          }
          rv = true; // FIXME
        }
      }
    }
    // TODO Should this return a list of deleted members?
    // TODO I should probably throw an exception if invalid priv
    return rv;
  }

  /**
   * Revoke an access privilege.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Revoke privilege on this {@link GrouperGroup}.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public boolean revoke(
                        GrouperSession s, GrouperGroup g, 
                        GrouperMember m, String priv
                       ) 
  {
    GrouperAccessImpl._init();
    boolean rv = false;
    if (this.can(priv) == true) {
      if (GrouperBackend.sessionValid(s)) {
        /*
         * FIXME I should be doing a GroupField lookup on `priv'
         */
        if (this.has(s, g, Grouper.PRIV_ADMIN)) {
          if (GrouperBackend.listDelVal(s, g, m, (String) privMap.get(priv)) == true) {
            rv = true;
          }
        }
      }
    } 
    Grouper.log().revoke(rv, s, g, m, priv);
    // TODO I should probably throw an exception if invalid priv
    return rv;
  }

  /**
   * List members who have the specified privilege on the 
   * specified group.
   * <p />
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Query for this {@link GrouperGroup}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperMember} members.
   */
  public List whoHas(GrouperSession s, GrouperGroup g, String priv) {
    GrouperAccessImpl._init();
    List members = new ArrayList();
    if (this.can(priv) == true) {
      Iterator iter = GrouperBackend.listVals(
                                              s, g, (String) privMap.get(priv)
                                             ).iterator();
      while (iter.hasNext()) {
        GrouperList   gl  = (GrouperList) iter.next();
        GrouperMember m   = gl.member();
        if (m != null) {
          members.add(m);
        }
      } 
    } // TODO Exception if invalid priv?
    return members;
  }


  /*
   * PRIVATE CLASS METHODS
   */

  /*
   * Initialize static variables
   */
  private static void _init() {
    if (initialized == false) {
      /*
       * TODO I can do better this.  Can't I just leverage the cached
       *      group fields information?
       */
      privMap = new HashMap();
      privMap.put("ADMIN", "admins");
      privMap.put("OPTIN", "optins");
      privMap.put("OPTOUT", "optouts");
      privMap.put("READ", "readers");
      privMap.put("UPDATE", "updaters");
      privMap.put("VIEW", "viewers");
      initialized = true;
    }
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /* (!javadoc)
   * Grouper's root-like account effectively has all privs
   */
  private boolean _isRoot(GrouperSession s) {
    boolean rv = false;
    if (s.subject().getId().equals(this.root)) {
      // This subject can do *everything*
      rv = true;
    }
    return rv;
  }

}

