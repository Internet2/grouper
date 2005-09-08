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
 * Default implementation of the {@link GrouperNaming} privilege interface.
 * <p />
 * <p>
 * This implementation uses the Groups Registry, groups and memberships
 * to record privilege information.
 * </p>
 *
 * @author  blair christensen.
 * @version $Id: GrouperNamingImpl.java,v 1.74 2005-09-08 16:18:22 blair Exp $
 */
public class GrouperNamingImpl implements GrouperNaming {

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
  public GrouperNamingImpl() {
    GrouperNamingImpl._init();
    this.root = Grouper.config("member.system");
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Verify whether this implementation of the {@link GrouperNaming}
   * interface can handle this privilege.
   * <p />
   *
   * @param   priv  The privilege to verify.
   * @return  True if this implemention handles the specified
   *   privilege.
   */
  public boolean can(String priv) {
    GrouperNamingImpl._init();
    if (privMap.containsKey(priv)) {
      return true;
    }
    return false;
  }

  /**
   * Grant an naming privilege on a naming {@link GrouperStem}.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Grant privileges on this {@link GrouperStem}.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public boolean grant(
    GrouperSession s, GrouperStem ns, GrouperMember m, String priv
  ) 
  {
    GrouperSession.validate(s);
    GrouperNamingImpl._init();
    boolean rv = false;
    if (this.can(priv) == true) {
      try {
        s.canWriteField(ns, (String) privMap.get(priv));
        try {
          s.dbSess().txStart();
          // Go straight to the source in order to use the passed in
          // session
          ns.listAddVal(s, ns, m, (String) privMap.get(priv));
          s.dbSess().txCommit();
          rv = true;
        } catch (RuntimeException e) {
          s.dbSess().txRollback();
          throw new RuntimeException(
            "Error granting privilege: " + e.getMessage()
          );
        }
      } catch (InsufficientPrivilegeException e) {
        // Ignore
      }
    } 
    Grouper.log().grant(rv, s, ns, m, priv);
    // TODO I should probably throw an exception if invalid priv
    return rv;
  }

  /**
   * List naming privileges for current subject on the specified naming group.
   * <p />
   *
   * @param   s   Act within this {@link GrouperSession}.
   * @param   ns  List privileges on this group.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, GrouperStem ns) {
    // TODO Should this run as root so that the user isn't restricted?
    GrouperNamingImpl._init();
    List          privs = new ArrayList();
    GrouperMember m     = s.getMember();
    Iterator      iter  = privMap.keySet().iterator();
    while (iter.hasNext()) {
      String  priv  = (String) iter.next();
      if (this.has(s, ns, m, priv) == true) {
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
   * @return  List of {@link GrouperStem} groups.
   */
  public List has(GrouperSession s, String priv) {
    // TODO Should this run as root so that the user isn't restricted?
    GrouperNamingImpl._init();
    List          privs = new ArrayList();
    if (this.can(priv) == true) {
      GrouperMember m = s.getMember();
      privs = m.listVals( (String) privMap.get(priv) );
    } 
    // TODO Throw exception if invalid priv?
    return privs;
  }

  /**
   * List naming privileges for specified member on the specified naming group.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Return privileges for this {@link GrouperStem}.
   * @param   m     List privileges for this {@link GrouperMember}.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, GrouperStem ns, GrouperMember m) {
    GrouperNamingImpl._init();
    List      privs = new ArrayList();
    Iterator  iter  = privMap.keySet().iterator();
    while (iter.hasNext()) {
      String  priv  = (String) iter.next();
      if (this.has(s, ns, m, priv) == true) {
        privs.add(priv);
      }
    }
    return privs;
  }

  /**
   * Verify whether current subject has the specified privilege on the
   * specified group.
   * <p />
   * <pre class="eg">
   * GrouperSession s = GrouperSession.start(subject);
   * if (s.naming().has(s, ns, Grouper.PRIV_CREATE)) {
   *   // the current subject can create groups within this namespace
   * }
   * s.stop();
   * </pre>
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Verify privilege for this group.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public boolean has(GrouperSession s, GrouperStem ns, String priv) {
    // Check privilege as root to ensure subjects own privs don't get
    // in the way
    return this.has(
      GrouperSession.getRootSession(), ns, s.getMember(), priv
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
   * @return  List of {@link GrouperStem} groups.
   */
  public List has(GrouperSession s, GrouperMember m, String priv) {
    GrouperNamingImpl._init();
    List          privs = new ArrayList();
    if (this.can(priv) == true) {
      privs = m.listVals( (String) privMap.get(priv) );
    } // TODO Exception if invalid priv?
    return privs;
  }

  /**
   * Verify whether the specified member has the specified privilege
   * on the specified naming group.
   * <p />
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Verify privilege for this group.
   * @param   m     Verify privilege for this member.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public boolean has(
                     GrouperSession s, GrouperStem ns, 
                     GrouperMember m, String priv
                    )
  {
    GrouperNamingImpl._init();
    boolean rv = false;
    if (this.can(priv) == true) {
      if (this._isRoot(m)) {
        rv = true;
      } else {
        rv = ns.hasMember(m, (String) privMap.get(priv));
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
   * @param   ns    Revoke privilege on this {@link GrouperStem}.
   * @param   priv  Privilege to revoke.
   */
  public boolean revoke(GrouperSession s, GrouperStem ns, String priv) {
    GrouperNamingImpl._init();
    boolean rv = false;
    Iterator iter = this.whoImmHas(s, ns, priv).iterator();
    while (iter.hasNext()) {
      GrouperMember m = (GrouperMember) iter.next();
      // TODO What if this fails for one or more members?
      this.revoke(s, ns, m, priv);
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
   * @param   ns    Revoke privilege on this {@link GrouperStem}.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public boolean revoke(
    GrouperSession s, GrouperStem ns, GrouperMember m, String priv
  ) 
  {
    GrouperNamingImpl._init();
    boolean rv = false;
    if (this.can(priv) == true) {
      try {
        s.canWriteField(ns, (String) privMap.get(priv));
        try {
          s.dbSess().txStart();
          // Go straight to the source in order to use the passed in
          // session
          ns.listDelVal(s, ns, m, (String) privMap.get(priv));
          s.dbSess().txCommit();
          rv = true;
        } catch (RuntimeException e) {
          s.dbSess().txRollback();
          throw new RuntimeException(
            "Error revoking privilege: " + e.getMessage()
          );
        }
      } catch (InsufficientPrivilegeException e) {
        // Ignore
      }
    } 
    Grouper.log().revoke(rv, s, ns, m, priv);
    // TODO I should probably throw an exception if invalid priv
    return rv;
  }

  /**
   * List members who have the specified privilege on the 
   * specified group.
   * <p />
   * <pre class="eg">
   * GrouperSession s = GrouperSession.start(subject);
   * List stemmers  = s.access().whoHas(s, group, Grouper.PRIV_STEM);  
   * </pre>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   ns    Query for this {@link GrouperStem}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperMember} members.
   */
  public List whoHas(GrouperSession s, GrouperStem ns, String priv) {
    GrouperNamingImpl._init();
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
      try {
        s.canReadField( (Group) ns, (String) privMap.get(priv));
        // And now retrieve the appropriate list keys, instantiate
        // them, and convert into their member objects.
        Iterator iter = Group.listValsKeys(
          s, ns.key(), (String) privMap.get(priv)
        ).iterator();
        while (iter.hasNext()) {
          String        key = (String) iter.next();
          GrouperList   lv  = GrouperList.loadByKey(s, key);
          GrouperMember m   = lv.member();
          if (m != null) {
            members.add(m);
          }
        }
      } catch (InsufficientPrivilegeException e) {
        // Ignore
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
      privMap.put(Grouper.PRIV_CREATE, "creators");
      privMap.put(Grouper.PRIV_STEM,   "stemmers");
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
    GrouperNamingImpl._init();
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

