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
 * @version $Id: GrouperNamingImpl.java,v 1.21 2004-11-22 01:40:23 blair Exp $
 */
public class GrouperNamingImpl implements GrouperNaming {

  /*
   * CONSTRUCTORS
   */
  public GrouperNamingImpl() {
    // Nothing -- Yet
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Grant a naming privilege on a {@link GrouperStem}.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   stem  Grant privileges on this {@link Grouper} stem.
   * @param   m     Grant privileges for this {@link GrouperMember}.
   * @param   priv  Privilege to grant.
   */
  public boolean grant(
                    GrouperSession s, GrouperStem stem, 
                    GrouperMember m, String priv
                   ) 
  {
    boolean rv = false;
    return rv;
  }

  /**
   * List stems where the current subject has the specified privilege.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperStem} stems.
   */
  public List has(GrouperSession s, String priv) {
    List privs = new ArrayList();
    return privs;
  }

  /**
   * List naming privileges for current subject on the specified stem.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   stem  List privileges on this stem.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, GrouperStem stem) {
    List privs = new ArrayList();
    return privs;
  }

  /**
   * List stems where the specified member has the specified
   * privilege.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperStem} stems.
   */
  public List has(GrouperSession s, GrouperMember m, String priv) {
    List privs = new ArrayList();
    return privs;
  }

  /**
   * List access privileges for specified member on the specified stem.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   stem  Return privileges for this {@link Grouper} stem.
   * @param   m     List privileges for this @link GrouperMember}.
   * @return  List of privileges.
   */
  public List has(GrouperSession s, GrouperStem stem, GrouperMember m) {
    List privs = new ArrayList();
    return privs;
  }

  /**
   * Verify whether current subject has the specified privilege on the
   * specified stem.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   stem  Verify privilege for this stem.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the stem.
   */
  public boolean has(GrouperSession s, GrouperStem stem, String priv) {
    boolean rv = false;
    return rv;
  }

  /**
   * Verify whether the specified member has the specified privilege
   * on the specified stem.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   stem  Verify privilege for this stem.
   * @param   m     Verify privilege for this member.
   * @param   priv  Verify this privilege.
   * @return  True if subject has this privilege on the stem.
   */
  public boolean has(
                     GrouperSession s, GrouperStem stem, 
                     GrouperMember m, String priv
                    ) 
  {
    boolean rv = false;
    return rv;
  }

  /**
   * Revoke a naming privilege on a {@link Grouper} stem.
   * <p>
   *
   * @param   s     Act within this {@link GrouperSession}.
   * @param   stem  Revoke privilege on this {@link Grouper} stem.
   * @param   m     Revoke privilege for this{@link GrouperMember}.
   * @param   priv  Privilege to revoke.
   */
  public boolean revoke(
                     GrouperSession s, GrouperStem stem, 
                     GrouperMember m, String priv
                    ) 
  {
    boolean rv = false;
    return rv;
  }

}

