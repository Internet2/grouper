/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package edu.internet2.middleware.grouper;

import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;

/**
 * <p>Use this class to copy a group to another stem.</p>
 * <p>Sample call to copy everything
 * 
 * <blockquote>
 * <pre>
 * GroupCopy groupCopy = new GroupCopy(child_group, stem);
 * Group newGroup = groupCopy.copyPrivilegesOfGroup(true).copyGroupAsPrivilege(true)
 *  .copyListMembersOfGroup(true).copyListGroupAsMember(true).copyAttributes(true)
 *  .save();
 * </pre>
 * </blockquote>
 * 
 * </p>
 */
public class GroupCopy {

  private Group group;

  private Stem stem;

  private boolean privilegesOfGroup = true;

  private boolean groupAsPrivilege = true;

  private boolean listMembersOfGroup = true;

  private boolean listGroupAsMember = true;

  private boolean attributes = true;
  
  private String extension = null;
  
  private String displayExtension = null;

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
   * Optionally set a new extension for the group.
   * @param value
   * @return GroupCopy
   */
  public GroupCopy setExtension(String value) {
    this.extension = value;
    return this;
  }
  
  /**
   * Optionally set a new display extension for the group.
   * @param value
   * @return GroupCopy
   */
  public GroupCopy setDisplayExtension(String value) {
    this.displayExtension = value;
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
        listMembersOfGroup, listGroupAsMember, attributes, true, true, true, extension, displayExtension);
  }
}
