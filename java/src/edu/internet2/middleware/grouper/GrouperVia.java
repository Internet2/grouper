/* 
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  org.apache.commons.lang.builder.ToStringBuilder;


/** 
 * Class representing a via (or lack of via) relationship.
 *
 * @author  blair christensen.
 * @version $Id: GrouperVia.java,v 1.2 2004-11-23 19:43:26 blair Exp $
 */
public class GrouperVia {

  // What we need to identify a via (or lack of via) relationship
  private GrouperMember member;   // I am a member
  private GrouperGroup  group;    // Of this group
  private GrouperGroup  via;      // Via this group


  /**
   * Create a {@link GrouperVia} object.
   */
  public GrouperVia() {
    // Nothing 
  }

  /**
   * Create a {@link GrouperVia} object.
   */
  public GrouperVia(GrouperMember member, GrouperGroup group, GrouperGroup via) {
    this.member  = member;
    this.group   = group;
    this.via     = via;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * TODO
   */
  public String toString() {
    return new ToStringBuilder(this).
      append("member", this.member).
      append("group", this.group).
      append("via", this.via).
      toString();
  }

  /*
   * PROTECTED INSTANCE METHODS
   */

  /**
   * TODO 
   */
  protected GrouperGroup group() {
    return this.group;
  }

  /**
   * TODO 
   */
  protected GrouperMember member() {
    return this.member;
  }

  /**
   * TODO 
   */
  protected GrouperGroup via() {
    return this.via;
  }


}

