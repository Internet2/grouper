package edu.internet2.middleware.directory.grouper;

/** 
 * Default implementation of the {@link GrouperPrivilege} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccessImpl.java,v 1.2 2004-04-27 05:24:41 blair Exp $
 */
public class InternalGrouperPrivilege implements GrouperPrivilege {
  /**
   * Create a {@link GrouperPrivilege} that handles all privileges
   * internal to grouper.
   */
  public InternalGrouperPrivilege(GrouperSession s) {
    // Nothing -- Yet
  }

  /**
   * Grant privilege on a {@link Grouper} group.
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
   * Revoke privilege on a {@link Grouper} group.
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
   * Verify whether a {@link GrouperMember} has a specified privilege
   * on a {@link GrouperGroup}.
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

