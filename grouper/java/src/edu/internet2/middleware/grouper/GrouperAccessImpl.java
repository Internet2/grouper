/* 
 * Copyright (C) 2004 Internet2
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
 * @version $Id: GrouperAccessImpl.java,v 1.19 2004-11-22 01:20:23 blair Exp $
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
   * Grant an access privilege on a {@link GrouperGroup}.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Grant privileges on this {@link GrouperGroup}.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public void grant(
                    GrouperSession s, GrouperGroup g, 
                    GrouperMember m, String priv
                   ) 
  {
    GrouperAccessImpl._init();
    if (privMap.containsKey(priv)) {
      GrouperBackend.listAddVal(g, s, m, (String) privMap.get(priv));
    } 
    // TODO I should probably throw an exception if invalid priv
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
    List privs = new ArrayList();
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
    List privs = new ArrayList();
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
    List privs = new ArrayList();
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
    return false;
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
    List privs = new ArrayList();
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
    if (privMap.containsKey(priv)) {
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
  public void revoke(
                     GrouperSession s, GrouperGroup g, 
                     GrouperMember m, String priv
                    ) 
  {
    GrouperAccessImpl._init();
    if (privMap.containsKey(priv)) {
      GrouperBackend.listDelVal(g, s, m, (String) privMap.get(priv));
    } 
    // TODO I should probably throw an exception if invalid priv
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

