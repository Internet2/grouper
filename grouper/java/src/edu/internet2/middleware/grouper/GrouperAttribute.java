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
 * TODO 
 *
 * @author  blair christensen.
 * @version $Id: GrouperAttribute.java,v 1.7 2004-11-05 19:21:36 blair Exp $
 */
public class GrouperAttribute implements Serializable {

  private String  groupKey;
  private String  groupField;
  private String  groupFieldValue;

  public GrouperAttribute() {
    groupKey        = null;
    groupField      = null;
    groupFieldValue = null;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  public boolean equals(Object o) {
     return EqualsBuilder.reflectionEquals(this, o);
   }

  public String field() {
    return this.getGroupField();
  }

  public int hashCode() {
     return HashCodeBuilder.reflectionHashCode(this);
   }

  public String key() {
    // TODO This does expose the group key.  Do we want that?
    return this.getGroupKey();
  }

  public void set(String key, String field, String value) {
    this.groupKey         = key;
    this.groupField       = field;
    this.groupFieldValue  = value;
  }

  public String toString() {
    return this.getGroupKey() + ":" + this.getGroupField() + ":" + 
           this.getGroupFieldValue();
  }

  public String value() {
    return this.getGroupFieldValue();
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

  private String getGroupField() {
    return this.groupField;
  }

  private void setGroupField(String groupField) {
    this.groupField = groupField;
  }

  private String getGroupFieldValue() {
    return this.groupFieldValue;
  }

  private void setGroupFieldValue(String groupFieldValue) {
    this.groupFieldValue = groupFieldValue;
  }

}

