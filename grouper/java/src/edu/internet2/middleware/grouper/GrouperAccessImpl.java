package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.util.List;

/** 
 * Default implementation of the {@link GrouperAccess} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccessImpl.java,v 1.6 2004-04-30 15:22:20 blair Exp $
 */
public class InternalGrouperAccess implements GrouperAccess {
  /**
   * Create a {@link GrouperAccess} that handles all privileges
   * internal to grouper.
   */
  public InternalGrouperAccess(GrouperSession s) {
    // Nothing -- Yet
  }

  /**
   * Grant an access privilege on a {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>XXX Confirm that subjectID has appropriate privilege to
   *      grant "priv" to "m".</li>
   *  <li>XXX Update <i>grouper_membership</i> table as apropriate.</li>
   *  <li>XXX Update <i>grouper_via</i> table as apropriate.</li>
   * </ul>
   */
  public void grant(GrouperGroup g, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * Revoke an access privilege on a {@link Grouper} group.
   * <p>
   * <ul>
   *  <li>XXX Confirm that subjectID has appropriate privilege to
   *      revoke "priv" from "m".</li>
   *  <li>XXX Update <i>grouper_membership</i> table as apropriate.</li>
   *  <li>XXX Update <i>grouper_via</i> table as apropriate.</li>
   * </ul>
   */
  public void revoke(GrouperGroup g, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * Return all access privileges that the current session's subject has on 
   * a {@link GrouperGroup}.
   * <p>
   * <ul>
   *  <li>XXX Query and return all memberships in the
   *      <i>grouper_membership</i> table.</li>
   * </ul>
   */
  public List has(GrouperGroup g) {
    return null;
  }

  /**
   * Return all access privileges for a {@link GrouperMember} on a 
   * {@link GrouperGroup}.
   * <p>
   * <ul>
   *  <li>XXX Query and return all memberships in the
   *      <i>grouper_membership</i> table that the subject
   *      has privileges to view.</li>
   * </ul>
   */
  public List has(GrouperGroup g, GrouperMember m) {
    return null;
  }

  /**
   * Verify whether the current session's subject has a specified 
   * access privilege on a {@link GrouperGroup}.
   * <p>
   * <ul>
   *  <li>XXX Confirm that subjectID has appropriate privilege to 
   *      verify "m"'s privileges.</li>
   *  <li>XXX Do I need to differentiate between "user" level checks
   *      and "system" level checks?</li>
   * </ul>
   */
  public boolean has(GrouperGroup g, String priv) {
    return false;
  }

  /**
   * Verify whether a {@link GrouperMember} has a specified privilege
   * access on a {@link GrouperGroup}.
   * <p>
   * <ul>
   *  <li>XXX Confirm that subjectID has appropriate privilege to 
   *      verify "m"'s privileges.</li>
   *  <li>XXX Do I need to differentiate between "user" level checks
   *      and "system" level checks?</li>
   * </ul>
   */
  public boolean has(GrouperGroup g, GrouperMember m, String priv) {
    return false;
  }
}

