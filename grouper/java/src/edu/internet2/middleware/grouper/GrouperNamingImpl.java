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
 * Default implementation of the {@link GrouperNaming} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNamingImpl.java,v 1.14 2004-10-12 20:32:00 blair Exp $
 */
public class InternalGrouperNaming implements GrouperNaming {

  private GrouperSession grprSession = null;

  /**
   * Create a naming interface object.
   *
   * @param   s   Session context.
   */
  public InternalGrouperNaming(GrouperSession s) {
    // Internal reference to the session we are using.
    grprSession = s;
  }

  /**
   * Grant a naming privilege on a {@link Grouper} namespace.
   */
  public void grant(GrouperNamespace namespace, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * Revoke a naming privilege on a {@link Grouper} namespace.
   */
  public void revoke(GrouperNamespace namespace, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * Return all naming privileges that the current session's subject has on 
   * a {@link Grouper} namespace.
   */
  public List has(GrouperNamespace namespace) {
    List privs = new ArrayList();
    return privs; 
  }

  /**
   * Return all naming privileges for a {@link GrouperMember} on a 
   * {@link Grouper} namespace.
   */
  public List has(GrouperNamespace namespace, GrouperMember m) {
    List privs = new ArrayList();
    return privs; 
  }

  /**
   * Verify whether the current session's subject has a specified 
   * naming privilege on a {@link Grouper} namespace.
   */
  public boolean has(GrouperNamespace namespace, String priv) {
    return false;
  }

  /**
   * Verify whether a {@link GrouperMember} has a specified naming privilege
   * on a {@link Grouper} namespace.
   */
  public boolean has(GrouperNamespace namespace, GrouperMember m, String priv) {
    return false;
  }

  /**
   * List namespaces where the current subject has the specified privilege.
   * <p>
   * TODO 
   *
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperNamespace} namespaces.
   */
  public List has(String priv) {
    List namespaces = new ArrayList();
    return namespaces;
  }

  /**
   * List namespaces where the specified member has the specified
   * privilege.
   * <p>
   * TODO 
   *
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperNamespace} namespaces.
   */
  public List has(GrouperMember m, String priv) {
    List namespaces = new ArrayList();
    return namespaces;
  }

}

