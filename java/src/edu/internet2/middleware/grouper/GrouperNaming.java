/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import java.util.*;

/** 
 * {@link Grouper} Naming Interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNaming.java,v 1.13 2004-10-13 16:53:15 blair Exp $
 */
public interface GrouperNaming {

  /**
   * Grant a naming privilege on a {@link Grouper} namespace.
   * <p>
   * See implementations for more information.
   *
   * @param   namespace  Grant privileges on this {@link Grouper} namespace.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public void grant(GrouperNamespace namespace, GrouperMember m, String priv);

  /**
   * Revoke a naming privilege on a {@link Grouper} namespace.
   * <p>
   * See implementations for more information.
   *
   * @param   namespace  Revoke privilege on this {@link Grouper} namespace.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public void revoke(GrouperNamespace namespace, GrouperMember m, String priv);

  /**
   * List naming privileges for current subject on the specified namespace.
   * <p>
   * See implementations for more information.
   *
   * @param   namespace  List privileges on this namespace.
   * @return  List of privileges.
   */
  public List has(GrouperNamespace namespace);

  /**
   * List access privileges for specified member on the specified namespace.
   * <p>
   * See implementations for more information.
   *
   * @param   namespace  Return privileges for this {@link Grouper} namespace.
   * @param   m     List privileges for this @link GrouperMember}.
   * @return  List of privileges.
   */
  public List has(GrouperNamespace namespace, GrouperMember m);

  /**
   * Verify whether current subject has the specified privilege on the
   * specified namespace.
   * <p>
   * See implementations for more information.
   *
   * @param   namespace  Verify privilege for this namespace.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the namespace.
   */
  public boolean has(GrouperNamespace namespace, String priv);

  /**
   * Verify whether the specified member has the specified privilege
   * on the specified namespace.
   * <p>
   * See implementations for more information.
   *
   * @param   namespace  Verify privilege for this namespace.
   * @param   m     Verify privilege for this member.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the namespace.
   */
  public boolean has(GrouperNamespace namespace, GrouperMember m, String priv);

  /**
   * List namespaces where the current subject has the specified privilege.
   * <p>
   * See implementations for more information.
   * <p>
   *
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperNamespace} namespaces.
   */
  public List has(String priv);

  /**
   * List namespaces where the specified member has the specified
   * privilege.
   * <p>
   * See implementations for more information.
   *
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperNamespace} namespaces.
   */
  public List has(GrouperMember m, String priv);

}

