package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.util.ArrayList;
import  java.util.List;

/** 
 * Class representing a {@link Grouper} member, whether an individual
 * or a {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.18 2004-05-02 01:31:21 blair Exp $
 */
public class GrouperMember {

  private GrouperSession  intSess     = null;
  /* groupID || memberID */
  private String          subjectID   = null;
  /* groupName || presentationID */
  private String          subjectName = null;
  private boolean         isGroup     = false; 

  /**
   * Create an object that represents a group member.
   * <p>
   * The member may be either an individual or a {@link GrouperGroup}.
   * If an individual, <i>isGroup</i> should be "false" and
   * <i>member</i> should be the <i>memberID</i>.  If a group,
   * <i>isGroup</i> should be true and <i>member</i> should be the
   * group name. 
   * <p>
   * XXX  This <b>may</b> trigger performance and ease-of-use
   * concerns.  A solution be to add an additional parameter that
   * specified whether <i>member</i> was the static (<i>groupID</i>
   * and <i>memberID</i>) or variable (<i>name</i> and
   * <i>presentationID</i>) representation of the member.
   * 
   * @param   s         Session context.
   * @param   member    Member identity.
   * @param   isGroup   True if the member is a group.
   */
  public GrouperMember(GrouperSession s, String member, boolean isGroup) {
    // Internal reference to the session we are using.
    this.intSess  = s;

    // XXX Bad assumptions!
    if (isGroup == true) {
      // This member is a group
      this.isGroup    = true;
      // XXX "member" is *probably* the groupID.  What should we store
      //     as subjectID?  If groupName, we need to perform another
      //     query to identify the groupName.  If groupID, we will need
      //     to lazy query for the groupName at a later time (i.e. in
      //     whoAmI()).
      this.subjectID  = member;
    } else {
      // This member is an individual
      this.isGroup    = false;
      // XXX Assuming member == memberID.  Confirm that this is true.
      this.subjectID  = member;
    }


  }

  /**
   * Retrieves all memberships of type "members".
   * <p>
   * <ul>
   *  <li>Fetch rows from the <i>grouper_membership</i> table
   *      that represent "members" and have the appropriate
   *      <i>groupID</i> and <i>memberID</i> values.</li>
   * </ul>
   *
   * @return  List of group memberships.
   */
  public List getMembership() {
    List membership = new ArrayList();
    return membership;
  }

  /**
   * Retrieves all memberships of type <i>groupField</i>.
   * <p>
   * <ul>
   *  <li>Fetch rows from the <i>grouper_membership</i> table with
   *      the appropriate <i>groupID</i>, <i>groupField</i>, and 
   *      <i>memberID</i> or <i>groupMemberID</i> values.</li>
   * </ul>
   *
   * @param   groupField  Type of group to return.
   * @return  List of group memberships.
   */
  public List getMembership(String groupField) {
    List membership = new ArrayList();
    return membership;
  }

  /**
   * Retrieves all memberships the specified type and immediacy.
   * <p>
   * <ul>
   *  <li>Fetch rows from the <i>grouper_membership</i> table with
   *      the appropriate <i>groupID</i>, <i>groupField</i>, 
   *      <i>isImmediate</i>, and <i>memberID</i> or
   *      <i>groupMemberID</i> values.</li>
   * </ul>
   *
   * @param   groupField  Type of group to return.
   * @param   isImmediate Immediacy of membership.
   * @return  List of group memberships..
   */
  public List getMembership(String groupField, boolean isImmediate) {
    List membership = new ArrayList();
    return membership;
  }

  /**
   * Declares whether this member object is a group.
   *
   * @return  True if a group.
   */
  public boolean isGroup() {
    return this.isGroup;
  }

  /**
   * Identify this member object.
   *
   * @return  <i>memberID</i> or group name.
   */
  public String whoAmI() {
    if (this.isGroup() == true) {
      // XXX Right now this returns the groupID.  We need to lookup,
      //     cache, and return the groupName.
      return this.subjectID;
    } else {
      // Return memberID for an individual member
      return this.subjectID;
    }
  }

  /**
   * List of groups providing the current subject membership in the
   * specified group.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient
   *      privileges to access this information.</li>
   *  <li>XXX Are we inspecting a column or a table for this
   *      information?</li>
   * </ul>
   *
   * @param   g Return via information for memberships in this group.
   * @return  List of groups.
   */
  public List via(GrouperGroup g) {
    List via = new ArrayList();
    return via;
  }

  /**
   * List of groups providing the specified member membership in the
   * specified group.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient
   *      privileges to access this information.</li>
   *  <li>XXX Are we inspecting a column or a table for this
   *      information?</li>
   * </ul>
   *
   * @param   g Return via information for memberships in this group.
   * @param   m Return via information for this member.
   * @return  List of {@link GrouperGroup} objects.
   */
  public List via(GrouperGroup g, GrouperMember m) {
    List via = new ArrayList();
    return via;
  }

  /**
   * List of groups providing the specified member the specified type
   * of membership in the specified group.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient
   *      privileges to access this information.</li>
   *  <li>XXX Are we inspecting a column or a table for this
   *      information?</li>
   * </ul>
   *
   * @param   g           Return via information for memberships in this group.
   * @param   m           Return via information for this member.
   * @param   groupField  Return via information for this type of
   *   membership.
   * @return  List of {@link GrouperGroup} objects.
   */
  public List via(GrouperGroup g, GrouperMember m, String groupField) {
    List via = new ArrayList();
    return via;
  }

}

