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
import  java.io.Serializable;
import  org.apache.commons.lang.builder.EqualsBuilder;
import  org.apache.commons.lang.builder.HashCodeBuilder;


/** 
 * Class representing a {@link Grouper} subject attribute.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubjectAttribute.java,v 1.3 2004-11-11 18:28:59 blair Exp $
 */
public class GrouperSubjectAttribute implements Serializable{

  // What we need to identify a subject attribute
  private String  id;
  private int     instance;
  private String  name;
  private String  searchValue;
  private String  typeID;
  private String  value;

  /**
   * Create a new {@link GrouperSubjectAttribute} object.
   */
  public GrouperSubjectAttribute() {
    this._init();
  }

  /**
   * Create a new {@link GrouperSubjectAttribute} object.
   *
   * @param typeID  Subject Type ID
   * @param name    Attribute name
   * @param value   Attribute value
   */
  public GrouperSubjectAttribute(String typeID, String name, String value) {
    this._init();
    this.typeID = typeID;
    this.name   = name;
    this.value  = value;
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  public boolean equals(Object o) {
     return EqualsBuilder.reflectionEquals(this, o);
   }


  public int hashCode() {
     return HashCodeBuilder.reflectionHashCode(this);
   }

  /**
   * Return attribute name.
   *
   * @return Name of {@link GrouperAttribute}.
   */
  public String name() {
    return this.getName();
  }

  /**
   * Return attribute search value.
   *
   * @return Search value of {@link GrouperAttribute}.
   */
  public String searchValue() {
    return this.getSearchValue();
  }

  public String toString() {
    return this.getClass().getName()  + ":" +
           this.typeID()              + ":" +
           this.name()                + ":" +
           this.value();
  }

  /**
   * Return Subject Type ID
   *
   * @return Subject Type ID of {@link GrouperSubjectAttribute}.
   */
    public String typeID() {
      return this.getSubjectTypeID();
    }

  /**
   * Return attribute value.
   *
   * @return Value of {@link GrouperAttribute}.
   */
  public String value() {
    return this.getValue();
  }


  /*
   * PRIVATE INSTANCE MTHODS
   */

  /*
   * Initialize instance variables.
   */
  private void _init() {
    this.name         = null;
    this.searchValue  = null;
    this.typeID       = null;
    this.value        = null;
  }


  /*
   * HIBERNATE
   */

  private int getInstance() {
    return this.instance;
  }

  private void setInstance(int instance) {
    this.instance = instance;
  }

  private String getSubjectID() {
    return this.id;
  }

  private void setSubjectID(String id) {
    this.id = id;
  }

  private String getSubjectTypeID() {
    return this.typeID;
  }

  private void setSubjectTypeID(String typeID) {
    this.typeID = typeID;
  }

  private String getName() {
    return this.name;
  }

  private void setName(String name) {
    this.name = name;
  }

  private String getSearchValue() {
    return this.searchValue;
  }

  private void setSearchValue(String searchValue) {
    this.searchValue = searchValue;
  }

  private String getValue() {
    return this.value;
  }

  private void setValue(String value) {
    this.value = value;
  }
 
}

