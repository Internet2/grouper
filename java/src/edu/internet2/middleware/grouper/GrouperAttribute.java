package edu.internet2.middleware.grouper;

import  java.io.Serializable;

/** 
 * TODO 
 *
 * @author  blair christensen.
 * @version $Id: GrouperAttribute.java,v 1.2 2004-08-19 19:16:32 blair Exp $
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

  public String toString() {
    return this.getGroupKey() + ":" + this.getGroupField() + ":" + 
           this.getGroupFieldValue();
  }

  public void set(String key, String field, String value) {
    this.groupKey         = key;
    this.groupField       = field;
    this.groupFieldValue  = value;
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
           java.lang.Math.abs( this.getGroupField().hashCode() );
  }

}

