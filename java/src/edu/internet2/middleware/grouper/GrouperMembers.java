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

import  java.io.Serializable;

/** 
 * TODO 
 *
 * @author  blair christensen.
 * @version $Id: GrouperMembers.java,v 1.3 2004-09-10 18:23:08 blair Exp $
 */
public class GrouperMembers implements Serializable {

  private String  memberID;
  private String  presentationID;

  public GrouperMembers() {
    memberID        = null;
    presentationID  = null;
  }

  public String toString() {
    return this.getMemberID() + ":" + this.getPresentationID();
  }

  /*
   * Below for Hibernate
   */

  protected String getMemberID() {
    return this.memberID;
  }

  private void setMemberID(String memberID) {
    this.memberID = memberID;
  }

  protected String getPresentationID() {
    return this.presentationID;
  }

  private void setPresentationID(String presentationID) {
    this.presentationID = presentationID;
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
    return java.lang.Math.abs( this.getMemberID().hashCode()       ) + 
           java.lang.Math.abs( this.getPresentationID().hashCode() ) ; 
  }

}

