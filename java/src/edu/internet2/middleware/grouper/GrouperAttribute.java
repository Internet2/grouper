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
 * @version $Id: GrouperAttribute.java,v 1.9 2004-11-28 04:36:17 blair Exp $
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

  // FIXME I hate you.
  public void set(String key, String field, String value) {
    this.groupKey         = key;
    this.groupField       = field;
    this.groupFieldValue  = value;
  }

  // FIXME I hate you.
  public String toString() {
    return this.getGroupKey() + ":" + this.getGroupField() + ":" + 
           this.getGroupFieldValue();
  }

  public String value() {
    return this.getGroupFieldValue();
  }


  /*
   * PROTECTED INSTANCE METHODS
   */
  protected String key() {
    // TODO This does expose the group key.  Do we want that?
    return this.getGroupKey();
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

