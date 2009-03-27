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
 * $Id: GroupMove.java,v 1.2 2009-03-27 15:38:20 shilen Exp $
 */
public class GroupMove {

  private Group group;

  private Stem stem;

  private boolean assignOldName = true;

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
  public GroupMove assignOldName(boolean value) {
    this.assignOldName = value;
    return this;
  }

  /**
   * Move the group using the options set in this class.
   * @throws GroupModifyException 
   * @throws InsufficientPrivilegeException 
   */
  public void save() throws GroupModifyException, InsufficientPrivilegeException {

    group.internal_move(stem, assignOldName);
  }
}
