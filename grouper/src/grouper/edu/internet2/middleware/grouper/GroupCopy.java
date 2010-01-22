/**
 * 
 */
package edu.internet2.middleware.grouper;

import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;

/**
 * Use this class to copy a group to another stem.
 * 
 * @author shilen
 * $Id: GroupCopy.java,v 1.3 2009-03-29 21:17:21 shilen Exp $
 */
public class GroupCopy {

  private Group group;

  private Stem stem;

  private boolean privilegesOfGroup = true;

  private boolean groupAsPrivilege = true;

  private boolean listMembersOfGroup = true;

  private boolean listGroupAsMember = true;

  private boolean attributes = true;

  /**
   * Create a new instance of this class if you would like to specify
   * specific options for a group copy.  After setting the options,
   * call save().
   * @param group Group to copy
   * @param stem  Stem where group should be copied
   */
  public GroupCopy(Group group, Stem stem) {
    this.group = group;
    this.stem = stem;
  }

  /**
   * Whether to copy privileges of the group.  Default is true.
   * @param value
   * @return GroupCopy
   */
  public GroupCopy copyPrivilegesOfGroup(boolean value) {
    this.privilegesOfGroup = value;
    return this;
  }

  /**
   * Whether to copy privileges where this group is a member.  Default is true.
   * @param value
   * @return GroupCopy
   */
  public GroupCopy copyGroupAsPrivilege(boolean value) {
    this.groupAsPrivilege = value;
    return this;
  }

  /**
   * Whether to copy the list memberships of the group.  Default is true.
   * @param value
   * @return GroupCopy
   */
  public GroupCopy copyListMembersOfGroup(boolean value) {
    this.listMembersOfGroup = value;
    return this;
  }

  /**
   * Whether to copy list memberships where this group is a member.  Default is true.
   * @param value
   * @return GroupCopy
   */
  public GroupCopy copyListGroupAsMember(boolean value) {
    this.listGroupAsMember = value;
    return this;
  }

  /**
   * Whether to copy attributes.  Default is true.
   * @param value
   * @return GroupCopy
   */
  public GroupCopy copyAttributes(boolean value) {
    this.attributes = value;
    return this;
  }

  /**
   * Copy the group using the options set in this class.
   * @return Group the new group
   * @throws GroupAddException 
   * @throws InsufficientPrivilegeException 
   */
  public Group save() throws GroupAddException, InsufficientPrivilegeException {

    GrouperSession.validate(GrouperSession.staticGrouperSession());

    return group.internal_copy(stem, privilegesOfGroup, groupAsPrivilege,
        listMembersOfGroup, listGroupAsMember, attributes, true, true, true);
  }
}
