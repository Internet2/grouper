package edu.internet2.middleware.directory.grouper;

/** 
 * Class representing a type of {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperType.java,v 1.2 2004-08-03 01:07:34 blair Exp $
 */
public class GrouperType {

  private int groupType = 0;

  /**
   * Create a {@link GrouperType} object.
   */
  public GrouperType() {
    // Nothing -- Yet
  }

  /*
   * Below for Hibernate
   */

  protected int getGroupType() {
    return this.groupType;
  }

  // XXX Do we really want to allow write access? 
  private void setGroupType(int groupType) {
    this.groupType = groupType;
  }

}

