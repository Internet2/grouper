package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.util.List;

/** 
 * Default implementation of the {@link GrouperNaming} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperNamingImpl.java,v 1.4 2004-04-30 15:29:15 blair Exp $
 */
public class InternalGrouperNaming implements GrouperNaming {

  private GrouperSession intSess = null;

  public InternalGrouperNaming(GrouperSession s) {
    // Internal reference to the session we are using.
    intSess = s;
  }

  /**
   * Grant a naming privilege on a {@link Grouper} group.
   */
  public void grant(GrouperGroup g, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * Revoke a naming privilege on a {@link Grouper} group.
   */
  public void revoke(GrouperGroup g, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * Return all naming privileges that the current session's subject has on 
   * a {@link GrouperGroup}.
   */
  public List has(GrouperGroup g) {
    return null;
  }

  /**
   * Return all naming privileges for a {@link GrouperMember} on a 
   * {@link GrouperGroup}.
   */
  public List has(GrouperGroup g, GrouperMember m) {
    return null;
  }

  /**
   * Verify whether the current session's subject has a specified 
   * naming privilege on a {@link GrouperGroup}.
   */
  public boolean has(GrouperGroup g, String priv) {
    return false;
  }

  /**
   * Verify whether a {@link GrouperMember} has a specified naming privilege
   * on a {@link GrouperGroup}.
   */
  public boolean has(GrouperGroup g, GrouperMember m, String priv) {
    return false;
  }
}

