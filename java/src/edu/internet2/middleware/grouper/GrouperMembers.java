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

/** 
 * TODO 
 *
 * @author  blair christensen.
 * @version $Id: GrouperMembers.java,v 1.5 2004-10-05 18:35:54 blair Exp $
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

  private String getMemberID() {
    return this.memberID;
  }

  private void setMemberID(String memberID) {
    this.memberID = memberID;
  }

  private String getPresentationID() {
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

