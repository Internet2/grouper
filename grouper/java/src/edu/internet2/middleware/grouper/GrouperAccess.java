package edu.internet2.middleware.directory.grouper;

/** 
 * {@link Grouper} Privilege Interface.
 * <p>
 * <ul>
 *  <li>XXX We may want/need a method that returns all of a user's
 *      {@link GrouperMember}'s privileges.</li>
 * </ul>
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccess.java,v 1.4 2004-04-29 17:11:22 blair Exp $
 */
public interface GrouperPrivilege {
  /**
   * Grant privilege on a {@link Grouper} group.
   * <p>
   * See implementations for more information.
   *
   * @param   g     {@link GrouperGroup} to grant privilege on.
   * @param   m     {@link GrouperMember} to grant privilege to.
   * @param   priv  Privilege type to grant.
   */
  public void grant(GrouperGroup g, GrouperMember m, String priv);

  /**
   * Revoke privilege on a {@link Grouper} group.
   * <p>
   * See implementations for more information.
   *
   * @param   g     {@link GrouperGroup} to revoke privilege on.
   * @param   m     {@link GrouperMember} to revoke privilege from.
   * @param   priv  Privilege type to revoke.
   */
  public void revoke(GrouperGroup g, GrouperMember m, String priv);

  /**
   * Verify whether a {@link GrouperMember} has a specified privilege
   * on a {@link GrouperGroup}.
   * <p>
   * See implementations for more information.
   *
   * @param   g     {@link GrouperGroup} to check privileges on.
   * @param   m     {@link GrouperMember} to check privileges for.
   * @param   priv  Privilege type to check for.
   * @return  Returns true if the {@link GrouperMember} has privilege
   *   <i>priv</i> on the {@link GrouperGroup}.
   */
  public boolean has(GrouperGroup g, GrouperMember m, String priv);
}

