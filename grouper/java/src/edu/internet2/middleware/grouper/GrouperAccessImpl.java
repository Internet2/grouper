package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.util.ArrayList;
import  java.util.List;

/** 
 * Default implementation of the {@link GrouperAccess} interface.
 *
 * @author  blair christensen.
 * @version $Id: GrouperAccessImpl.java,v 1.8 2004-05-28 17:49:42 blair Exp $
 */
public class InternalGrouperAccess implements GrouperAccess {

  /**
   * Create a access interface object.
   *
   * @param   s   Session context.
   */
  public InternalGrouperAccess(GrouperSession s) {
    // Nothing -- Yet
  }

  /**
   * Grant an access privilege.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient
   *      privileges to grant the privilege.</li>
   *  <li>Update <i>grouper_membership</i> table with new access
   *      privilege.</li>
   *  <li>Update the <i>grouper_membership</i> and <i>grouper_via</i>
   *      tables as appropriate to reflect any new effective
   *      memberships.</li>
   * </ul>
   */
  public void grant(GrouperGroup g, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * Revoke an access privilege.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient
   *      privileges to grant the privilege.</li>
   *  <li>Update <i>grouper_membership</i> table to reflect revoked
   *      access privilege.</li>
   *  <li>Update the <i>grouper_membership</i> and <i>grouper_via</i>
   *      tables as appropriate to reflect any new effective
   *      memberships.</li>
   * </ul>
   */
  public void revoke(GrouperGroup g, GrouperMember m, String priv) {
    // Nothing -- Yet
  }

  /**
   * List access privileges for current subject on the specified group.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient
   *      privileges to view the privileges.</li>
   *  <li>Fetch rows from the <i>grouper_membership</i> table with the
   *      appropriate <i>groupID</i> and <i>memberID</i> values.</li>
   * </ul>
   */
  public List has(GrouperGroup g) {
    List privs = new ArrayList();
    return privs;
  }

  /**
   * List access privileges for specified member on the specified group.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient
   *      privileges to view the privileges.</li>
   *  <li>Fetch rows from the <i>grouper_membership</i> table with the
   *      appropriate <i>groupID</i> and <i>memberID</i> or
   *      <i>groupMemberID</i> values.</li>
   * </ul>
   */
  public List has(GrouperGroup g, GrouperMember m) {
    List privs = new ArrayList();
    return privs;
  }

  /**
   * Verify whether the current session's subject has a specified 
   * access privilege on a {@link GrouperGroup}.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient
   *      privileges to view the specified privilege.</li>
   *  <li>Verify the privilege.</li>
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
   *  <li>Verify that the current subject has sufficient
   *      privileges to view the specified privilege.</li>
   *  <li>Verify the privilege.</li>
   * </ul>
   */
  public boolean has(GrouperGroup g, GrouperMember m, String priv) {
    return false;
  }

  /**
   * List groups where the current subject has the specified privilege.
   * <p>
   * TODO 
   *
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperGroup} groups.
   */
  public List has(String priv) {
    List groups = new ArrayList();
    return groups;
  }

  /**
   * List groups where the specified member has the specified
   * privilege.
   * <p>
   * TODO 
   *
   * @param   m     Query for this {@link GrouperMember}.
   * @param   priv  Query for this privilege type.
   * @return  List of {@link GrouperGroup} groups.
   */
  public List has(GrouperMember m, String priv) {
    List groups = new ArrayList();
    return groups;
  }

}

