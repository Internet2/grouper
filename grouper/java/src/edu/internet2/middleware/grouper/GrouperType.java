package edu.internet2.middleware.grouper;

/** 
 * Class representing a type of {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperType.java,v 1.4 2004-08-06 15:19:51 blair Exp $
 */
public class GrouperType {

  private int groupType;

  /**
   * Create a {@link GrouperType} object.
   */
  public GrouperType() {
    groupType = 0;
  }

  public String toString() {
    return "" + this.getGroupType();
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

