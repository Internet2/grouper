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
 * @version $Id: GrouperList.java,v 1.6 2004-11-05 19:21:36 blair Exp $
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
    return this.getGroupKey() + ":" + this.getGroupField() + ":" + 
           this.getMemberKey();
  }

  public void set(String key, String field, String subject, boolean immediate) {
    // FIXME This is all pretty dubious
    this.groupKey     = key;
    this.groupField   = field;
    this.memberKey    = subject;
    this.isImmediate  = immediate;
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

}

