/* 
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
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
 * TODO 
 *
 * @author  blair christensen.
 * @version $Id: GrouperSchema.java,v 1.10 2004-11-23 19:43:26 blair Exp $
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


  /*
   * PUBLIC INSTANCE METHODS
   */

  public boolean equals(Object o) {
     return EqualsBuilder.reflectionEquals(this, o);
   }

  public int hashCode() {
     return HashCodeBuilder.reflectionHashCode(this);
   }

  public String toString() {
    return this.getGroupKey() + ":" + this.getGroupType();
  }

  public String type() {
    return this.getGroupType();
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize instance variables
   */
  private void _init() {
    this.setGroupKey(null);   
    this.setGroupType(null);
  }


  /*
   * HIBERNATE
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

}

