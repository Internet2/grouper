package edu.internet2.middleware.directory.grouper;

import  java.util.List;

/** 
 * {@link Grouper} Access Interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccess.java,v 1.8 2004-04-30 15:22:20 blair Exp $
 */
public interface GrouperAccess {
  /**
   * Grant an access privilege on a {@link Grouper} group.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Grant privileges on this {@link GrouperGroup}.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public void grant(GrouperGroup g, GrouperMember m, String priv);

  /**
   * Revoke an access privilege on a {@link Grouper} group.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Revoke privilege on this {@link GrouperGroup}.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public void revoke(GrouperGroup g, GrouperMember m, String priv);

  /**
   * Return all access privileges that the current session's subject has on 
   * a {@link GrouperGroup}.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Return privileges for this {@link GrouperGroup}.
   * @return  List of privileges.
   */
  public List has(GrouperGroup g);

  /**
   * Return all access privileges for a {@link GrouperMember} on a 
   * {@link GrouperGroup}.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Return privileges for this {@link GrouperGroup}.
   * @param   m     Return privileges for this {@link GrouperMember}.
   * @return  List of privileges.
   */
  public List has(GrouperGroup g, GrouperMember m);

  /**
   * Verify whether the current session's subject has a specified 
   * access privilege on a {@link GrouperGroup}.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Check privilege on this {@link GrouperGroup}.
   * @param   priv  Check for this privilege.
   * @return  True if the {@link GrouperMember} has the privilege.
   */
  public boolean has(GrouperGroup g, String priv);

  /**
   * Verify whether a {@link GrouperMember} has a specified access privilege
   * on a {@link GrouperGroup}.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Check privilege on this {@link GrouperGroup}.
   * @param   m     Check privilege for this {@link GrouperMember}.
   * @param   priv  Check for this privilege.
   * @return  True if the {@link GrouperMember} has the privilege.
   */
  public boolean has(GrouperGroup g, GrouperMember m, String priv);
}

