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
 * Class representing a 
 * Class representing a type of {@link GrouperGroup}.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubjectType.java,v 1.6 2004-11-05 18:46:27 blair Exp $
 */
public class GrouperMemberType {

  // What we need to identify a member type
  private String adapterClass;
  private String name;
  private String typeID;


  /**
   * Create a {@link GrouperMemberType} object.
   */
  public GrouperMemberType() {
    this._init();
  }


  /*
   * PUBLIC INSTANCE METHODS 
   */

  /**
   * Return member type adapter class.
   *
   * @return Adapter class of type.
   */
  public String adapterClass() {
    return this.getAdapterClass();
  }

  /**
   * Return member type name.
   *
   * @return  Name of type.
   */
  public String name() {
    return this.getName();
  }

  public String toString() {
    return this.getClass().getName()  + ":" +
           this.typeID()              + ":" +
           this.name()                + ":" +
           this.adapterClass();
  }

  /**
   * Return member type ID.
   *
   * @return ID of type.
   */
  public String typeID() {
    return this.getMemberTypeID();
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initialize instance variables.
   */
  private void _init() {
    this.adapterClass = null;
    this.typeID       = null;
    this.name         = null;
  }


  /*
   * HIBERNATE
   */

  private String getAdapterClass() {
    return this.adapterClass;
  }

  private void setAdapterClass(String adapterClass) {
    this.adapterClass = adapterClass;
  }

  private String getMemberTypeID() {
    return this.typeID;
  }

  private void setMemberTypeID(String id) {
    this.typeID = id;
  }

  private String getName() {
    return this.name;
  }

  private void setName(String name) {
    this.name = name;
  }

}

