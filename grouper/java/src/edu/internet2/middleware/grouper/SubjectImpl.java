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
import  edu.internet2.middleware.subject.*;
import  java.io.Serializable;
import  org.apache.commons.lang.builder.EqualsBuilder;
import  org.apache.commons.lang.builder.HashCodeBuilder;


/** 
 * Implementation of the I2MI {{@link Subject}} interface.
 *
 * @author  blair christensen.
 * @version $Id: SubjectImpl.java,v 1.3 2004-11-12 17:55:30 blair Exp $
 */
public class SubjectImpl 
  implements Serializable,Subject
{

  // What we need to identify a subject
  private String id;
  private String typeID;


  public SubjectImpl() {
    super();
  }


  /*
   * PUBLIC INSTANCE METHODS
   */

  public void addAttribute(String name, String value) {
    // XXX Nothing -- Yet
  }

  public String[] getAttributeArray(String name) {
    return null; 
  }

  public String getDescription() {
    return null;
  }

  public String getDisplayId() {
    return null; 
  }

  public String getId() {
    return null;
  }

  public String getName() {
    return null; 
  }

  public SubjectType getSubjectType()  {
    return null;
  }


  /*
   * HIBERNATE
   */

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

}
