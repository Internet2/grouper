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
 * @version $Id: GrouperAccessImpl.java,v 1.17 2004-11-20 18:08:55 blair Exp $
 */
public class GrouperAccessImpl implements GrouperAccess {

  /*
   * CONSTRUCTORS
   */
  public GrouperAccessImpl() {
    // Nothing -- Yet
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Grant an access privilege.
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
    // Nothing -- Yet
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
    return false;
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
    // Nothing -- Yet
  }

}

