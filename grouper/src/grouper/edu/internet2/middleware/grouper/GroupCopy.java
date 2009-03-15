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
 * $Id: GroupCopy.java,v 1.1 2009-03-15 23:13:50 shilen Exp $
 */
public class GroupCopy {

  private Group group;

  private Stem stem;

  private boolean privilegesOfGroup = false;

  private boolean groupAsPrivilege = false;

  private boolean listMembersOfGroup = false;

  private boolean listGroupAsMember = false;

  private boolean attributes = false;

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
   * Whether to copy privileges of the group.  Default is false.
   * @param value
   * @return GroupCopy
   */
  public GroupCopy copyPrivilegesOfGroup(boolean value) {
    this.privilegesOfGroup = value;
    return this;
  }

  /**
   * Whether to copy privileges where this group is a member.  Default is false.
   * @param value
   * @return GroupCopy
   */
  public GroupCopy copyGroupAsPrivilege(boolean value) {
    this.groupAsPrivilege = value;
    return this;
  }

  /**
   * Whether to copy the list memberships of the group.  Default is false.
   * @param value
   * @return GroupCopy
   */
  public GroupCopy copyListMembersOfGroup(boolean value) {
    this.listMembersOfGroup = value;
    return this;
  }

  /**
   * Whether to copy list memberships where this group is a member.  Default is false.
   * @param value
   * @return GroupCopy
   */
  public GroupCopy copyListGroupAsMember(boolean value) {
    this.listGroupAsMember = value;
    return this;
  }

  /**
   * Whether to copy attributes.  Default is false.
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

    // verify that the subject has read privileges to the group
    if (!PrivilegeHelper.canRead(GrouperSession.staticGrouperSession(), group,
        GrouperSession.staticGrouperSession().getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_READ);
    }

    return group.internal_copy(stem, privilegesOfGroup, groupAsPrivilege,
        listMembersOfGroup, listGroupAsMember, attributes, true, true);
  }
}
