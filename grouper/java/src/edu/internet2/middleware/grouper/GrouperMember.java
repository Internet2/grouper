/* 
 * Copyright (C) 2004 Internet2
 * Copyright (C) 2004 The University Of Chicago
 * All Rights Reserved. 
 *
 * See the LICENSE file in the top-level directory of the 
 * distribution for licensing information.
 */

package edu.internet2.middleware.grouper;

import  edu.internet2.middleware.grouper.*;
import  java.util.*;

/** 
 * Class representing a {@link Grouper} member, whether an individual
 * or a {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.27 2004-10-05 18:35:54 blair Exp $
 */
public class GrouperMember {

  // Operational attributes and information
  private String memberID;
  private String memberKey;
  private String memberType;

  /**
   * Create a new {@link GrouperMember} object.
   */
  public GrouperMember() {
    this._init();
  }

  /**
   * Create a new {@link GrouperMember} object.
   *
   * @param id    Member ID
   * @param type  Member Type
   */
  public GrouperMember(String id, String type) {
    this._init();
    this.memberID   = id;
    this.memberType = type;
  }

  public String toString() {
    return this.getClass().getName()  + ":" +
           this.getMemberType()       + ":" +
           this.getMemberID(); 
  }

  /**
   * Return Member ID.
   *
   * @return Member ID of {@link GrouperMember}.
   */
  public String memberID() {
    return this.getMemberID();
  }

  /**
   * Return Member Type.
   *
   * @return Member Type of {@link GrouperMember}.
   */
  public String memberType() {
    return this.getMemberType();
  }


  /*
   * PUBLIC METHODS ABOVE, PRIVATE METHODS BELOW 
   */


  /*
   * Initialize instance variables.
   */
  private void _init() {
    this.memberID   = null;
    this.memberKey  = null;
    this.memberType = null;
  }


  /*
   * Below for Hibernate
   */


  private String getMemberKey() {
    return this.memberKey;
  }

  private void setMemberKey(String memberKey) {
    this.memberKey = memberKey;
  }

  private String getMemberType() {
    return this.memberType;
  }

  private void setMemberType(String memberType) {
    this.memberType = memberType;
  }

  private String getMemberID() {
    return this.memberID;
  }

  private void setMemberID(String memberID) {
    this.memberID = memberID;
  }

}

