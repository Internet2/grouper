/* 
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  java.util.*;

/** 
 * Default implementation of the {@link GrouperAccess} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccessImpl.java,v 1.32 2004-11-29 18:51:12 blair Exp $
 */
public class GrouperAccessImpl implements GrouperAccess {

  /*
   * PRIVATE CLASS VARIABLES
   */
  private static Map      privMap;
  private static boolean  initialized = false;
 

  /*
   * CONSTRUCTORS
   */
  public GrouperAccessImpl() {
    GrouperAccessImpl._init();
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Verify whether this implementation of the {@link GrouperAccess}
   * interface can handle this privilege.
   *
   * @param   priv  The privilege to verify.
   * @return  Boolean true if this implementation handles the specified
   * privilege, boolean false otherwise.
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
   * <p>
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
      Grouper.LOGGER.debug("Grant " + priv + " on " + g + " to " + m);
      if (GrouperBackend.listAddVal(s, g, m, (String) privMap.get(priv)) == true) {
        rv = true;
      }
    } 
    // TODO I should probably throw an exception if invalid priv
    return rv;
  }

  /**
   * List access privileges for current subject on the specified group.
   * <p>
   *
   * @param   s   Act within this {@link GrouperSession}.
   * @param   g   List privileges on this group.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, GrouperGroup g) {
    GrouperAccessImpl._init();
    List          privs = new ArrayList();
    GrouperMember m     = GrouperMember.lookup( s.subject() );
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
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperGroup} groups.
   */
  public List has(GrouperSession s, String priv) {
    GrouperAccessImpl._init();
    List          privs = new ArrayList();
    if (this.can(priv) == true) {
      GrouperMember m     = GrouperMember.lookup( s.subject() ); 
      privs = GrouperBackend.listVals(s, m, (String) privMap.get(priv));
    } 
    // TODO Throw exception if invalid priv?
    return privs;
  }

  /**
   * List access privileges for specified member on the specified group.
   * <p>
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
   * <p>
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
      GrouperMember m = GrouperMember.lookup( s.subject() );
      rv = GrouperBackend.listVal(s, g, m, (String) privMap.get(priv));
    } else {
      // TODO I should probably throw an exception
      rv = false;
    }
    return rv;
  }

  /**
   * List groups where the specified member has the specified
   * privilege.
   * <p>
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
   * <p>
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
   * Revoke an access privilege.
   * <p>
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
      if (GrouperBackend.listDelVal(s, g, m, (String) privMap.get(priv)) == true) {
        rv = true;
      }
    } 
    // TODO I should probably throw an exception if invalid priv
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
      Iterator iter = this.whoHas(s, g, priv).iterator();
      while (iter.hasNext()) {
        GrouperMember m = (GrouperMember) iter.next();
        this.revoke(s, g, m, priv);
      }
      rv = true; // FIXME
    }
    // TODO Should this return a list of deleted members?
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
   * PRIVATE STATIC METHODS
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

}

