package edu.internet2.middleware.grouper;

import  java.io.Serializable;

/** 
 * TODO 
 *
 * @author  blair christensen.
 * @version $Id: GrouperSchema.java,v 1.2 2004-08-19 19:12:49 blair Exp $
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

  public void set(String key, int type) {
    this.groupKey   = key;
    this.groupType  = type;
  }

  /*
   * Below for Hibernate
   */

  private String getGroupKey() {
    return this.groupKey;
  }

  private void setGroupKey(String groupKey) {
    this.groupKey = groupKey;
  }

  private int getGroupType() {
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

