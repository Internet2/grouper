package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.util.List;

/** 
 * Class representing a {@link Grouper} member, whether an individual
 * or a group.
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.17 2004-05-01 17:54:18 blair Exp $
 */
public class GrouperMember {

  private GrouperSession  intSess     = null;
  /* groupID || memberID */
  private String          subjectID   = null;
  /* groupName || presentationID */
  private String          subjectName = null;
  private boolean         isGroup     = false; 

  /**
   * Create a {@link GrouperMember} object that represents a single
   * {@link Grouper} member.
   * <p>
   * <i>member</i> could be either a memberId or a groupID.
   * <p>
   * <ul>
   *  <li>If isGroup==true:</li>
   *  <ul>
   *   <li>If member==groupID in the <i>grouper_group</i> table,
   *       create a {@link GrouperMember} object representing
   *       the group groupID.</li>
   *  </ul>
   *  <li>If isGroup==false:</li>
   *  <ul>
   *   <li>If GrouperSubject(member, false) succeeds,
   *       create a {@link GrouperMember} object representing
   *       the member memberID.</li>
   *  </ul>
   *  <li>Sets isGroup as appropriate</li>
   *  <li>XXX Cache memberships -- or not?</li>
   * </ul>
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
   * Returns a list of all {@link GrouperGroup} memberships of type
   * "members".
   * <p>
   * <ul>
   *  <li>Fetch and return rows from the <i>grouper_membership</i>
   *      table that represent "members" and have the appropriate
   *      <i>memberID</i> value.</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   * @return  List of group memberships.
   */
  public List getMembership() {
    List membership = null;
    return membership;
  }

  /**
   * Returns a list of all {@link GrouperGroup} memberships of type
   * <i>groupField</li>.
   * <p>
   * <ul>
   *  <li>Fetch and return rows from the <i>grouper_membership</i>
   *      table with the appropriate <i>memberID</i> and <i>groupField</i>
   *      values.</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   * @param   groupField  Type of group to return.
   * @return  List of group memberships.
   */
  public List getMembership(String groupField) {
    List membership = null;
    return membership;
  }

  /**
   * Returns a list of all {@link GrouperGroup} memberships of type
   * <i>groupField</li>.
   * <p>
   * <ul>
   *  <li>Fetch and return rows from the <i>grouper_membership</i>
   *      table with the appropriate <i>groupID</i>, <i>groupField</i>,
   *      and <i>isImmediate</i> values.</li>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   * @param   groupField  Type of group to return.
   * @param   isImmediate Return only immediate or non-immediate
   *          memberships.
   * @return  List of group memberships..
   */
  public List getMembership(String groupField, boolean isImmediate) {
    List membership = null;
    return membership;
  }

  /**
   * Returns true if member object is a group.
   *
   * @return  True if @{link Grouper} member is a {@link Grouper}
   *  group, false otherwise.
   */
  public boolean isGroup() {
    return this.isGroup;
  }

  /**
   * Identifies a {@link GrouperMember} object.
   * <p>
   * <ul>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   * @return  String representing the <i>memberID</i> for this 
   *   {@link GrouperMember} object.
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
   * Returns list of {@link GrouperGroup} objects that give the current
   * sessions's subject "member" membership in a particular 
   * {@link GrouperGroup}.
   * <p>
   * <ul>
   *  <li>XXX Are we inspecting a column or a table for this
   *      information?</li>
   * </ul>
   *
   * @param   g Return via information for memberships in this group.
   * @return  List of {@link GrouperGroup} objects.
   */
  public List via(GrouperGroup g) {
    return null;
  }

  /**
   * Returns list of {@link GrouperGroup} objects that give a
   * particular {@link GrouperMember} "member" membership in
   * a particular {@link GrouperGroup}.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privs to
   *      access this information.</li>
   *  <li>XXX Are we inspecting a column or a table for this
   *      information?</li>
   * </ul>
   *
   * @param   g Return via information for memberships in this group.
   * @param   m Return via information for this member.
   * @return  List of {@link GrouperGroup} objects.
   */
  public List via(GrouperGroup g, GrouperMember m) {
    return null;
  }

  /**
   * Returns list of {@link GrouperGroup} objects that give a
   * particular {@link GrouperMember} "groupField"  make a given 
   * {@link GrouperMember} a <i>groupField</i> membership in a
   * particular {@link GrouperGroup}.
   * <p>
   * <ul>
   *  <li>Verify that the current subject has sufficient privs to
   *      access this information.</li>
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
    return null;
  }

}

