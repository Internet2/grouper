/* 
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  java.util.*;


/** 
 * {@link Grouper} Access Interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNaming.java,v 1.21 2004-11-23 22:16:43 blair Exp $
 */
public interface GrouperNaming {

  /**
   * Verify whether this implementation of the {@link GrouperNaming}
   * interface can handle this privilege.
   *
   * @param   priv  The privilege to verify.
   * @return  Boolean true if this implementation handles the specified
   * privilege, boolean false otherwise.
   */
  public boolean can(String priv);

  /**
   * Grant an naming privilege on a <i>naming</i> {@link GrouperGroup}.
   * <p>
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Grant privileges on this {@link GrouperGroup}.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public boolean grant(GrouperSession s, GrouperGroup g, GrouperMember m, String priv);

  /**
   * List naming privileges for current subject on the specified naming group.
   * <p>
   * See implementations for more information.
   *
   * @param   s   Act within this {@link GrouperSession}.
   * @param   g   List privileges on this group.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, GrouperGroup g);

  /**
   * List groups where the current subject has the specified privilege.
   * <p>
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperGroup} groups.
   */
  public List has(GrouperSession s, String priv);

  /**
   * List naming privileges for specified member on the specified naming group.
   * <p>
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Return privileges for this {@link GrouperGroup}.
   * @param   m     List privileges for this {@link GrouperMember}.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, GrouperGroup g, GrouperMember m);

  /**
   * Verify whether current subject has the specified privilege on the
   * specified group.
   * <p>
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Verify privilege for this group.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public boolean has(GrouperSession s, GrouperGroup g, String priv);

  /**
   * List groups where the specified member has the specified
   * privilege.
   * <p>
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperGroup} groups.
   */
  public List has(GrouperSession s, GrouperMember m, String priv);

  /**
   * Verify whether the specified member has the specified privilege
   * on the specified naming group.
   * <p>
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Verify privilege for this group.
   * @param   m     Verify privilege for this member.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public boolean has(GrouperSession s, GrouperGroup g, GrouperMember m, String priv);

  /**
   * Revoke an naming privilege.
   * <p>
   * See implementations for more information.
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   g     Revoke privilege on this {@link GrouperGroup}.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public boolean revoke(GrouperSession s, GrouperGroup g, GrouperMember m, String priv);

}

