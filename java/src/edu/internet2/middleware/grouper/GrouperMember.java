package edu.internet2.middleware.directory.grouper;

import java.util.List;

/** 
 * Class representing a {@link Grouper} member, whether an individual
 * or a group.
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.9 2004-04-28 16:24:42 blair Exp $
 */
public class GrouperMember {

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
   *  <li>XXX Cache privs</li>
   *  <li>XXX Cache memberships</li>
   * </ul>
   */
  public GrouperMember(GrouperSession s, String member, boolean isGroup) {
    // Nothing -- Yet
  }

  /**
   * Return immediate memberships for "member".
   * <p>
   * <ul>
   *  <li>Query the <i>grouper_membership</i> table for rows matching
   *      the appropriate "memberID", "groupField", and
   *      "isImmediate".</li>
   *  <li>XXX What authZ considerations are here?</li>
   * </ul>
   * 
   * @param  groupField  Type of group field to return.
   * @return List of {@link GrouperGroup} objects representing
   * immediate memberships.
   */
  public List immediateMemberships(String groupField) {
    return null;
  }

  /**
   * Return effective memberships for "member".
   * <p>
   * <ul>
   *  <li>Query the <i>grouper_membership</i> table for rows matching
   *      the appropriate "memberID", "groupField", and
   *      "isImmediate".</li>
   *  <li>XXX What authZ considerations are here?</li>
   * </ul>
   * 
   * @param  groupField  Type of group field to return.
   * @return List of {@link GrouperGroup} objects representing
   * effective memberships
   */
  public List effectiveMemberships(String groupField) {
    return null;
  }

  /**
   * Returns true if member object is a group.
   * <p>
   * <ul>
   *  <li>XXX Determine what exactly is being set internally</li>
   * </ul>
   *
   * @return True if @{lilnk Grouper} member is a {@link Grouper}
   * group, false otherwise.
   */
  public boolean isGroup() {
    return false;
  }

  /**
   * Identifies a {@link GrouperMember} object.
   * <p>
   * <ul>
   *  <li>XXX Add to docs/examples/.</li>
   * </ul>
   *
   * @return String representing the <i>memberID</i> for this 
   *         {@link GrouperMember} object.
   */
  public String whoami() {
    // Nothing -- Yet
  }

}

