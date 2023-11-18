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

import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;

/**
 * Use this class to move a group to another stem.
 * 
 * @author shilen
 * $Id: GroupMove.java,v 1.3 2009-03-29 21:17:21 shilen Exp $
 */
public class GroupMove {

  private Group group;

  private Stem stem;

  private boolean assignAlternateName = true;

  /**
   * Create a new instance of this class if you would like to specify
   * specific options for a group move.  After setting the options,
   * call save().
   * @param group Group to move
   * @param stem  Stem where group should be moved
   */
  public GroupMove(Group group, Stem stem) {
    this.group = group;
    this.stem = stem;
  }

  /**
   * Whether to add the current name of the group to the group's alternate names list.  
   * Certain operations like group name queries (GroupFinder.findByName()) will find 
   * groups by their current and alternate names.  Currently, Grouper only supports one
   * alternate name per group, so if groups are moved/renamed multiple times, only the last name
   * will be kept as an alternate name.  Default is true.
   * @param value
   * @return GroupMove
   */
  public GroupMove assignAlternateName(boolean value) {
    this.assignAlternateName = value;
    return this;
  }

  /**
   * Move the group using the options set in this class.
   * @throws GroupModifyException 
   * @throws InsufficientPrivilegeException 
   */
  public void save() throws GroupModifyException, InsufficientPrivilegeException {

    group.internal_move(stem, assignAlternateName);
  }
}
