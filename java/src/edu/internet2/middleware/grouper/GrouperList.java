package edu.internet2.middleware.grouper;

import  java.io.Serializable;

/** 
 * TODO 
 *
 * @author  blair christensen.
 * @version $Id: GrouperList.java,v 1.1 2004-08-11 19:17:43 blair Exp $
 */
public class GrouperMembership implements Serializable {

  private String  groupKey;
  private String  groupField;
  private String  memberKey;
  private boolean isImmediate;
  private String  via;
  private String  removeAfter;

  public GrouperMembership() {
    groupKey    = null;
    groupField  = null;
    memberKey   = null;
    isImmediate = false;
    via         = null;
    removeAfter = null;
  }

  public String toString() {
    return this.getGroupKey() + ":" + this.getGroupField() + ":" + 
           this.getMemberKey();
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

  private String getMemberKey() {
    return this.memberKey;
  }

  private void setMemberKey(String memberKey) {
    this.memberKey = memberKey;
  }

  private boolean getIsImmediate() {
    return this.isImmediate;
  }

  private void setIsImmediate(boolean isImmediate) {
    this.isImmediate = isImmediate;
  }

  private String getVia() {
    return this.via;
  }

  private void setVia(String via) {
    this.via = via;
  }

  private String getRemoveAfter() {
    return this.removeAfter;
  }

  private void setRemoveAfter(String removeAfter) {
    this.removeAfter = removeAfter;
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
    return java.lang.Math.abs( this.getGroupKey().hashCode()   ) + 
           java.lang.Math.abs( this.getGroupField().hashCode() ) +
           java.lang.Math.abs( this.getMemberKey().hashCode()  );
  }

}

