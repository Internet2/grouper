package edu.internet2.middleware.grouper;

import  java.io.Serializable;

/** 
 * TODO 
 *
 * @author  blair christensen.
 * @version $Id: GrouperSchema.java,v 1.1 2004-08-06 18:37:20 blair Exp $
 */
public class GrouperSchema implements Serializable {

  private String  groupKey;
  private int     groupType;

  public GrouperSchema() {
    groupKey  = null;
    groupType = 0;
  }

  public String toString() {
    return this.getGroupKey() + ":" + this.getGroupType();
  }

  /*
   * Below for Hibernate
   */

  protected String getGroupKey() {
    return this.groupKey;
  }

  private void setGroupKey(String groupKey) {
    this.groupKey = groupKey;
  }

  protected int getGroupType() {
    return this.groupType;
  }

  private void setGroupType(int groupType) {
    this.groupType = groupType;
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
    return java.lang.Math.abs( this.getGroupKey().hashCode() ) + 
           this.getGroupType(); 
  }

}

