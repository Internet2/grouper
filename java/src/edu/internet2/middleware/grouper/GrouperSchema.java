/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  java.io.Serializable;

/** 
 * TODO 
 *
 * @author  blair christensen.
 * @version $Id: GrouperSchema.java,v 1.7 2004-10-05 18:35:54 blair Exp $
 */
public class GrouperSchema implements Serializable {

  private String groupKey;
  private String groupType;

  public GrouperSchema() {
    this._init();
  }

  public GrouperSchema(String key, String type) {
    this._init();
    this.setGroupKey(key);
    this.setGroupType(type);
  }

  public String toString() {
    return this.getGroupKey() + ":" + this.getGroupType();
  }

  /*
   * PUBLIC METHODS ABOVE, PRIVATE METHODS BELOW
   */

  /*
   * Initialize instance variables
   */
  private void _init() {
    this.setGroupKey(null);   
    this.setGroupType(null);
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

  private String getGroupType() {
    return this.groupType;
  }

  private void setGroupType(String groupType) {
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
           java.lang.Math.abs( this.getGroupType().hashCode() ); 
  }

}

