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


/** 
 * Class representing a {@link Grouper} member, whether an individual
 * or a {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperMember.java,v 1.30 2004-11-08 20:11:57 blair Exp $
 */
public class GrouperMember {

  // What we need to identify a member
  private String id;
  private String key;
  private String type;
  private String typeID;

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
    this.id   = id;
    this.type = type;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  /**
   * Return Member ID.
   *
   * @return Member ID of the {@link GrouperMember}
   */
  public String id() {
    return this.getMemberID();
  }

  /**
   * Return Member Key.
   * <p />
   * FIXME REMOVE, REPLACE, REFACTOR
   *
   * @return Member key of the {@link GrouperMember}
   */
  public String key() {
    return this.getMemberKey();
  }

  public String toString() {
    return this.getClass().getName()  + ":" +
           this.typeID()              + ":" +
           this.type()                + ":" +
           this.id();
  }

  /**
   * Return Member Type.
   *
   * @return Member Type of {@link GrouperMember}.
   */
  public String type() {
    return this.getMemberType();
  }

  /**
   * Return Member Type ID
   *
   * @return Member Type ID of {@link GrouperMember}.
   */
  public String typeID() {
    return this.getMemberTypeID();
  }


  /*
   * PRIVATE INSTANCE MTHODS
   */

  /*
   * Initialize instance variables.
   */
  private void _init() {
    this.id     = null;
    this.key    = null;
    this.type   = null;
    this.typeID = null;
  }


  /*
   * HIBERNATE
   */

  private String getMemberID() {
    return this.id;
  }

  private void setMemberID(String id) {
    this.id = id;
  }

  private String getMemberKey() {
    return this.key;
  }

  private void setMemberKey(String key) {
    this.key = key;
  }

  private String getMemberType() {
    return this.type;
  }

  private void setMemberType(String type) {
    this.type = type;
  }

  private String getMemberTypeID() {
    return this.typeID;
  }

  private void setMemberTypeID(String typeID) {
    this.typeID = typeID;
  }

}

