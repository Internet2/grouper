package edu.internet2.middleware.directory.grouper;

/** 
 * Class representing a type of {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperType.java,v 1.1 2004-07-26 17:03:49 blair Exp $
 */
public class GrouperGroupType {

  private int groupType = 0;

  /**
   * Create a {@link GrouperGroupType} object.
   */
  public GrouperGroupType() {
    // Nothing -- Yet
  }

  /*
   * Below for Hibernate
   */

  private int getGroupType() {
    return this.groupType;
  }

  private void setGroupType(int groupType) {
    this.groupType = groupType;
  }

}

