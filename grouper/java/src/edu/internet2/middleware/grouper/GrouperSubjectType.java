/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

/** 
 * Class representing a type of {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubjectType.java,v 1.5 2004-10-05 18:35:54 blair Exp $
 */
public class GrouperMemberType {

  private String memberType;
  private String displayName;

  /**
   * Create a {@link GrouperMemberType} object.
   */
  public GrouperMemberType() {
    memberType  = null;
    displayName = null;
  }

  public String toString() {
    return "" + this.getMemberType() + ":" + this.getDisplayName();
  }

  /*
   * Below for Hibernate
   */

  private String getMemberType() {
    return this.memberType;
  }

  // XXX Do we really want to allow write access? 
  private void setMemberType(String memberType) {
    this.memberType = memberType;
  }

  private String getDisplayName() {
    return this.displayName;
  }

  // XXX Do we really want to allow write access? 
  private void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}

