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
 * TODO 
 *
 * @author  blair christensen.
 * @version $Id: GrouperList.java,v 1.3 2004-08-24 17:37:57 blair Exp $
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

  public void set(String key, String field, String subject, boolean immediate) {
    // FIXME This is all pretty dubious
    this.groupKey     = key;
    this.groupField   = field;
    this.memberKey    = subject;
    this.isImmediate  = immediate;
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

