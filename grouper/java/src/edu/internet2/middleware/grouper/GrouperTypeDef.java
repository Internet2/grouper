/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
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
 * @version $Id: GrouperTypeDef.java,v 1.8 2004-09-19 01:34:02 blair Exp $
 */
public class GrouperTypeDef implements Serializable {

  private String groupType;
  private String groupField;

  /**
   * Create a {@link GrouperTypeDef} object.
   * <p>
   * XXX Is this class needed?  Or do {@link GrouperField} and 
   *     {@link GrouperType} provide everything this class might be
   *     needed for?
   */
  public GrouperTypeDef() {
    groupType   = null;
    groupField  = null;
  }

  public String toString() {
    return this.getGroupType()  + ":" + this.getGroupField();
  }

  /**
   * Returns {@link GroupType}.
   *
   * @return  Returns integer representing {@link GroupType}.
   */
  public String groupType() {
    return this.getGroupType();
  }

  /**
   * Returns {@link GroupField}.
   *
   * @return  Returns name of {@link GroupField}.
   */
  public String groupField() {
    return this.getGroupField();
  }

  /*
   * Below for Hibernate
   */

  private String getGroupType() {
    return this.groupType;
  }

  private void setGroupType(String groupType) {
    this.groupType = groupType;
  }

  private String getGroupField() {
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
    return java.lang.Math.abs( this.getGroupType().hashCode()  ) +
           java.lang.Math.abs( this.getGroupField().hashCode() ); 
  }

}

