package edu.internet2.middleware.directory.grouper;

import  java.io.Serializable;

/** 
 * Class representing a type definition for a {@link Grouper}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperTypeDef.java,v 1.2 2004-08-03 01:09:52 blair Exp $
 */
public class GrouperTypeDef implements Serializable {

  private int     groupType;
  private String  groupField;

  /*
   * XXX Is this class actually needed?
   */

  /**
   * Create a {@link GrouperTypeDef} object.
   * <p>
   * XXX Is this class needed?  Or do {@link GrouperField} and 
   *     {@link GrouperType} provide everything this class might be
   *     needed for?
   */
  public GrouperTypeDef() {
    // Nothing -- Yet
  }

  /*
   * Below for Hibernate
   */

  protected int getGroupType() {
    return this.groupType;
  }

  private void setGroupType(int groupType) {
    this.groupType = groupType;
  }

  protected String getGroupField() {
    return this.groupField;
  }

  private void setGroupField(String groupField) {
    this.groupField = groupField;
  }

  // XXX Simplistic!  And probably wrong!
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return false;
  }

  // XXX Is this wise?  Correct?  Sufficient?
  public int hashCode() {
    return this.getGroupType() + java.lang.Math.abs( this.getGroupField().hashCode() ); 
  }

}

