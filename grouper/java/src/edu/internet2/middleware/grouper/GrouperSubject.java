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
import  java.util.*;
import  org.apache.commons.lang.builder.EqualsBuilder;
import  org.apache.commons.lang.builder.HashCodeBuilder;


/** 
 * Class for performing subject lookups.
 *
 * @author  blair christensen.
 * @version $Id: GrouperSubject.java,v 1.14 2004-11-11 18:28:59 blair Exp $
 */
public class GrouperSubject implements Serializable {

  // What we need to identify a subject
  private String id;
  private String typeID;


  public GrouperSubject() {
    this._init();
  }
    

  /*
   * PUBLIC CLASS METHODS 
   */

  /**
   * Query the <i>grouper_member</i> table for a specific member.
   *
   * @param   id      Member ID
   * @param   typeID  Member Type ID
   * @return  {@link GrouperMember} object or null.
   */
  public static GrouperMember lookup(String id, String typeID) {
    return GrouperBackend.member(id, typeID);
  }


  /*
   * PRIVATE INSTANCE METHODS
   */

  /*
   * Initailize instance variables
   */
  private void _init() {
    this.id     = null;
    this.typeID = null;
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

