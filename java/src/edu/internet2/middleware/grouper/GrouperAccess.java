package edu.internet2.middleware.directory.grouper;

import  java.util.List;

/** 
 * {@link Grouper} Access Interface.
 * <p>
 * <ul>
 *  <li>XXX We may want/need a method that returns all of a user's
 *      {@link GrouperMember}'s privileges.</li>
 * </ul>
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccess.java,v 1.6 2004-04-30 15:15:55 blair Exp $
 */
public interface GrouperAccess {
  /**
   * Grant privilege on a {@link Grouper} group.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Grant privileges on this {@link GrouperGroup}.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public void grant(GrouperGroup g, GrouperMember m, String priv);

  /**
   * Revoke privilege on a {@link Grouper} group.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Revoke privilege on this {@link GrouperGroup}.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public void revoke(GrouperGroup g, GrouperMember m, String priv);

  /**
   * Return all privileges that the current session's subject has on 
   * a {@link GrouperGroup}.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Return privileges for this {@link GrouperGroup}.
   * @return  List of privileges.
   */
  public List has(GrouperGroup g);

  /**
   * Return all privileges for a {@link GrouperMember} on a 
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
   * privilege on a {@link GrouperGroup}.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Check privilege on this {@link GrouperGroup}.
   * @param   priv  Check for this privilege.
   * @return  True if the {@link GrouperMember} has the privilege.
   */
  public boolean has(GrouperGroup g, String priv);

  /**
   * Verify whether a {@link GrouperMember} has a specified privilege
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

