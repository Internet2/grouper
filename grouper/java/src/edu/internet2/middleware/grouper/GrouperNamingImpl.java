/* 
 * Copyright (C) 2004 TODO
 * All Rights Reserved. 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted only as authorized by the Academic
 * Free License version 2.1.
 *
 * A copy of this license is available in the file LICENSE in the
 * top-level directory of the distribution or, alternatively, at
 * <http://www.opensource.org/licenses/afl-2.1.php>
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  java.util.*;

/** 
 * Default implementation of the {@link GrouperNaming} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNamingImpl.java,v 1.11 2004-09-08 19:31:08 blair Exp $
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
   * Grant a naming privilege on a {@link Grouper} stem.
   */
  public void grant(GrouperStem stem, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * Revoke a naming privilege on a {@link Grouper} stem.
   */
  public void revoke(GrouperStem stem, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * Return all naming privileges that the current session's subject has on 
   * a {@link Grouper} stem.
   */
  public List has(GrouperStem stem) {
    List privs = new ArrayList();
    return privs; 
  }

  /**
   * Return all naming privileges for a {@link GrouperMember} on a 
   * {@link Grouper} stem.
   */
  public List has(GrouperStem stem, GrouperMember m) {
    List privs = new ArrayList();
    return privs; 
  }

  /**
   * Verify whether the current session's subject has a specified 
   * naming privilege on a {@link Grouper} stem.
   */
  public boolean has(GrouperStem stem, String priv) {
    return false;
  }

  /**
   * Verify whether a {@link GrouperMember} has a specified naming privilege
   * on a {@link Grouper} stem.
   */
  public boolean has(GrouperStem stem, GrouperMember m, String priv) {
    return false;
  }

  /**
   * List stems where the current subject has the specified privilege.
   * <p>
   * TODO 
   *
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperStem} stems.
   */
  public List has(String priv) {
    List stems = new ArrayList();
    return stems;
  }

  /**
   * List stems where the specified member has the specified
   * privilege.
   * <p>
   * TODO 
   *
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperStem} stems.
   */
  public List has(GrouperMember m, String priv) {
    List stems = new ArrayList();
    return stems;
  }

}

