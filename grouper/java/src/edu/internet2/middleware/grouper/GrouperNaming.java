package edu.internet2.middleware.directory.grouper;

import java.util.*;

/** 
 * {@link Grouper} Naming Interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNaming.java,v 1.7 2004-08-03 01:31:21 blair Exp $
 */
public interface GrouperNaming {

  /**
   * Grant a naming privilege on a {@link Grouper} stem.
   * <p>
   * See implementations for more information.
   *
   * @param   stem  Grant privileges on this {@link Grouper} stem.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public void grant(GrouperStem stem, GrouperMember m, String priv);

  /**
   * Revoke a naming privilege on a {@link Grouper} stem.
   * <p>
   * See implementations for more information.
   *
   * @param   stem  Revoke privilege on this {@link Grouper} stem.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public void revoke(GrouperStem stem, GrouperMember m, String priv);

  /**
   * List naming privileges for current subject on the specified stem.
   * <p>
   * See implementations for more information.
   *
   * @param   stem  List privileges on this stem.
   * @return  List of privileges.
   */
  public List has(GrouperStem stem);

  /**
   * List access privileges for specified member on the specified stem.
   * <p>
   * See implementations for more information.
   *
   * @param   stem  Return privileges for this {@link Grouper} stem.
   * @param   m     List privileges for this @link GrouperMember}.
   * @return  List of privileges.
   */
  public List has(GrouperStem stem, GrouperMember m);

  /**
   * Verify whether current subject has the specified privilege on the
   * specified stem.
   * <p>
   * See implementations for more information.
   *
   * @param   stem  Verify privilege for this stem.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the stem.
   */
  public boolean has(GrouperStem stem, String priv);

  /**
   * Verify whether the specified member has the specified privilege
   * on the specified stem.
   * <p>
   * See implementations for more information.
   *
   * @param   stem  Verify privilege for this stem.
   * @param   m     Verify privilege for this member.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the stem.
   */
  public boolean has(GrouperStem stem, GrouperMember m, String priv);

  /**
   * List stems where the current subject has the specified privilege.
   * <p>
   * See implementations for more information.
   * <p>
   *
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperStem} stems.
   */
  public List has(String priv);

  /**
   * List stems where the specified member has the specified
   * privilege.
   * <p>
   * See implementations for more information.
   *
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperStem} stems.
   */
  public List has(GrouperMember m, String priv);

}

