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


/** 
 * Default implementation of the {@link GrouperAccess} privilege interface.
 * <p />
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccessImpl.java,v 1.73 2005-07-17 14:45:31 blair Exp $
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
   * Grant an access privilege on a {@link Group}.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Grant privileges on this {@link Group}.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public boolean grant(
                       GrouperSession s, Group g, 
                       GrouperMember m, String priv
                      ) 
  {
    // XXX boolean rv = rs.access().grant(rs, this, m, Grouper.PRIV_ADMIN);
    GrouperAccessImpl._init();
    boolean rv = false;
    if (this.can(priv) == true) {
      /*
       * FIXME I should be doing a GroupField lookup on `priv'
       */
      if (this.has(s, g, Grouper.PRIV_ADMIN)) {
        s.dbSess().txStart();
        try {
          // We need to use the internal method in Group, not the
          // public method in GrouperGroup, to ensure that we have
          // sufficient privs to grant the privilege.
          g.listAddVal(s, g, m, (String) privMap.get(priv));
          s.dbSess().txCommit();
          rv = true;
        } catch (RuntimeException e) {
          s.dbSess().txRollback();
          throw new RuntimeException(
                      "Error granting privilege: " + e
                    );
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
  public List has(GrouperSession s, Group g) {
    // TODO Should this run as root so that the user isn't restricted?
    GrouperAccessImpl._init();
    List          privs = new ArrayList();
    GrouperMember m     = GrouperMember.load(s, s.subject());
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
   * @return  List of {@link Group} groups.
   */
  public List has(GrouperSession s, String priv) {
    // TODO Should this run as root so that the user isn't restricted?
    GrouperAccessImpl._init();
    List          privs = new ArrayList();
    if (this.can(priv) == true) {
      GrouperMember m = GrouperMember.load(s, s.subject()); 
      privs = m.listVals( (String) privMap.get(priv) );
    } 
    // TODO Throw exception if invalid priv?
    return privs;
  }

  /**
   * List access privileges for specified member on the specified group.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Return privileges for this {@link Group}.
   * @param   m     List privileges for this {@link GrouperMember}.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, Group g, GrouperMember m) {
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
   * <pre>
   * GrouperSession s = GrouperSession.start(subject);
   * if (s.access().has(s, group, Grouper.PRIV_OPTIN)) {
   *   // the current subject can optin to this group
   * }
   * s.stop();
   * </pre>
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Verify privilege for this group.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public boolean has(GrouperSession s, Group g, String priv) {
    // Check privilege as root to ensure subjects own privs don't get
    // in the way
    return this.has(
      GrouperSession.getRootSession(), g, s.getMember(), priv
    );
  }

  /**
   * List groups where the specified member has the specified
   * privilege.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link Group} groups.
   */
  public List has(GrouperSession s, GrouperMember m, String priv) {
    GrouperAccessImpl._init();
    List          privs = new ArrayList();
    if (this.can(priv) == true) {
      privs = m.listVals( (String) privMap.get(priv) );
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
    GrouperSession s, Group g, GrouperMember m, String priv
  )
  {
    GrouperAccessImpl._init();
    boolean rv = false;
    if (this.can(priv) == true) {
      if (this._isRoot(m)) {
        rv = true;
      } else {
        // FIXME Not sure...
        if        (g instanceof GrouperGroup) {
          rv = ( (GrouperGroup) g).hasMember(m, (String) privMap.get(priv));
        } else if (g instanceof GrouperStem) {
          rv = ( (GrouperStem) g).hasMember(m, (String) privMap.get(priv));
        } else {
          throw new RuntimeException("Unknown group class: " + g);
        }
      }
    }
    return rv;
  }

  /**
   * Revoke all privileges of the specified type on the specified
   * group.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Revoke privilege on this {@link Group}.
   * @param   priv  Privilege to revoke.
   */
  public boolean revoke(GrouperSession s, Group g, String priv) {
    GrouperAccessImpl._init();
    boolean rv = false;
    Iterator iter = this.whoImmHas(s, g, priv).iterator();
    while (iter.hasNext()) {
      GrouperMember m = (GrouperMember) iter.next();
      this.revoke(s, g, m, priv);
    }
    rv = true; // FIXME
    // TODO Should this return a list of deleted members?
    // TODO I should probably throw an exception if invalid priv
    return rv;
  }

  /**
   * Revoke an access privilege.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Revoke privilege on this {@link Group}.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public boolean revoke(
                        GrouperSession s, Group g, 
                        GrouperMember m, String priv
                       ) 
  {
    GrouperAccessImpl._init();
    boolean rv = false;
    if (this.can(priv) == true) {
      /*
       * FIXME I should be doing a GroupField lookup on `priv'
       */
      if (this.has(s, g, Grouper.PRIV_ADMIN)) {
        s.dbSess().txStart();
        try {
          g.listDelVal(m, (String) privMap.get(priv));
          s.dbSess().txCommit();
          rv = true;
        } catch (RuntimeException e) {
          s.dbSess().txRollback();
          throw new RuntimeException(
                      "Error revoking privilege: " + e
                    );
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
   * <pre>
   * GrouperSession s = GrouperSession.start(subject);
   * List admins  = s.access().whoHas(s, group, Grouper.PRIV_ADMIN);  
   * </pre>
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Query for this {@link Group}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperMember} members.
   */
  public List whoHas(GrouperSession s, Group g, String priv) {
    GrouperAccessImpl._init();
    List members = new ArrayList();

    if (this.can(priv) == true) {
      /*
       * Verify that this subject has privileges to see this ist.
       * One of the reasons why we need to do this is that an empty
       * list could possibly be interpreted in two very different
       * ways (no members | no privilege to see the members) until
       * we add in the special _ALL_ subject and dispense with the
       * fuzzy magic of empty lists for _VIEW_ and _READ_ privileges.
       */
      if (this.has(s, g, priv)) {
        // And now retrieve the appropriate list keys, instantiate
        // them, and convert into their member objects.
        Iterator iter = Group.listValsKeys(
          s, g.key(), (String) privMap.get(priv)
        ).iterator();
        while (iter.hasNext()) {
          String        key = (String) iter.next();
          GrouperList   lv  = GrouperList.loadByKey(s, key);
          GrouperMember m   = lv.member();
          if (m != null) {
            members.add(m);
          }
        }
      }
    }
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
      privMap.put(Grouper.PRIV_ADMIN,   "admins");
      privMap.put(Grouper.PRIV_OPTIN,   "optins");
      privMap.put(Grouper.PRIV_OPTOUT,  "optouts");
      privMap.put(Grouper.PRIV_READ,    "readers");
      privMap.put(Grouper.PRIV_UPDATE,  "updaters");
      privMap.put(Grouper.PRIV_VIEW,    "viewers");
      initialized = true;
    }
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Is this the root account?
   */
  private boolean _isRoot(GrouperMember m) {
    boolean rv = false;
    if (m.subjectID().equals(this.root)) {
      rv = true; 
    }
    return rv;
  }

  /* 
   * Is this the root account?
   */
  private boolean _isRoot(GrouperSession s) {
    boolean rv = false;
    if (s.subject().getId().equals(this.root)) {
      rv = true;
    }
    return rv;
  }

  /*
   * Immediate list members who have the specified privilege on the
   * specified group.
   */
  private List whoImmHas(GrouperSession s, Group g, String priv) {
    GrouperAccessImpl._init();
    List members = new ArrayList();
    
    if (this.can(priv) == true) {
      Iterator iter = g.listImmVals( (String) privMap.get(priv)).iterator();
      while (iter.hasNext()) {
        GrouperList   gl  = (GrouperList) iter.next();
        gl.setSession(s);
        GrouperMember m   = gl.member();
        if (m != null) {
          members.add(m);
        }
      } 
    } // TODO Exception if invalid priv?

    return members;
  }

}

