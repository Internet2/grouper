package edu.internet2.middleware.directory.grouper;

import java.util.List;

/** 
 * {@link Grouper} Naming Interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNaming.java,v 1.4 2004-05-02 04:45:16 blair Exp $
 */
public interface GrouperNaming {

  /**
   * Grant a naming privilege on a {@link Grouper} group.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Grant privileges on this {@link GrouperGroup}.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public void grant(GrouperGroup g, GrouperMember m, String priv);

  /**
   * Revoke a naming privilege on a {@link Grouper} group.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Revoke privilege on this {@link GrouperGroup}.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public void revoke(GrouperGroup g, GrouperMember m, String priv);

  /**
   * List naming privileges for current subject on the specified group.
   * <p>
   * See implementations for more information.
   *
   * @param   g   List privileges on this group.
   * @return  List of privileges.
   */
  public List has(GrouperGroup g);

  /**
   * List access privileges for specified member on the specified group.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Return privileges for this {@link GrouperGroup}.
   * @param   m     List privileges for this @link GrouperMember}.
   * @return  List of privileges.
   */
  public List has(GrouperGroup g, GrouperMember m);

  /**
   * Verify whether current subject has the specified privilege on the
   * specified group.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Verify privilege for this group.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public boolean has(GrouperGroup g, String priv);

  /**
   * Verify whether the specified member has the specified privilege
   * on the specified group.
   * <p>
   * See implementations for more information.
   *
   * @param   g     Verify privilege for this group.
   * @param   m     Verify privilege for this member.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the group.
   */
  public boolean has(GrouperGroup g, GrouperMember m, String priv);

}

