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
import  org.apache.commons.lang.builder.EqualsBuilder;
import  org.apache.commons.lang.builder.HashCodeBuilder;


/** 
 * Class representing a type definition for a {@link Grouper}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperTypeDef.java,v 1.10 2004-11-05 19:21:36 blair Exp $
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


  /*
   * PUBLIC INSTANCE METHODS
   */

  public boolean equals(Object o) {
     return EqualsBuilder.reflectionEquals(this, o);
   }

  /**
   * Returns {@link GroupField}.
   *
   * @return  Returns name of {@link GroupField}.
   */
  public String groupField() {
    return this.getGroupField();
  }

  /**
   * Returns {@link GroupType}.
   *
   * @return  Returns integer representing {@link GroupType}.
   */
  public String groupType() {
    return this.getGroupType();
  }

  public int hashCode() {
     return HashCodeBuilder.reflectionHashCode(this);
   }

  public String toString() {
    return this.getGroupType()  + ":" + this.getGroupField();
  }


  /*
   * HIBERNATE
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

}

