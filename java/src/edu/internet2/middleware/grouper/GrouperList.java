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
 * @version $Id: GrouperList.java,v 1.12 2004-11-23 19:28:47 blair Exp $
 */
public class GrouperList implements Serializable {

  private String  groupKey;
  private String  groupField;
  private String  memberKey;
  private String  via;
  private String  removeAfter;


  public GrouperList() {
    this._init();
  }

  public GrouperList(GrouperGroup g, GrouperMember m, String list, GrouperGroup via) {
    // TODO The fact that this is public is troubling...
    this._init();
    // FIXME Stop relying upon the groupKey!
    this.groupKey   = g.key();
    // FIXME This isn't going to work
    this.memberKey  = m.key();
    // FIXME Validation?
    this.groupField = list;
    // FIXME Stop relying upon the groupKey!
    if (via != null) {
      this.via = via.key();
    } else {
      this.via = null;
    }
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * TODO MAY NOT REMAIN
   */
  public String groupKey() {
    return this.getGroupKey();
  }

  /**
   * TODO MAY NOT REMAIN
   */
  public String groupField() {
    return this.getGroupField();
  }

  /**
   * TODO MAY NOT REMAIN
   */
  public String memberKey() {
    return this.getMemberKey();
  }

  /**
   * TODO MAY NOT REMAIN
   */
  public String via() {
    return this.getVia();
  }

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
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize instance variables
   */
  private void _init() {
    this.groupKey     = null;
    this.groupField   = null;
    this.memberKey    = null;
    this.via          = null;
    this.removeAfter  = null;
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

