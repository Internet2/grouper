package edu.internet2.middleware.directory.grouper;

import  edu.internet2.middleware.directory.grouper.*;
import  java.util.List;

/** 
 * Class representing a {@link Grouper} member, whether an individual
 * or a group.
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.14 2004-04-30 17:30:29 blair Exp $
 */
public class GrouperMember {

  private GrouperSession  intSess = null;
  private String          subject = null;
  private boolean         isGroup = false; 

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
    this.subject = member;
    this.isGroup = false;

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
    return null;
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
    return null;
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
    return null;
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
    return this.subject;
  }

}

