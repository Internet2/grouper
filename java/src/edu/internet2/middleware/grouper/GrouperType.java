/* 
 * Copyright (C) 2004 University Corporation for Advanced Internet Development, Inc.
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
 * @version $Id: GrouperType.java,v 1.10 2004-11-23 19:43:26 blair Exp $
 */
public class GrouperType {

  private String groupType;

  /**
   * Create a {@link GrouperType} object.
   */
  public GrouperType() {
    groupType = null;
  }

  public String toString() {
    return "" + this.getGroupType();
  }

  /*
   * Below for Hibernate
   */

  private String getGroupType() {
    return this.groupType;
  }

  private void setGroupType(String groupType) {
    this.groupType = groupType;
  }

}

