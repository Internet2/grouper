package edu.internet2.middleware.directory.grouper;

import java.util.List;

/** 
 * Class representing a {@link Grouper} member, whether an individual
 * or a group.
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.5 2004-04-14 03:05:42 blair Exp $
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

  public List immediateMemberships(int groupFieldID) {
    return null;
  }

  public List immediateMemberships(String groupFieldID) {
    return null;
  }

  public List effectiveMemberships(int groupFieldID) {
    return null;
  }

  public List effectiveMemberships(String groupFieldID) {
    return null;
  }

  public boolean isGroup() {
    /* 
      Returns true if the GrouperMember object is a group. 
      - XXX How does it know if it is a group or not?
    */
    return false;
  }

}

