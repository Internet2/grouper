/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
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

/** 
 * Class representing a type of {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperType.java,v 1.6 2004-09-10 18:23:09 blair Exp $
 */
public class GrouperType {

  private int groupType;

  /**
   * Create a {@link GrouperType} object.
   */
  public GrouperType() {
    groupType = 0;
  }

  public String toString() {
    return "" + this.getGroupType();
  }

  /*
   * Below for Hibernate
   */

  protected int getGroupType() {
    return this.groupType;
  }

  // XXX Do we really want to allow write access? 
  private void setGroupType(int groupType) {
    this.groupType = groupType;
  }

}

