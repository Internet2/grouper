/* 
 * Copyright (C) 2004 TODO
 * All Rights Reserved. 
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted only as authorized by the Academic
 * Free License version 2.1.
 *
 * A copy of this license is available in the file LICENSE in the
 * top-level directory of the distribution or, alternatively, at
 * <http://www.opensource.org/licenses/afl-2.1.php>
 */

package edu.internet2.middleware.grouper;

import  java.io.Serializable;

/** 
 * Class representing a type definition for a {@link Grouper}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperTypeDef.java,v 1.5 2004-08-24 17:37:58 blair Exp $
 */
public class GrouperTypeDef implements Serializable {

  private int     groupType;
  private String  groupField;

  /**
   * Create a {@link GrouperTypeDef} object.
   * <p>
   * XXX Is this class needed?  Or do {@link GrouperField} and 
   *     {@link GrouperType} provide everything this class might be
   *     needed for?
   */
  public GrouperTypeDef() {
    groupType   = 0;
    groupField  = null;
  }

  public String toString() {
    return this.getGroupType()  + ":" + this.getGroupField();
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

